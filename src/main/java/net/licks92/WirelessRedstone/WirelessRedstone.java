package net.licks92.WirelessRedstone;

import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;
import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Configuration.ConfigurationConverter;
import net.licks92.WirelessRedstone.Configuration.WirelessConfiguration;
import net.licks92.WirelessRedstone.Listeners.WirelessBlockListener;
import net.licks92.WirelessRedstone.Listeners.WirelessPlayerListener;
import net.licks92.WirelessRedstone.Listeners.WirelessWorldListener;
import net.licks92.WirelessRedstone.Permissions.WirelessPermissions;
import net.licks92.WirelessRedstone.Strings.WirelessStrings;
import net.licks92.WirelessRedstone.Strings.WirelessXMLStringsLoader;
import net.licks92.WirelessRedstone.WorldEdit.WorldEditLoader;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * This class is the main class of the plugin. It controls the Configuration,
 * the Listeners, it sends the metrics and controls the actions when enabling /
 * disabling.
 *
 * @author licks92
 */
public class WirelessRedstone extends JavaPlugin {
    public static WirelessConfiguration config;
    public static WirelessStrings strings;
    public static WirelessGlobalCache cache;
    private static WRLogger logger;
    public static WireBox WireBox;
    public WirelessPermissions permissions;
    public static Permission perms;
    public WirelessWorldListener worldlistener;
    public WirelessBlockListener blocklistener;
    public WirelessPlayerListener playerlistener;
    private BukkitTask updateChecker;
    public Updater updater;
    private static WirelessRedstone instance;
    private static Metrics metrics;


    /**
     * Wireless Redstone logger
     *
     * @return logger
     */
    public static WRLogger getWRLogger() {
        return logger;
    }

    /**
     * Calls the actions to do when disabling the plugin.
     */
    @Override
    public void onDisable() {
        try {
            config.updateReceivers();
            config.close();
            updateChecker.cancel();

//            metrics = null;
            instance = null;
        } catch (Exception ignored) {
        }
    }

    /**
     * Calls the actions to do when enabling the plugin (i.e when starting the
     * server)
     */
    @Override
    public void onEnable() {
        instance = this;
        PluginDescriptionFile pdFile = getDescription();

        new ConfigurationConverter(this);

        WireBox = new WireBox(this);
        config = new WirelessConfiguration(this);
        if (config.getDebugMode()) {
            logger = new WRLogger("[WirelessRedstone]", getServer()
                    .getConsoleSender(), true, config.getColourfulLogging());
            logger.info("Debug Mode activated !");
            logger.info("Log level set to FINEST because of the debug mode");
        } else {
            logger = new WRLogger("[WirelessRedstone]", this.getServer()
                    .getConsoleSender(), false, config.getColourfulLogging());
        }
        config.initStorage();
        cache = new WirelessGlobalCache(this, config.getCacheRefreshFrequency());

        WirelessRedstone.logger.info(pdFile.getName() + " version "
                + pdFile.getVersion() + " is loading...");

        updater = new Updater(this, 37345, getFile(), UpdateType.NO_DOWNLOAD,
                true);

        // Load strings
        strings = new WirelessXMLStringsLoader(this, config.getLanguage());
        // strings = new WirelessStrings();

        if (config.doCheckForUpdates()) {
            updateChecker = this.getServer().getScheduler()
                    .runTaskTimerAsynchronously(getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
                                    getWRLogger()
                                            .info(WirelessRedstone.strings.newUpdateAvailable);
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }, 0, 20 * 60 * 30);
        }

        // Load listeners
        worldlistener = new WirelessWorldListener(this);
        blocklistener = new WirelessBlockListener(this);
        playerlistener = new WirelessPlayerListener(this);

        if(Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            WirelessRedstone.getWRLogger().debug("Hooking into WorldEdit ...");
            new WorldEditLoader();
        }

        WirelessRedstone.logger.info("Loading Permissions...");

        permissions = new WirelessPermissions(this);
        config.save();

