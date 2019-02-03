package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessPoint;
import net.licks92.WirelessRedstone.WirelessRedstone;

import java.io.File;
import java.util.ArrayList;
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
        Collection<WirelessChannel> channels = new ArrayList<>();

        return channels;
    }

    private SignType getSignType(String signTypeSerialized) {
        if (signTypeSerialized.equalsIgnoreCase("transmitter")) {
            return SignType.TRANSMITTER;
        } else if (signTypeSerialized.equalsIgnoreCase("receiver")) {
            return SignType.RECEIVER;
        } else if (signTypeSerialized.equalsIgnoreCase("screen")) {
            return SignType.SCREEN;
        } else if (signTypeSerialized.contains("receiver")) {
            String[] receiver = signTypeSerialized.split("_");

            if (receiver[1].equalsIgnoreCase("inverter")) {
                return SignType.RECEIVER_INVERTER;
            } else if (receiver[1].equalsIgnoreCase("delayer")) {
                return SignType.RECEIVER_DELAYER;
            } else if (receiver[1].equalsIgnoreCase("switch")) {
                return SignType.RECEIVER_SWITCH;
            } else if (receiver[1].equalsIgnoreCase("clock")) {
                return SignType.RECEIVER_CLOCK;
            }
        }

        return null;
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
        return super.wipeData();
    }
}
