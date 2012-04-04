package net.licks92.WirelessRedstone;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

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
		
		ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
		ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
		ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");

		getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		reloadConfig();
		
		File oldConfig = new File(plugin.getDataFolder(), "settings.yml");
		if(oldConfig.exists())
		{
			convertOldConfigToNew(oldConfig);
		}
	}
	
	public void convertOldConfigToNew(File file)
	{
		WirelessConfiguration oldConfiguration = new WirelessConfiguration(plugin, plugin.getDataFolder());
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
}