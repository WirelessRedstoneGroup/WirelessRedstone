package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessPoint;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

    private ConcurrentHashMap<String, WirelessChannel> allChannels = new ConcurrentHashMap<>();
    private BukkitTask refreshingTask;
    private StorageConfiguration storage;

    public StorageManager(StorageType type, String channelFolder) {
        File channelFolderFile = new File(WirelessRedstone.getInstance().getDataFolder(), channelFolder);
        channelFolderFile.mkdir();
        switch (type) {
            case SQLITE:
                storage = new SQLiteStorage(channelFolder);
                break;
            case YAML:
                storage = new YamlStorage(channelFolder);
                break;
            default:
                storage = new YamlStorage(channelFolder);
                break;
        }

        int refreshRate = ConfigManager.getConfig().getCacheRefreshRate();
        if (refreshRate < 60) {
            refreshRate = 60;
            ConfigManager.getConfig().setValue(ConfigManager.ConfigPaths.CACHEREFRESHRATE, 60);
        }
        if (refreshRate > 480) {
            refreshRate = 480;
            ConfigManager.getConfig().setValue(ConfigManager.ConfigPaths.CACHEREFRESHRATE, 480);
        }

        int timeInTicks = refreshRate * 20;
        refreshingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                //TODO: Check if this is necessary
//                updateList();
            }
        }, timeInTicks, timeInTicks);
    }

    public void updateChannels(boolean async) {
        if (async) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(WirelessRedstone.getInstance(), new Runnable() {
                public void run() {
                    updateList();
                }
            });
        } else {
            updateList();
        }
    }

    protected void updateList() {
        allChannels.clear();
        Collection<WirelessChannel> channels = getStorage().getAllChannels();

        for (WirelessChannel channel : channels) {
            allChannels.put(channel.getName(), channel);
        }
    }

    protected void updateList(String channelName, WirelessChannel channel) {
        if (channel == null){
            allChannels.remove(channelName);
        } else {
            allChannels.put(channelName, channel);
        }
    }

    protected void wipeList() {
        allChannels.clear();
    }

    public StorageConfiguration getStorage() {
        return storage;
    }

    public Collection<WirelessChannel> getChannels() {
        return allChannels.values();
    }

    public WirelessChannel getChannel(String channelName) {
        return allChannels.get(channelName);
    }

    public Collection<WirelessPoint> getAllSigns() {
        List<WirelessPoint> collection = new ArrayList<>();
        for (WirelessChannel channel : getChannels()) {
            collection.addAll(channel.getSigns());
        }
        return collection;
    }

}