        WirelessRedstone.logger.info("Registering commands...");
        getCommand("wirelessredstone").setExecutor(new WirelessCommands(this));
        getCommand("wrhelp").setExecutor(new WirelessCommands(this));
        getCommand("wrr").setExecutor(new WirelessCommands(this));
        getCommand("wrt").setExecutor(new WirelessCommands(this));
        getCommand("wrs").setExecutor(new WirelessCommands(this));
        getCommand("wrremove").setExecutor(new WirelessCommands(this));
        getCommand("wra").setExecutor(new WirelessCommands(this));
        getCommand("wrlist").setExecutor(new WirelessCommands(this));
        getCommand("wri").setExecutor(new WirelessCommands(this));
        getCommand("wrlock").setExecutor(new WirelessCommands(this));
        getCommand("wractivate").setExecutor(new WirelessCommands(this));
        getCommand("wrversion").setExecutor(new WirelessCommands(this));
        getCommand("wrtp").setExecutor(new WirelessCommands(this));

        WirelessRedstone.logger.info("Loading Chunks...");
        LoadChunks();

        // Metrics
        if (config.getMetrics()) {
            try {
                metrics = new Metrics(this);

                // Channel metrics
                final Metrics.Graph channelGraph = metrics.createGraph("Channel metrics");
                channelGraph.addPlotter(new Metrics.Plotter("Total channels") {
                    @Override
                    public int getValue() {
                        return config.getAllChannels().size();
                    }
                });
                channelGraph.addPlotter(new Metrics.Plotter("Total signs") {
                    @Override
                    public int getValue() {
                        return cache.getAllSigns().size();
                    }
                });

                // Sign Metrics
                final Metrics.Graph signGraph = metrics.createGraph("Sign metrics");
                signGraph.addPlotter(new Metrics.Plotter("Transmitters") {
                    @Override
                    public int getValue() {
                        int total = 0;
                        for (WirelessChannel channel : config.getAllChannels()) {
                            total += channel.getTransmitters().size();
                        }
                        return total;
                    }
                });
                signGraph.addPlotter(new Metrics.Plotter("Receivers") {
                    @Override
                    public int getValue() {
                        int total = 0;
                        for (WirelessChannel channel : config.getAllChannels()) {
                            total += channel.getReceivers().size();
                        }
                        return total;
                    }
                });
                signGraph.addPlotter(new Metrics.Plotter("Screens") {
                    @Override
                    public int getValue() {
                        int total = 0;
                        for (WirelessChannel channel : config.getAllChannels()) {
                            total += channel.getScreens().size();
                        }
                        return total;
                    }
                });

                final Metrics.Graph storageGraph = metrics.createGraph("Storage used");
                storageGraph.addPlotter(new Metrics.Plotter("SQLite") {
                    @Override
                    public int getValue() {
                        if (config.getSaveOption().equalsIgnoreCase("SQLITE")) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });

                storageGraph.addPlotter(new Metrics.Plotter("MySQL") {
                    @Override
                    public int getValue() {
                        if (config.getSaveOption().equalsIgnoreCase("MYSQL")) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });

                storageGraph.addPlotter(new Metrics.Plotter("Yaml") {
                    @Override
                    public int getValue() {
                        if (config.getSaveOption().equalsIgnoreCase("YAML") || config.getSaveOption().equalsIgnoreCase("YML")) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });

                final Metrics.Graph receiverTypesProportion = metrics
                        .createGraph("Different types of receivers");

                receiverTypesProportion.addPlotter(new Metrics.Plotter("Default") {
                    @Override
                    public int getValue() {
                        int total = 0;
                        for (WirelessChannel channel : config.getAllChannels()) {
                            total += channel.getReceiversOfType(WirelessReceiver.Type.Default).size();
                        }
                        return total;
                    }
                });
                receiverTypesProportion.addPlotter(new Metrics.Plotter("Inverters") {
                    @Override
                    public int getValue() {
                        int total = 0;
                        for (WirelessChannel channel : config.getAllChannels()) {
                            total += channel.getReceiversOfType(WirelessReceiver.Type.Inverter).size();
                        }
                        return total;
                    }
                });
                receiverTypesProportion.addPlotter(new Metrics.Plotter("Delayers") {
                    @Override
                    public int getValue() {
                        int total = 0;
                        for (WirelessChannel channel : config.getAllChannels()) {
                            total += channel.getReceiversOfType(WirelessReceiver.Type.Delayer).size();
                        }
                        return total;
                    }
                });
                receiverTypesProportion.addPlotter(new Metrics.Plotter("Clocks") {
                    @Override
                    public int getValue() {
                        int total = 0;
                        for (WirelessChannel channel : config.getAllChannels()) {
                            total += channel.getReceiversOfType(WirelessReceiver.Type.Clock).size();
                        }
                        return total;
                    }
                });
                receiverTypesProportion.addPlotter(new Metrics.Plotter("Switchers") {
                    @Override
                    public int getValue() {
                        int total = 0;
                        for (WirelessChannel channel : config.getAllChannels()) {
                            total += channel.getReceiversOfType(WirelessReceiver.Type.Switch).size();
                        }
                        return total;
                    }
                });

                final Metrics.Graph permissionsGraph = metrics
                        .createGraph("Plugin used for Permissions");
                permissionsGraph.addPlotter(new Metrics.Plotter("Vault") {
                    @Override
                    public int getValue() {
                        if (permissions.permPlugin.equalsIgnoreCase("Vault"))
                            return 1;
                        else
                            return 0;
                    }
                });

                permissionsGraph.addPlotter(new Metrics.Plotter("PermissionsEx") {
                    @Override
                    public int getValue() {
                        if (permissions.permPlugin.equalsIgnoreCase("PermissionsEx"))
                            return 1;
                        else
                            return 0;
                    }
                });

                permissionsGraph.addPlotter(new Metrics.Plotter("PermissionsBukkit") {
                    @Override
                    public int getValue() {
                        if (permissions.permPlugin.equalsIgnoreCase("PermissionsBukkit"))
                            return 1;
                        else
                            return 0;
                    }
                });

                permissionsGraph.addPlotter(new Metrics.Plotter("bPermissions") {
                    @Override
                    public int getValue() {
                        if (permissions.permPlugin.equalsIgnoreCase("bPermissions"))
                            return 1;
                        else
                            return 0;
                    }
                });

                permissionsGraph.addPlotter(new Metrics.Plotter("GroupManager") {
                    @Override
                    public int getValue() {
                        if (permissions.permPlugin.equalsIgnoreCase("GroupManager"))
                            return 1;
                        else
                            return 0;
                    }
                });
                metrics.start();

                permissionsGraph.addPlotter(new Metrics.Plotter("Bukkit OP Permissions") {
                    @Override
                    public int getValue() {
                        if (permissions.permPlugin.equalsIgnoreCase("Bukkit OP Permissions"))
                            return 1;
                        else
                            return 0;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Loading finished !
        System.out.println(pdFile.getName() + " version " + pdFile.getVersion()
                + " is enabled!");
    }

    /**
     * Load the chunks which contain a wireless sign.
     */
    public void LoadChunks() {
        if (WirelessRedstone.config.isCancelChunkUnloads()) {
            for (IWirelessPoint point : cache.getAllSigns()) {
                Location location = point.getLocation();
                if (location.getWorld() == null)
                    continue; // world currently not loaded.

                Chunk center = location.getBlock().getChunk();
                World world = center.getWorld();
                int range = WirelessRedstone.config.getChunkUnloadRange();
                for (int dx = -(range); dx <= range; dx++) {
                    for (int dz = -(range); dz <= range; dz++) {
                        Chunk chunk = world.getChunkAt(center.getX() + dx,
                                center.getZ() + dz);
                        world.loadChunk(chunk);
                    }
                }
            }
        }
    }

    public static String getBukkitVersion() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static boolean sameLocation(Location loc1, Location loc2){
        return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ() && loc1.getWorld() == loc2.getWorld();
    }

    /**
     * Returns this object
     *
     * @return plugin
     */
    public WirelessRedstone getPlugin() {
        return this;
    }

    /**
     * Returns this object
     *
     * @return plugin
     */
    public static WirelessRedstone getInstance() {
        return instance;
    }
}
