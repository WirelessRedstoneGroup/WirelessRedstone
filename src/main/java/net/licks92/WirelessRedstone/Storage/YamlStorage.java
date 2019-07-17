package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessPoint;
import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverClock;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverDelayer;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverInverter;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverSwitch;
import net.licks92.WirelessRedstone.Signs.WirelessScreen;
import net.licks92.WirelessRedstone.Signs.WirelessTransmitter;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class YamlStorage extends StorageConfiguration {

    private File channelFolder;
    private final FilenameFilter yamlFilter = (dir, name) -> name.toLowerCase().endsWith(".yml");

    public YamlStorage(String channelFolder) {
        this.channelFolder = new File(WirelessRedstone.getInstance().getDataFolder(), channelFolder);

        //Initialize the serialization
        ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
        ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
        ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
        ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
        ConfigurationSerialization.registerClass(WirelessReceiverInverter.class, "WirelessReceiverInverter");
        ConfigurationSerialization.registerClass(WirelessReceiverDelayer.class, "WirelessReceiverDelayer");
        ConfigurationSerialization.registerClass(WirelessReceiverClock.class, "WirelessReceiverClock");
        ConfigurationSerialization.registerClass(WirelessReceiverSwitch.class, "WirelessReceiverSwitch");
    }


    @Override
    public boolean initStorage() {
        //TODO: Initstorage

        WirelessRedstone.getStorageManager().updateChannels(false);

        StorageType oldStorageType = canConvertFromType();
        if (oldStorageType != null) {
            return WirelessRedstone.getStorageManager().moveStorageFromType(oldStorageType);
        }

        return true;
    }

    @Override
    public boolean close() {
        //TODO: See if there is a better way to save active state
        for (WirelessChannel channel : WirelessRedstone.getStorageManager().getChannels()) {
            setChannel(channel.getName(), channel);
        }

        return true;
    }

    @Override
    protected Collection<WirelessChannel> getAllChannels() {
        Collection<WirelessChannel> channels = new ArrayList<>();

        for (File f : Objects.requireNonNull(channelFolder.listFiles(yamlFilter))) {
            FileConfiguration channelConfig = new YamlConfiguration();
            try {
                channelConfig.load(f);
            } catch (InvalidConfigurationException | IOException e) {
                e.printStackTrace();
            }

            String channelName;
            try {
                channelName = f.getName().split(".yml")[0];
            } catch (ArrayIndexOutOfBoundsException ex) {
                continue;
            }

            Object channel = channelConfig.get(channelName);
            if (channel instanceof WirelessChannel) {
                channels.add((WirelessChannel) channel);
                WirelessRedstone.getWRLogger().debug("Found channel: " + ((WirelessChannel) channel).getName());
            } else if (channel == null) {
                WirelessRedstone.getWRLogger().debug("File " + f.getName() + " does not contain a Wireless Channel. Removing it.");
                f.delete();
            } else
                WirelessRedstone.getWRLogger().warning("Channel " + channel + " is not of type WirelessChannel.");
        }

        return channels;
    }

    @Override
    public boolean createChannel(WirelessChannel channel) {
        if (!setChannel(channel.getName(), channel))
            return false;

        return super.createChannel(channel);
    }

    @Override
    public boolean createWirelessPoint(String channelName, WirelessPoint wirelessPoint) {
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        channel.addWirelessPoint(wirelessPoint);

        if (!setChannel(channelName, channel))
            return false;

        return super.createWirelessPoint(channelName, wirelessPoint);
    }

    @Override
    public boolean removeWirelessPoint(String channelName, WirelessPoint wirelessPoint) {
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        channel.removeWirelessPoint(wirelessPoint);

        if (!setChannel(channelName, channel))
            return false;

        return super.removeWirelessPoint(channelName, wirelessPoint);
    }

    @Override
    public boolean updateChannel(String channelName, WirelessChannel channel) {
        if (!setChannel(channelName, channel))
            return false;

        return super.updateChannel(channelName, channel);
    }

    @Override
    public boolean removeChannel(String channelName, boolean removeSigns) {
        File file = new File(channelFolder, channelName + ".yml");

        if (file.exists())
            file.delete();

        return super.removeChannel(channelName, removeSigns);
    }

    @Override
    public boolean wipeData() {
        for (File f : Objects.requireNonNull(channelFolder.listFiles(yamlFilter))) {
            f.delete();
        }

        return super.wipeData();
    }

    @Override
    public void updateSwitchState(WirelessChannel channel) {
        for (WirelessReceiver receiver : channel.getReceivers()) {
            if (receiver instanceof WirelessReceiverSwitch) {
                setChannel(channel.getName(), channel);
                break;
            }
        }
    }

    @Override
    protected StorageType canConvertFromType() {
        for (File file : Objects.requireNonNull(channelFolder.listFiles())) {
            if (file.getName().contains(".db")) {
                return StorageType.SQLITE;
            }
        }

        return null;
    }

    private boolean setChannel(String channelName, WirelessChannel channel) {
        FileConfiguration channelConfig = new YamlConfiguration();
        try {
            File channelFile = new File(channelFolder, channelName + ".yml");

            if (channel != null)
                channelFile.createNewFile();

            channelConfig.load(channelFile);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        channelConfig.set(channelName, channel);

        try {
            channelConfig.save(new File(channelFolder, channelName + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}