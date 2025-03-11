package net.licks92.wirelessredstone;

import io.sentry.Sentry;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.licks92.wirelessredstone.commands.Admin.AdminCommandManager;
import net.licks92.wirelessredstone.commands.CommandManager;
import net.licks92.wirelessredstone.compat.InternalWorldEditHooker;
import net.licks92.wirelessredstone.listeners.BlockListener;
import net.licks92.wirelessredstone.listeners.PlayerListener;
import net.licks92.wirelessredstone.listeners.WorldListener;
import net.licks92.wirelessredstone.sentry.WirelessRedstoneSentryClientFactory;
import net.licks92.wirelessredstone.signs.WirelessReceiver;
import net.licks92.wirelessredstone.signs.WirelessScreen;
import net.licks92.wirelessredstone.signs.WirelessTransmitter;
import net.licks92.wirelessredstone.storage.StorageConfiguration;
import net.licks92.wirelessredstone.storage.StorageManager;
import net.licks92.wirelessredstone.string.StringManager;
import net.licks92.wirelessredstone.string.Strings;
import net.licks92.wirelessredstone.worldedit.WorldEditLoader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

public class WirelessRedstone extends JavaPlugin {

    public static final String CHANNEL_FOLDER = "channels";

    private static WirelessRedstone instance;
    private static WRLogger wrLogger; // Updated to reflect Adventure Logger
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
    private BukkitAudiences audience; // For central Adventure API handling

    // Static getters for easy access
    public static WirelessRedstone getInstance() {
        return instance;
    }

    public static WRLogger getWRLogger() {
        return wrLogger;
    }

    public static StringManager getStringManager() {
        return stringManager;
    }

    public static StorageManager getStorageManager() {
        return storageManager;
    }

