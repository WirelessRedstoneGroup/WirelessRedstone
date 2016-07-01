package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Libs.*;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.*;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
            Main.getWRLogger().info("Successfully closed SQLite sqLite.getConnection().");
        } catch (SQLException e) {
            Main.getWRLogger().warning("Cannot close SQLite sqLite.getConnection().");
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
                        .setIfNotExist(false).toString());
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
                    .set(sqlChannelName + "='" + newChannelName + "'")
                    .where(sqlChannelName + "='" + channelName + "'")
                    .toString());
            statement.executeUpdate("RENAME TABLE '" + channelName + "' TO '" + newChannelName + "'");

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

            Main.getWRLogger().info("Channels saved in archive: " + zipName);
        } catch (IOException e) {
            e.printStackTrace();
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
                removeWirelessChannel(channelRemove);
            }

            return true;
        } catch (Exception e) {
            Main.getWRLogger().severe("An error occured. Enable debug mode to see the stacktraces.");
            if (ConfigManager.getConfig().getDebugMode()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public boolean convertFromAnotherStorage(StorageType type) {
        Main.getWRLogger().info("Backuping the channels/ folder before transfer.");
        boolean canConinue = true;

        if (type == StorageType.YAML)
            canConinue = backupData("yml");

        if (!canConinue) {
            Main.getWRLogger().severe("Backup failed! Data transfer abort...");
            return false;
        } else {
            Main.getWRLogger().info("Backup done. Starting data transfer...");

            if (type == StorageType.YAML) {
                YamlStorage yaml = new YamlStorage(channelFolderStr);
                yaml.initiate(false);
                for (WirelessChannel channel : yaml.getAllChannels()) {
                    createWirelessChannel(channel);
                }
                yaml.close();
                for (File f : channelFolder.listFiles()) {
                    if (f.getName().contains(".yml")) {
                        f.delete();
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isChannelEmpty(WirelessChannel channel) {
        return (channel.getReceivers().size() < 1) && (channel.getTransmitters().size() < 1) && (channel.getScreens().size() < 1);
    }

    @Override
    public Collection<WirelessChannel> getAllChannels() {
        Statement statement;
        try {
            statement = sqLite.getConnection().createStatement();
            ArrayList<WirelessChannel> channels = new ArrayList<WirelessChannel>();

            ResultSet rs = null;

            try {
                rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
            } catch (NullPointerException ex) {
                Main.getWRLogger().severe("SQL: NullPointerException when asking for the list of channels!");
                return new ArrayList<WirelessChannel>();
            }
            ArrayList<String> channelNames = new ArrayList<String>();
            while (rs.next()) {
                channelNames.add(getNormalName(rs.getString("name")));
            }
            rs.close();
            statement.close();

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
    public WirelessChannel getWirelessChannel(String r_channelName) {
        try {
            Statement statement = sqLite.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
            ArrayList<String> channels = new ArrayList<String>();

            while (rs.next()) {
                channels.add(getNormalName(rs.getString("name")));
            }
            rs.close(); // Always close the ResultSet

            for (String channelName : channels) {
                if (channelName.equals(r_channelName)) {
                    // Get the ResultSet from the table we want
                    ResultSet rsChannelInfo = statement.executeQuery("SELECT * FROM " + getDatabaseFriendlyName(channelName));
                    try {
                        rsChannelInfo.getString("name");
                    } catch (SQLException ex) {
                        statement.executeUpdate("DROP TABLE " + getDatabaseFriendlyName(channelName));
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
                        if (rsChannelInfo.getString(sqlChannelOwners) != null)
                            owners.add(rsChannelInfo.getString(sqlChannelOwners));
                    }
                    channel.setOwners(owners);
                    rsChannelInfo.close();

                    // Because a SQLite ResultSet is TYPE_FORWARD only, we have
                    // to create a third ResultSet and close the second
                    ResultSet rsSigns = statement.executeQuery("SELECT * FROM " + getDatabaseFriendlyName(channelName));

                    // Set the wireless signs
                    ArrayList<WirelessReceiver> receivers = new ArrayList<WirelessReceiver>();
                    ArrayList<WirelessTransmitter> transmitters = new ArrayList<WirelessTransmitter>();
                    ArrayList<WirelessScreen> screens = new ArrayList<WirelessScreen>();
                    rsSigns.next();// Because first row does not contain a wireless sign
                    while (rsSigns.next()) {
                        if (rsSigns.getString(sqlSignType).equals("receiver")) {
                            WirelessReceiver receiver = new WirelessReceiver();
                            receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).equals("receiver_inverter")) {
                            WirelessReceiverInverter receiver = new WirelessReceiverInverter();
                            receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
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
                            receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
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
                            receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
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
                            receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).equals("transmitter")) {
                            WirelessTransmitter transmitter = new WirelessTransmitter();
                            transmitter.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
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
                            screen.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
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
                    return channel;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Channel not found
    }

    @Override
    public IWirelessPoint getWirelessRedstoneSign(Location loc) {
        for (WirelessChannel channel : getAllChannels()) {
            for (WirelessReceiver receiver : channel.getReceivers()) {
                if (Utils.sameLocation(receiver.getLocation(), loc)) return receiver;
            }
            for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                if (Utils.sameLocation(transmitter.getLocation(), loc)) return transmitter;
            }
            for (WirelessScreen screen : channel.getScreens()) {
                if (Utils.sameLocation(screen.getLocation(), loc)) return screen;
            }
        }
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
                    return StorageType.YAML;
                }
            }
        } catch (NullPointerException ignored) {
        }

        return null;
    }

    @Override
    public String getWirelessChannelName(Location loc) {
        for (WirelessChannel channel : getAllChannels()) {
            for (WirelessReceiver receiver : channel.getReceivers()) {
                if (Utils.sameLocation(receiver.getLocation(), loc)) return channel.getName();
            }
            for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                if (Utils.sameLocation(transmitter.getLocation(), loc)) return channel.getName();
            }
            for (WirelessScreen screen : channel.getScreens()) {
                if (Utils.sameLocation(screen.getLocation(), loc)) return channel.getName();
            }
        }
        return null;
    }

    @Override
    public void updateChannel(String channelName, WirelessChannel channel) {
        try {
            int locked = (channel.isLocked()) ? 1 : 0;
            Statement statement = sqLite.getConnection().createStatement();

            statement.executeUpdate(new UpdateBuilder(getDatabaseFriendlyName(channelName))
                    .set(sqlChannelName + "='" + channel.getName() + "'")
                    .set(sqlChannelLocked + "='" + locked + "'")
                    .where(sqlChannelId + "='" + channel.getId() + "'")
                    .toString());

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateReceivers() {
        for (WirelessChannel channel : getAllChannels()) {
            for (WirelessReceiver receiver : channel.getReceivers()) {
                if (receiver instanceof WirelessReceiverSwitch) {
                    Main.getWRLogger().debug("Updating Switcher from channel " + channel.getName());
                    updateSwitch(channel, receiver);
                }
            }
        }
    }

    @Override
    public void checkChannel(String channelName) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            if (isChannelEmpty(channel)) removeWirelessChannel(channelName);
        }
    }

    @Override
    public void removeWirelessChannel(String channelName) {
        try {
            Main.getSignManager().removeSigns(getWirelessChannel(channelName));
            if (!sqlTableExists(channelName)) return;
            Statement statement = sqLite.getConnection().createStatement();
            statement.executeUpdate("DROP TABLE " + getDatabaseFriendlyName(channelName));
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Main.getGlobalCache().update();
        }
    }

    public boolean initiate(boolean allowConvert) {
        Main.getWRLogger().info("Establishing sqLite.getConnection() to database...");

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

        Main.getWRLogger().info("sqLite.getConnection() established.");

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

    private void updateSwitch(WirelessChannel channel, WirelessReceiver receiver) {
        try {
            Statement statement = sqLite.getConnection().createStatement();

            statement.executeUpdate(new UpdateBuilder(getDatabaseFriendlyName(channel.getName()))
                    .set(sqlSignType + "='" + "receiver_switch_" + ((WirelessReceiverSwitch) receiver).getState() + "'")
                    .where(sqlSignWorld + "='" + receiver.getWorld() + "'")
                    .where(sqlSignX + "='" + receiver.getX() + "'")
                    .where(sqlSignY + "='" + receiver.getY() + "'")
                    .where(sqlSignZ + "='" + receiver.getZ() + "'")
                    .toString());

            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean removeWirelessPoint(String channelName, Location loc, String world) {
        try {
            Statement statement = sqLite.getConnection().createStatement();
            String sql = new DeleteBuilder(getDatabaseFriendlyName(channelName))
                    .where(sqlSignX + "='" + loc.getBlockX() + "'")
                    .where(sqlSignY + "='" + loc.getBlockY() + "'")
                    .where(sqlSignZ + "='" + loc.getBlockZ() + "'")
                    .where(sqlSignWorld + "='" + world + "'")
                    .toString();
            Main.getWRLogger().debug("Statement to delete wireless sign : " + sql);
            statement.executeUpdate(sql);
            statement.close();
            Main.getGlobalCache().update();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Private method to purge data. Don't use it anywhere else
     */
    private boolean removeWirelessReceiver(String channelName, Location loc, String world) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeReceiverAt(loc, world);
            return removeWirelessPoint(channelName, loc, world);
        } else return false;
    }

    /**
     * Private method to purge data. Don't use it anywhere else
     */
    private boolean removeWirelessTransmitter(String channelName, Location loc, String world) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeTransmitterAt(loc, world);
            return removeWirelessPoint(channelName, loc, world);
        } else return false;
    }

    /**
     * Private method to purge data. Don't use it anywhere else
     */
    private boolean removeWirelessScreen(String channelName, Location loc, String world) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeScreenAt(loc, world);
            return removeWirelessPoint(channelName, loc, world);
        } else return false;
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
