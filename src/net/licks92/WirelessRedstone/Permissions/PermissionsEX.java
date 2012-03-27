package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.entity.Player;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsEX implements IPermissions
{
	public static WirelessRedstone plugin;
	
	public PermissionsEX(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
	}
	
	@Override
	public boolean hasPermission(Player base, String node)
	{
		return PermissionsEx.getPermissionManager().has(base, node);
	}

}
