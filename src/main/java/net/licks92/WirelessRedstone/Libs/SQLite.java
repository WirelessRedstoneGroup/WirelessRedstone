package net.licks92.WirelessRedstone.Libs;

import com.sun.rowset.CachedRowSetImpl;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.sqlite.SQLiteConfig;

import javax.sql.rowset.CachedRowSet;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SQLite {

    private Plugin plugin;
    private String path;
    private Connection connection;
    private Boolean updateGlobalCache;
    private Boolean lockTimer;

    private ArrayList<PreparedStatement> preparedStatements;
    private boolean isProcessing = false;

    public SQLite(Plugin plugin, String path, final Boolean updateGlobalCache) throws SQLException, ClassNotFoundException {
        this.plugin = plugin;
        this.path = path;
        this.updateGlobalCache = updateGlobalCache;
        this.preparedStatements = new ArrayList<>();
        this.lockTimer = false;

        if (connection == null) {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File file = new File(plugin.getDataFolder(), path);
            if (!(file.exists())) {
                try {
                    file.createNewFile();
                    WirelessRedstone.getWRLogger().debug("Created new DB file.");
                } catch (IOException e) {
                    WirelessRedstone.getWRLogger().debug("Unable to create database!");
                }
            }

            Class.forName("org.sqlite.JDBC");

            SQLiteConfig config = new SQLiteConfig();
            config.setSharedCache(true);
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toPath().toString() + File.separator
                    + path, config.toProperties());
        }

        final Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (WirelessRedstone.getInstance() == null) {
                    timer.cancel();
                    return;
                }

                if (!WirelessRedstone.getInstance().isEnabled()) { // Plugin was disabled
                    timer.cancel();
                    return;
                }

                if (lockTimer)
                    return;

                boolean canContinue = false;
                boolean error = false;

                if (getConnection() != null) {
                    try {
                        if (preparedStatements.size() > 0) {
                            canContinue = true;
                            PreparedStatement preparedStatement = preparedStatements.get(0);

                            if (preparedStatement == null) {
                                preparedStatements.remove(0);
                                return;
                            }

                            WirelessRedstone.getWRLogger().debug("Excuting next preparedstatement.");

                            preparedStatement.execute();
                            preparedStatement.close();

                            if (updateGlobalCache) {
                                if (WirelessRedstone.getGlobalCache() == null)
                                    Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                                        @Override
                                        public void run() {
                                            WirelessRedstone.getGlobalCache().update(false); //We are already asking this async
                                        }
                                    }, 1L);
                                else WirelessRedstone.getGlobalCache().update(false);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        error = true;
                    } finally {
                        if (canContinue) {
                            preparedStatements.remove(0);
                            if (!error) {
                                if (preparedStatements.size() == 0) {
                                    WirelessRedstone.getWRLogger().debug("No more preparedstatements left.");
                                }
                            } else {
                                WirelessRedstone.getWRLogger().warning("An error occured. Please notify the developer.");
                            }
                        }
                    }
                }
            }
        }, 0, 25);
    }

    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null)
            return connection;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File file = new File(plugin.getDataFolder(), path);
        if (!(file.exists())) {
            try {
                file.createNewFile();
                WirelessRedstone.getWRLogger().debug("Created new DB file.");
            } catch (IOException e) {
                WirelessRedstone.getWRLogger().debug("Unable to create database!");
            }
        }

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toPath().toString() + File.separator
                + path);

        WirelessRedstone.getWRLogger().debug("jdbc:sqlite:" + plugin.getDataFolder().toPath().toString() + File.separator
                + path);

        return connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }

    public ResultSet query(final PreparedStatement preparedStatement) {
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CachedRowSet query_test(final PreparedStatement preparedStatement) {
        CachedRowSet rowSet = null;

        if (getConnection() != null) {
            try {
                ExecutorService exe = Executors.newCachedThreadPool();

                Future<CachedRowSet> future = exe.submit(new Callable<CachedRowSet>() {
                    public CachedRowSet call() {
                        try {
                            ResultSet resultSet = preparedStatement.executeQuery();

                            CachedRowSet cachedRowSet = new CachedRowSetImpl();
                            cachedRowSet.populate(resultSet);
                            resultSet.close();

//                            preparedStatement.getConnection().close();
                            preparedStatement.close();
                            if (cachedRowSet.next()) {
                                return cachedRowSet;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                });

                if (future.get() != null) {
                    rowSet = future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return rowSet;
    }

    /*
     * Execute a query
     *
     * @param preparedStatement query to be executed.
     */
    public void execute(final PreparedStatement preparedStatement) {
        try {
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            WirelessRedstone.getWRLogger().warning("An error occured. Please notify the developer.");
        }
//        execute(preparedStatement, updateGlobalCache);
    }

    /*
    * Execute a query
    *
    * @param preparedStatement query to be executed.
    * @param updateGlobalCache update the global cache
    */
    public void execute(final PreparedStatement preparedStatement, final Boolean updateGlobalCache) {
        preparedStatements.add(preparedStatement);
    }

    public void lockTimer(){
        this.lockTimer = true;
    }

    public void unlockTime(){
        this.lockTimer = false;
    }
}
