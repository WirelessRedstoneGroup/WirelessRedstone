package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Configuration.NewWirelessConfiguration;
import net.licks92.WirelessRedstone.Configuration.WirelessStringProvider;
import net.licks92.WirelessRedstone.Listeners.WirelessBlockListener;
import net.licks92.WirelessRedstone.Listeners.WirelessPlayerListener;
import net.licks92.WirelessRedstone.Listeners.WirelessWorldListener;
import net.licks92.WirelessRedstone.Permissions.IPermissions;
import net.licks92.WirelessRedstone.Permissions.Vault;
import net.licks92.WirelessRedstone.Permissions.WirelessPermissions;
import net.licks92.WirelessRedstone.Permissions.opPermissions;
import net.licks92.WirelessRedstone.Utils.Metrics;
import net.licks92.WirelessRedstone.Utils.Metrics.Graph;
import net.licks92.WirelessRedstone.Utils.Metrics.Plotter;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WirelessRedstone extends JavaPlugin
{
	public static NewWirelessConfiguration config;
	public static WirelessStringProvider strings;
	private static StackableLogger logger = new StackableLogger("WirelessRedstone");
	public WireBox WireBox = new WireBox(this);
	public WirelessPermissions permissions;
	public static Permission perms;
	public WirelessWorldListener worldlistener;
	public WirelessBlockListener blocklistener;
	public WirelessPlayerListener playerlistener;
	private static Metrics metrics;

	public static StackableLogger getStackableLogger()
	{
		return logger;
	}

	@Override
	public void onDisable()
	{

	}
	

	@Override
	public void onEnable()
	{
		PluginDescriptionFile pdfFile = getDescription();
		
		//Load config and strings
		config = new NewWirelessConfiguration(this);
		strings = new WirelessStringProvider(this);
		
		//Load listeners
		worldlistener = new WirelessWorldListener(this);
		blocklistener = new WirelessBlockListener(this);
		playerlistener = new WirelessPlayerListener(this);
		
		PluginManager pm = getServer().getPluginManager();
		WirelessRedstone.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is loading...");
		WirelessRedstone.logger.setLogLevel(config.getLogLevel());
		WirelessRedstone.logger.fine("Loading Permissions...");
		
		permissions = new WirelessPermissions(this);
		if(config.getDebugMode())
		{
			logger.info("Debug Mode activated !");
		}
		config.save();

		this.WireBox.UpdateChacheNoThread();

		WirelessRedstone.logger.fine("Registering commands...");
		getCommand("wirelessredstone").setExecutor(new WirelessCommands(this));
		getCommand("wrhelp").setExecutor(new WirelessCommands(this));
		getCommand("wrr").setExecutor(new WirelessCommands(this));
		getCommand("wrt").setExecutor(new WirelessCommands(this));
		getCommand("wrs").setExecutor(new WirelessCommands(this));
		getCommand("wrremove").setExecutor(new WirelessCommands(this));
		getCommand("wra").setExecutor(new WirelessCommands(this));
		getCommand("wrlist").setExecutor(new WirelessCommands(this));
		getCommand("wri").setExecutor(new WirelessCommands(this));

		WirelessRedstone.logger.fine("Loading Chunks");
		LoadChunks();
		
		//Metrics
		try
		{
			metrics = new Metrics(this);
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
			metrics.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
	}

	public void LoadChunks()
	{
		if (WirelessRedstone.config.isCancelChunkUnloads())
		{
			for (IWirelessPoint loc : this.WireBox.getAllSigns())
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
	
	public WirelessRedstone getPlugin()
	{
		return this;
	}
}
