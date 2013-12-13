package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Configuration.WirelessConfiguration;
import net.licks92.WirelessRedstone.Listeners.WirelessBlockListener;
import net.licks92.WirelessRedstone.Listeners.WirelessPlayerListener;
import net.licks92.WirelessRedstone.Listeners.WirelessWorldListener;
import net.licks92.WirelessRedstone.Permissions.WirelessPermissions;
import net.licks92.WirelessRedstone.Strings.WirelessYMLStringsLoader;
import net.licks92.WirelessRedstone.Strings.WirelessStrings;
import net.licks92.WirelessRedstone.Strings.WirelessXMLStringsLoader;
import net.licks92.WirelessRedstone.Utils.Metrics;
import net.licks92.WirelessRedstone.Utils.Metrics.Graph;
import net.licks92.WirelessRedstone.Utils.Metrics.Plotter;
import net.licks92.WirelessRedstone.Utils.Updater;
import net.licks92.WirelessRedstone.Utils.Updater.UpdateResult;
import net.licks92.WirelessRedstone.Utils.Updater.UpdateType;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * This class is the main class of the plugin. It controls the Configuration, the Listeners,
 * it sends the metrics and controls the actions when enabling / disabling.
 * 
 * @author licks92
 * 
 * @version 1.8.4b
 */
public class WirelessRedstone extends JavaPlugin
{
	public static WirelessConfiguration config;
	public static WirelessStrings strings;
	public static WirelessGlobalCache cache;
	private WirelessXMLStringsLoader stringLoader;
	private static WRLogger logger;
	public static WireBox WireBox;
	public WirelessPermissions permissions;
	public static Permission perms;
	public WirelessWorldListener worldlistener;
	public WirelessBlockListener blocklistener;
	public WirelessPlayerListener playerlistener;
	private static Metrics metrics;
	private BukkitTask updateChecker;
	public Updater updater;
	
	/**
	 * Wireless Redstone logger
	 * 
	 * @return logger
	 */
	public static WRLogger getWRLogger()
	{
		return logger;
	}
	
	
	/**
	 * Calls the actions to do when disabling the plugin.
	 */
	@Override
	public void onDisable()
	{
		try
		{
			config.close();
			updateChecker.cancel();
		} catch (NullPointerException ex) {
			
		}
	}
	
