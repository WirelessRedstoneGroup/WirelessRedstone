package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.util.Map;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class ConfigurationUpdater
{
	private FileConfiguration config;
	private IWirelessStorageConfiguration storage;
	
	public ConfigurationUpdater(FileConfiguration r_config, File channelFolder, WirelessRedstone plugin)
	{
		ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
		ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
		ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
		ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
		
		config = r_config;
		
		//Actually works with Yaml only (and the data would transfer in SQL database after.
		if(config.getBoolean("UseSQL"))
			storage = new SQLStorage(channelFolder, plugin);
		else
			storage = new YamlStorage(channelFolder, plugin);
		storage.init();
	}
	
	public boolean update()
	{
		try {
		ConfigurationSection cSection = config.getConfigurationSection("WirelessChannels");
		
		Map<String, Object> channelMap = cSection.getValues(false);
		for(String channelName : channelMap.keySet())
		{
			WirelessRedstone.getWRLogger().debug("Copying channel " + channelName + " into the new database...");
			storage.createWirelessChannel((WirelessChannel)cSection.get(channelName));
			WirelessRedstone.getWRLogger().debug("Successfully copied channel " + channelName + " into the new database!");
		}
		
		config.set("WirelessChannels", null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		return true;
	}
}
