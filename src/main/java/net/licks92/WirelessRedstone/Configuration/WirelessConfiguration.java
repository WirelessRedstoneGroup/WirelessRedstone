package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class WirelessConfiguration implements IWirelessStorageConfiguration {
    private static final String CHANNEL_FOLDER = "/channels";

    private File channelFolder;
    private File configFile;
    private WirelessRedstone plugin;
    private IWirelessStorageConfiguration storage;

    public char[] badCharacters = {'|', '-', '*', '/', '<', '>', ' ', '=', '~', '!', '^', '(', ')', ':'};

    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public WirelessConfiguration(WirelessRedstone r_plugin) {
        plugin = r_plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        configFile.getParentFile().mkdirs();
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                WirelessRedstone.getWRLogger().severe("Couldn't create the configuration file!");
            }
            createFromTemplate(plugin.getResource("config.yml"));
        }

        //Loading and saving
        /*
		getConfig().options().copyHeader(true);
		getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		reloadConfig();*/
    }

    private void createFromTemplate(InputStream input) {
        InputStream istr = input;
        FileOutputStream ostr = null;

        try {
            if (istr == null) {
                WirelessRedstone.getWRLogger().severe("Could not find the configuration template in the archive."
                        + " Please download the plugin again.");
            }
            ostr = new FileOutputStream(configFile);
            byte[] buffer = new byte[1024];
            int length = 0;
            length = istr.read(buffer);
            while (length > 0) {
                ostr.write(buffer, 0, length);
                length = istr.read(buffer);
            }
        } catch (IOException ex) {
            WirelessRedstone.getWRLogger().severe("Couldn't write the config!");
        } finally {
            try {
                istr.close();
            } catch (IOException ex) {
                WirelessRedstone.getWRLogger().severe("Couldn't close the resource config stream");
            }
            try {
                ostr.close();
            } catch (IOException ex) {
                WirelessRedstone.getWRLogger().severe("Couldn't close the config file.");
            }
        }
    }

    public boolean initStorage() {
        //Create the channel folder
        channelFolder = new File(plugin.getDataFolder(), CHANNEL_FOLDER);
        channelFolder.mkdir();

        //Create the storage config
        if (getSQLUsage()) {
            storage = new SQLStorage(channelFolder, plugin);
        } else
            storage = new YamlStorage(channelFolder, plugin);

        return storage.initStorage();
    }

    public boolean close() {
        return storage.close();
    }

    public boolean canConvert() {
        return storage.canConvert();
    }

    public boolean convertFromAnotherStorage() {
        return storage.convertFromAnotherStorage();
    }

    public boolean wipeData() {
        return storage.wipeData();
    }

    public boolean backupData() {
        return storage.backupData();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    /**
     * @return The Wireless Channel with the given name, null if doesn't exist or an error happened
     */
    public WirelessChannel getWirelessChannel(String channelName) {
        return storage.getWirelessChannel(channelName);
    }

    /**
     * Creates a WPoint in the channel with the given name.
     *
     * @return True if everything happened fine, false if something wrong happened.
     */
    public boolean createWirelessPoint(String channelName, IWirelessPoint point) {
        return storage.createWirelessPoint(channelName, point);
    }

    /**
     * Creates a channel in the database.
     *
     * @param channel - A wireless channel which has to contain at least one sign.
     */
    public boolean createWirelessChannel(WirelessChannel channel) {
        return storage.createWirelessChannel(channel);
    }

    /**
     * Simply removes the channel with the given name from the database.
     */
    public void removeWirelessChannel(String channelName) {
        storage.removeWirelessChannel(channelName);
    }

    /**
     * Renames a wireless channel.
     *
     * @param channelName    - The actual channel name.
     * @param newChannelName - The new channel name.
     * @return true if everything went fine.
     */
    public boolean renameWirelessChannel(String channelName, String newChannelName) {
        return storage.renameWirelessChannel(channelName, newChannelName);
    }

    /**
     * Removes a WReceiver from a channel.
     *
     * @param channelName - The channel of the channel which contains the point.
     * @param loc         - The location of the point.
     * @return true if everything went fine.
     */
    public boolean removeWirelessReceiver(String channelName, Location loc) {
        return storage.removeWirelessReceiver(channelName, loc);
    }

    /**
     * Removes a WTransmitter from a channel.
     *
     * @param channelName - The channel of the channel which contains the point.
     * @param loc         - The location of the point.
     * @return true if everything went fine.
     */
    public boolean removeWirelessTransmitter(String channelName, Location loc) {
        return storage.removeWirelessTransmitter(channelName, loc);
    }

    /**
     * Removes a WScreen from a channel.
     *
     * @param channelName - The channel of the channel which contains the point.
     * @param loc         - The location of the point.
     * @return true if everything went fine.
     */
    public boolean removeWirelessScreen(String channelName, Location loc) {
        return storage.removeWirelessScreen(channelName, loc);
    }

    /**
     * @return a list which contains all the channels that exist in the database.
     */
    public Collection<WirelessChannel> getAllChannels() {
        return storage.getAllChannels();
    }

    /**
     * This method will update the fields of the specified channel, but it WON'T update the WirelessPoint list.
     * Don't use it in order to update the IWirelessPoint list because it won't save it.
     */
    public void updateChannel(String channelName, WirelessChannel channel) {
        storage.updateChannel(channelName, channel);
    }

    public String getLanguage() {
        return getConfig().getString("Language", "default");
    }

    public boolean getColourfulLogging() {
        return getConfig().getBoolean("ColourfulLogging", true);
    }

    public boolean doCheckForUpdates() {
        return getConfig().getBoolean("CheckForUpdates", true);
    }

    public boolean getVaultUsage() {
        return getConfig().getBoolean("UseVault", false);
    }

    public boolean getSQLUsage() {
        return getConfig().getBoolean("UseSQL", true);
    }

    public int getInteractTransmitterTime() {
        return getConfig().getInt("InteractTransmitterTime", 1000);
    }

    /**
     * @return The cache refresh frequency in seconds.
     */
    public int getCacheRefreshFrequency() {
        //150 by default
        return getConfig().getInt("CacheRefreshFrequency", 150);
    }

    public void save() {
        plugin.saveConfig();
    }

    public boolean isCancelChunkUnloads() {
        return getConfig().getBoolean("cancelChunkUnloads", true);
    }

    public int getChunkUnloadRange() {
        return getConfig().getInt("cancelChunkUnloadRange", 4);
    }

    public boolean getSignDrop() {
        return getConfig().getBoolean("DropSignWhenBroken", true);
    }

    public boolean getDebugMode() {
        return getConfig().getBoolean("DebugMode", false);
    }
}