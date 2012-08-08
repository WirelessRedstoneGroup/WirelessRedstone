package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class NewWirelessConfiguration
{
	private static final String CHANNEL_SECTION = "WirelessChannels";
	private WirelessRedstone plugin;
	
	private FileConfiguration getConfig()
	{
		return plugin.getConfig();
	}
	
	public NewWirelessConfiguration(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		
		//Initialize the serialization
		ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
		ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
		ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
		ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
		
		//Loading and saving
		getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		reloadConfig();
		
		//Try to know if the config is an OldConfiguration, and convert it
		File oldConfig = new File(plugin.getDataFolder(), "settings.yml");
		if(oldConfig.exists())
		{
			convertOldConfigToNew(oldConfig);
		}
		
		//Language selection
		
	}
	
	public void convertOldConfigToNew(File file)
	{
		OldWirelessConfiguration oldConfiguration = new OldWirelessConfiguration(plugin.getDataFolder());
		getConfig().set("WirelessChannels", oldConfiguration.get("WirelessChannels"));
		
		file.delete();
	}

	public void reloadConfig()
	{
		plugin.reloadConfig();
	}

	public Level getLogLevel()
	{
		return Level.parse(getConfig().getString("LogLevel"));
	}

	public boolean getVaultUsage()
	{
		return getConfig().getBoolean("UseVault");
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
	
	public boolean getDebugMode()
	{
		return getConfig().getBoolean("DebugMode", false);
	}
	
	public WirelessChannel getWirelessChannel(String channelName)
	{
		ConfigurationSection section = getConfig().getConfigurationSection(CHANNEL_SECTION);
		if(section == null)
			return null; // section not found.
		
		Object channel = section.get(channelName);
		if(channel == null)
			return null; // channel not found
		else if(!(channel instanceof WirelessChannel))
		{
			plugin.getLogger().warning("Channel "+channelName+" does not seem to be of type WirelessChannel.");
			return null;
		}
		else
			return (WirelessChannel)channel;
	}
	
	public void setWirelessChannel(String channelName, WirelessChannel channel)
	{
		ConfigurationSection section = getConfig().getConfigurationSection(CHANNEL_SECTION);
		if(section == null)
			return;
		
		section.set(channelName, channel);
	}
	
	public Collection<WirelessChannel> getAllChannels()
	{
		ConfigurationSection section = getConfig().getConfigurationSection(CHANNEL_SECTION);
		if(section == null)
			return new ArrayList<WirelessChannel>(0);
		
		Map<String, Object> values = section.getValues(true);
		List<WirelessChannel> channels = new ArrayList<WirelessChannel>();
		for(String cname : values.keySet())
		{
			Object channel = section.get(cname);
			if(channel instanceof WirelessChannel)
			{
				channels.add((WirelessChannel)channel);
			}
			else
				plugin.getLogger().warning("Channel "+channel+" is not of type WirelessChannel.");
		}
		return channels;	
	}
	
	public Object get(String path)
	{
		return getConfig().get(path);
	}
	
	public void set(String path, Object channel)
	{
		getConfig().set(path, channel);
	}
}