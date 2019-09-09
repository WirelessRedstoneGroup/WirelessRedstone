package net.licks92.wirelessredstone.storage;

import net.licks92.wirelessredstone.ConfigManager;
import net.licks92.wirelessredstone.signs.WirelessChannel;
import net.licks92.wirelessredstone.signs.WirelessPoint;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

    private final ConcurrentHashMap<String, WirelessChannel> allChannels = new ConcurrentHashMap<>();
    private final BukkitTask refreshingTask;
    private final StorageType storageType;
    private final StorageConfiguration storage;
    private final String channelFolder;
    private final File channelFolderFile;

    public StorageManager(StorageType type, String channelFolder) {
        this.storageType = type;
        this.channelFolder = channelFolder;

        this.channelFolderFile = new File(WirelessRedstone.getInstance().getDataFolder(), channelFolder);
        this.channelFolderFile.mkdir();
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
            Bukkit.getServer().getScheduler().runTaskAsynchronously(WirelessRedstone.getInstance(), this::updateList);
        } else {
            updateList();
        }
    }

    protected void updateList() {
        allChannels.clear();
        Collection<WirelessChannel> channels = getStorage().getAllChannels();

        channels.forEach(channel -> allChannels.put(channel.getName(), channel));
    }

    protected void updateList(String channelName, WirelessChannel channel) {
        if (channel == null) {
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
        getChannels().stream()
                .map(WirelessChannel::getSigns)
                .forEach(collection::addAll);
        return collection;
    }

    protected boolean moveStorageFromType(StorageType storageType) {
        if (!getStorage().backupData()) {
            WirelessRedstone.getWRLogger().severe("Porting data to other storage type failed due to a backup problem!");
            return false;
        }

        StorageConfiguration storage;
        if (storageType == StorageType.YAML) {
            storage = new YamlStorage(channelFolder);
        } else if (storageType == StorageType.SQLITE) {
            storage = new SQLiteStorage(channelFolder);
            DatabaseClient.init(new File(WirelessRedstone.getInstance().getDataFolder(), channelFolder).toString());
        } else {
            return false;
        }

        Collection<WirelessChannel> channels = storage.getAllChannels();
        channels.forEach(getStorage()::createChannel);
        storage.close();

        if (storageType == StorageType.YAML) {
            final FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(".yml");

            Arrays.stream(Objects.requireNonNull(channelFolderFile.listFiles(filter)))
                    .forEach(File::delete);
        } else {
            final FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(".db");

            Arrays.stream(Objects.requireNonNull(channelFolderFile.listFiles(filter)))
                    .forEach(File::delete);
        }

        WirelessRedstone.getStorageManager().updateChannels(false);

        return true;
    }

}
