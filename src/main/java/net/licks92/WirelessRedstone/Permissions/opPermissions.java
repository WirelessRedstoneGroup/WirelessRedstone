package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.entity.Player;

public class opPermissions implements IPermissions
{
	public opPermissions(WirelessRedstone wirelessRedstone)
	{
		
	}

	@Override
	public boolean hasPermission(Player base, String node)
	{
		if (base.isOp())
		{
			return true;
		}
		return false;
	}

}
