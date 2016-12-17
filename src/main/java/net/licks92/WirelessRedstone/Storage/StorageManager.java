package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.WirelessRedstone;

import java.io.File;

public class StorageManager {

    IWirelessStorageConfiguration storage;

    public StorageManager(StorageType type, String channelFolder) {
        File channelFolderFile = new File(WirelessRedstone.getInstance().getDataFolder(), channelFolder);
        channelFolderFile.mkdir();
        switch (type) { //TODO: Add MYSQL support
            case SQLITE:
                storage = new SQLiteStorage(channelFolder);
                break;
            case YAML:
                storage = new YamlStorage(channelFolder);
                break;
            case MYSQL:
                storage = new YamlStorage(channelFolder);
                break;
            default:
                storage = new YamlStorage(channelFolder);
                break;
        }
    }

    public IWirelessStorageConfiguration getStorage() {
        return storage;
    }
}
