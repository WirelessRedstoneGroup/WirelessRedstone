package net.licks92.WirelessRedstone.Configuration;

import java.util.Collection;

import org.bukkit.Location;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;

public interface IWirelessStorageConfiguration {
    boolean initStorage();

    boolean close();

    boolean canConvert();

    boolean convertFromAnotherStorage();

    WirelessChannel getWirelessChannel(String channelName);

    Collection<WirelessChannel> getAllChannels();

    boolean createWirelessChannel(WirelessChannel channel);

    void removeWirelessChannel(String channelName);

    /**
     * Important. Update the cache after creating a wireless point!
     *
     * @param channelName
     * @param point
     * @return true if everything went well.
     */
    boolean createWirelessPoint(String channelName, IWirelessPoint point);

    boolean removeWirelessReceiver(String channelName, Location loc);

    boolean removeWirelessTransmitter(String channelName, Location loc);

    boolean removeWirelessScreen(String channelName, Location loc);

    void updateChannel(String channelName, WirelessChannel channel);

    boolean renameWirelessChannel(String channelName, String newChannelName);

    boolean wipeData();

    boolean backupData();
}
