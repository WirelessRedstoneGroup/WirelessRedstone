package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Commands.Admin.AdminCommandManager;
import net.licks92.WirelessRedstone.Commands.CommandManager;
import net.licks92.WirelessRedstone.Listeners.BlockListener;
import net.licks92.WirelessRedstone.Listeners.PlayerListener;
import net.licks92.WirelessRedstone.Listeners.WorldListener;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.Storage.IWirelessStorageConfiguration;
import net.licks92.WirelessRedstone.Storage.StorageManager;
import net.licks92.WirelessRedstone.Storage.StorageType;
import net.licks92.WirelessRedstone.String.StringLoader;
import net.licks92.WirelessRedstone.String.StringManager;
import net.licks92.WirelessRedstone.WorldEdit.WorldEditHooker;
import net.licks92.WirelessRedstone.WorldEdit.WorldEditLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private Boolean fullyStarted = false;

    private static Main instance;
    private static GlobalCache globalCache; //GlobalCache -> Manage global cache, SignManager -> Manage WireBox functions
    private static SignManager signManager;
    private static WRLogger WRLogger;
    private static StringManager stringManager;
    private static StorageManager storageManager;
    private static PermissionsManager permissionsManager;
    private static Updater updater;
    private static Metrics metrics;
    private static WorldEditHooker worldEditHooker;
    private static CommandManager commandManager;
    private static AdminCommandManager adminCommandManager;

    private ConfigManager config;

    public static Main getInstance() {
        return instance;
    }

    public static GlobalCache getGlobalCache() {
        return globalCache;
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
    }

    public static PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public static Updater getUpdater() {
        return updater;
    }

    public static Metrics getMetrics() {
        return metrics;
    }

    public static WorldEditHooker getWorldEditHooker() {
        return worldEditHooker;
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static AdminCommandManager getAdminCommandManager() {
        return adminCommandManager;
    }

    public static void setWorldEditHooker(WorldEditHooker worldEditHooker) {
        Main.worldEditHooker = worldEditHooker;
    }

    private static final String CHANNEL_FOLDER = "channels";

    @Override
    public void onDisable() {
        if (fullyStarted) {
            try {
                Main.getStorage().updateReceivers();
                storageManager.getStorage().close();
            } catch (Exception ex) {
                WRLogger.severe("An error occured when disabling the plugin!");
                ex.printStackTrace();
            }
        }
        fullyStarted = false;

        worldEditHooker = null;
        commandManager = null;
        adminCommandManager = null;
        metrics = null;
        updater = null;
        stringManager = null;
        WRLogger = null;
        globalCache = null;
        signManager = null;
        storageManager = null;
        instance = null;
    }

    @Override
    public void onEnable() {
        instance = this;
        config = ConfigManager.getConfig();
        WRLogger = new WRLogger("[WirelessRedstone]", getServer().getConsoleSender(), config.getDebugMode(), config.getColorLogging());

        if (config.getDebugMode())
            WRLogger.info("Debug mode enabled!");

        if (config.getConfigVersion() <= 1) {
            if (!(new ConfigConverter(config.getConfigVersion(), CHANNEL_FOLDER).success())){
                WRLogger.severe("**********");
                WRLogger.severe("Updating config files FAILED! The plugin is now disabled to prevent further damages.");
                WRLogger.severe("**********");
                getPluginLoader().disablePlugin(this);
                return;
            }
        }

        stringManager = new StringLoader(config.getLanguage());
        signManager = new SignManager();
        storageManager = new StorageManager(config.getStorageType(), CHANNEL_FOLDER);

        if (!storageManager.getStorage().initStorage()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        globalCache = new GlobalCache(config.getCacheRefreshRate());
        permissionsManager = new PermissionsManager();
        commandManager = new CommandManager();
        adminCommandManager = new AdminCommandManager();

        PluginManager pm = getServer().getPluginManager();

        if (!Utils.isCompatible()) {
            WRLogger.severe("**********");
            WRLogger.severe("This plugin isn't compatible with this Minecraft version! Please check the bukkit/spigot page.");
            WRLogger.severe("**********");
            getPluginLoader().disablePlugin(this);
            return;
        }

        WRLogger.debug("Loading Chunks...");
        Utils.loadChunks();

        if (pm.isPluginEnabled("WorldEdit")) {
            WRLogger.debug("Loading WorldEdit support...");
            new WorldEditLoader();
        } else
            WRLogger.debug("WorldEdit not enabled. Skipping WorldEdit support.");

        WRLogger.debug("Loading listeners...");
        //Don't need to store the instance because we won't touch it.
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new PlayerListener(), this);

        if (config.getUpdateCheck())
            updater = new Updater();

        WRLogger.debug("Loading commands...");
        getCommand("wirelessredstone").setExecutor(commandManager);
        getCommand("wr").setExecutor(commandManager);
        getCommand("wredstone").setExecutor(commandManager);
        getCommand("wifi").setExecutor(commandManager);

        getCommand("wradmin").setExecutor(adminCommandManager);
        getCommand("wra").setExecutor(adminCommandManager);

        if (config.getMetrics()) {
            WRLogger.debug("Loading metrics...");
            loadMetrics();
        }

        fullyStarted = true;
        WRLogger.info("Plugin is now loaded");
    }

    public void resetStorageManager() {
        storageManager = new StorageManager(config.getStorageType(), CHANNEL_FOLDER);
    }

    private void loadMetrics() {
        try {
            metrics = new Metrics(this);

            // Channel metrics
            Metrics.Graph channelGraph = metrics.createGraph("Channel metrics");
            channelGraph.addPlotter(new Metrics.Plotter("Total channels") {
                @Override
                public int getValue() {
                    return Main.getStorage().getAllChannels().size();
                }
            });
            channelGraph.addPlotter(new Metrics.Plotter("Total signs") {
                @Override
                public int getValue() {
                    return Main.getGlobalCache().getAllSigns().size();
                }
            });

            // Sign Metrics
            Metrics.Graph signGraph = metrics.createGraph("Sign metrics");
            signGraph.addPlotter(new Metrics.Plotter("Transmitters") {
                @Override
                public int getValue() {
                    int total = 0;
                    for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
                        total += channel.getTransmitters().size();
                    }
                    return total;
                }
            });
            signGraph.addPlotter(new Metrics.Plotter("Receivers") {
                @Override
                public int getValue() {
                    int total = 0;
                    for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
                        total += channel.getReceivers().size();
                    }
                    return total;
                }
            });
            signGraph.addPlotter(new Metrics.Plotter("Screens") {
                @Override
                public int getValue() {
                    int total = 0;
                    for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
                        total += channel.getScreens().size();
                    }
                    return total;
                }
            });

            Metrics.Graph receiverTypesProportion = metrics.createGraph("Different types of receivers");
            receiverTypesProportion.addPlotter(new Metrics.Plotter("Default") {
                @Override
                public int getValue() {
                    int total = 0;
                    for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
                        total += channel.getReceiversOfType(WirelessReceiver.Type.DEFAULT).size();
                    }
                    return total;
                }
            });
            receiverTypesProportion.addPlotter(new Metrics.Plotter("Inverters") {
                @Override
                public int getValue() {
                    int total = 0;
                    for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
                        total += channel.getReceiversOfType(WirelessReceiver.Type.INVERTER).size();
                    }
                    return total;
                }
            });
            receiverTypesProportion.addPlotter(new Metrics.Plotter("Delayers") {
                @Override
                public int getValue() {
                    int total = 0;
                    for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
                        total += channel.getReceiversOfType(WirelessReceiver.Type.DELAYER).size();
                    }
                    return total;
                }
            });
            receiverTypesProportion.addPlotter(new Metrics.Plotter("Clocks") {
                @Override
                public int getValue() {
                    int total = 0;
                    for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
                        total += channel.getReceiversOfType(WirelessReceiver.Type.CLOCK).size();
                    }
                    return total;
                }
            });
            receiverTypesProportion.addPlotter(new Metrics.Plotter("Switchers") {
                @Override
                public int getValue() {
                    int total = 0;
                    for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
                        total += channel.getReceiversOfType(WirelessReceiver.Type.SWITCH).size();
                    }
                    return total;
                }
            });

            //Storage metrics
            Metrics.Graph storageGraph = metrics.createGraph("Storage used");
            storageGraph.addPlotter(new Metrics.Plotter("SQLite") {
                @Override
                public int getValue() {
                    if (ConfigManager.getConfig().getStorageType() == StorageType.SQLITE) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            storageGraph.addPlotter(new Metrics.Plotter("MySQL") {
                @Override
                public int getValue() {
                    if (ConfigManager.getConfig().getStorageType() == StorageType.MYSQL) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            storageGraph.addPlotter(new Metrics.Plotter("Yaml") {
                @Override
                public int getValue() {
                    if (ConfigManager.getConfig().getStorageType() == StorageType.YAML) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            Metrics.Graph permissionsGraph = metrics.createGraph("Plugin used for Permissions");
            permissionsGraph.addPlotter(new Metrics.Plotter("Bukkit permissions") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            metrics.start();
        } catch (Exception e) {
            WRLogger.warning("Failed to load metrics. Turn on debug mode to see the stack trace.");
            if (ConfigManager.getConfig().getDebugMode())
                e.printStackTrace();
        }
    }
}
