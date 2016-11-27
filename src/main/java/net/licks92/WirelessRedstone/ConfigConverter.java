package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Storage.StorageType;
import org.bukkit.util.FileUtil;

import java.io.File;

public class ConfigConverter {

    private Boolean success = true;

    public ConfigConverter(Integer upgrade, String channelFolder) {
        switch (upgrade) {
            case 1: {
                File channelFolderFile = new File(Main.getInstance().getDataFolder(), channelFolder);
                channelFolderFile.mkdir();

                if (ConfigManager.getConfig().getStorageType() == StorageType.SQLITE
                        && new File(channelFolderFile + File.separator + "channels.db").exists()) {
                    new File(channelFolderFile + File.separator + "WirelessRedstoneDatabase.db").delete();
                    FileUtil.copy(new File(channelFolderFile + File.separator + "channels.db"), new File(channelFolderFile + File.separator + "WirelessRedstoneDatabase.db"));
                }

                ConfigManager.getConfig().setValue(ConfigPaths.CONFIGVERSION, 2);
                ConfigManager.getConfig().setValue(ConfigPaths.UPDATECHECK, true);

                break;
            }
            default:
                break;
        }
    }

    public boolean success() {
        return success;
    }
}
