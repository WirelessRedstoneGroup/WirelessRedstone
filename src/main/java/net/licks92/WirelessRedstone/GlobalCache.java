package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Signs.IWirelessPoint;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Storage.StorageType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GlobalCache {

    private Collection<WirelessChannel> allChannels = null;
    private ArrayList<IWirelessPoint> allSigns = null;
    private ArrayList<Location> allReceiverLocations = null;
    private ArrayList<Location> allSignLocations = null;
    private BukkitTask refreshingTask = null;

    public GlobalCache(Integer refreshTime){
        //At plugin startup we have to directly update the cache.
        update(false);

        Integer timeInTicks = refreshTime * 20;

        refreshingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, timeInTicks, timeInTicks);
    }

    private void updateAllSigns(boolean async) {
        if (async)
            Bukkit.getServer().getScheduler().runTaskAsynchronously(WirelessRedstone.getInstance(), new Runnable() {
                public void run() {
                    updateList();
                }
            });
        else {
            updateList();
        }
    }

    private void updateList(){
        ArrayList<IWirelessPoint> returnlist = new ArrayList<IWirelessPoint>();
        ArrayList<Location> returnlistLocations = new ArrayList<Location>();
        ArrayList<Location> returnReceiverLocations = new ArrayList<Location>();
        Collection<WirelessChannel> returnChannelList = WirelessRedstone.getStorage().getAllChannels(true);

        for (WirelessChannel channel : returnChannelList) {
            try {
                for (IWirelessPoint point : channel.getReceivers()) {
                    returnlist.add(point);
                    returnlistLocations.add(point.getLocation());
                    returnReceiverLocations.add(point.getLocation());
                }

                for (IWirelessPoint point : channel.getTransmitters()) {
                    returnlist.add(point);
                    returnlistLocations.add(point.getLocation());
                }

                for (IWirelessPoint point : channel.getScreens()) {
                    returnlist.add(point);
                    returnlistLocations.add(point.getLocation());
                }
            } catch (Exception ignored) {

            }
        }

        allChannels = returnChannelList;
        allSigns = returnlist;
        allSignLocations = returnlistLocations;
        allReceiverLocations = returnReceiverLocations;
    }

    public void update() {
        update(false); //Changed to sync as temp solution to avoid database locked exceptions
    }

    public void update(boolean async) {
        updateAllSigns(async);
    }

    public Collection<WirelessChannel> getAllChannels(){
        return allChannels;
    }

    public List<IWirelessPoint> getAllSigns() {
        return allSigns;
    }

    public List<Location> getAllReceiverLocations() {
        return allReceiverLocations;
    }

    public List<Location> getAllSignLocations() {
        return allSignLocations;
    }
}
