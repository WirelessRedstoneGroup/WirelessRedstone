package net.licks92.WirelessRedstone;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class NewWirelessConfiguration
{
	private WirelessRedstone plugin;
	private File configFile;
	private FileConfiguration config;
	private StackableLogger logger;
	
	public NewWirelessConfiguration(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		logger = WirelessRedstone.getStackableLogger();
		
		configFile = new File(plugin.getDataFolder(), "config.yml");
		
		ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
		ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
		ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
		reloadConfig();
		File oldConfig = new File(plugin.getDataFolder(), "settings.yml");
		if(oldConfig.exists())
		{
			ConvertToNewConfig(oldConfig);
		}
	}
	
	private void ConvertToNewConfig(File file)
	{
		WirelessConfiguration oldConfig = new WirelessConfiguration(plugin, plugin.getDataFolder());
		
		logger.info("Converted old config !");
		
		config.set("WirelessChannels", oldConfig.get("WirelessChannels"));
		
		file.delete();
	}

	public void reloadConfig()
	{
		if(!(configFile.exists()))
		{
			try
			{
				if(!(configFile.getParentFile().exists()))
					configFile.getParentFile().mkdirs();
				configFile.createNewFile();
				config = YamlConfiguration.loadConfiguration(configFile);
				GenerateDefaults();
				return;
			}
			catch (IOException ex)
			{
				logger.severe("Failed to create the config file !");
				ex.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(configFile);
		if(config.toString() == null)
			GenerateDefaults();
	}
	
	private void GenerateDefaults()
	{
		config.set("LogLevel", Level.INFO.getName().toUpperCase());
		config.set("cancelChunkUnloads", true);
		config.set("cancelChunkUnloadRange", 4);
		config.set("UseVault", true);
		logger.info("Generated new configuration !");
		save();
	}

	public Level getLogLevel()
	{
		return Level.parse(config.getString("LogLevel"));
	}

	public boolean getVaultUsage()
	{
		return config.getBoolean("UseVault");
	}

	public void save()
	{
		try
		{
			config.save(configFile);
		}
		catch (NullPointerException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public boolean isCancelChunkUnloads()
	{
		return config.getBoolean("cancelChunkUnloads", true);
	}

	public int getChunkUnloadRange()
	{
		return config.getInt("cancelChunkUnloadRange", 4);
	}

	public Object get(String path)
	{
		return config.get(path);
	}
	public void set(String path, Object channel)
	{
		config.set(path, channel);
	}
}