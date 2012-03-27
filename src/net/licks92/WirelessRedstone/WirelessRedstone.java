package net.licks92.WirelessRedstone;


import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.licks92.WirelessRedstone.Permissions.*;
import net.milkbowl.vault.permission.Permission;

public class WirelessRedstone extends JavaPlugin
{
	public static NewWirelessConfiguration config;
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
		config = new NewWirelessConfiguration(this);
		listener = new WirelessListener(this);
		PluginManager pm = getServer().getPluginManager();
		WirelessRedstone.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is loading...");
		WirelessRedstone.logger.setLogLevel(config.getLogLevel());
		WirelessRedstone.logger.fine("Loading Permissions...");
		Plugin vaultPlugin = pm.getPlugin("Vault");
		Plugin permissionsEx = pm.getPlugin("PermissionsEx");
		Plugin bPermissions = pm.getPlugin("bPermissions");

		// Choosing Permissions it need to be used. Stolen from Essentials.
		// Credits to Essentials Team
		if (vaultPlugin != null && config.getVaultUsage())
		{
			this.permissionsHandler = new Vault(this);
			WirelessRedstone.logger.info("Using Vault !");
		}
		/*else if(PermissionsEX != null)
		{
			this.permissionsHandler = new PermissionsEX(this);
			WirelessRedstone.logger.info("Using PermissionsEx !");
		}
		else if(bPermissions != null)
		{
			this.permissionsHandler = new bPermsPermissions(this);
			WirelessRedstone.logger.info("Using bPermissions !");
		}*/
		else
		{
			WirelessRedstone.logger.info("Any of the supported permissions plugins has been detected! Defaulting to OP/Config files!.");
			this.permissionsHandler = new opPermissions(this);
		}

		WirelessRedstone.logger.fine("Loaded Permissions...");
		config.save();

		this.WireBox.UpdateChacheNoThread();

		WirelessRedstone.logger.fine("Registering commands...");
		getCommand("wrhelp").setExecutor(new WirelessCommands(this));
		getCommand("wrr").setExecutor(new WirelessCommands(this));
		getCommand("wrt").setExecutor(new WirelessCommands(this));
		getCommand("wrremove").setExecutor(new WirelessCommands(this));
		getCommand("wrc").setExecutor(new WirelessCommands(this));
		getCommand("wrlist").setExecutor(new WirelessCommands(this));
		getCommand("wri").setExecutor(new WirelessCommands(this));
		//getCommand("wr").setExecutor(new WirelessCommands(this));

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
