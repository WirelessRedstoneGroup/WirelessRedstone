package net.licks92.WirelessRedstone.Storage;

import com.husky.sqlite.SQLite;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Libs.CreateBuilder;
import net.licks92.WirelessRedstone.Libs.DeleteBuilder;
import net.licks92.WirelessRedstone.Libs.InsertBuilder;
import net.licks92.WirelessRedstone.Libs.UpdateBuilder;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.*;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            if (ConfigManager.getConfig().getDebugMode())
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

                if (Main.getGlobalCache() == null)
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
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
        if (!sqlTableExists(channelName)) {
            Main.getWRLogger().severe("Could not create this wireless point in the channel " + channelName + ", it does not exist!");
        }

        Integer isWallSign;
        String signType;

        if (point instanceof WirelessTransmitter) {
            signType = "transmitter";
        } else if (point instanceof WirelessScreen) {
            signType = "screen";
        } else if (point instanceof WirelessReceiver) {
            if (point instanceof WirelessReceiverInverter) signType = "receiver_inverter";
            else if (point instanceof WirelessReceiverDelayer)
                signType = "receiver_delayer_" + ((WirelessReceiverDelayer) (point)).getDelay();
            else if (point instanceof WirelessReceiverSwitch) {
                boolean state;
                if (Main.getSignManager().switchState.get(((WirelessReceiverSwitch) (point)).getLocation()) != null)
                    state = Main.getSignManager().switchState.get(((WirelessReceiverSwitch) (point)).getLocation());
                else state = false;
                signType = "receiver_switch_" + state;
            } else if (point instanceof WirelessReceiverClock)
                signType = "receiver_clock_" + ((WirelessReceiverClock) (point)).getDelay();
            else signType = "receiver";
        } else {
            return false;
        }

        if (point.getIsWallSign())
            isWallSign = 1;
        else
            isWallSign = 0;

        try {
            Statement statement = sqLite.getConnection().createStatement();
            statement.executeUpdate(new InsertBuilder(getDatabaseFriendlyName(channelName))
                    .addColumnWithValue(sqlSignType, signType)
                    .addColumnWithValue(sqlSignX, point.getX())
                    .addColumnWithValue(sqlSignY, point.getY())
                    .addColumnWithValue(sqlSignZ, point.getZ())
                    .addColumnWithValue(sqlSignWorld, point.getWorld())
                    .addColumnWithValue(sqlDirection, point.getDirection().toString().toUpperCase())
                    .addColumnWithValue(sqlSignOwner, point.getOwner())
                    .addColumnWithValue(sqlIsWallSign, isWallSign)
                    .toString());
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean removeIWirelessPoint(String channelName, Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel == null) return false;

        for (WirelessReceiver receiver : channel.getReceivers()) {
            if (Utils.sameLocation(receiver.getLocation(), loc)) return removeWirelessReceiver(channelName, loc);
        }
        for (WirelessTransmitter transmitter : channel.getTransmitters()) {
            if (Utils.sameLocation(transmitter.getLocation(), loc)) return removeWirelessTransmitter(channelName, loc);
        }
        for (WirelessScreen screen : channel.getScreens()) {
            if (Utils.sameLocation(screen.getLocation(), loc)) return removeWirelessScreen(channelName, loc);
        }
        return false;
    }

    @Override
    public boolean removeWirelessReceiver(String channelName, Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeReceiverAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    @Override
    public boolean removeWirelessTransmitter(String channelName, Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeTransmitterAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    @Override
    public boolean removeWirelessScreen(String channelName, Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeScreenAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    @Override
    public boolean renameWirelessChannel(String channelName, String newChannelName) {
        WirelessChannel channel = getWirelessChannel(channelName);

        List<IWirelessPoint> signs = new ArrayList<IWirelessPoint>();

        signs.addAll(channel.getReceivers());
        signs.addAll(channel.getTransmitters());
        signs.addAll(channel.getScreens());

        for (IWirelessPoint sign : signs) {
            Location loc = new Location(Bukkit.getWorld(sign.getWorld()), sign.getX(), sign.getY(), sign.getZ());
            Sign signBlock = (Sign) loc.getBlock();
            signBlock.setLine(1, newChannelName);
        }

        try {
            Statement statement = sqLite.getConnection().createStatement();

            statement.executeUpdate(new UpdateBuilder(getDatabaseFriendlyName(channelName))
                    .set(sqlChannelName + "=" + newChannelName)
                    .where(sqlChannelName + "=" + channelName)
                    .toString());
            statement.executeUpdate("RENAME TABLE " + channelName + " TO " + newChannelName);

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
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
        } catch (NullPointerException ignored) {
        }

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

    private boolean initiate(boolean allowConvert) {
        Main.getWRLogger().info("Establishing connection to database...");

        try {
            sqLite.openConnection();
        } catch (SQLException | ClassNotFoundException ex) {
            Main.getWRLogger().severe("Error while starting plugin. Message: " + ex.getLocalizedMessage() + ". Enable debug mode to see the full stack trace.");

            if (ConfigManager.getConfig().getDebugMode())
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

    private boolean removeWirelessPoint(String channelName, Location loc, String world) {
        try {
            Statement statement = sqLite.getConnection().createStatement();
            String sql = new DeleteBuilder(getDatabaseFriendlyName(channelName))
                    .where(sqlSignX + "=" + loc.getBlockX())
                    .where(sqlSignY + "=" + loc.getBlockY())
                    .where(sqlSignZ + "=" + loc.getBlockZ())
                    .where(sqlSignWorld + "=" + world)
                    .toString();
            statement.executeUpdate(sql);
            Main.getWRLogger().debug("Statement to delete wireless sign : " + sql);
            statement.close();
            Main.getGlobalCache().update();
        } catch (SQLException ex){
            ex.printStackTrace();
            return false;
        }

        return true;
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
