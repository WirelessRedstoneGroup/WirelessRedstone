package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class WirelessPermissions
{
	public IPermissions permissionsHandler;
	private String canCreateReceiver = "wirelessredstone.create.receiver";
	private String canCreateTransmitter = "wirelessredstone.create.transmitter";
	private String canCreateScreen = "wirelessredstone.create.screen";
	private String canRemoveReceiver = "wirelessredstone.remove.receiver";
	private String canRemoveTransmitter = "wirelessredstone.remove.transmitter";
	private String canRemoveScreen = "wirelessredstone.remove.screen";
	private String isWirelessAdmin = "wirelessredstone.admin.isAdmin";
	private String canRemoveChannel = "wirelessredstone.commands.removechannel";
	private String canUseListCommand = "wirelessredstone.commands.list";
	private String canSeeHelp = "wirelessredstone.commands.help";
	private String canSeeChannelInfo = "wirelessredstone.commands.info";
	private String canLockChannel = "wirelessredstone.commands.lock";
	private String canWipeData = "wirelessredstone.admin.wipedata";
	
	public WirelessPermissions(WirelessRedstone plugin)
	{
		PluginManager pm = plugin.getServer().getPluginManager();

		// Choosing Permissions it need to be used. Stolen from Essentials.
		// Credits to Essentials Team
		if (pm.getPlugin("Vault") != null && WirelessRedstone.config.getVaultUsage())
		{
			this.permissionsHandler = new Vault(plugin);
			WirelessRedstone.getStackableLogger().info("Using Vault for permissions !");
		}
		else if(pm.getPlugin("PermissionsEx") != null)
		{
			WirelessRedstone.getStackableLogger().info("Using PermissionsEx for permissions !");
			this.permissionsHandler = new SuperPerms(plugin);
		}
		else if(pm.getPlugin("PermissionsBukkit") != null)
		{
			WirelessRedstone.getStackableLogger().info("Using PermissionsBukkit for permissions !");
			this.permissionsHandler = new SuperPerms(plugin);
		}
		else if(pm.getPlugin("bPermissions") != null)
		{
			WirelessRedstone.getStackableLogger().info("Using bPermissions for permissions !");
			this.permissionsHandler = new SuperPerms(plugin);
		}
		else
		{
			WirelessRedstone.getStackableLogger().info("Any of the supported permissions plugins has been detected! Defaulting to OP/Config files!");
			this.permissionsHandler = new opPermissions(plugin);
		}

		WirelessRedstone.getStackableLogger().fine("Loaded Permissions...");
	}
	
	public boolean canCreateReceiver(Player player)
	{
		return permissionsHandler.hasPermission(player, canCreateReceiver);
	}
	
	public boolean canCreateTransmitter(Player player)
	{
		return permissionsHandler.hasPermission(player, canCreateTransmitter);
	}
	
	public boolean canCreateScreen(Player player)
	{
		return permissionsHandler.hasPermission(player, canCreateScreen);
	}
	
	public boolean canRemoveReceiver(Player player)
	{
		return permissionsHandler.hasPermission(player, canRemoveReceiver);
	}

	public boolean canRemoveTransmitter(Player player)
	{
		return permissionsHandler.hasPermission(player, canRemoveTransmitter);
	}

	public boolean canRemoveScreen(Player player)
	{
		return permissionsHandler.hasPermission(player, canRemoveScreen);
	}
	
	public boolean isWirelessAdmin(Player player)
	{
		return permissionsHandler.hasPermission(player, isWirelessAdmin);
	}
	
	public boolean canRemoveChannel(Player player)
	{
		return permissionsHandler.hasPermission(player, canRemoveChannel);
	}
	
	public boolean canUseListCommand(Player player)
	{
		return permissionsHandler.hasPermission(player, canUseListCommand);
	}
	
	public boolean canSeeHelp(Player player)
	{
		return permissionsHandler.hasPermission(player, canSeeHelp);
	}
	
	public boolean canSeeChannelInfo(Player player)
	{
		return permissionsHandler.hasPermission(player, canSeeChannelInfo);
	}
	
	public boolean canLockChannel(Player player)
	{
		return permissionsHandler.hasPermission(player, canLockChannel);
	}
	
	public boolean canWipeData(Player player)
	{
		return permissionsHandler.hasPermission(player, canWipeData);
	}
}
