package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessPoint;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Collection;

public abstract class StorageConfiguration {

    public abstract boolean initStorage();

    public abstract boolean close();

    protected abstract Collection<WirelessChannel> getAllChannels();

    public boolean createChannel(WirelessChannel channel) {
        WirelessRedstone.getStorageManager().updateList(channel.getName(), channel);
        return true;
    }

    public boolean createWirelessPoint(String channelName, WirelessPoint wirelessPoint) {
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        //TODO: Investigate if this duplicates the wirelesspoint into the channel
//        channel.addWirelessPoint(wirelessPoint);
        WirelessRedstone.getStorageManager().updateList(channelName, channel);
        return true;
    }

    public boolean removeWirelessPoint(String channelName, WirelessPoint wirelessPoint) {
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        //TODO: Investigate if this duplicates the wirelesspoint into the channel
//        channel.removeWirelessPoint(wirelessPoint);
        WirelessRedstone.getStorageManager().updateList(channelName, channel);
        return true;
    }

    public boolean updateChannel(String channelName, WirelessChannel channel) {
        WirelessRedstone.getStorageManager().updateList(channel.getName(), channel);
        return true;
    }

    public boolean removeChannel(String channelName, boolean removeSigns) {
        if (removeSigns) {
            WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);

            for (WirelessPoint point : channel.getSigns()) {
                World world = Bukkit.getWorld(point.getWorld());

                if (world == null)
                    continue;

                Location loc = new Location(world, point.getX(), point.getY(), point.getZ());
                loc.getBlock().setType(Material.AIR);
            }
        }
        WirelessRedstone.getStorageManager().updateList(channelName, null);

        return true;
    }

    public boolean wipeData() {
        WirelessRedstone.getStorageManager().wipeList();
        return true;
    }

}
