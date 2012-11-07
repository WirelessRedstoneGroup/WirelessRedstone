package net.licks92.WirelessRedstone.Configuration;

import java.util.Collection;

import org.bukkit.Location;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;

public interface IWirelessStorageConfiguration
{
	boolean init();
	
	boolean close();
	
	boolean canConvert();
	
	boolean convert();
	
	WirelessChannel getWirelessChannel(String channelName);
	
	Collection<WirelessChannel> getAllChannels();
	
	void createWirelessChannel(String channelName, WirelessChannel channel);
	
	void removeWirelessChannel(String channelName);
	
	void createWirelessPoint(String channelName, IWirelessPoint point);
	
	boolean removeWirelessReceiver(String channelName, Location loc);
	
	boolean removeWirelessTransmitter(String channelName, Location loc);
	
	boolean removeWirelessScreen(String channelName, Location loc);
	
	void updateChannel(String channelName, WirelessChannel channel);
	
	boolean wipeData();
	
	boolean backupData();
}
