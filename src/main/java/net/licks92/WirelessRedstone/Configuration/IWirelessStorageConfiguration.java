package net.licks92.WirelessRedstone.Configuration;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import org.bukkit.Location;

import java.util.Collection;

public interface IWirelessStorageConfiguration {
    boolean initStorage();

    boolean close();

    boolean canConvert();

    boolean convertFromAnotherStorage();

    boolean isChannelEmpty(WirelessChannel channel);

    WirelessChannel getWirelessChannel(String channelName);

    String getWirelessChannelName(Location loc);

    IWirelessPoint getWirelessRedstoneSign(Location loc);

    Collection<WirelessChannel> getAllChannels();

    boolean createWirelessChannel(WirelessChannel channel);

    void checkChannel(String channelName);

    void removeWirelessChannel(String channelName);

    /**
     * Important. Update the cache after creating a wireless point!
     *
     * @param channelName
     * @param point
     * @return true if everything went well.
     */
    boolean createWirelessPoint(String channelName, IWirelessPoint point);

    boolean removeIWirelessPoint(String channelName, Location loc);

    boolean removeWirelessReceiver(String channelName, Location loc);

    boolean removeWirelessTransmitter(String channelName, Location loc);

    boolean removeWirelessScreen(String channelName, Location loc);

    void updateChannel(String channelName, WirelessChannel channel);

    boolean renameWirelessChannel(String channelName, String newChannelName);

    boolean wipeData();

    boolean backupData(String extension);

    boolean purgeData();

    int restoreData();
}
