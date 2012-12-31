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
	
	boolean convertToAnotherStorage();
	
	WirelessChannel getWirelessChannel(String channelName);
	
	Collection<WirelessChannel> getAllChannels();
	
	boolean createWirelessChannel(String channelName, WirelessChannel channel);
	
	void removeWirelessChannel(String channelName);
	
	boolean createWirelessPoint(String channelName, IWirelessPoint point);
	
	boolean removeWirelessReceiver(String channelName, Location loc);
	
	boolean removeWirelessTransmitter(String channelName, Location loc);
	
	boolean removeWirelessScreen(String channelName, Location loc);
	
	void updateChannel(String channelName, WirelessChannel channel);
	
	boolean renameWirelessChannel(String channelName, String newChannelName);
	
	boolean wipeData();
	
	boolean backupData();
}
