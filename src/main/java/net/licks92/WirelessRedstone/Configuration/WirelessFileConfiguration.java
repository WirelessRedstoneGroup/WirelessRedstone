package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

public class WirelessFileConfiguration implements IWirelessStorageConfiguration
{
	private File channelFolder;
	
	public WirelessFileConfiguration(File channelFolder)
	{
		this.channelFolder = channelFolder;
	}
	
	@Override
	public boolean init()
	{
		//Initialize the serialization
		ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
		ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
		ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
		ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
		
		return true;
	}

	@Override
	public boolean close()
	{
		return true;
	}

	@Override
	public WirelessChannel getWirelessChannel(String channelName)
	{
		FileConfiguration channelConfig = new YamlConfiguration();
		try
		{
			File channelFile = new File(channelFolder, channelName + ".yml");
			channelConfig.load(channelFile);
		}
		catch (FileNotFoundException e)
		{
			//WirelessRedstone.getStackableLogger().debug("File " + channelName + ".yml wans't found in the channels folder, returning null.");
			return null;
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
			WirelessRedstone.getStackableLogger().warning("Channel "+channelName+" does not seem to be of type WirelessChannel.");
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
			if(channel != null)
				channelFile.createNewFile();
			channelConfig.load(channelFile);
		}
		catch (FileNotFoundException e)
		{
			return;
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

	@Override
	public void createWirelessChannel(String channelName, WirelessChannel channel)
	{
		setWirelessChannel(channelName, channel);
	}

	@Override
	public void removeWirelessChannel(String channelName)
	{
		setWirelessChannel(channelName, null);
		for(File f : channelFolder.listFiles())
		{
			if(f.getName().equals(channelName))
			{
				f.delete();
			}
		}
	}

	@Override
	public void createWirelessPoint(String channelName, IWirelessPoint point)
	{
		WirelessChannel channel = getWirelessChannel(channelName);
		if(point instanceof WirelessReceiver)
			channel.addReceiver((WirelessReceiver) point);
		else if(point instanceof WirelessTransmitter)
			channel.addTransmitter((WirelessTransmitter) point);
		else if(point instanceof WirelessScreen)
			channel.addScreen((WirelessScreen) point);
		setWirelessChannel(channelName, channel);
	}

	@Override
	public void removeWirelessPoint(String channelName, Location loc)
	{
		for (WirelessReceiver receiver : getWirelessChannel(channelName).getReceivers())
		{
			if (receiver.getX() == loc.getBlockX()
					&& receiver.getY() == loc.getBlockY()
					&& receiver.getZ() == loc.getBlockZ())
			{
				getWirelessChannel(channelName).removeReceiverAt(loc);
			}
		}
		for (WirelessTransmitter transmitter : getWirelessChannel(channelName).getTransmitters())
		{
			if (transmitter.getX() == loc.getBlockX()
					&& transmitter.getY() == loc.getBlockY()
					&& transmitter.getZ() == loc.getBlockZ())
			{
				getWirelessChannel(channelName).removeTransmitterAt(loc);
			}
		}
		for (WirelessScreen screen : getWirelessChannel(channelName).getScreens())
		{
			if (screen.getX() == loc.getBlockX()
					&& screen.getY() == loc.getBlockY()
					&& screen.getZ() == loc.getBlockZ())
			{
				getWirelessChannel(channelName).removeScreenAt(loc);
			}
		}
	}

	@Override
	public boolean wipeData()
	{
		for(File f : channelFolder.listFiles())
		{
			removeWirelessChannel(f.getName());
		}
		return true;
	}

	@Override
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
				WirelessRedstone.getStackableLogger().warning("Channel "+channel+" is not of type WirelessChannel.");
		}
		return channels;
	}

	@Override
	public void updateChannel(String channelName, WirelessChannel channel)
	{
		setWirelessChannel(channelName, channel);
	}

}
