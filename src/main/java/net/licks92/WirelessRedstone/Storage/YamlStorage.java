package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.*;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

public class YamlStorage implements IWirelessStorageConfiguration {

    private final File channelFolder;
    private final String channelFolderStr;

    public YamlStorage(String channelFolder) {
        this.channelFolder = new File(Main.getInstance().getDataFolder(), channelFolder);
        this.channelFolderStr = channelFolder;
    }

    @Override
    public boolean initStorage() {
        return initiate(true);
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public boolean createWirelessChannel(WirelessChannel channel) {
        setWirelessChannel(channel.getName(), channel);

        return true;
    }

    @Override
    public boolean createWirelessPoint(String channelName, IWirelessPoint point) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (point instanceof WirelessReceiver) {
            Main.getWRLogger().debug("Yaml config: Creating a receiver of class "
                    + point.getClass());
            if (point instanceof WirelessReceiverInverter) {
                channel.addReceiver((WirelessReceiverInverter) point);
                Main.getWRLogger().debug("Yaml Config: Adding an inverter");
            } else if (point instanceof WirelessReceiverSwitch) {
                channel.addReceiver((WirelessReceiverSwitch) point);
                Main.getWRLogger().debug("Yaml Config: Adding an Switch");
            } else if (point instanceof WirelessReceiverDelayer) {
                channel.addReceiver((WirelessReceiverDelayer) point);
                Main.getWRLogger().debug("Yaml Config: Adding an Delayer");
            } else if (point instanceof WirelessReceiverClock) {
                channel.addReceiver((WirelessReceiverClock) point);
                Main.getWRLogger().debug("Yaml Config: Adding an Clock");
            } else {
                channel.addReceiver((WirelessReceiver) point);
                Main.getWRLogger().debug("Yaml Config: Adding a default receiver");
            }
        } else if (point instanceof WirelessTransmitter)
            channel.addTransmitter((WirelessTransmitter) point);
        else if (point instanceof WirelessScreen)
            channel.addScreen((WirelessScreen) point);
        setWirelessChannel(channelName, channel);

        return true;
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

    @Override
    public StorageType canConvert() {
        try {
            for (File file : channelFolder.listFiles()) {
                if (file.getName().contains("MYSQL")) {
                    return StorageType.MYSQL;
                }
            }
            for (File file : channelFolder.listFiles()) {
                if (file.getName().contains(".db")) {
                    return StorageType.SQLITE;
                }
            }
        } catch (NullPointerException ignored) {}

        return null;
    }

    private boolean initiate(boolean allowConvert){
        //Initialize the serialization
        ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
        ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
        ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
        ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
//        ConfigurationSerialization.registerClass(WirelessReceiverInverter.class, "WirelessReceiverInverter");
//        ConfigurationSerialization.registerClass(WirelessReceiverDelayer.class, "WirelessReceiverDelayer");
//        ConfigurationSerialization.registerClass(WirelessReceiverClock.class, "WirelessReceiverClock");
//        ConfigurationSerialization.registerClass(WirelessReceiverSwitch.class, "WirelessReceiverSwitch");

        if (canConvert() != null && allowConvert) {
            Main.getWRLogger().info("WirelessRedstone found a channel in a different storage format.");
            Main.getWRLogger().info("Beginning data transfer to Yaml...");
            if (convertFromAnotherStorage(canConvert())) {
                Main.getWRLogger().info("Done! All the channels are now stored in the Yaml Files.");
            }
        }
        return true;
    }

    private void setWirelessChannel(String channelName, WirelessChannel channel) {
        FileConfiguration channelConfig = new YamlConfiguration();
        try {
            File channelFile = new File(channelFolder, channelName + ".yml");

            if (channel != null)
                channelFile.createNewFile();

            channelConfig.load(channelFile);
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        channelConfig.set(channelName, channel);

        try {
            channelConfig.save(new File(channelFolder, channelName + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
