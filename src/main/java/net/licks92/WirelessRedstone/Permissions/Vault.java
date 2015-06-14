package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault implements IPermissions {
    public static Permission perms = null;
    public static WirelessRedstone plugin = null;

    public Vault(WirelessRedstone r_plugin) {
        plugin = r_plugin;
        if (!setupPermissions()) {
            plugin.getLogger().warning("[Wireless Redstone] Couldn't setup Vault Permissions !");
        }
    }

    @Override
    public boolean hasPermission(Player base, String node) {
        if (perms.has(base, node)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

}
