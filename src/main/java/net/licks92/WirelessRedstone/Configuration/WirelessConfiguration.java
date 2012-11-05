package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class WirelessConfiguration implements IWirelessStorageConfiguration
{
	private static final String CHANNEL_FOLDER = "/channels";
	
	private File channelFolder;
	private WirelessRedstone plugin;
	private IWirelessStorageConfiguration storage;
	
	private FileConfiguration getConfig()
	{
		return plugin.getConfig();
	}
	
	public WirelessConfiguration(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		
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
		
		//Create the storage config
		if(getSQLUsage())
		{
			storage = new SQLStorage(channelFolder, plugin);
			/*WirelessRedstone.getStackableLogger().info("SQL Storage is not available yet. You will still use the yaml files to store the channels.");
			storage = new WirelessFileConfiguration(channelFolder);*/
		}
		else
			storage = new YamlStorage(channelFolder, plugin);
		
		storage.init();
		
		return true;
	}
	
	public boolean close()
	{
		storage.close();
		
		return true;
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
	
	public void createWirelessPoint(String channelName, IWirelessPoint point)
	{
		storage.createWirelessPoint(channelName, point);
	}
	
	public void createWirelessChannel(String channelName, WirelessChannel channel)
	{
		storage.createWirelessChannel(channelName, channel);
	}
	
	public void removeWirelessChannel(String channelName)
	{
		storage.removeWirelessChannel(channelName);
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