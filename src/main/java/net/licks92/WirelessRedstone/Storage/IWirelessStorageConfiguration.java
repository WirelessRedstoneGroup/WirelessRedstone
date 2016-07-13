package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.Signs.IWirelessPoint;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import org.bukkit.Location;

import java.util.Collection;

public interface IWirelessStorageConfiguration {

    boolean initStorage();
    boolean close();
    boolean createWirelessChannel(WirelessChannel channel);
    boolean createWirelessPoint(String channelName, IWirelessPoint point);
    boolean removeIWirelessPoint(String channelName, Location loc);
    boolean removeWirelessReceiver(String channelName, Location loc);
    boolean removeWirelessTransmitter(String channelName, Location loc);
    boolean removeWirelessScreen(String channelName, Location loc);
    boolean renameWirelessChannel(String channelName, String newChannelName);
    boolean wipeData();
    boolean backupData(String extension);
    boolean purgeData();
    boolean convertFromAnotherStorage(StorageType type);
    boolean isChannelEmpty(WirelessChannel channel);

    // Only call this on startup or channel refresh
    Collection<WirelessChannel> getAllChannels();

    WirelessChannel getWirelessChannel(String channelName);

    IWirelessPoint getWirelessRedstoneSign(Location loc);

    StorageType canConvert();
    StorageType restoreData();

    String getWirelessChannelName(Location loc);

    void updateChannel(String channelName, WirelessChannel channel);
    void updateReceivers();
    void checkChannel(String channelName);
    void removeWirelessChannel(String channelName);

}