    public static SignManager getSignManager() {
        return signManager;
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static Strings getStrings() {
        return stringManager.getStrings();
    }

    @Override
    public void onEnable() {
        instance = this;

        // Initialize Adventure Audience
        audience = BukkitAudiences.create(this);

        // Compatibility check
        if (!Utils.isCompatible()) {
            logIncompatibleVersion();
            return; // Exit early if not compatible
        }

        // Initialize logger with Adventure API
        config = ConfigManager.getConfig();
        wrLogger = new WRLogger("[WirelessRedstone]", (ConsoleCommandSender) audience.console(), config.getDebugMode(), config.getColorLogging());

        wrLogger.info("Enabling WirelessRedstone");

        // Load configurations
        config.update(CHANNEL_FOLDER);

        sentryEnabled = config.getSentry() && !"TRUE".equalsIgnoreCase(System.getProperty("mc.development"));

        stringManager = new StringManager(config.getLanguage());
        storageManager = new StorageManager(config.getStorageType(), CHANNEL_FOLDER);

        // Initialize storage
        storageManager = new StorageManager(this);
        if (!storageManager.getStorage().initStorage()) {
            wrLogger.severe("Failed to initialize storage. Plugin will stop.");

            disablePlugin(); // Clean lifecycle management, safely disable
            return; // Exit early since we can't continue
        }

        storageLoaded = true;

        // Initialize managers
        signManager = new SignManager();
        commandManager = new CommandManager();
        adminCommandManager = new AdminCommandManager();

        // Initialize Sentry for error reporting
        setupSentry();

        // Register events and commands
        registerEvents();
        registerCommands();

        // Load WorldEdit integration
        loadWorldEditIntegration();

        // Enable metrics (if applicable)
        setupMetrics();

        // Check for plugin updates
        checkForUpdates();

        wrLogger.info("WirelessRedstone has been successfully enabled!");
    }

    /**
     * Cleanly disables the plugin by throwing an unchecked exception for server shutdown.
     * This avoids the use of deprecated `getPluginLoader()`.
     */
    private void disablePlugin() {
        throw new IllegalStateException("WirelessRedstone was disabled due to a critical error during initialization.");
    }

    @Override
    public void onDisable() {
        wrLogger.info("Disabling WirelessRedstone...");

        if (storageLoaded) {
            getStorageManager().getStorage().close();
        }

        if (worldEditHooker != null) {
            worldEditHooker.unRegister();
        }

        if (audience != null) {
            audience.close();
        }

        // Cleanup resources
        instance = null;
        wrLogger = null;
    }

    private void logIncompatibleVersion() {
        String serverVersion = Bukkit.getBukkitVersion();
        Component errorMsg = Component.text(
                "WirelessRedstone is not compatible with server version: " + serverVersion,
                NamedTextColor.RED
        );

        // Output the error message using Adventure API
        audience.console().sendMessage(errorMsg);

        getPluginLoader().disablePlugin(this);
    }

    private void setupMetrics() {
        if (!config.getMetrics()) return;

        metrics = new Metrics(this);

        // Main sign type metrics
        metrics.addCustomChart(new Metrics.AdvancedPie("main_sign_types", () -> {
            try {
                return Map.of(
                        "Transmitters", countSigns(WirelessTransmitter.class),
                        "Receivers", countSigns(WirelessReceiver.class),
                        "Screens", countSigns(WirelessScreen.class)
                );
            } catch (Exception e) {
                wrLogger.warning("Error tracking metrics: " + e.getMessage());
                return Collections.emptyMap();
            }
        }));
    }

    private void setupSentry() {
        if (!sentryEnabled) return;

        try (var resourceStream = getResource("plugin.yml")) {
            if (resourceStream == null) {
                wrLogger.severe("Could not load 'plugin.yml'. Missing from the jar.");
                return;
            }

            var pluginConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream));
            var sentryDsn = pluginConfig.getString("sentry.dsn", "");
            Sentry.init(sentryDsn, new WirelessRedstoneSentryClientFactory());

            resetSentryContext();

            wrLogger.info("Sentry initialized successfully.");
        } catch (Exception e) {
            wrLogger.severe("Failed to initialize Sentry: " + e.getMessage());
        }
    }

    private void resetSentryContext() {
        Sentry.clearContext();
        var version = Bukkit.getBukkitVersion().split("-")[0];
        Sentry.getStoredClient().addTag("MC_version", version);
        Sentry.getStoredClient().addTag("MC_implementation", "Paper");
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new PlayerListener(), this);

        wrLogger.info("Events registered successfully.");
    }

    private void registerCommands() {
        Map<String, CommandExecutor> commands = Map.of(
                "wirelessredstone", commandManager,
                "wradmin", adminCommandManager
        );

        commands.forEach((commandName, executor) -> {
            var command = getCommand(commandName);
            if (command != null) {
                command.setExecutor(executor);
                command.setTabCompleter((TabCompleter) executor);
            } else {
                wrLogger.warning("Command '" + commandName + "' is missing from plugin.yml.");
            }
        });
    }

    public static StorageConfiguration getStorage() {
        if (storageManager == null || storageManager.getStorage() == null) {
            throw new IllegalStateException("StorageManager or StorageConfiguration is not initialized!");
        }
        return storageManager.getStorage();
    }


    private void loadWorldEditIntegration() {
        if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            new WorldEditLoader();
            wrLogger.info("WorldEdit integration enabled.");
        } else {
            wrLogger.warning("WorldEdit not found. Skipping integration.");
        }
    }

    private void checkForUpdates() {
        if (!config.getUpdateCheck()) return;

        UpdateChecker.init(this).requestUpdateCheck().whenComplete((result, throwable) -> {
            if (throwable == null && result.updateAvailable()) {
                wrLogger.info("An update is available: Version " + result.getNewestVersion());
            }
        });
    }

    private int countSigns(Class<?> signType) {
        if (storageManager == null || storageManager.getAllSigns() == null) return 0;
        return (int) storageManager.getAllSigns().stream()
                .filter(signType::isInstance)
                .count();
    }
}