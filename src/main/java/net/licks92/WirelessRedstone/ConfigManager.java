package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Storage.StorageType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class ConfigManager {

    private static final ConfigManager configuration = new ConfigManager("config");

    public static ConfigManager getConfig() {
        return configuration;
    }

    private final File file;
    private final FileConfiguration config;

    private ConfigManager(final String fileName) {
        if (!Main.getInstance().getDataFolder().exists()) {
            Main.getInstance().getDataFolder().mkdir();
        }

        file = new File(Main.getInstance().getDataFolder(), fileName + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                copyStream(Main.getInstance().getResource("config.yml"), new FileOutputStream(file));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
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
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getDebugMode(){
        return config.getBoolean(ConfigPaths.DEBUGMODE.getValue(), false);
    }

    public boolean getColorLogging(){
        return config.getBoolean(ConfigPaths.COLORLOGGING.getValue(), true);
    }

    public boolean getUpdateCheck(){
        return config.getBoolean(ConfigPaths.UPDATECHECK.getValue(), true);
    }

    public boolean getCancelChunkUnload(){
        return config.getBoolean(ConfigPaths.CANCELCHUNKUNLOAD.getValue(), true);
    }

    public boolean getVault(){
        return config.getBoolean(ConfigPaths.USEVAULT.getValue(), true);
    }

    public boolean getSilentMode(){
        return config.getBoolean(ConfigPaths.SILENTMODE.getValue(), false);
    }

    public boolean useORLogic(){
        return !config.getString(ConfigPaths.GATELOGIC.getValue(), "OR").equalsIgnoreCase("IGNORE");
    }

    public Integer getCancelChunkUnloadRange(){
        return config.getInt(ConfigPaths.CANCELCHUNKUNLOADRANGE.getValue(), 4);
    }

    public Integer getInteractTransmitterTime(){
        return config.getInt(ConfigPaths.INTERACTTRANSMITTERTIME.getValue(), 1000);
    }

    public Integer getCacheRefreshRate(){
        return config.getInt(ConfigPaths.CACHEREFRESHRATE.getValue(), 150);
    }

    public String getLanguage(){
        return config.getString(ConfigPaths.LANGUAGE.getValue(), "en");
    }

    public StorageType getStorageType(){
        switch (config.getString(ConfigPaths.SAVEMODE.getValue(), "SQLITE").toUpperCase()){
            case "YAML":
                return StorageType.YAML;
            case "YML":
                return StorageType.YAML;
            case "MYSQL":
                return StorageType.MYSQL;
            default:
                return StorageType.YAML; //TODO: Switch back to SQLITE
        }
    }
}

enum ConfigPaths {
    DEBUGMODE("DebugMode"), LANGUAGE("Language"), COLORLOGGING("ColourfulLogging"), UPDATECHECK("CheckForUpdates"), CANCELCHUNKUNLOAD("cancelChunkUnloads"),
        CANCELCHUNKUNLOADRANGE("cancelChunkUnloadRange"), USEVAULT("UseVault"), SILENTMODE("SilentMode"), INTERACTTRANSMITTERTIME("InteractTransmitterTime"),
        CACHEREFRESHRATE("CacheRefreshFrequency"), GATELOGIC("gateLogic"), SAVEMODE("saveOption");

    private String name;

    private ConfigPaths(String name) {
        this.name = name;
    }

    public String getValue() {
        return name;
    }
}
