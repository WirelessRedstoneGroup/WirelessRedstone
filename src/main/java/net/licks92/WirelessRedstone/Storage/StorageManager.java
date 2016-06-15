package net.licks92.WirelessRedstone.Storage;

import net.licks92.WirelessRedstone.Main;

import java.io.File;

public class StorageManager {

    IWirelessStorageConfiguration storage;

    public StorageManager(StorageType type, String channelFolder) {
        File channelFolderFile = new File(Main.getInstance().getDataFolder(), channelFolder);
        channelFolderFile.mkdir();
        switch (type) { //TODO: Add SQLITE and MYSQL support
            case SQLITE:
                break;
            case YAML:
                storage = new YamlStorage(channelFolder);
                break;
            case MYSQL:
                break;
            default:
                break;
        }
    }

    public IWirelessStorageConfiguration getStorage() {
        return storage;
    }
}
