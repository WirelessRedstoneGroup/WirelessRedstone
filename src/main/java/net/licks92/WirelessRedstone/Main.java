package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Storage.IWirelessStorageConfiguration;
import net.licks92.WirelessRedstone.Storage.StorageManager;
import net.licks92.WirelessRedstone.String.StringLoader;
import net.licks92.WirelessRedstone.String.StringManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

    private static Main instance;
    private static ChannelManager channelManager; //ChannelManager -> Manage global cache, SignManager -> Manage WireBox functions
    private static SignManager signManager;
    private static WRLogger WRLogger;
    private static StringManager stringManager;
    private static StorageManager storageManager;
    private ConfigManager config;

    public static Main getInstance() {
        return instance;
    }
    public static ChannelManager getChannelManager() {
        return channelManager;
    }
    public static SignManager getSignManager() {
        return signManager;
    }
    public static WRLogger getWRLogger() {
        return WRLogger;
    }
    public static StringManager getStrings() {
        return stringManager;
    }
    public static IWirelessStorageConfiguration getStorage() {
        return storageManager.getStorage();
    };

    private static final String CHANNEL_FOLDER = "/channels";

    @Override
    public void onDisable() {
        try{
            storageManager.getStorage().close();
        } catch (Exception ex){
            WRLogger.severe("An error occured when disabling the plugin!");
            ex.printStackTrace();
        }

        stringManager = null;
        WRLogger = null;
        channelManager = null;
        storageManager = null;
        instance = null;
    }

    @Override
    public void onEnable(){
        instance = this;
        config = ConfigManager.getConfig();
        WRLogger = new WRLogger("[WirelessRedstone]", getServer().getConsoleSender(), config.getDebugMode(), config.getColorLogging());
        storageManager = new StorageManager(config.getStorageType(), CHANNEL_FOLDER);
        channelManager = new ChannelManager(config.getCacheRefreshRate());

        if(config.getDebugMode())
            WRLogger.info("Debug mode enabled!");

        stringManager = new StringLoader(config.getLanguage());

        PluginManager pm = getServer().getPluginManager();

        if(!Utils.isCompatible()){
            WRLogger.severe("**********");
            WRLogger.severe("This plugin isn't compatible with this Minecraft version! Download a different version.");
            WRLogger.severe("**********");
            return;
        }

        WRLogger.info("Loading Chunks...");
        Utils.loadChunks();
    }

}
