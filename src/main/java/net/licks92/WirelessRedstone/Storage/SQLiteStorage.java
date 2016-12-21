package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Libs.*;
import net.licks92.WirelessRedstone.Signs.*;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SQLiteStorage implements IWirelessStorageConfiguration {

    private Boolean useGlobalCache = true;

    private File channelFolder;
    private String channelFolderStr;
    private SQLite sqLite;

    private String sqlIsWallSign = SQLiteMap.sqlIsWallSign;
    private String sqlDirection = SQLiteMap.sqlDirection;
    private String sqlChannelId = SQLiteMap.sqlChannelId;
    private String sqlChannelName = SQLiteMap.sqlChannelName;
    private String sqlChannelLocked = SQLiteMap.sqlChannelLocked;
    private String sqlChannelOwners = SQLiteMap.sqlChannelOwners;
    private String sqlSignOwner = SQLiteMap.sqlSignOwner;
    private String sqlSignWorld = SQLiteMap.sqlSignWorld;
    private String sqlSignX = SQLiteMap.sqlSignX;
    private String sqlSignY = SQLiteMap.sqlSignY;
    private String sqlSignZ = SQLiteMap.sqlSignZ;
    private String sqlSignType = SQLiteMap.sqlSignType;


    public SQLiteStorage(String channelFolder) {
        this.channelFolder = new File(WirelessRedstone.getInstance().getDataFolder(), channelFolder);
        this.channelFolderStr = channelFolder;
        this.sqLite = new SQLite(WirelessRedstone.getInstance(), channelFolder + File.separator + "WirelessRedstoneDatabase.db", useGlobalCache);
    }

    @Override
    public boolean initStorage() {
        return initiate(true);
    }

    @Override
    public boolean close() {
        try {
            sqLite.closeConnection();
            WirelessRedstone.getWRLogger().info("Successfully closed SQLite sqLite.getConnection().");
        } catch (SQLException e) {
            WirelessRedstone.getWRLogger().warning("Cannot close SQLite sqLite.getConnection().");
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
                WirelessRedstone.getWRLogger().severe("Channel created with no IWirelessPoint in, stopping the creation of the channel.");
                return false;
            }

            try {
                // Create the table
                PreparedStatement create = sqLite.getConnection().prepareStatement(new CreateBuilder(WirelessRedstone.getUtils().getDatabaseFriendlyName(channel.getName()))
                        .addColumn(sqlChannelId, "int").addColumn(sqlChannelName, "char(64)")
                        .addColumn(sqlChannelLocked, "int(1)").addColumn(sqlChannelOwners, "char(255)")
                        .addColumn(sqlDirection, "char(255)").addColumn(sqlIsWallSign, "int(1)")
                        .addColumn(sqlSignType, "char(255)").addColumn(sqlSignX, "int")
                        .addColumn(sqlSignY, "int").addColumn(sqlSignZ, "int")
                        .addColumn(sqlSignWorld, "char(255)").addColumn(sqlSignOwner, "char(255)")
                        .setIfNotExist(false).toString());

                //We can't async this statement because it is to important and it can cause nullpointer exceptions while inserting data
//                sqLite.execute(create);
                create.execute();
                create.close();

                PreparedStatement insert = sqLite.getConnection().prepareStatement(new InsertBuilder(WirelessRedstone.getUtils().getDatabaseFriendlyName(channel.getName()))
                        .addColumnWithValue(sqlChannelId, channel.getId())
                        .addColumnWithValue(sqlChannelName, channel.getName())
                        .addColumnWithValue(sqlChannelLocked, 0)
                        .addColumnWithValue(sqlChannelOwners, channel.getOwners().get(0))
                        .toString());

//                sqLite.execute(insert);
                insert.execute();
                insert.close();

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
                return true;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        WirelessRedstone.getWRLogger().debug("Tried to create a channel that already exists in the database");
        return false;
    }

    @Override
    public boolean createWirelessPoint(String channelName, IWirelessPoint point) {
        if (!sqlTableExists(channelName)) {
            WirelessRedstone.getWRLogger().severe("Could not create this wireless point in the channel " + channelName + ", it does not exist!");
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
                if (WirelessRedstone.getSignManager().switchState.get(((WirelessReceiverSwitch) (point)).getLocation()) != null)
                    state = WirelessRedstone.getSignManager().switchState.get(((WirelessReceiverSwitch) (point)).getLocation());
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
            PreparedStatement insert = sqLite.getConnection().prepareStatement(new InsertBuilder(WirelessRedstone.getUtils().getDatabaseFriendlyName(channelName))
                    .addColumnWithValue(sqlSignType, signType)
                    .addColumnWithValue(sqlSignX, point.getX())
                    .addColumnWithValue(sqlSignY, point.getY())
                    .addColumnWithValue(sqlSignZ, point.getZ())
                    .addColumnWithValue(sqlSignWorld, point.getWorld())
                    .addColumnWithValue(sqlDirection, point.getDirection().toString().toUpperCase())
                    .addColumnWithValue(sqlSignOwner, point.getOwner())
                    .addColumnWithValue(sqlIsWallSign, isWallSign)
                    .toString());
            sqLite.execute(insert);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean removeIWirelessPoint(String channelName, Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName, true);
        if (channel == null) return false;

        for (WirelessReceiver receiver : channel.getReceivers()) {
            if (WirelessRedstone.getUtils().sameLocation(receiver.getLocation(), loc))
                return removeWirelessReceiver(channelName, loc);
        }
        for (WirelessTransmitter transmitter : channel.getTransmitters()) {
            if (WirelessRedstone.getUtils().sameLocation(transmitter.getLocation(), loc))
                return removeWirelessTransmitter(channelName, loc);
        }
        for (WirelessScreen screen : channel.getScreens()) {
            if (WirelessRedstone.getUtils().sameLocation(screen.getLocation(), loc))
                return removeWirelessScreen(channelName, loc);
        }
        return false;
    }

    @Override
    public boolean removeWirelessReceiver(String channelName, Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName, true);
        if (channel != null) {
            channel.removeReceiverAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    @Override
    public boolean removeWirelessTransmitter(String channelName, Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName, true);
        if (channel != null) {
            channel.removeTransmitterAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    @Override
    public boolean removeWirelessScreen(String channelName, Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName, true);
        if (channel != null) {
            channel.removeScreenAt(loc);
            return removeWirelessPoint(channelName, loc, loc.getWorld().getName());
        } else return false;
    }

    @Override
    public boolean renameWirelessChannel(String channelName, String newChannelName) {
        WirelessChannel channel = getWirelessChannel(channelName, true);

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
            PreparedStatement update = sqLite.getConnection().prepareStatement(new UpdateBuilder(WirelessRedstone.getUtils().getDatabaseFriendlyName(channelName))
                    .set(sqlChannelName + "='" + newChannelName + "'")
                    .where(sqlChannelName + "='" + channelName + "'")
                    .toString());
            sqLite.execute(update);
            PreparedStatement rename = sqLite.getConnection().prepareStatement("RENAME TABLE '" + channelName + "' TO '" + newChannelName + "'");
            sqLite.execute(rename);
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
            PreparedStatement master = sqLite.getConnection().prepareStatement("SELECT 'name' FROM sqlite_master WHERE type = \"table\"");
            ResultSet rs = sqLite.query(master);
            ArrayList<String> tables = new ArrayList<String>();
            while (rs.next()) {
                tables.add(rs.getString(sqlChannelName));
            }
            rs.close();

            // Erase all the tables
            for (String channelName : tables) {
                removeWirelessChannel(channelName, false);
            }

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

            WirelessRedstone.getWRLogger().info("Channels saved in archive: " + zipName);
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
            WirelessRedstone.getWRLogger().severe("An error occured. Enable debug mode to see the stacktraces.");
            if (ConfigManager.getConfig().getDebugMode()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public boolean convertFromAnotherStorage(StorageType type) {
        WirelessRedstone.getWRLogger().info("Backuping the channels/ folder before transfer.");
        boolean canConinue = true;

        if (type == StorageType.YAML)
            canConinue = backupData("yml");

        if (!canConinue) {
            WirelessRedstone.getWRLogger().severe("Backup failed! Data transfer abort...");
            return false;
        } else {
            WirelessRedstone.getWRLogger().info("Backup done. Starting data transfer...");

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
        return channel == null || (channel.getReceivers().size() < 1) && (channel.getTransmitters().size() < 1) && (channel.getScreens().size() < 1);
    }

    @Override
    public Collection<WirelessChannel> getAllChannels() {
        return getAllChannels(false);
    }

    @Override
    public Collection<WirelessChannel> getAllChannels(Boolean forceUpdate) {
        if (useGlobalCache && WirelessRedstone.getGlobalCache() != null && !forceUpdate) {
            if (WirelessRedstone.getGlobalCache().getAllChannels() != null) {
//                WirelessRedstone.getWRLogger().debug("Accessed all WirelessChannel from cache");
                return WirelessRedstone.getGlobalCache().getAllChannels();
            }
        }

        try {
            ArrayList<WirelessChannel> channels = new ArrayList<>();

            ResultSet rs = null;

            try {
                PreparedStatement master = sqLite.getConnection().prepareStatement("SELECT `name` FROM sqlite_master WHERE type = \"table\"");
                rs = sqLite.query(master);
            } catch (NullPointerException ex) {
                WirelessRedstone.getWRLogger().severe("SQL: NullPointerException when asking for the list of channels!");
                return new ArrayList<>();
            }
            ArrayList<String> channelNames = new ArrayList<>();
            while (rs.next()) {
                channelNames.add(getNormalName(rs.getString("name")));
            }
            rs.close();

            for (String channelName : channelNames) {
                channels.add(getWirelessChannel(channelName, forceUpdate));
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
        return getWirelessChannel(r_channelName, false);
    }

    @Override
    public WirelessChannel getWirelessChannel(String r_channelName, Boolean forceUpdate) {
        if (useGlobalCache && WirelessRedstone.getGlobalCache() != null && !forceUpdate) {
            if (WirelessRedstone.getGlobalCache().getAllChannels() != null) {
                WirelessChannel channel = null;

                for (WirelessChannel cacheChannel : WirelessRedstone.getGlobalCache().getAllChannels()) {
                    if (cacheChannel == null) {
                        break;
                    }
                    if (cacheChannel.getName().equalsIgnoreCase(r_channelName)) {
                        channel = cacheChannel;
                        break;
                    }
                }

//                WirelessRedstone.getWRLogger().debug("Accessed WirelessChannel from cache");
                if (channel != null)
                    return channel;
                else
                    WirelessRedstone.getGlobalCache().update();
            }
        }

        try {
            PreparedStatement master = sqLite.getConnection().prepareStatement("SELECT `name` FROM sqlite_master WHERE type = \"table\"");
            ResultSet rs = sqLite.query(master);
            ArrayList<String> channels = new ArrayList<String>();

            while (rs.next()) {
                channels.add(getNormalName(rs.getString("name")));
            }
            rs.close(); // Always close the ResultSet

            for (String channelName : channels) {
                if (channelName.equals(r_channelName)) {
                    // Get the ResultSet from the table we want
                    master = sqLite.getConnection().prepareStatement("SELECT * FROM " + WirelessRedstone.getUtils().getDatabaseFriendlyName(channelName));
                    ResultSet rsChannelInfo = sqLite.query(master);
                    try {
                        rsChannelInfo.getString("name");
                    } catch (SQLException ex) {
                        PreparedStatement drop = sqLite.getConnection().prepareStatement("DROP TABLE " + WirelessRedstone.getUtils().getDatabaseFriendlyName(channelName));
                        sqLite.execute(drop);
                        rsChannelInfo.close();
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
                    master = sqLite.getConnection().prepareStatement("SELECT * FROM " + WirelessRedstone.getUtils().getDatabaseFriendlyName(channelName));
                    ResultSet rsSigns = sqLite.query(master);

                    // Set the wireless signs
                    ArrayList<WirelessReceiver> receivers = new ArrayList<WirelessReceiver>();
                    ArrayList<WirelessTransmitter> transmitters = new ArrayList<WirelessTransmitter>();
                    ArrayList<WirelessScreen> screens = new ArrayList<WirelessScreen>();
                    rsSigns.next();// Because first row does not contain a wireless sign
                    while (rsSigns.next()) {
                        if (rsSigns.getString(sqlSignType) == null)
                            continue;

                        if (rsSigns.getString(sqlSignType).equals("receiver")) {
                            WirelessReceiver receiver = new WirelessReceiver();
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            try {
                                receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                try {
                                    Integer directionInt = Integer.parseInt(rsSigns.getString(sqlDirection));
                                    if (receiver.getIsWallSign())
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                    else
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).equals("receiver_inverter")) {
                            WirelessReceiverInverter receiver = new WirelessReceiverInverter();
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            try {
                                receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                try {
                                    Integer directionInt = Integer.parseInt(rsSigns.getString(sqlDirection));
                                    if (receiver.getIsWallSign())
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                    else
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                } catch (NumberFormatException ignored) {
                                }
                            }
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
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            try {
                                receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                try {
                                    Integer directionInt = Integer.parseInt(rsSigns.getString(sqlDirection));
                                    if (receiver.getIsWallSign())
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                    else
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                } catch (NumberFormatException ignored) {
                                }
                            }
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
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            try {
                                receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                try {
                                    Integer directionInt = Integer.parseInt(rsSigns.getString(sqlDirection));
                                    if (receiver.getIsWallSign())
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                    else
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                } catch (NumberFormatException ignored) {
                                }
                            }
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
                            receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            try {
                                receiver.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                try {
                                    Integer directionInt = Integer.parseInt(rsSigns.getString(sqlDirection));
                                    if (receiver.getIsWallSign())
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                    else
                                        receiver.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            receiver.setOwner(rsSigns.getString(sqlSignOwner));
                            receiver.setWorld(rsSigns.getString(sqlSignWorld));
                            receiver.setX(rsSigns.getInt(sqlSignX));
                            receiver.setY(rsSigns.getInt(sqlSignY));
                            receiver.setZ(rsSigns.getInt(sqlSignZ));
                            receivers.add(receiver);
                        } else if (rsSigns.getString(sqlSignType).equals("transmitter")) {
                            WirelessTransmitter transmitter = new WirelessTransmitter();
                            transmitter.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            try {
                                transmitter.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                try {
                                    Integer directionInt = Integer.parseInt(rsSigns.getString(sqlDirection));
                                    if (transmitter.getIsWallSign())
                                        transmitter.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                    else
                                        transmitter.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            transmitter.setOwner(rsSigns.getString(sqlSignOwner));
                            transmitter.setWorld(rsSigns.getString(sqlSignWorld));
                            transmitter.setX(rsSigns.getInt(sqlSignX));
                            transmitter.setY(rsSigns.getInt(sqlSignY));
                            transmitter.setZ(rsSigns.getInt(sqlSignZ));
                            transmitters.add(transmitter);
                        }
                        if (rsSigns.getString(sqlSignType).equals("screen")) {
                            WirelessScreen screen = new WirelessScreen();
                            screen.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            try {
                                screen.setDirection(BlockFace.valueOf(rsSigns.getString(sqlDirection).toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                try {
                                    Integer directionInt = Integer.parseInt(rsSigns.getString(sqlDirection));
                                    if (screen.getIsWallSign())
                                        screen.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                    else
                                        screen.setDirection(WirelessRedstone.getUtils().intToBlockFaceSign(directionInt));
                                } catch (NumberFormatException ignored) {
                                }
                            }
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
                if (WirelessRedstone.getUtils().sameLocation(receiver.getLocation(), loc)) return receiver;
            }
            for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                if (WirelessRedstone.getUtils().sameLocation(transmitter.getLocation(), loc)) return transmitter;
            }
            for (WirelessScreen screen : channel.getScreens()) {
                if (WirelessRedstone.getUtils().sameLocation(screen.getLocation(), loc)) return screen;
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
    public StorageType restoreData() {
        try {
            if (getLastBackup() == null) {
                if (ConfigManager.getConfig().getDebugMode())
                    WirelessRedstone.getWRLogger().debug("Couldn't get last backup, aborting restore");
                return null;
            }

            File mainFolder = new File(channelFolder.getCanonicalPath().split(channelFolder.getName())[0]);

            return unZip(mainFolder + File.separator + getLastBackup(), channelFolder.getAbsolutePath());
        } catch (Exception e) {
            if (ConfigManager.getConfig().getDebugMode()) e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getWirelessChannelName(Location loc) {
        for (WirelessChannel channel : getAllChannels()) {
            for (WirelessReceiver receiver : channel.getReceivers()) {
                if (WirelessRedstone.getUtils().sameLocation(receiver.getLocation(), loc)) return channel.getName();
            }
            for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                if (WirelessRedstone.getUtils().sameLocation(transmitter.getLocation(), loc)) return channel.getName();
            }
            for (WirelessScreen screen : channel.getScreens()) {
                if (WirelessRedstone.getUtils().sameLocation(screen.getLocation(), loc)) return channel.getName();
            }
        }
        return null;
    }

    @Override
    public void updateChannel(String channelName, WirelessChannel channel) {
        try {
            int locked = (channel.isLocked()) ? 1 : 0;
            PreparedStatement update = sqLite.getConnection().prepareStatement(new UpdateBuilder(WirelessRedstone.getUtils().getDatabaseFriendlyName(channelName))
                    .set(sqlChannelName + "='" + channel.getName() + "'")
                    .set(sqlChannelLocked + "='" + locked + "'")
                    .where(sqlChannelId + "='" + channel.getId() + "'")
                    .toString());
            sqLite.execute(update);

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateReceivers() {
        for (WirelessChannel channel : getAllChannels()) {
            for (WirelessReceiver receiver : channel.getReceivers()) {
                if (receiver instanceof WirelessReceiverSwitch) {
                    WirelessRedstone.getWRLogger().debug("Updating Switcher from channel " + channel.getName());
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
        removeWirelessChannel(channelName, true);
    }

    private void removeWirelessChannel(String channelName, Boolean removeSigns) {
        try {
            if (removeSigns)
                WirelessRedstone.getSignManager().removeSigns(getWirelessChannel(channelName, true));

            if (!sqlTableExists(channelName)) return;
            PreparedStatement drop = sqLite.getConnection().prepareStatement("DROP TABLE " + WirelessRedstone.getUtils().getDatabaseFriendlyName(channelName));
            sqLite.execute(drop);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (WirelessRedstone.getGlobalCache() == null)
                Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        WirelessRedstone.getGlobalCache().update();
                    }
                }, 1L);
            else WirelessRedstone.getGlobalCache().update();
        }
    }

    public boolean initiate(boolean allowConvert) {
        WirelessRedstone.getWRLogger().debug("Establishing sqLite.getConnection() to database...");

        try {
            sqLite.openConnection();
        } catch (SQLException | ClassNotFoundException ex) {
            WirelessRedstone.getWRLogger().severe("Error while starting plugin. Message: " + ex.getLocalizedMessage() + ". Enable debug mode to see the full stack trace.");

            if (ConfigManager.getConfig().getDebugMode())
                ex.printStackTrace();

            WirelessRedstone.getWRLogger().severe("**********");
            WirelessRedstone.getWRLogger().severe("Plugin can't connect to the SQLite database. Shutting down plugin...");
            WirelessRedstone.getWRLogger().severe("**********");
            return false;
        }

        WirelessRedstone.getWRLogger().debug("sqLite.getConnection() established.");

        if (canConvert() != null && allowConvert) {
            WirelessRedstone.getWRLogger().info("WirelessRedstone found a channel in a different storage format.");
            WirelessRedstone.getWRLogger().info("Beginning data transfer to SQLite...");
            if (convertFromAnotherStorage(canConvert())) {
                WirelessRedstone.getWRLogger().info("Done! All the channels are now stored in the SQLite database.");
            }
        }

        Collection<String> remove = new ArrayList<>();

        for (WirelessChannel channel : getAllChannels()) {
            if ((channel.getReceivers().size() < 1) && (channel.getTransmitters().size() < 1) && (channel.getScreens().size() < 1)) {
                remove.add(channel.getName());
            }
        }

        for (String channelRemove : remove) {
            removeWirelessChannel(channelRemove);
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
            PreparedStatement update = sqLite.getConnection().prepareStatement(new UpdateBuilder(WirelessRedstone.getUtils().getDatabaseFriendlyName(channel.getName()))
                    .set(sqlSignType + "='" + "receiver_switch_" + ((WirelessReceiverSwitch) receiver).getState() + "'")
                    .where(sqlSignWorld + "='" + receiver.getWorld() + "'")
                    .where(sqlSignX + "='" + receiver.getX() + "'")
                    .where(sqlSignY + "='" + receiver.getY() + "'")
                    .where(sqlSignZ + "='" + receiver.getZ() + "'")
                    .toString());

            update.execute();
            update.close(); //We don't want to update the cache because this is only run when the plugin disables
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean removeWirelessPoint(String channelName, Location loc, String world) {
        try {
            String sql = new DeleteBuilder(WirelessRedstone.getUtils().getDatabaseFriendlyName(channelName))
                    .where(sqlSignX + "='" + loc.getBlockX() + "'")
                    .where(sqlSignY + "='" + loc.getBlockY() + "'")
                    .where(sqlSignZ + "='" + loc.getBlockZ() + "'")
                    .where(sqlSignWorld + "='" + world + "'")
                    .toString();
            WirelessRedstone.getWRLogger().debug("Statement to delete wireless sign : " + sql);
            PreparedStatement delete = sqLite.getConnection().prepareStatement(sql);
            sqLite.execute(delete);
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
        WirelessChannel channel = getWirelessChannel(channelName, true);
        if (channel != null) {
            channel.removeReceiverAt(loc, world);
            return removeWirelessPoint(channelName, loc, world);
        } else return false;
    }

    /**
     * Private method to purge data. Don't use it anywhere else
     */
    private boolean removeWirelessTransmitter(String channelName, Location loc, String world) {
        WirelessChannel channel = getWirelessChannel(channelName, true);
        if (channel != null) {
            channel.removeTransmitterAt(loc, world);
            return removeWirelessPoint(channelName, loc, world);
        } else return false;
    }

    /**
     * Private method to purge data. Don't use it anywhere else
     */
    private boolean removeWirelessScreen(String channelName, Location loc, String world) {
        WirelessChannel channel = getWirelessChannel(channelName, true);
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
        for (char character : WirelessRedstone.getUtils().badCharacters) {
            String ascii = "" + (int) character;
            String code = "_char_" + ascii + "_";
            if (asciiName.contains(code)) {
                asciiName = asciiName.replace(code, String.valueOf(character));
            }
        }
        return asciiName;
    }

    private String getLastBackup() {
        ArrayList<String> files = new ArrayList<String>();
        try {
            File folder = new File(channelFolder.getCanonicalPath().split(channelFolder.getName())[0]);
            for (final File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory() && fileEntry.getName().startsWith("WRBackup")) {
                    files.add(fileEntry.getName());
                }
            }
        } catch (Exception e) {
            if (ConfigManager.getConfig().getDebugMode()) e.printStackTrace();
            return null;
        }

        return (!files.isEmpty()) ? files.get(files.size() - 1) : null;
    }

    private StorageType unZip(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];
        try {
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();

            StorageType returnValue = null;
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                if (ConfigManager.getConfig().getDebugMode())
                    WirelessRedstone.getWRLogger().debug("File unziped: " + newFile.getAbsoluteFile());

                if (fileName.endsWith(".db")) {
                    returnValue = StorageType.SQLITE;
                    if (ConfigManager.getConfig().getDebugMode())
                        WirelessRedstone.getWRLogger().debug("Found SQLite file! Changing storage type to SQLite after restore.");
                } else if (fileName.endsWith(".yml")) {
                    returnValue = StorageType.YAML;
                    if (ConfigManager.getConfig().getDebugMode())
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

            if (ConfigManager.getConfig().getDebugMode())
                WirelessRedstone.getWRLogger().debug("Unpacking zip done!");

            return returnValue;
        } catch (IOException ex) {
            if (ConfigManager.getConfig().getDebugMode())
                ex.printStackTrace();
        }
        return null;
    }
}
