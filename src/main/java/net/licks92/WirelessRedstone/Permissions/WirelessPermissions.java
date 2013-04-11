package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class WirelessPermissions
{
	public IPermissions permissionsHandler;
	public String permPlugin;
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
	private String canBackupData = "wirelessredstone.admin.backupdata";
	private String canActivateChannel = "wirelessredstone.commands.activate";
	
	public WirelessPermissions(WirelessRedstone plugin)
	{
		PluginManager pm = plugin.getServer().getPluginManager();

		// Choosing Permissions it need to be used. Stolen from Essentials.
		// Credits to Essentials Team
		if (pm.getPlugin("Vault") != null && WirelessRedstone.config.getVaultUsage())
		{
			this.permissionsHandler = new Vault(plugin);
			permPlugin = "Vault";
			WirelessRedstone.getWRLogger().info("Using Vault for permissions !");
		}
		else if(pm.getPlugin("PermissionsEx") != null)
		{
			this.permissionsHandler = new SuperPerms(plugin);
			permPlugin = "PermissionsEx";
			WirelessRedstone.getWRLogger().info("Using PermissionsEx for permissions !");
		}
		else if(pm.getPlugin("PermissionsBukkit") != null)
		{
			this.permissionsHandler = new SuperPerms(plugin);
			permPlugin = "PermissionsBukkit";
			WirelessRedstone.getWRLogger().info("Using PermissionsBukkit for permissions !");
		}
		else if(pm.getPlugin("bPermissions") != null)
		{
			this.permissionsHandler = new SuperPerms(plugin);
			permPlugin = "bPermissions";
			WirelessRedstone.getWRLogger().info("Using bPermissions for permissions !");
		}
		else if(pm.getPlugin("GroupManager") != null)
		{
			this.permissionsHandler = new SuperPerms(plugin);
			permPlugin = "GroupManager";
			WirelessRedstone.getWRLogger().info("Using GroupManager for permissions !");
		}
		else
		{
			WirelessRedstone.getWRLogger().info("None of the supported permissions plugins has been detected! Defaulting to OP/Config files!");
			permPlugin = "Bukkit OP Permissions";
			this.permissionsHandler = new opPermissions(plugin);
		}

		WirelessRedstone.getWRLogger().info("Loaded Permissions...");
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
	
	public boolean canBackupData(Player player)
	{
		return permissionsHandler.hasPermission(player, canBackupData);
	}

	public boolean canActivateChannel(Player player)
	{
		return permissionsHandler.hasPermission(player, canActivateChannel );
	}
}
