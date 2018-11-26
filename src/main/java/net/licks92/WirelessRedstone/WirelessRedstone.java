package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Commands.Admin.AdminCommandManager;
import net.licks92.WirelessRedstone.Commands.CommandManager;
import net.licks92.WirelessRedstone.Listeners.BlockListener;
import net.licks92.WirelessRedstone.Listeners.PlayerListener;
import net.licks92.WirelessRedstone.Listeners.WorldListener;
import net.licks92.WirelessRedstone.Storage.StorageConfiguration;
import net.licks92.WirelessRedstone.Storage.StorageManager;
import net.licks92.WirelessRedstone.String.StringManager;
import net.licks92.WirelessRedstone.String.Strings;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WirelessRedstone extends JavaPlugin {

    private static final String CHANNEL_FOLDER = "channels";

    private static WirelessRedstone instance;
    private static WRLogger WRLogger;
    private static StringManager stringManager;
    private static StorageManager storageManager;
    private static SignManager signManager;
    private static CommandManager commandManager;
    private static AdminCommandManager adminCommandManager;

    private ConfigManager config;
    private boolean fullyLoaded = false;


    public static WirelessRedstone getInstance() {
        return instance;
    }

    public static WRLogger getWRLogger() {
        return WRLogger;
    }

    public static StringManager getStringManager() {
        return stringManager;
    }

    public static Strings getStrings() {
        return getStringManager().getStrings();
    }

    public static StorageManager getStorageManager() {
        return storageManager;
    }

    public static StorageConfiguration getStorage() {
        return getStorageManager().getStorage();
    }

    public static SignManager getSignManager() {
        return signManager;
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static AdminCommandManager getAdminCommandManager() {
        return adminCommandManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        config = ConfigManager.getConfig();
        WRLogger = new WRLogger("[WirelessRedstone]", getServer().getConsoleSender(), config.getDebugMode(), config.getColorLogging());
        stringManager = new StringManager(config.getLanguage());

        storageManager = new StorageManager(config.getStorageType(), CHANNEL_FOLDER);

        if (!storageManager.getStorage().initStorage()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        if (!Utils.isCompatible()) {
            WRLogger.severe("**********");
            WRLogger.severe("This plugin isn't compatible with this Minecraft version! Please check the bukkit/spigot page.");
            WRLogger.severe("**********");
            getPluginLoader().disablePlugin(this);
        }

        signManager = new SignManager();
        commandManager = new CommandManager();
        adminCommandManager = new AdminCommandManager();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new PlayerListener(), this);

        getCommand("wirelessredstone").setExecutor(commandManager);
        getCommand("wr").setExecutor(commandManager);
        getCommand("wredstone").setExecutor(commandManager);
        getCommand("wifi").setExecutor(commandManager);

        getCommand("wirelessredstone").setTabCompleter(commandManager);
        getCommand("wr").setTabCompleter(commandManager);
        getCommand("wredstone").setTabCompleter(commandManager);
        getCommand("wifi").setTabCompleter(commandManager);

        getCommand("wradmin").setExecutor(adminCommandManager);
        getCommand("wra").setExecutor(adminCommandManager);

        getCommand("wradmin").setTabCompleter(adminCommandManager);
        getCommand("wra").setTabCompleter(adminCommandManager);

        fullyLoaded = true;
    }

    @Override
    public void onDisable() {
        if (fullyLoaded) {
            getStorage().close();
        }

        fullyLoaded = false;
        adminCommandManager = null;
        commandManager = null;
        signManager = null;
        storageManager = null;
        stringManager = null;
        config = null;
        WRLogger = null;
        instance = null;
    }

    public void resetStrings() {
        stringManager = null;
        stringManager = new StringManager(config.getLanguage());
    }
}
