package net.licks92.WirelessRedstone.Storage;

import com.husky.sqlite.SQLite;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Libs.CreateBuilder;
import net.licks92.WirelessRedstone.Libs.InsertBuilder;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.IWirelessPoint;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class SQLiteStorage implements IWirelessStorageConfiguration {

    private File channelFolder;
    private String channelFolderStr;
    private SQLite sqLite;

    private String sqlIsWallSign = "isWallSign";
    private String sqlDirection = "direction";
    private String sqlChannelId = "id";
    private String sqlChannelName = "name";
    private String sqlChannelLocked = "locked";
    private String sqlChannelOwners = "owners";
    private String sqlSignOwner = "signOwner";
    private String sqlSignWorld = "world";
    private String sqlSignX = "x";
    private String sqlSignY = "y";
    private String sqlSignZ = "z";
    private String sqlSignType = "signType";


    public SQLiteStorage(String channelFolder) {
        this.channelFolder = new File(Main.getInstance().getDataFolder(), channelFolder);
        this.channelFolderStr = channelFolder;
        this.sqLite = new SQLite(Main.getInstance(), channelFolder + File.separator + "WirelessRedstoneDatabase.db");
    }

    @Override
    public boolean initStorage() {
        return initiate(true);
    }

    @Override
    public boolean close() {
        try {
            sqLite.closeConnection();
            Main.getWRLogger().info("Successfully closed SQLite connection.");
        } catch (SQLException e) {
            Main.getWRLogger().warning("Cannot close SQLite connection.");
            if(ConfigManager.getConfig().getDebugMode())
                e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean createWirelessChannel(WirelessChannel channel) {
        if (!sqlTableExists(channel.getName())) {
            // Get the type of the sign that has been created
            if (channel.getReceivers().isEmpty() && channel.getTransmitters().isEmpty() && channel.getScreens().isEmpty()) {
                Main.getWRLogger().severe("Channel created with no IWirelessPoint in, stopping the creation of the channel.");
                return false;
            }

            try {
                // Create the table
                Statement statement = sqLite.getConnection().createStatement();
                statement.executeUpdate(new CreateBuilder(getDatabaseFriendlyName(channel.getName()))
                        .addColumn(sqlChannelId, "int").addColumn(sqlChannelName, "char(64)")
                        .addColumn(sqlChannelLocked, "int(1)").addColumn(sqlChannelOwners, "char(255)")
                        .addColumn(sqlDirection, "char(255)").addColumn(sqlIsWallSign, "int(1)")
                        .addColumn(sqlSignType, "char(255)").addColumn(sqlSignX, "int")
                        .addColumn(sqlSignY, "int").addColumn(sqlSignZ, "int")
                        .addColumn(sqlSignWorld, "char(255)").addColumn(sqlSignOwner, "char(255)")
                        .toString());
                statement.executeUpdate(new InsertBuilder(getDatabaseFriendlyName(channel.getName()))
                        .addColumnWithValue(sqlChannelId, channel.getId())
                        .addColumnWithValue(sqlChannelName, channel.getName())
                        .addColumnWithValue(sqlChannelLocked, 0)
                        .addColumnWithValue(sqlChannelOwners, channel.getOwners().get(0))
                        .toString());

                statement.close();

                // Create the wireless points
                ArrayList<IWirelessPoint> points = new ArrayList<IWirelessPoint>();
                for (IWirelessPoint ipoint : channel.getReceivers()) {
                    points.add(ipoint);
                }
                for (IWirelessPoint ipoint : channel.getTransmitters()) {
                    points.add(ipoint);
                }
                for (IWirelessPoint ipoint : channel.getScreens()) {
                    points.add(ipoint);
                }
                for (IWirelessPoint ipoint : points) {
                    createWirelessPoint(channel.getName(), ipoint);
                }

                if (Main.getGlobalCache() == null) Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        Main.getGlobalCache().update();
                    }
                }, 1L);
                else Main.getGlobalCache().update();
                return true;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        Main.getWRLogger().debug("Tried to create a channel that already exists in the database");
        return false;
    }

    @Override
    public boolean createWirelessPoint(String channelName, IWirelessPoint point) {
        return false;
    }

    @Override
    public boolean removeIWirelessPoint(String channelName, Location loc) {
        return false;
    }

    @Override
    public boolean removeWirelessReceiver(String channelName, Location loc) {
        return false;
    }

    @Override
    public boolean removeWirelessTransmitter(String channelName, Location loc) {
        return false;
    }

    @Override
    public boolean removeWirelessScreen(String channelName, Location loc) {
        return false;
    }

    @Override
    public boolean renameWirelessChannel(String channelName, String newChannelName) {
        return false;
    }

    @Override
    public boolean wipeData() {
        // Backup before wiping
        if (channelFolder.listFiles().length > 0) backupData("db");

        try {
            // Get the names of all the tables
            Statement statement = sqLite.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
            ArrayList<String> tables = new ArrayList<String>();
            while (rs.next()) {
                tables.add(rs.getString(sqlChannelName));
            }
            rs.close();
            statement.close();

            // Erase all the tables
//            for (String channelName : tables) {
//                removeWirelessChannel(channelName);
//            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean backupData(String extension) {
        return false;
    }

    @Override
    public boolean purgeData() {
        return false;
    }

    @Override
    public boolean convertFromAnotherStorage(StorageType type) {
        return false;
    }

    @Override
    public boolean isChannelEmpty(WirelessChannel channel) {
        return false;
    }

    @Override
    public Collection<WirelessChannel> getAllChannels() {
        return null;
    }

    @Override
    public WirelessChannel getWirelessChannel(String channelName) {
        return null;
    }

    @Override
    public IWirelessPoint getWirelessRedstoneSign(Location loc) {
        return null;
    }

    @Override
    public StorageType canConvert() {
        try {
            for (File file : channelFolder.listFiles()) {
                if (file.getName().contains(".MYSQL")) {
                    return StorageType.MYSQL;
                }
            }
            for (File file : channelFolder.listFiles()) {
                if (file.getName().contains(".yml")) {
                    return StorageType.SQLITE;
                }
            }
        } catch (NullPointerException ignored) {}

        return null;
    }

    @Override
    public String getWirelessChannelName(Location loc) {
        return null;
    }

    @Override
    public void updateChannel(String channelName, WirelessChannel channel) {

    }

    @Override
    public void updateReceivers() {

    }

    @Override
    public void checkChannel(String channelName) {

    }

    @Override
    public void removeWirelessChannel(String channelName) {

    }

    private boolean initiate(boolean allowConvert){
        Main.getWRLogger().info("Establishing connection to database...");

        try {
            sqLite.openConnection();
        } catch (SQLException | ClassNotFoundException ex) {
            Main.getWRLogger().severe("Error while starting plugin. Message: " + ex.getLocalizedMessage() + ". Enable debug mode to see the full stack trace.");

            if(ConfigManager.getConfig().getDebugMode())
                ex.printStackTrace();

            Main.getWRLogger().severe("**********");
            Main.getWRLogger().severe("Plugin can't connect to the SQLite database. Shutting down plugin...");
            Main.getWRLogger().severe("**********");
            return false;
        }

        Main.getWRLogger().info("Connection established.");

        if (canConvert() != null && allowConvert) {
            Main.getWRLogger().info("WirelessRedstone found a channel in a different storage format.");
            Main.getWRLogger().info("Beginning data transfer to SQLite...");
            if (convertFromAnotherStorage(canConvert())) {
                Main.getWRLogger().info("Done! All the channels are now stored in the SQLite database.");
            }
        }
        return true;
    }

    private boolean sqlTableExists(String name) {
        try {
            Statement statement = sqLite.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");

            while (rs.next()) {
                if (getNormalName(rs.getString(sqlChannelName)).equals(name)) {
                    rs.close();
                    statement.close();
                    return true;
                }
            }

            rs.close();
            statement.close();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getNormalName(String asciiName) {
        if (asciiName.contains("num_")) {
            asciiName = asciiName.replace("num_", "");
            return asciiName;
        }
        for (char character : Utils.badCharacters) {
            String ascii = "" + (int) character;
            String code = "_char_" + ascii + "_";
            if (asciiName.contains(code)) {
                asciiName = asciiName.replace(code, String.valueOf(character));
            }
        }
        return asciiName;
    }

    private String getDatabaseFriendlyName(String normalName) {
        try {
            Integer.parseInt(normalName);
            normalName = "num_" + normalName;
        } catch (NumberFormatException ignored) {
        }

        for (char character : Utils.badCharacters) {
            if (normalName.contains(String.valueOf(character))) {
                String ascii = "" + (int) character;
                String code = "_char_" + ascii + "_";
                normalName = normalName.replace(String.valueOf(character), code);
            }
        }
        return normalName;
    }
}
