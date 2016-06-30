package net.licks92.WirelessRedstone;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite {

    private Plugin plugin;
    private String path;
    private Connection connection;

    public SQLite(Plugin plugin, String path) {
        this.plugin = plugin;
        this.path = path;
    }

    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if(connection != null)
            return connection;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File file = new File(plugin.getDataFolder(), path);
        if (!(file.exists())) {
            try {
                file.createNewFile();
                Main.getWRLogger().debug("Created new DB file.");
            } catch (IOException e) {
                Main.getWRLogger().debug("Unable to create database!");
            }
        }

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toPath().toString() + File.separator
                + path);

        Main.getWRLogger().debug("jdbc:sqlite:" + plugin.getDataFolder().toPath().toString() + File.separator
                + path);

        return connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }
}
