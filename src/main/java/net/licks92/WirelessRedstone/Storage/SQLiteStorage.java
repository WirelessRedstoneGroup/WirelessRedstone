package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessPoint;
import net.licks92.WirelessRedstone.WirelessRedstone;

import java.io.File;
import java.util.Collection;

public class SQLiteStorage extends StorageConfiguration {

    private String channelFolder;

    public SQLiteStorage(String channelFolder) {
        this.channelFolder = channelFolder;
    }

    @Override
    public boolean initStorage() {
        try {
            DatabaseClient.getInstance(WirelessRedstone.getInstance().getDataFolder() + File.separator + channelFolder);
            WirelessRedstone.getStorageManager().updateChannels(false);
            return true;
        } catch (RuntimeException e) {
            WirelessRedstone.getWRLogger().severe("There was an error accessing the database!");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean close() {
        DatabaseClient.getInstance().getDatabase().close();
        return true;
    }

    @Override
    protected Collection<WirelessChannel> getAllChannels() {
        return DatabaseClient.getInstance().getAllChannels();
    }

    @Override
    public boolean createChannel(WirelessChannel channel) {
        return super.createChannel(channel);
    }

    @Override
    public boolean createWirelessPoint(String channelName, WirelessPoint wirelessPoint) {
        return super.createWirelessPoint(channelName, wirelessPoint);
    }

    @Override
    public boolean removeWirelessPoint(String channelName, WirelessPoint wirelessPoint) {
        return super.removeWirelessPoint(channelName, wirelessPoint);
    }

    @Override
    public boolean updateChannel(String channelName, WirelessChannel channel) {
        return super.updateChannel(channelName, channel);
    }

    @Override
    public boolean removeChannel(String channelName, boolean removeSigns) {
        return super.removeChannel(channelName, removeSigns);
    }

    @Override
    public boolean wipeData() {
        DatabaseClient.getInstance().recreateDatabase();

        return super.wipeData();
    }
}
