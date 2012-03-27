package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.entity.Player;

import de.bananaco.bpermissions.imp.Permissions;

public class BPermissions implements IPermissions
{
	public static WirelessRedstone plugin;
	
	public BPermissions(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
	}
	
	@Override
	public boolean hasPermission(Player base, String node)
	{
		return Permissions.hasPermission(base, node);
	}

}