	/**
	 * Calls the actions to do when enabling the plugin (i.e when starting the server)
	 */
	@Override
	public void onEnable()
	{
		PluginDescriptionFile pdFile = getDescription();
		
		WireBox = new WireBox(this);
		config = new WirelessConfiguration(this);
		if(config.getDebugMode())
		{
			logger = new WRLogger("[WirelessRedstone]", getServer().getConsoleSender(), true, config.getColourfulLogging());
			logger.info("Debug Mode activated !");
			logger.info("Log level set to FINEST because of the debug mode");
		}
		else
		{
			logger = new WRLogger("[WirelessRedstone]", this.getServer().getConsoleSender(), false, config.getColourfulLogging());
		}
		config.initStorage();
		cache = new WirelessGlobalCache(this, config.getCacheRefreshFrequency());
		
		WirelessRedstone.logger.info(pdFile.getName() + " version " + pdFile.getVersion() + " is loading...");
		
		updater = new Updater(this, 37345, getFile(), UpdateType.NO_DOWNLOAD, true);
		
		if(config.doCheckForUpdates())
		{
			updateChecker = this.getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if(updater.getResult() == UpdateResult.UPDATE_AVAILABLE)
						{
							getWRLogger().info(WirelessRedstone.strings.newUpdateAvailable);
						}
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			}, 0, 24000/50);
		}
		
		//Load strings
		stringLoader = new WirelessXMLStringsLoader(this, config.getLanguage());
		strings = stringLoader.getStrings();
		
		//Load listeners
		worldlistener = new WirelessWorldListener(this);
		blocklistener = new WirelessBlockListener(this);
		playerlistener = new WirelessPlayerListener(this);
		
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

		WirelessRedstone.logger.info("Loading Chunks...");
		LoadChunks();
		
		//Metrics
		try
		{
			metrics = new Metrics(this);
			
			// Channel metrics
			final net.licks92.WirelessRedstone.Utils.Metrics.Graph channelGraph = metrics.createGraph("Channel metrics");
			channelGraph.addPlotter(new Plotter("Total channels")
			{
				@Override
				public int getValue()
				{
					return config.getAllChannels().size();
				}
			});
			channelGraph.addPlotter(new Plotter("Total signs")
			{
				@Override
				public int getValue()
				{
					return cache.getAllSigns().size();
				}
			});
			
			// Sign Metrics
			final Graph signGraph = metrics.createGraph("Sign metrics");
			signGraph.addPlotter(new Plotter("Transmitters")
			{
				@Override
				public int getValue()
				{
					int total = 0;
					for(WirelessChannel channel : config.getAllChannels())
					{
						total+=channel.getTransmitters().size();
					}
					return total;
				}
			});
			signGraph.addPlotter(new Plotter("Receivers")
			{
				@Override
				public int getValue()
				{
					int total = 0;
					for(WirelessChannel channel : config.getAllChannels())
					{
						total+=channel.getReceivers().size();
					}
					return total;
				}
			});
			signGraph.addPlotter(new Plotter("Screens")
			{
				@Override
				public int getValue()
				{
					int total = 0;
					for(WirelessChannel channel : config.getAllChannels())
					{
						total+=channel.getScreens().size();
					}
					return total;
				}
			});
			
			final Graph storageGraph = metrics.createGraph("Storage used");
			storageGraph.addPlotter(new Plotter("SQL")
			{
				@Override
				public int getValue()
				{
					if(config.getSQLUsage())
					{
						return 1;
					}
					else
					{
						return 0;
					}
				}
			});
			
			storageGraph.addPlotter(new Plotter("Yaml files")
			{
				@Override
				public int getValue()
				{
					if(!config.getSQLUsage())
					{
						return 1;
					}
					else
					{
						return 0;
					}
				}
			});
			
			final Graph permissionsGraph = metrics.createGraph("Plugin used for Permissions");
			permissionsGraph.addPlotter(new Plotter("Vault")
			{
				@Override
				public int getValue()
				{
					if(permissions.permPlugin == "Vault")
						return 1;
					else
						return 0;
				}
			});
			
			permissionsGraph.addPlotter(new Plotter("PermissionsEx")
			{
				@Override
				public int getValue()
				{
					if(permissions.permPlugin == "PermissionsEx")
						return 1;
					else
						return 0;
				}
			});
			
			permissionsGraph.addPlotter(new Plotter("PermissionsBukkit")
			{
				@Override
				public int getValue()
				{
					if(permissions.permPlugin == "PermissionsBukkit")
						return 1;
					else
						return 0;
				}
			});
			
			permissionsGraph.addPlotter(new Plotter("bPermissions")
			{
				@Override
				public int getValue()
				{
					if(permissions.permPlugin == "bPermissions")
						return 1;
					else
						return 0;
				}
			});
			
			permissionsGraph.addPlotter(new Plotter("GroupManager")
			{
				@Override
				public int getValue()
				{
					if(permissions.permPlugin == "GroupManager")
						return 1;
					else
						return 0;
				}
			});
			metrics.start();
			
			permissionsGraph.addPlotter(new Plotter("Bukkit OP Permissions")
			{
				@Override
				public int getValue()
				{
					if(permissions.permPlugin == "Bukkit OP Permissions")
						return 1;
					else
						return 0;
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//Loading finished !
		System.out.println(pdFile.getName() + " version " + pdFile.getVersion() + " is enabled!");
	}
	
	/**
	 * Load the chunks which contain a wireless sign.
	 */
	public void LoadChunks()
	{
		if (WirelessRedstone.config.isCancelChunkUnloads())
		{
			for (IWirelessPoint point : cache.getAllSigns())
			{
				Location location = point.getLocation();
				if(location.getWorld() == null)
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
	
	/**
	 * Returns this object
	 * 
	 * @return plugin
	 */
	public WirelessRedstone getPlugin()
	{
		return this;
	}
}
