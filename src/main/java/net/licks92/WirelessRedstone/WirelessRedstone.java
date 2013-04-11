package net.licks92.WirelessRedstone;

import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilderFactory;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Configuration.WirelessConfiguration;
import net.licks92.WirelessRedstone.Listeners.WirelessBlockListener;
import net.licks92.WirelessRedstone.Listeners.WirelessPlayerListener;
import net.licks92.WirelessRedstone.Listeners.WirelessWorldListener;
import net.licks92.WirelessRedstone.Permissions.WirelessPermissions;
import net.licks92.WirelessRedstone.Strings.WirelessStringLoader;
import net.licks92.WirelessRedstone.Strings.WirelessStrings;
import net.licks92.WirelessRedstone.Utils.BukkitMetrics;
import net.licks92.WirelessRedstone.Utils.BukkitMetrics.Graph;
import net.licks92.WirelessRedstone.Utils.BukkitMetrics.Plotter;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;



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
	private WirelessStringLoader stringLoader;
	private static WRLogger logger;
	public static WireBox WireBox;
	public WirelessPermissions permissions;
	public static Permission perms;
	public WirelessWorldListener worldlistener;
	public WirelessBlockListener blocklistener;
	public WirelessPlayerListener playerlistener;
	private static BukkitMetrics metrics;
	private BukkitTask updateChecker;
	public double currentversion;
	public double newversion;
	
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
		config.init();
		
		WirelessRedstone.logger.info(pdFile.getName() + " version " + pdFile.getVersion() + " is loading...");
		
		currentversion = Double.valueOf(getDescription().getVersion().split("b")[0].replaceFirst("\\.", ""));
		
		/*
		 * Check for updates on the repository dev.bukkit
		 * 
		 * This code has been taken from the Vault plugin.
		 * 
		 * All credits to the developpers of Vault (http://dev.bukkit.org/server-mods/vault/);
		 */
		if(config.doCheckForUpdates())
		{
			updateChecker = this.getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						newversion = updateCheck(currentversion);
						
						logger.debug("Current version is " + currentversion + " where repository version is " + newversion);
						
						if(newversion > currentversion)
						{
							logger.info(strings.newUpdateAvailable);
						}
						else if(newversion < currentversion)
						{
							logger.debug("You are using a version that is higher than the repository version! Did you download it on the github code repo?");
						}
						else //If it's the same version...
						{
							logger.debug("You are using the same version as the official repository.");
						}
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			}, 0, 24000);
		}
		
		//Load strings
		stringLoader = new WirelessStringLoader(this, config.getLanguage());
		strings = stringLoader.getStrings();
		
		//Load listeners
		worldlistener = new WirelessWorldListener(this);
		blocklistener = new WirelessBlockListener(this);
		playerlistener = new WirelessPlayerListener(this);
		
		WirelessRedstone.logger.info("Loading Permissions...");
		
		permissions = new WirelessPermissions(this);
		config.save();

		WireBox.UpdateChacheNoThread();

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

		WirelessRedstone.logger.info("Loading Chunks...");
		LoadChunks();
		
		//Metrics
		try
		{
			metrics = new BukkitMetrics(this);
			
			// Channel metrics
			final Graph channelGraph = metrics.createGraph("Channel metrics");
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
					return WireBox.getAllSigns().size();
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
			for (IWirelessPoint loc : WireBox.getAllSigns())
			{
				Location location = WireBox.getPointLocation(loc);
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
	
	/**
	 * Gets the number of the latest version on the bukkit repository
	 * 
	 * @param currentVersion
	 * @return repoversion
	 * @throws Exception
	 */
	public double updateCheck(double currentVersion) throws Exception {
        String pluginUrlString = "http://dev.bukkit.org/server-mods/wireless-redstone/files.rss";
        try {
            URL url = new URL(pluginUrlString);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("item");
            Node firstNode = nodes.item(0);
            if (firstNode.getNodeType() == 1) {
                Element firstElement = (Element)firstNode;
                NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                Element firstNameElement = (Element) firstElementTagName.item(0);
                NodeList firstNodes = firstNameElement.getChildNodes();
                return Double.valueOf(firstNodes.item(0).getNodeValue().replace("Wireless Redstone ", "").split("b")[0].replaceFirst("\\.", "").trim());
            }
        }
        catch(SocketException ex)
        {
        	logger.warning("Could not get the informations about the latest version. Maybe your internet connection is broken?");
        }
        catch(UnknownHostException ex)
        {
        	logger.warning("Could not find the host dev.bukkit. Maybe your server can't connect to the internet.");
        }
        catch(SAXParseException ex)
        {
        	logger.warning("Could not connect to bukkitdev. It seems that something's blocking the plugin. Do you have an internet protection?");
        }
        return currentVersion;
    }
}
