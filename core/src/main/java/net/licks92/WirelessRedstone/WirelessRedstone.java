package net.licks92.WirelessRedstone;

import io.sentry.Sentry;
import net.licks92.WirelessRedstone.Commands.Admin.AdminCommandManager;
import net.licks92.WirelessRedstone.Commands.CommandManager;
import net.licks92.WirelessRedstone.Compat.InternalWorldEditHooker;
import net.licks92.WirelessRedstone.Listeners.BlockListener;
import net.licks92.WirelessRedstone.Listeners.PlayerListener;
import net.licks92.WirelessRedstone.Listeners.WorldListener;
import net.licks92.WirelessRedstone.Sentry.EventExceptionHandler;
import net.licks92.WirelessRedstone.Sentry.WirelessRedstoneSentryClientFactory;
import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverClock;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverDelayer;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverInverter;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverSwitch;
import net.licks92.WirelessRedstone.Signs.WirelessScreen;
import net.licks92.WirelessRedstone.Signs.WirelessTransmitter;
import net.licks92.WirelessRedstone.Storage.StorageConfiguration;
import net.licks92.WirelessRedstone.Storage.StorageManager;
import net.licks92.WirelessRedstone.String.StringManager;
import net.licks92.WirelessRedstone.String.Strings;
import net.licks92.WirelessRedstone.WorldEdit.WorldEditLoader;
import net.licks92.WirelessRedstone.materiallib.MaterialLib;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class WirelessRedstone extends JavaPlugin {

    public static final String CHANNEL_FOLDER = "channels";

    private static WirelessRedstone instance;
    private static WRLogger WRLogger;
    private static StringManager stringManager;
    private static StorageManager storageManager;
    private static SignManager signManager;
    private static CommandManager commandManager;
    private static AdminCommandManager adminCommandManager;
    private static Metrics metrics;

    private ConfigManager config;
    private InternalWorldEditHooker worldEditHooker;
    private boolean storageLoaded = false;
    private boolean sentryEnabled = true;


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

    public static Metrics getMetrics() {
        return metrics;
    }

    public boolean isSentryEnabled() {
        return sentryEnabled;
    }

    public InternalWorldEditHooker getWorldEditHooker() {
        return worldEditHooker;
    }

    public void setWorldEditHooker(InternalWorldEditHooker worldEditHooker) {
        this.worldEditHooker = worldEditHooker;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!Utils.isCompatible()) {
            WRLogger.severe("**********");
            WRLogger.severe("This plugin isn't compatible with this Minecraft version! Please check the bukkit/spigot page.");
            WRLogger.severe("**********");
            getPluginLoader().disablePlugin(this);
        }

        new MaterialLib(this).initialize();

        config = ConfigManager.getConfig();
        config.update(CHANNEL_FOLDER);
        sentryEnabled = config.getSentry() && !"TRUE".equalsIgnoreCase(System.getProperty("mc.development"));
        WRLogger = new WRLogger("[WirelessRedstone]", getServer().getConsoleSender(), config.getDebugMode(), config.getColorLogging());
        stringManager = new StringManager(config.getLanguage());

        storageManager = new StorageManager(config.getStorageType(), CHANNEL_FOLDER);

        if (!storageManager.getStorage().initStorage()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        storageLoaded = true;

        signManager = new SignManager();
        commandManager = new CommandManager();
        adminCommandManager = new AdminCommandManager();

        if (sentryEnabled) {
            YamlConfiguration pluginConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(Objects.requireNonNull(getResource("plugin.yml")))
            );

            getWRLogger().debug("Sentry dsn: " + pluginConfig.getString("sentry.dsn", ""));

            Sentry.init(pluginConfig.getString("sentry.dsn", ""), new WirelessRedstoneSentryClientFactory());
            resetSentryContext();
        }

        PluginManager pm = getServer().getPluginManager();

        boolean eventCatchingSuccess = true;
        try {
            if (sentryEnabled) {
                EventExceptionHandler eventExceptionHandler = new EventExceptionHandler() {
                    @Override
                    public boolean handle(Throwable ex, Event event) {
                        Sentry.capture(ex);
                        // getLogger().log(Level.SEVERE, "Error " + ex.getMessage() + " occured for " + event, ex);

                        // Don't pass it on
                        // return true;
                        // Use Bukkit's default exception handler
                        return false;
                    }
                };

                EventExceptionHandler.registerEvents(new WorldListener(), this, eventExceptionHandler);
                EventExceptionHandler.registerEvents(new BlockListener(), this, eventExceptionHandler);
                EventExceptionHandler.registerEvents(new PlayerListener(), this, eventExceptionHandler);
            }
        } catch (RuntimeException ex) {
            eventCatchingSuccess = false;
            getWRLogger().warning("Couldn't register events with Sentry catcher.");
            Sentry.capture(ex);
        }

        if (!eventCatchingSuccess || !sentryEnabled) {
            pm.registerEvents(new WorldListener(), this);
            pm.registerEvents(new BlockListener(), this);
            pm.registerEvents(new PlayerListener(), this);
        }

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

        if (pm.isPluginEnabled("WorldEdit")) {
            new WorldEditLoader();
        }

        if (config.getMetrics()) {
            metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.AdvancedPie("main_sign_types", new Callable<Map<String, Integer>>() {
                @Override
                public Map<String, Integer> call() {
                    Map<String, Integer> valueMap = new HashMap<>();
                    valueMap.put("Transmitters", getSigns(SignType.TRANSMITTER));
                    valueMap.put("Receivers", getSigns(SignType.RECEIVER));
                    valueMap.put("Screens", getSigns(SignType.SCREEN));
                    return valueMap;
                }

                private int getSigns(SignType type) {
                    if (type == SignType.TRANSMITTER) {
                        return (int) getStorageManager().getAllSigns().stream()
                                .filter(point -> point instanceof WirelessTransmitter)
                                .count();
                    } else if (type == SignType.RECEIVER) {
                        return (int) getStorageManager().getAllSigns().stream()
                                .filter(point -> point instanceof WirelessReceiver)
                                .count();
                    } else {
                        return (int) getStorageManager().getAllSigns().stream()
                                .filter(point -> point instanceof WirelessScreen)
                                .count();
                    }
                }
            }));

            metrics.addCustomChart(new Metrics.AdvancedPie("receiver_sign_types", new Callable<Map<String, Integer>>() {
                @Override
                public Map<String, Integer> call() {
                    Map<String, Integer> valueMap = new HashMap<>();
                    valueMap.put("Normal", getSigns(SignType.RECEIVER));
                    valueMap.put("Inverter", getSigns(SignType.RECEIVER_INVERTER));
                    valueMap.put("Delayer", getSigns(SignType.RECEIVER_DELAYER));
                    valueMap.put("Clock", getSigns(SignType.RECEIVER_CLOCK));
                    valueMap.put("Switch", getSigns(SignType.RECEIVER_SWITCH));
                    return valueMap;
                }

                private int getSigns(SignType type) {
                    if (type == SignType.RECEIVER_INVERTER) {
                        return (int) getStorageManager().getAllSigns().stream()
                                .filter(point -> point instanceof WirelessReceiverInverter)
                                .count();
                    } else if (type == SignType.RECEIVER_DELAYER) {
                        return (int) getStorageManager().getAllSigns().stream()
                                .filter(point -> point instanceof WirelessReceiverDelayer)
                                .count();
                    } else if (type == SignType.RECEIVER_CLOCK) {
                        return (int) getStorageManager().getAllSigns().stream()
                                .filter(point -> point instanceof WirelessReceiverClock)
                                .count();
                    } else if (type == SignType.RECEIVER_SWITCH) {
                        return (int) getStorageManager().getAllSigns().stream()
                                .filter(point -> point instanceof WirelessReceiverSwitch)
                                .count();
                    } else {
                        return (int) getStorageManager().getAllSigns().stream()
                                .filter(point -> point instanceof WirelessReceiver)
                                .count();
                    }
                }
            }));

            metrics.addCustomChart(new Metrics.SimplePie("storage_types", () ->
                    ConfigManager.getConfig().getStorageType().toString()
            ));
        }

        if (config.getUpdateCheck()) {
            UpdateChecker.init(this).requestUpdateCheck().whenComplete((updateResult, throwable) -> {
                if (updateResult.updateAvailable()) {
                    Bukkit.getScheduler().runTask(this, () -> getWRLogger().info(getStrings().newUpdate
                            .replaceAll("%%NEWVERSION", updateResult.getNewestVersion())
                            .replaceAll("%%URL", updateResult.getUrl())));
                }
            });
        }
    }

    @Override
    public void onDisable() {
        if (storageLoaded) {
            getStorage().close();
        }

        if (worldEditHooker != null) {
            worldEditHooker.unRegister();
        }

        storageLoaded = false;
        adminCommandManager = null;
        commandManager = null;
        signManager = null;
        storageManager = null;
        stringManager = null;
        config = null;
        WRLogger = null;
        instance = null;
    }

    public void resetSentryContext() {
        Sentry.clearContext();

        Sentry.getStoredClient().setRelease(getDescription().getVersion());

        String version = Bukkit.getBukkitVersion();
        if (version.contains("-")) {
            version = version.split("-")[0];
        }

        String serverImplementation = "Spigot";
        if (Bukkit.getVersion().contains("Paper")) {
            serverImplementation = "Paper";
        } else if (Bukkit.getVersion().contains("Taco")) {
            serverImplementation = "TacoSpigot";
        }

        Sentry.getStoredClient().addTag("MC_version", version);
        Sentry.getStoredClient().addTag("MC_implementation", serverImplementation);
    }

    /**
     * Re-initialize strings. This can be used to switch languages after a config change.
     * <p>
     * Removes reference to stringManager and place a new reference.
     */
    public void resetStrings() {
        stringManager = null;
        stringManager = new StringManager(config.getLanguage());
    }
}
