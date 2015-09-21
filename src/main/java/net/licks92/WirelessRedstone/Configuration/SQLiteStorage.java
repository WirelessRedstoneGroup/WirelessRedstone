package net.licks92.WirelessRedstone.Configuration;

import com.husky.sqlite.SQLite;
import net.licks92.WirelessRedstone.Channel.*;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Location;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class SQLiteStorage implements IWirelessStorageConfiguration {

    private final WirelessRedstone plugin;
    private final File channelFolder;
    private final SQLite sqLite;
    private Connection connection;

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

    public SQLiteStorage(WirelessRedstone plugin, File channelFolder) {
        this.plugin = plugin;
        this.channelFolder = channelFolder;
        sqLite = new SQLite(plugin, channelFolder.getAbsolutePath() + File.separator + "channels.db");
    }

    @Override
    public boolean initStorage() {
        return init(true);
    }

    private boolean init(boolean allowConvert) {
        WirelessRedstone.getWRLogger().debug(
                "Establishing connection to database...");

        try {
            connection = sqLite.openConnection();
        } catch (SQLException | ClassNotFoundException e) {
            WirelessRedstone.getWRLogger().severe("Couldn't open an connection from the database!");
            e.printStackTrace();
        }

        WirelessRedstone.getWRLogger().debug("Connection to SQLite Database has been established!");

        if (canConvert() && allowConvert) {
            WirelessRedstone.getWRLogger().info("Beginning data transfer... (Yaml -> DB)");
            if (convertFromAnotherStorage()) {
                WirelessRedstone.getWRLogger().info("Done! All the channels are now stored in the SQLite Database.");
            }
        }
        return true;
    }

    @Override
    public boolean close() {
        try {
            sqLite.closeConnection();
            return true;
        } catch (SQLException e) {
            WirelessRedstone.getWRLogger().severe("Couldn't close connection from the database!");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean canConvert() {
        for (File file : channelFolder.listFiles()) {
            if (file.getName().contains(".yml")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean convertFromAnotherStorage() {
        WirelessRedstone.getWRLogger().info("Backingup all the channels...");

        if (!backupData("yml")) {
            WirelessRedstone.getWRLogger().severe("Backup failed! Aborting data transfer!");
        } else {
            WirelessRedstone.getWRLogger().info("Backup done. Starting data transfer...");

            YamlStorage yaml = new YamlStorage(channelFolder, plugin);
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
        }
        return true;
    }

    @Override
    public boolean isChannelEmpty(WirelessChannel channel) {
        return (channel.getReceivers().size() < 1)
                && (channel.getTransmitters().size() < 1)
                && (channel.getScreens().size() < 1);
    }

    @Override
    public WirelessChannel getWirelessChannel(String channelNameInput) {
        try {
            //Start connection
            Statement statement = connection.createStatement();

            //Start query
            ResultSet rsGetChannel = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\";");
            ArrayList<String> channels = new ArrayList<>();

            //Get resultset and get the channel name (maybe get multiple names)

            while (rsGetChannel.next()) {
                channels.add(getNormalName(rsGetChannel.getString("name")));
            }

            for (String channelName : channels) {
                if (channelName.equals(channelNameInput)) {
                    //Get channel info
                    PreparedStatement psChannelInfo = connection.prepareStatement("SELECT * FROM ?;");
                    psChannelInfo.setString(1, getDBName(channelName));
                    psChannelInfo.executeUpdate();
                    ResultSet rsChannelInfo = psChannelInfo.getResultSet();

                    WirelessChannel channel = new WirelessChannel(rsChannelInfo.getString(sqlChannelName));

                    // Set the Id, the name, and the locked variable
                    channel.setId(rsChannelInfo.getInt(sqlChannelId));
                    switch (rsChannelInfo.getInt(sqlChannelLocked)) {
                        case 0:
                            channel.setLocked(false);
                            break;
                        case 1:
                            channel.setLocked(true);
                            break;
                        default:
                            channel.setLocked(false);
                            break;
                    }

                    // Set the owners
                    ArrayList<String> owners = new ArrayList<>();
                    while (rsChannelInfo.next()) {
                        if (rsChannelInfo.getString(sqlChannelOwners) != null)
                            owners.add(rsChannelInfo.getString(sqlChannelOwners));
                    }
                    channel.setOwners(owners);
                    rsChannelInfo.close();

                    // Because a SQLite ResultSet is TYPE_FORWARD only, we have
                    // to create a third ResultSet and close the second
                    PreparedStatement psSigns = connection.prepareStatement("SELECT * FROM ?;");
                    psChannelInfo.setString(1, getDBName(channelName));
                    psChannelInfo.executeUpdate();
                    ResultSet rsSigns = psChannelInfo.getResultSet();

                    ArrayList<WirelessReceiver> receivers = new ArrayList<>();
                    ArrayList<WirelessTransmitter> transmitters = new ArrayList<>();
                    ArrayList<WirelessScreen> screens = new ArrayList<>();
                    rsSigns.next(); //First row doesn't contain any sign

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
                        } else if (rsSigns.getString(sqlSignType).equals("receiver_delayer_")) {
                            String str = rsSigns.getString(sqlSignType).split("receiver_delayer_")[1];
                            int delay;
                            try {
                                delay = Integer.parseInt(str);
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
                        } else if (rsSigns.getString(sqlSignType).equals("receiver_clock_")) {
                            String str = rsSigns.getString(sqlSignType).split("receiver_clock_")[1];
                            int delay;
                            try {
                                delay = Integer.parseInt(str);
                            } catch (NumberFormatException ex) {
                                delay = 0;
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
                        } else if (rsSigns.getString(sqlSignType).equals("receiver_switch_")) {
                            String str = rsSigns.getString(sqlSignType).split("receiver_switch_")[1];
                            boolean toggle;
                            try {
                                toggle = Boolean.parseBoolean(str);
                            } catch (Exception ex) {
                                toggle = false;
                            }
                            WirelessReceiverSwitch receiver = new WirelessReceiverSwitch(toggle);
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
                        } else if (rsSigns.getString(sqlSignType).equals("screen")) {
                            WirelessScreen screen = new WirelessScreen();
                            screen.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
                            screen.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
                            screen.setOwner(rsSigns.getString(sqlSignOwner));
                            screen.setWorld(rsSigns.getString(sqlSignWorld));
                            screen.setX(rsSigns.getInt(sqlSignX));
                            screen.setY(rsSigns.getInt(sqlSignY));
                            screen.setZ(rsSigns.getInt(sqlSignZ));
                            screens.add(screen);
                        } else {
                            WirelessRedstone.getWRLogger().warning("I don't recognize '" + rsSigns.getString(sqlSignType)
                                    + "' as a signtype!");
                        }

                        channel.setReceivers(receivers);
                        channel.setTransmitters(transmitters);
                        channel.setScreens(screens);

                        rsSigns.close();
                        statement.close();
                        return channel;
                    }
                }
            }
            statement.close();
            return null;
        } catch (SQLException e) {
            WirelessRedstone.getWRLogger().severe("Couldn't get WirelessChannel from DB");
            if(WirelessRedstone.config.getDebugMode())
                e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getWirelessChannelName(Location loc) {
        for(WirelessChannel channel : getAllChannels()){
            for(WirelessReceiver receiver : channel.getReceivers()){
                if(WirelessRedstone.sameLocation(receiver.getLocation(), loc))
                    return channel.getName();
            }
            for(WirelessTransmitter transmitter : channel.getTransmitters()){
                if(WirelessRedstone.sameLocation(transmitter.getLocation(), loc))
                    return channel.getName();
            }
            for(WirelessScreen screen : channel.getScreens()){
                if(WirelessRedstone.sameLocation(screen.getLocation(), loc))
                    return channel.getName();
            }
        }
        return null;
    }

    @Override
    public IWirelessPoint getWirelessRedstoneSign(Location loc) {
        for(WirelessChannel channel : getAllChannels()){
            for(WirelessReceiver receiver : channel.getReceivers()){
                if(WirelessRedstone.sameLocation(receiver.getLocation(), loc))
                    return receiver;
            }
            for(WirelessTransmitter transmitter : channel.getTransmitters()){
                if(WirelessRedstone.sameLocation(transmitter.getLocation(), loc))
                    return transmitter;
            }
            for(WirelessScreen screen : channel.getScreens()){
                if(WirelessRedstone.sameLocation(screen.getLocation(), loc))
                    return screen;
            }
        }
        return null;
    }

    @Override
    public Collection<WirelessChannel> getAllChannels() {
        try {
            Statement statement;
            statement = connection.createStatement();
            ArrayList<WirelessChannel> channels = new ArrayList<>();

            ResultSet rs = null;

            try {
                rs = statement
                        .executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
            } catch (NullPointerException ex) {
                WirelessRedstone.getWRLogger().warning("SQLite: NullPointerException while requesting all the channels, "
                        + "there're probably no channels");
                return new ArrayList<>();
            }
            ArrayList<String> channelNames = new ArrayList<>();

            while (rs.next()) {
                channelNames.add(getNormalName(rs.getString(sqlChannelName)));
            }
            rs.close();
            statement.close();

            for (String channelName : channelNames) {
                channels.add(getWirelessChannel(channelName));
            }
            return channels;
        } catch (SQLException | NullPointerException e) {
            if(WirelessRedstone.config.getDebugMode())
                e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean createWirelessChannel(WirelessChannel channel) {
        return false;
    }

    @Override
    public void checkChannel(String channelName) {

    }

    @Override
    public void removeWirelessChannel(String channelName) {

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
    public void updateChannel(String channelName, WirelessChannel channel) {

    }

    @Override
    public boolean renameWirelessChannel(String channelName, String newChannelName) {
        return false;
    }

    @Override
    public boolean wipeData() {
        return false;
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
    public int restoreData() {
        return 0;
    }

    @Override
    public void updateReceivers() {

    }

    public String getDBName(String normalName) {
        try {
            Integer.parseInt(normalName);
            normalName = "num_" + normalName;
        } catch (NumberFormatException ignored) {
        }

        for (char character : WirelessRedstone.config.badCharacters) {
            if (normalName.contains(String.valueOf(character))) {
                String ascii = "" + (int) character;
                String code = "_char_" + ascii + "_";
                normalName = normalName
                        .replace(String.valueOf(character), code);
            }
        }
        return normalName;
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
}
