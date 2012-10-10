package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class NewWirelessConfiguration
{
	private static final String CHANNEL_FOLDER = "/channels";
	
	private File channelFolder;
	private WirelessRedstone plugin;
	private boolean saveInSQLDatabase;
	
	private FileConfiguration getConfig()
	{
		return plugin.getConfig();
	}
	
	public NewWirelessConfiguration(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		
		//Loading and saving
		getConfig().options().copyHeader(true);
		getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		reloadConfig();
		
		//Storage system => SQL or config files
		if(getSQLUsage())
		{
			initSQLSave();
		}
		else
		{
			initTextSave();
		}
		
		//Language selection
		//To implement
		
		//Show debug informations about the config...
		if(getDebugMode())
			System.out.println(channelFolder.getAbsolutePath());
	}

	private boolean initTextSave()
	{
		saveInSQLDatabase = false;
		
		//Initialize the serialization
		ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
		ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
		ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
		ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
		
		//Try to know if the config is an OldConfiguration, and convert it
		File oldConfig = new File(plugin.getDataFolder(), "settings.yml");
		if(oldConfig.exists())
		{
			convertOldConfigToNew(oldConfig);
		}
		
		//Create the channel folder
		channelFolder = new File(plugin.getDataFolder(), CHANNEL_FOLDER);
		channelFolder.mkdir();
		
		return true;
	}
	
	private boolean initSQLSave()
	{
		saveInSQLDatabase = true;
		return true;
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
	
	private boolean getSQLUsage()
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
	
	public WirelessChannel getWirelessChannel(String channelName)
	{
		YamlConfiguration channelConfig = new YamlConfiguration();
		try
		{
			File channelFile = new File(channelFolder, channelName + ".yml");
			channelFile.createNewFile();
			channelConfig.load(channelFile);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
		
		Object channel = channelConfig.get(channelName);
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
		FileConfiguration channelConfig = new YamlConfiguration();
		try
		{
			File channelFile = new File(channelFolder, channelName + ".yml");
			channelFile.createNewFile();
			channelConfig.load(channelFile);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
		
		channelConfig.set(channelName, channel);
		
		try
		{
			channelConfig.save(new File(channelFolder, channelName + ".yml"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Collection<WirelessChannel> getAllChannels()
	{
		ArrayList<File> fileList = new ArrayList<File>();
		
		if(fileList.isEmpty())
			return new ArrayList<WirelessChannel>(0);
		
		List<WirelessChannel> channels = new ArrayList<WirelessChannel>();
		
		for(File f : channelFolder.listFiles())
		{
			fileList.add(f);
			FileConfiguration channelConfig = new YamlConfiguration();
			try
			{
				channelConfig.load(f);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
			Object channel = channelConfig.get(f.getName().split(".yml").toString());
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