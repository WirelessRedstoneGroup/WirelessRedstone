package net.licks92.WirelessRedstone;


import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.licks92.WirelessRedstone.Permissions.*;
import net.milkbowl.vault.permission.Permission;

public class WirelessRedstone extends JavaPlugin
{
	public static WirelessConfiguration config;
	private static StackableLogger logger = new StackableLogger("WirelessRedstone");
	public WireBox WireBox = new WireBox(this);
	public IPermissions permissionsHandler;
	public static Permission perms = null;
	public WirelessListener listener = null;

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
		config = new WirelessConfiguration(this.getDataFolder());
		listener = new WirelessListener(this);
		PluginManager pm = getServer().getPluginManager();
		WirelessRedstone.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is loading...");
		WirelessRedstone.logger.setLogLevel(config.getLogLevel());
		WirelessRedstone.logger.fine("Loading Permissions...");
		Plugin vaultPlugin = pm.getPlugin("Vault");

		// Choosing Permissions it need to be used. Stolen from Essentials.
		// Credits to Essentials Team
		if (vaultPlugin != null)
		{
			this.permissionsHandler = new VaultPermissions(this);
			WirelessRedstone.logger.info("Using Vault");
		}
		else
		{
			WirelessRedstone.logger.info("Vault hasn't been detected! Defaulting to OP/Config files!.");
			this.permissionsHandler = new opPermissions(this);
		}

		WirelessRedstone.logger.fine("Loaded Permissions...");
		config.save();

		this.WireBox.UpdateChacheNoThread();

		/* Deprecated since CB 1.1-R5
		WirelessRedstone.logger.info("Registering Events...");
		pm.registerEvent(Event.Type.SIGN_CHANGE, this.blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.REDSTONE_CHANGE, this.blockListener,
				Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.CHUNK_UNLOAD, this.worldListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_FROMTO, this.blockListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, this.blockListener,
				Event.Priority.Normal, this);*/

		WirelessRedstone.logger.fine("Registering commands...");
		getCommand("wrhelp").setExecutor(new WirelessCommands(this));
		getCommand("wrr").setExecutor(new WirelessCommands(this));
		getCommand("wrt").setExecutor(new WirelessCommands(this));
		getCommand("wrremove").setExecutor(new WirelessCommands(this));
		getCommand("wrc").setExecutor(new WirelessCommands(this));
		getCommand("wrlist").setExecutor(new WirelessCommands(this));
		getCommand("wri").setExecutor(new WirelessCommands(this));

		WirelessRedstone.logger.fine("Loading Chunks");
		LoadChunks();

		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");
	}

	public void LoadChunks() {
		if (WirelessRedstone.config.isCancelChunkUnloads()) {
			for (IWirelessPoint loc : this.WireBox.getAllSigns()) {
				Location location = WireBox.getPointLocation(loc);
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
