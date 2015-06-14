package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.entity.Player;

public class SuperPerms implements IPermissions {
    public SuperPerms(WirelessRedstone plugin) {
        // Nothing to do
    }

    @Override
    public boolean hasPermission(Player base, String node) {
        return base.hasPermission(node);
    }

}
