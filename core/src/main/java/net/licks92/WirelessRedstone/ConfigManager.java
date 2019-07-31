package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Storage.StorageType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;


public class ConfigManager {

    private static final ConfigManager configuration = new ConfigManager("config");

    public static ConfigManager getConfig() {
        return configuration;
    }

    private final File file;
    private final YamlConfiguration config;

    private ConfigManager(final String fileName) {
        if (!WirelessRedstone.getInstance().getDataFolder().exists()) {
            WirelessRedstone.getInstance().getDataFolder().mkdir();
        }

        file = new File(WirelessRedstone.getInstance().getDataFolder(), fileName + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                copyStream(Objects.requireNonNull(WirelessRedstone.getInstance().getResource("config.yml")), new FileOutputStream(file));
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        if (getCacheRefreshRate() < 60 || getCacheRefreshRate() > 480)
            setValue(ConfigPaths.CACHEREFRESHRATE, (getCacheRefreshRate() < 60) ? 60 : 480);
    }

    /* Extract files out of the jar file. */
    private void copyStream(InputStream input, OutputStream output)
            throws IOException {
        // Reads up to 8K at a time.
        byte[] buffer = new byte[8192];
        int read;

        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
    }

    /* Save the config.
     * Very imported if something has changed. */
    public void save() {
        try {
            config.save(file);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public void copyDefaults(){
        if (config == null)
            return;

        config.options().copyHeader(true);
        config.options().copyDefaults(true);
    }

    public void update(String channelFolder) {
        switch (getConfigVersion()) {
            case 1: {
                File channelFolderFile = new File(WirelessRedstone.getInstance().getDataFolder(), channelFolder);
                channelFolderFile.mkdir();

                if (getStorageType() == StorageType.SQLITE
                        && new File(channelFolderFile + File.separator + "channels.db").exists()
                        && !(new File(channelFolderFile + File.separator + "WirelessRedstoneDatabase.db").exists())) {
                    new File(channelFolderFile + File.separator + "WirelessRedstoneDatabase.db").delete();
                    FileUtil.copy(
                            new File(channelFolderFile + File.separator + "channels.db"),
                            new File(channelFolderFile + File.separator + "WirelessRedstoneDatabase.db"));
                }

                copyDefaults();

                setValue(ConfigPaths.CONFIGVERSION, 2);
                setValue(ConfigPaths.UPDATECHECK, true);

                // no break; because we want the switch to fall through next versions
            }
            case 2: {
                removeValue("cancelChunkUnloads");
                removeValue("cancelChunkUnloadRange");

                copyDefaults();

                setValue(ConfigPaths.CONFIGVERSION, 3);
                setValue(ConfigPaths.METRICS, true);

                // no break; because we want the switch to fall through next versions
            }
            case 3: {
                copyDefaults();

                setValue(ConfigPaths.CONFIGVERSION, 4);
                setValue(ConfigPaths.SENTRY, true);

                break;
            }
            default:
                break;
        }
    }

    public void setStorageType(StorageType storageType) {
        config.set(ConfigPaths.SAVEMODE.getValue(), storageType.toString().toUpperCase());
        save();
    }

    public void setValue(ConfigPaths configPaths, Object object) {
        config.set(configPaths.getValue(), object);
        save();
    }

    public void removeValue(String path) {
        config.set(path, null);
        save();
    }

    public Integer getConfigVersion() {
        return config.getInt(ConfigPaths.CONFIGVERSION.getValue(), 1);
    }

    public boolean getDebugMode() {
        return config.getBoolean(ConfigPaths.DEBUGMODE.getValue(), false);
    }

    public boolean getColorLogging() {
        return config.getBoolean(ConfigPaths.COLORLOGGING.getValue(), true);
    }

    public boolean getUpdateCheck() {
        return config.getBoolean(ConfigPaths.UPDATECHECK.getValue(), true);
    }

    public boolean getVault() {
        return config.getBoolean(ConfigPaths.USEVAULT.getValue(), true);
    }

    public boolean getSilentMode() {
        return config.getBoolean(ConfigPaths.SILENTMODE.getValue(), false);
    }

    public boolean getDropSignBroken() {
        return config.getBoolean(ConfigPaths.DROPSIGNBROKEN.getValue(), true);
    }

    public boolean useORLogic() {
        return !config.getString(ConfigPaths.GATELOGIC.getValue(), "OR").equalsIgnoreCase("IGNORE");
    }

    public Integer getInteractTransmitterTime() {
        return config.getInt(ConfigPaths.INTERACTTRANSMITTERTIME.getValue(), 1000);
    }

    public Integer getCacheRefreshRate() {
        return config.getInt(ConfigPaths.CACHEREFRESHRATE.getValue(), 150);
    }

    public String getLanguage() {
        return config.getString(ConfigPaths.LANGUAGE.getValue(), "en");
    }

    public boolean getMetrics() {
        return config.getBoolean(ConfigPaths.METRICS.getValue(), true);
    }

    public boolean getSentry() {
        return config.getBoolean(ConfigPaths.SENTRY.getValue(), true);
    }

    public StorageType getStorageType() {
        switch (config.getString(ConfigPaths.SAVEMODE.getValue(), "YML").toUpperCase()) {
            case "YAML":
            case "YML":
                return StorageType.YAML;
            case "SQLITE":
                return StorageType.SQLITE;
            default:
                return StorageType.YAML;
        }
    }

    public enum ConfigPaths {
        CONFIGVERSION("ConfigVersion"), DEBUGMODE("DebugMode"), LANGUAGE("Language"), COLORLOGGING("ColourfulLogging"),
        UPDATECHECK("CheckForUpdates"), USEVAULT("UseVault"), SILENTMODE("SilentMode"),
        INTERACTTRANSMITTERTIME("InteractTransmitterTime"), CACHEREFRESHRATE("CacheRefreshFrequency"),
        GATELOGIC("gateLogic"), SAVEMODE("saveOption"), DROPSIGNBROKEN("DropSignWhenBroken"),
        METRICS("Metrics"), SENTRY("Sentry");

        private final String name;

        ConfigPaths(String name) {
            this.name = name;
        }

        public String getValue() {
            return name;
        }
    }

}
