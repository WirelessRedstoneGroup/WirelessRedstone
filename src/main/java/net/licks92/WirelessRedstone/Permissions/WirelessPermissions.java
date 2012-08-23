package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
	
	public WirelessPermissions(WirelessRedstone plugin)
	{
		Plugin vaultPlugin = plugin.getServer().getPluginManager().getPlugin("Vault");

		// Choosing Permissions it need to be used. Stolen from Essentials.
		// Credits to Essentials Team
		if (vaultPlugin != null && WirelessRedstone.config.getVaultUsage())
		{
			this.permissionsHandler = new Vault(plugin);
			WirelessRedstone.getStackableLogger().info("Using Vault !");
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
}
