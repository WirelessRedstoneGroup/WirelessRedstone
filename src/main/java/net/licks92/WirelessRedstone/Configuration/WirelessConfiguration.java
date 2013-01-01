package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class WirelessConfiguration implements IWirelessStorageConfiguration
{
	private static final String CHANNEL_FOLDER = "/channels";
	
	private File channelFolder;
	private WirelessRedstone plugin;
	private IWirelessStorageConfiguration storage;
	
	public char[] badCharacters = {'|','-','*','/','<','>',' ','=','~','!','^','(',')'};
	
	private FileConfiguration getConfig()
	{
		return plugin.getConfig();
	}
	
	public WirelessConfiguration(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		
		ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
		ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
		ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
		ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
		
		//Loading and saving
		getConfig().options().copyHeader(true);
		getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		reloadConfig();
	}
	
	public boolean init()
	{
		//Create the channel folder
		channelFolder = new File(plugin.getDataFolder(), CHANNEL_FOLDER);
		channelFolder.mkdir();
		
		if(getConfig().isSet("WirelessChannels"))
		{
			WirelessRedstone.getWRLogger().info("Updating data into new format ! If there's an error, turn the debug mode on in the config.yml file and try again.");
			ConfigurationUpdater updater = new ConfigurationUpdater(getConfig(), channelFolder, plugin);
			updater.update();
			WirelessRedstone.getWRLogger().info("Successfully transfered your channels into the new database format.");
		}
		
		//Create the storage config
		if(getSQLUsage())
		{
			storage = new SQLStorage(channelFolder, plugin);
		}
		else
			storage = new YamlStorage(channelFolder, plugin);
		
		return storage.init();
	}
	
	public boolean close()
	{
		return storage.close();
	}
	
	public boolean canConvert()
	{
		return storage.canConvert();
	}
	
	public boolean convertFromAnotherStorage()
	{
		return storage.convertFromAnotherStorage();
	}
	
	public boolean wipeData()
	{
		return storage.wipeData();
	}
	
	public boolean backupData()
	{
		return storage.backupData();
	}

	public void reloadConfig()
	{
		plugin.reloadConfig();
	}
	
	public WirelessChannel getWirelessChannel(String channelName)
	{
		return storage.getWirelessChannel(channelName);
	}
	
	public boolean createWirelessPoint(String channelName, IWirelessPoint point)
	{
		return storage.createWirelessPoint(channelName, point);
	}
	
	public boolean createWirelessChannel(String channelName, WirelessChannel channel)
	{
		return storage.createWirelessChannel(channelName, channel);
	}
	
	public void removeWirelessChannel(String channelName)
	{
		storage.removeWirelessChannel(channelName);
	}
	
	public boolean renameWirelessChannel(String channelName, String newChannelName)
	{
		return storage.renameWirelessChannel(channelName, newChannelName);
	}

	public boolean removeWirelessReceiver(String channelName, Location loc)
	{
		return storage.removeWirelessReceiver(channelName, loc);
	}
	
	public boolean removeWirelessTransmitter(String channelName, Location loc)
	{
		return storage.removeWirelessTransmitter(channelName, loc);
	}
	
	public boolean removeWirelessScreen(String channelName, Location loc)
	{
		return storage.removeWirelessScreen(channelName, loc);
	}
	
	public Collection<WirelessChannel> getAllChannels()
	{
		return storage.getAllChannels();
	}
	
	/*
	 * updateChannel() has to update the fields that concern the channel (means that it has not to update the wirelesssigns).
	 */
	public void updateChannel(String channelName, WirelessChannel channel)
	{
		storage.updateChannel(channelName, channel);
	}
	
	public Level getLogLevel()
	{
		return Level.parse(getConfig().getString("LogLevel"));
	}
	
	public boolean doCheckForUpdates()
	{
		return getConfig().getBoolean("CheckForUpdates");
	}

	public boolean getVaultUsage()
	{
		return getConfig().getBoolean("UseVault");
	}
	
	public boolean getSQLUsage()
	{
		return getConfig().getBoolean("UseSQL");
	}

	public void save()
	{
		plugin.saveConfig();
	}

	public boolean isCancelChunkUnloads()
	{
		return getConfig().getBoolean("cancelChunkUnloads", true);
	}

	public int getChunkUnloadRange()
	{
		return getConfig().getInt("cancelChunkUnloadRange", 4);
	}
	
	public boolean getSignDrop()
	{
		return getConfig().getBoolean("DropSignWhenBroken", true);
	}
	
	public boolean getDebugMode()
	{
		return getConfig().getBoolean("DebugMode", false);
	}
}