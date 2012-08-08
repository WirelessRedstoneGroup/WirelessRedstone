package net.licks92.WirelessRedstone.Permissions;

import org.bukkit.entity.Player;

public interface IPermissions
{
	boolean hasPermission(Player base, String node);
}
