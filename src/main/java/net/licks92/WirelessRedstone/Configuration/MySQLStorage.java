package net.licks92.WirelessRedstone.Configuration;

import com.husky.mysql.MySQL;
import com.husky.sqlite.SQLite;
import net.licks92.WirelessRedstone.Channel.*;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MySQLStorage implements IWirelessStorageConfiguration {
    private final MySQL mySQL;
    private final File channelFolder;
    private final String channelFolderStr;

    private final String sqlIsWallSign = "iswallsign";
    private final String sqlDirection = "direction";
    private final String sqlChannelId = "id";
    private final String sqlChannelName = "name";
    private final String sqlChannelLocked = "locked";
    private final String sqlChannelOwners = "owners";
    private final String sqlSignOwner = "signowner";
    private final String sqlSignWorld = "world";
    private final String sqlSignX = "x";
    private final String sqlSignY = "y";
    private final String sqlSignZ = "z";
    private final String sqlSignType = "signtype";

    private final WirelessRedstone plugin;

    public MySQLStorage(String channelFolder, WirelessRedstone plugin, String host, String port,
                        String database, String username, String password) {
        this.plugin = plugin;
        this.channelFolder = new File(plugin.getDataFolder(), channelFolder);
        this.channelFolderStr = channelFolder;
        mySQL = new MySQL(plugin, host, port, database, username, password);
    }

    public MySQLStorage(String channelFolder, WirelessRedstone plugin) {
        String host = plugin.getConfig().getString("MySQL.host", "localhost");
        String port = plugin.getConfig().getString("MySQL.port", "3306");
        String database = plugin.getConfig().getString("MySQL.database", "WirelessRedstone");
        String username = plugin.getConfig().getString("MySQL.username", "root");
        String password = plugin.getConfig().getString("MySQL.password", "root");
        this.plugin = plugin;
        this.channelFolder = new File(plugin.getDataFolder(), channelFolder);
        this.channelFolderStr = channelFolder;
        mySQL = new MySQL(plugin, host, port, database, username, password);
    }

    @Override
    public boolean initStorage() {
        return init(true);
    }

    public boolean init(final boolean allowConvert) {
        if (canConvert() != 0 && allowConvert) {
            WirelessRedstone.getWRLogger().info("Beginning data transfer to MySQL storage...");
            if (convertFromAnotherStorage(canConvert())) {
                WirelessRedstone.getWRLogger().info("Done! All the channels are now stored in the MySQL Database.");
            } else {
                WirelessRedstone.getWRLogger().severe("Data transfer failed!");
            }
        }
        return true;
    }

    public String getNormalName(String asciiName) {
        if (asciiName.contains("num_")) {
            asciiName = asciiName.replace("num_", "");
            return asciiName;
        }
        for (char character : WirelessRedstone.config.badCharacters) {
            String ascii = "" + (int) character;
            String code = "_char_" + ascii + "_";
            if (asciiName.contains(code)) {
                asciiName = asciiName.replace(code, String.valueOf(character));
            }
        }
        return asciiName;
    }

    public String getDBName(String normalName) {
	    /*
         * Here we test if the string contains only numbers. If the parse method
		 * sends an exception, it means that it doesn't contain only numbers and
		 * then we continue. In the other case, we will simply put a specific
		 * caracter at the beginning of the channel name, in order to not cause
		 * an exception with the database.
		 */
        try {
            Integer.parseInt(normalName);
            normalName = "num_" + normalName;
        } catch (NumberFormatException ignored) {
        }

        for (char character : WirelessRedstone.config.badCharacters) {
            if (normalName.contains(String.valueOf(character))) {
                String ascii = "" + (int) character;
                String code = "_char_" + ascii + "_";
                normalName = normalName.replace(String.valueOf(character), code);
            }
        }
        return normalName;
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public Integer canConvert() {
        for (File file : channelFolder.listFiles()) {
            if (file.getName().contains(".yml")) {
                return 1;
            } else if(file.getName().contains(".db")) {
                return 2;
            }
        }
        return 0;
    }

    @Override
    public boolean convertFromAnotherStorage(Integer type) {
        WirelessRedstone.getWRLogger().info("Backuping the channels/ folder before transfer.");
        boolean canConinue = true;
        if(type == 1){
            canConinue = backupData("yml");
        } else if(type == 2){
            canConinue = backupData("db");
        }
        if (!canConinue) {
            WirelessRedstone.getWRLogger().severe("Backup failed! Data transfer abort...");
            return false;
        } else {
            WirelessRedstone.getWRLogger().info("Backup done. Starting data transfer...");

            if(type == 1) {
                YamlStorage yaml = new YamlStorage(channelFolderStr, plugin);
                yaml.init(false);
                for (WirelessChannel channel : yaml.getAllChannels()) {
                    createWirelessChannel(channel);
                }
                yaml.close();
                for (File f : channelFolder.listFiles()) {
                    if (f.getName().contains(".yml")) {
                        f.delete();
                    }
                }
            } else if(type == 2) {
                SQLiteStorage sql = new SQLiteStorage(channelFolderStr, plugin);
                sql.init(false);
                for (WirelessChannel channel : sql.getAllChannels()) {
                    //Something fails here! Channels do not transfer the transmitter that's strange!
                    createWirelessChannel(channel);
                }
                sql.close();
                for (File f : channelFolder.listFiles()) {
                    if (f.getName().contains(".db")) {
                        f.delete();
                    }
                }
            }
        }
        return true;
    }

    private boolean sqlTableExists(final String name) {
        try {
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return false;
            }
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");

            while (rs.next()) {
                if (getNormalName(rs.getString("name")).equals(name)) {
                    rs.close();
                    statement.close();
                    return true;
                }
            }

            rs.close();
            statement.close();
            closeConnection(connection);
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean wipeData() {
        // Backup before wiping
        if (channelFolder.listFiles().length > 0) backupData("db");

        try {
            // Get the names of all the tables
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return false;
            }
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
            ArrayList<String> tables = new ArrayList<String>();
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
            rs.close();
            statement.close();
            closeConnection(connection);

            // Erase all the tables
            for (String channelName : tables) {
                removeWirelessChannel(channelName);
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean backupData(final String extension) {
        try {
            String zipName = "WRBackup " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-"
                    + Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR)
                    + "_" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "." + Calendar.getInstance().get(Calendar.MINUTE)
                    + "." + Calendar.getInstance().get(Calendar.SECOND);
            FileOutputStream fos = new FileOutputStream((channelFolder.getCanonicalPath().split(channelFolder.getName())[0]) + zipName + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : channelFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().contains("." + extension)) {
                    FileInputStream fis = new FileInputStream(file);

                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;

                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }

                    zos.closeEntry();
                    fis.close();
                }
            }

            zos.close();
            fos.close();

            WirelessRedstone.getWRLogger().info("Channels saved in archive: " + zipName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public WirelessChannel getWirelessChannel(final String r_channelName) {
        try {
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return null;
            }
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
            ArrayList<String> channels = new ArrayList<String>();

            while (rs.next()) {
                channels.add(getNormalName(rs.getString("name")));
            }
            rs.close(); // Always close the ResultSet

            for (String channelName : channels) {
                if (channelName.equals(r_channelName)) {
                    // Get the ResultSet from the table we want
                    ResultSet rsChannelInfo = statement.executeQuery("SELECT * FROM " + getDBName(channelName));
                    try {
                        rsChannelInfo.getString("name");
                    } catch (SQLException ex) {
                        statement.executeUpdate("DROP TABLE " + getDBName(channelName));
                        rsChannelInfo.close();
                        statement.close();
                        return null;
                    }

                    // Create an empty WirelessChannel
                    WirelessChannel channel = new WirelessChannel(rsChannelInfo.getString(sqlChannelName));

                    // Set the Id, the name, and the locked variable
                    channel.setId(rsChannelInfo.getInt(sqlChannelId));
                    if (rsChannelInfo.getInt(sqlChannelLocked) == 1) channel.setLocked(true);
                    else if (rsChannelInfo.getInt(sqlChannelLocked) == 0) channel.setLocked(false);
                    else channel.setLocked(false);

                    // Set the owners
                    ArrayList<String> owners = new ArrayList<String>();
                    while (rsChannelInfo.next()) {
                        if (rsChannelInfo.getString(sqlChannelOwners) != null) owners.add(rsChannelInfo.getString(sqlChannelOwners));
                    }
                    channel.setOwners(owners);
                    rsChannelInfo.close();

                    // Because a SQLite ResultSet is TYPE_FORWARD only, we have
                    // to create a third ResultSet and close the second
                    ResultSet rsSigns = statement.executeQuery("SELECT * FROM " + getDBName(channelName));

                    // Set the wireless signs
                    ArrayList<WirelessReceiver> receivers = new ArrayList<WirelessReceiver>();
                    ArrayList<WirelessTransmitter> transmitters = new ArrayList<WirelessTransmitter>();
                    ArrayList<WirelessScreen> screens = new ArrayList<WirelessScreen>();
                    rsSigns.next();// Because first row does not contain a wireless sign
                    while (rsSigns.next()) {
                        if (rsSigns.getString(sqlSignType).equals("receiver")) {
                            WirelessReceiver receiver = new WirelessReceiver();
                            receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).equals("receiver_inverter")) {
                            WirelessReceiverInverter receiver = new WirelessReceiverInverter();
                            receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).contains("receiver_delayer_")) {
                            String signtype = rsSigns.getString(sqlSignType);
                            signtype = signtype.split("receiver_delayer_")[1];
                            int delay;
                            try {
                                delay = Integer.parseInt(signtype);
                            } catch (NumberFormatException ex) {
                                delay = 0;
                            }
                            WirelessReceiverDelayer receiver = new WirelessReceiverDelayer(delay);
                            receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).contains("receiver_switch_")) {
                            String signtype = rsSigns.getString(sqlSignType);
                            signtype = signtype.split("receiver_switch_")[1];
                            boolean state;
                            try {
                                state = Boolean.parseBoolean(signtype);
                            } catch (NumberFormatException ex) {
                                state = false;
                            }
                            WirelessReceiverSwitch receiver = new WirelessReceiverSwitch(state);
                            receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).contains("receiver_clock_")) {
                            String signtype = rsSigns.getString(sqlSignType);
                            signtype = signtype.split("receiver_clock_")[1];
                            int delay;
                            try {
                                delay = Integer.parseInt(signtype);
                            } catch (NumberFormatException ex) {
                                delay = 20;
                            }
                            WirelessReceiverClock receiver = new WirelessReceiverClock(delay);
                            receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).equals("transmitter")) {
                            WirelessTransmitter transmitter = new WirelessTransmitter();
                            transmitter.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
                            transmitter.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            transmitter.setOwner(rsSigns.getString(sqlSignOwner));
                            transmitter.setWorld(rsSigns.getString(sqlSignWorld));
                            transmitter.setX(rsSigns.getInt(sqlSignX));
                            transmitter.setY(rsSigns.getInt(sqlSignY));
                            transmitter.setZ(rsSigns.getInt(sqlSignZ));
                            transmitters.add(transmitter);
                        }
                        if (rsSigns.getString(sqlSignType).equals("screen")) {
                            WirelessScreen screen = new WirelessScreen();
                            screen.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
                            screen.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            screen.setOwner(rsSigns.getString(sqlSignOwner));
                            screen.setWorld(rsSigns.getString(sqlSignWorld));
                            screen.setX(rsSigns.getInt(sqlSignX));
                            screen.setY(rsSigns.getInt(sqlSignY));
                            screen.setZ(rsSigns.getInt(sqlSignZ));
                            screens.add(screen);
                        }
                    }
                    channel.setReceivers(receivers);
                    channel.setTransmitters(transmitters);
                    channel.setScreens(screens);

                    // Done. Return channel
                    rsSigns.close();
                    statement.close();
                    closeConnection(connection);
                    return channel;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Channel not found
    }

    @Override
    public boolean createWirelessChannel(final WirelessChannel channel) {
        if (!sqlTableExists(channel.getName())) // Check if channel already
        // exists
        {
            // Get the type of the sign that has been created
            if (!channel.getReceivers().isEmpty() && !channel.getTransmitters().isEmpty() && !channel.getScreens().isEmpty()) {
                WirelessRedstone.getWRLogger().severe("Channel created with no IWirelessPoint in, stopping the creation of the channel.");
                return false;
            }

            try {
                // Create the table
                Connection connection = getConnection();
                if(connection == null) {
                    WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                    return false;
                }
                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE TABLE " + getDBName(channel.getName()) + " ( "

                        // First columns are for the channel
                        + sqlChannelId + " int," + sqlChannelName + " char(64)," + sqlChannelLocked + " int (1)," + sqlChannelOwners
                        + " char(64),"

                        // After there are the signs colums
                        + sqlSignType + " char(32)," + sqlSignX + " int," + sqlSignY + " int," + sqlSignZ + " int," + sqlDirection
                        + " int," + sqlSignOwner + " char(64)," + sqlSignWorld + " char(128)," + sqlIsWallSign + " int(1)" + " ) ");

                // Fill the columns name, id and locked
                statement.executeUpdate("INSERT INTO " + getDBName(channel.getName()) + " (" + sqlChannelId + "," + sqlChannelName + ","
                        + sqlChannelLocked + "," + sqlChannelOwners + ") " + "VALUES (" + channel.getId() + "," + "'" + channel.getName()
                        + "',"	+ "0" + "," + "'" + channel.getOwners().get(0) + "')");
                // owner
                // Finished this part
                statement.close();
                closeConnection(connection);

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

                if (WirelessRedstone.cache == null) Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        WirelessRedstone.cache.update();
                    }
                }, 1L);
                else WirelessRedstone.cache.update();
                return true;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        WirelessRedstone.getWRLogger().debug("Tried to create a channel that already exists in the database");
        return false;
    }

    @Override
    public boolean renameWirelessChannel(final String channelName, final String newChannelName) {
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
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return false;
            }
            Statement statement = connection.createStatement();

            // Remove the old channel in the config
            statement.executeUpdate("DROP TABLE " + getDBName(channelName));

            statement.close();
            closeConnection(connection);

            // Set a new channel - HAVE TO FIND A BETTER WAY THAN JUST REMOVING
            // THE TABLE AND CREATE AN OTHER
            createWirelessChannel(channel);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void removeWirelessChannel(final String channelName) {
        try {
            WirelessRedstone.WireBox.removeSigns(getWirelessChannel(channelName));
            if (!sqlTableExists(channelName)) return;
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return;
            }
            Statement statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE " + getDBName(channelName));
            statement.close();
            closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            WirelessRedstone.cache.update();
        }
    }

    @Override
    public Collection<WirelessChannel> getAllChannels() {
        Statement statement;
        try {
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return null;
            }
            statement = connection.createStatement();
            ArrayList<WirelessChannel> channels = new ArrayList<WirelessChannel>();

            ResultSet rs = null;

            try {
                rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
            } catch (NullPointerException ex) {
                WirelessRedstone.getWRLogger().severe("SQL: NullPointerException when asking for the list of channels!");
                return new ArrayList<WirelessChannel>();
            }
            ArrayList<String> channelNames = new ArrayList<String>();
            while (rs.next()) {
                channelNames.add(getNormalName(rs.getString("name")));
            }
            rs.close();
            statement.close();
            closeConnection(connection);

            for (String channelName : channelNames) {
                channels.add(getWirelessChannel(channelName));
            }
            return channels;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored) {

        }
        return null; // Channel not found
    }

    @Override
    public boolean createWirelessPoint(final String channelName, final IWirelessPoint point) {
        if (!sqlTableExists(channelName)) {
            WirelessRedstone.getWRLogger().severe("Could not create this wireless point in the channel " + channelName + ", it does not exist!");
        }

        int iswallsign;
        String signtype;

        if (point instanceof WirelessReceiver) {
            if (point instanceof WirelessReceiverInverter) signtype = "receiver_inverter";
            else if (point instanceof WirelessReceiverDelayer)
                signtype = "receiver_delayer_" + ((WirelessReceiverDelayer) (point)).getDelay();
            else if (point instanceof WirelessReceiverSwitch) {
                boolean state;
                if (WirelessRedstone.WireBox.switchState.get(((WirelessReceiverSwitch) (point)).getLocation()) != null)
                    state = WirelessRedstone.WireBox.switchState.get(((WirelessReceiverSwitch) (point)).getLocation());
                else state = false;
                signtype = "receiver_switch_" + state;
            } else if (point instanceof WirelessReceiverClock) signtype = "receiver_clock_" + ((WirelessReceiverClock) (point)).getDelay();
            else signtype = "receiver";
        } else if (point instanceof WirelessTransmitter) {
            signtype = "transmitter";
        } else if (point instanceof WirelessScreen) {
            signtype = "screen";
        } else {
            return false;
        }

        if (point.getIsWallSign()) {
            iswallsign = 1;
        } else {
            iswallsign = 0;
        }

        try {
            int intDirection = WirelessRedstone.WireBox.signFaceToInt(point.getDirection());
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return false;
            }
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO " + getDBName(channelName) + " (" + sqlSignType + "," + sqlSignX
                    + "," + sqlSignY + "," + sqlSignZ + "," + sqlDirection + "," + sqlSignOwner + ","
                    + sqlSignWorld + "," + sqlIsWallSign + ") " + "VALUES ('" + signtype + "',"
                    + point.getX() + "," + point.getY() + "," + point.getZ() + "," + intDirection + "," + "'"
                    + point.getOwner() + "'," + "'" + point.getWorld() + "'," + iswallsign + " ) ");
            statement.close();
            closeConnection(connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    @Override
    public void updateChannel(final String channelName, final WirelessChannel channel) {
        try {
            int locked = (channel.isLocked()) ? 1 : 0;
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return;
            }
            Statement statement = connection.createStatement();

            // Update name and lock status
            statement.executeUpdate("UPDATE " + getDBName(channelName) + " SET "
                    + sqlChannelName + "='" + channel.getName() + "' ," + sqlChannelLocked + "=" + locked + " " + "WHERE "
                    + sqlChannelId + "=" + channel.getId());

            // Then update the owners
			/*
			 * Temporary disabled because it makes the plugin crashing.
			 * statement.executeUpdate("ALTER TABLE " + getDBName(channelName) +
			 * " DROP COLUMN " + sqlChannelOwners);
			 * statement.executeUpdate("ALTER TABLE " + getDBName(channelName) +
			 * " ADD COLUMN " + sqlChannelOwners); for(String owner :
			 * channel.getOwners()) { statement.executeUpdate("INSERT INTO " +
			 * getDBName(channelName) + " (" + sqlChannelOwners + ") VALUES " +
			 * owner); }
			 */
            statement.close();
            closeConnection(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean removeWirelessReceiver(final String channelName, final Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeReceiverAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    @Override
    public boolean removeWirelessTransmitter(final String channelName, final Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeTransmitterAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    @Override
    public boolean removeWirelessScreen(final String channelName, final Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeScreenAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    /**
     * Private method to purge data. Don't use it anywhere else
     */
    private boolean removeWirelessReceiver(final String channelName, final Location loc, final String world) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeReceiverAt(loc, world);
            return removeWirelessPoint(channelName, loc, world);
        } else return false;
    }

    /**
     * Private method to purge data. Don't use it anywhere else
     */
    private boolean removeWirelessTransmitter(final String channelName, final Location loc, final String world) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeTransmitterAt(loc, world);
            return removeWirelessPoint(channelName, loc, world);
        } else return false;
    }

    /**
     * Private method to purge data. Don't use it anywhere else
     */
    private boolean removeWirelessScreen(final String channelName, final Location loc, final String world) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeScreenAt(loc, world);
            return removeWirelessPoint(channelName, loc, world);
        } else return false;
    }

    private boolean removeWirelessPoint(final String channelName, final Location loc, final String world) {
        try {
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return false;
            }
            Statement statement = connection.createStatement();
            String sql = "DELETE FROM " + getDBName(channelName) + " WHERE " + sqlSignX + "="
                    + loc.getBlockX() + " AND " + sqlSignY + "=" + loc.getBlockY() + " AND " + sqlSignZ + "="
                    + loc.getBlockZ() + " AND " + sqlSignWorld + "='" + world + "'";
            statement.executeUpdate(sql);
            WirelessRedstone.getWRLogger().debug("Statement to delete wireless sign : " + sql);
            statement.close();
            closeConnection(connection);
            WirelessRedstone.cache.update();
        } catch (SQLException ex) {
            WirelessRedstone.getWRLogger().debug(ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean purgeData() {
        try {
            // Get the names of all the tables
            Collection<WirelessChannel> channels = new ArrayList<WirelessChannel>();
            channels = getAllChannels();

            ArrayList<String> remove = new ArrayList<String>();
            ArrayList<IWirelessPoint> removeSigns = new ArrayList<IWirelessPoint>();

            // Erase channel if empty or world doesn't exist
            for (WirelessChannel channel : channels) {
                HashMap<Location, String> receivers = new HashMap<Location, String>();
                HashMap<Location, String> transmitters = new HashMap<Location, String>();
                HashMap<Location, String> screens = new HashMap<Location, String>();
                ArrayList<Location> locationCheck = new ArrayList<Location>();

                for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                    if (locationCheck.contains(transmitter.getLocation()))
                        receivers.put(transmitter.getLocation(), channel.getName() + "~" + transmitter.getWorld());
                    else locationCheck.add(transmitter.getLocation());
                    if (Bukkit.getWorld(transmitter.getWorld()) == null) {
                        transmitters.put(transmitter.getLocation(), channel.getName() + "~" + transmitter.getWorld());
                    }
                }

                for (WirelessReceiver receiver : channel.getReceivers()) {
                    if (locationCheck.contains(receiver.getLocation()))
                        receivers.put(receiver.getLocation(), channel.getName() + "~" + receiver.getWorld());
                    else locationCheck.add(receiver.getLocation());
                    if (Bukkit.getWorld(receiver.getWorld()) == null) {
                        receivers.put(receiver.getLocation(), channel.getName() + "~" + receiver.getWorld());
                    }
                }

                for (WirelessScreen screen : channel.getScreens()) {
                    if (locationCheck.contains(screen.getLocation()))
                        receivers.put(screen.getLocation(), channel.getName() + "~" + screen.getWorld());
                    else locationCheck.add(screen.getLocation());
                    if (Bukkit.getWorld(screen.getWorld()) == null) {
                        screens.put(screen.getLocation(), channel.getName() + "~" + screen.getWorld());
                    }
                }

                for (Map.Entry<Location, String> receiverRemove : receivers.entrySet()) {
                    removeWirelessReceiver(receiverRemove.getValue().split("~")[0], receiverRemove.getKey(), receiverRemove.getValue().split("~")[1]);
                }
                for (Map.Entry<Location, String> transmitterRemove : transmitters.entrySet()) {
                    removeWirelessTransmitter(transmitterRemove.getValue().split("~")[0], transmitterRemove.getKey(), transmitterRemove.getValue().split("~")[1]);
                }
                for (Map.Entry<Location, String> screenRemove : screens.entrySet()) {
                    removeWirelessScreen(screenRemove.getValue().split("~")[0], screenRemove.getKey(), screenRemove.getValue().split("~")[1]);
                }

                if ((channel.getReceivers().size() < 1) && (channel.getTransmitters().size() < 1) && (channel.getScreens().size() < 1)) {
                    remove.add(channel.getName());
                }
            }

            for (String channelRemove : remove) {
                WirelessRedstone.config.removeWirelessChannel(channelRemove);
            }

            return true;
        } catch (Exception e) {
            WirelessRedstone.getWRLogger().severe("An error occured. Enable debug mode to see the stacktraces.");
            if (WirelessRedstone.config.getDebugMode()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public IWirelessPoint getWirelessRedstoneSign(final Location loc) {
        for (WirelessChannel channel : getAllChannels()) {
            for (WirelessReceiver receiver : channel.getReceivers()) {
                if (WirelessRedstone.sameLocation(receiver.getLocation(), loc)) return receiver;
            }
            for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                if (WirelessRedstone.sameLocation(transmitter.getLocation(), loc)) return transmitter;
            }
            for (WirelessScreen screen : channel.getScreens()) {
                if (WirelessRedstone.sameLocation(screen.getLocation(), loc)) return screen;
            }
        }
        return null;
    }

    @Override
    public String getWirelessChannelName(final Location loc) {
        for (WirelessChannel channel : getAllChannels()) {
            for (WirelessReceiver receiver : channel.getReceivers()) {
                if (WirelessRedstone.sameLocation(receiver.getLocation(), loc)) return channel.getName();
            }
            for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                if (WirelessRedstone.sameLocation(transmitter.getLocation(), loc)) return channel.getName();
            }
            for (WirelessScreen screen : channel.getScreens()) {
                if (WirelessRedstone.sameLocation(screen.getLocation(), loc)) return channel.getName();
            }
        }
        return null;
    }

    @Override
    public boolean removeIWirelessPoint(final String channelName, final Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel == null) return false;
        for (WirelessReceiver receiver : channel.getReceivers()) {
            if (WirelessRedstone.sameLocation(receiver.getLocation(), loc)) return removeWirelessReceiver(channelName, loc);
        }
        for (WirelessTransmitter transmitter : channel.getTransmitters()) {
            if (WirelessRedstone.sameLocation(transmitter.getLocation(), loc)) return removeWirelessTransmitter(channelName, loc);
        }
        for (WirelessScreen screen : channel.getScreens()) {
            if (WirelessRedstone.sameLocation(screen.getLocation(), loc)) return removeWirelessScreen(channelName, loc);
        }
        return false;
    }

    @Override
    public boolean isChannelEmpty(WirelessChannel channel) {
        return (channel.getReceivers().size() < 1) && (channel.getTransmitters().size() < 1) && (channel.getScreens().size() < 1);
    }


    @Override
    public void checkChannel(String channelName) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            if (isChannelEmpty(channel)) removeWirelessChannel(channelName);
        }
    }

    @Override
    public int restoreData() {
        try {
            if (getLastBackup() == null) {
                if (WirelessRedstone.config.getDebugMode())
                    WirelessRedstone.getWRLogger().debug("Couldn't get last backup, aborting restore");
                return 0;
            }

            File mainFolder = new File(channelFolder.getCanonicalPath().split(channelFolder.getName())[0]);


            return unZip(mainFolder + File.separator + getLastBackup(), channelFolder.getAbsolutePath());
        } catch (Exception e) {
            if (WirelessRedstone.config.getDebugMode()) e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void updateReceivers() {
        for (WirelessChannel channel : getAllChannels()) {
            for (WirelessReceiver receiver : channel.getReceivers()) {
                if (receiver instanceof WirelessReceiverSwitch) {
                    if (WirelessRedstone.config.getDebugMode())
                        WirelessRedstone.getWRLogger().debug("Updating Switcher from channel " + channel.getName());
                    updateSwitch(channel, receiver);
                }
            }
        }
    }

    private void updateSwitch(WirelessChannel channel, WirelessReceiver receiver) {
        try {
            Connection connection = getConnection();
            if(connection == null) {
                WirelessRedstone.getWRLogger().severe("Can't connect to MySQL database!");
                return;
            }
            Statement statement = connection.createStatement();

            // Update name and lock status
            statement.executeUpdate("UPDATE " + getDBName(channel.getName()) + " SET " + sqlSignType
                    + "='receiver_switch_" + ((WirelessReceiverSwitch) receiver).getState() + "' WHERE "
                    + sqlSignWorld + "='" + receiver.getWorld() + "' AND " + sqlSignX + "=" + receiver.getX() + " AND "
                    + sqlSignY + "=" + receiver.getY() + " AND " + sqlSignZ + "=" + receiver.getZ());
            statement.close();
            closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getLastBackup() {
        ArrayList<String> files = new ArrayList<String>();
        try {
            File folder = new File(channelFolder.getCanonicalPath().split(channelFolder.getName())[0]);
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    continue;
                } else if (!fileEntry.getName().startsWith("WRBackup")) {
                    continue;
                } else {
                    files.add(fileEntry.getName());
                }
            }
        } catch (Exception e) {
            if (WirelessRedstone.config.getDebugMode()) e.printStackTrace();
            return null;
        }
        if (!files.isEmpty()) return files.get(files.size() - 1);

        if (WirelessRedstone.config.getDebugMode()) WirelessRedstone.getWRLogger().debug("There are no backups, aborting restore");
        return null;
    }

    private int unZip(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];
        try {
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            int returnValue = 1;
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                if (WirelessRedstone.config.getDebugMode())
                    WirelessRedstone.getWRLogger().debug("File unziped: " + newFile.getAbsoluteFile());

                if (fileName.endsWith(".db")) {
                    returnValue = 2;
                    if (WirelessRedstone.config.getDebugMode())
                        WirelessRedstone.getWRLogger().debug("Found DB file! Changing storage type to DB after restore.");
                } else if (fileName.endsWith(".yml")) {
                    returnValue = 3;
                    if (WirelessRedstone.config.getDebugMode())
                        WirelessRedstone.getWRLogger().debug("Found yml file! Changing storage type to yml after restore.");
                }

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            if (WirelessRedstone.config.getDebugMode()) WirelessRedstone.getWRLogger().debug("Unpacking zip done!");

            return returnValue;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private Connection getConnection(){
        try{
            return mySQL.openConnection();
        } catch (SQLException | ClassNotFoundException ex) {
            return null;
        }
    }
    private boolean closeConnection(Connection connection){
        try{
            connection.close();
            return true;
        } catch (SQLException ex){
            return false;
        }
    }
}
