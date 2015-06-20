package net.licks92.WirelessRedstone.Permissions;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class WirelessPermissions {
    public IPermissions permissionsHandler;
    public String permPlugin;
    private final String canCreateReceiver = "wirelessredstone.create.receiver";
    private final String canCreateTransmitter = "wirelessredstone.create.transmitter";
    private final String canCreateScreen = "wirelessredstone.create.screen";
    private final String canRemoveReceiver = "wirelessredstone.remove.receiver";
    private final String canRemoveTransmitter = "wirelessredstone.remove.transmitter";
    private final String canRemoveScreen = "wirelessredstone.remove.screen";
    private final String isWirelessAdmin = "wirelessredstone.admin.isAdmin";
    private final String canRemoveChannel = "wirelessredstone.commands.removechannel";
    private final String canUseListCommand = "wirelessredstone.commands.list";
    private final String canSeeHelp = "wirelessredstone.commands.help";
    private final String canSeeChannelInfo = "wirelessredstone.commands.info";
    private final String canLockChannel = "wirelessredstone.commands.lock";
    private final String canWipeData = "wirelessredstone.admin.wipedata";
    private final String canBackupData = "wirelessredstone.admin.backupdata";
    private final String canPurgeData = "wirelessredstone.admin.purgedata";
    private final String canActivateChannel = "wirelessredstone.commands.activate";
    private final String canSeeVersion = "wirelessredstone.commands.version";
    private final String canTeleportToSign = "wirelessredstone.commands.tp";

    public WirelessPermissions(final WirelessRedstone plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();

        // Choosing Permissions it need to be used. Stolen from Essentials.
        // Credits to Essentials Team
        if (pm.getPlugin("Vault") != null && WirelessRedstone.config.getVaultUsage()) {
            this.permissionsHandler = new Vault(plugin);
            permPlugin = "Vault";
            WirelessRedstone.getWRLogger().info("Using Vault for permissions !");
        } else if (pm.getPlugin("PermissionsEx") != null) {
            this.permissionsHandler = new SuperPerms(plugin);
            permPlugin = "PermissionsEx";
            WirelessRedstone.getWRLogger().info("Using PermissionsEx for permissions !");
        } else if (pm.getPlugin("PermissionsBukkit") != null) {
            this.permissionsHandler = new SuperPerms(plugin);
            permPlugin = "PermissionsBukkit";
            WirelessRedstone.getWRLogger().info("Using PermissionsBukkit for permissions !");
        } else if (pm.getPlugin("bPermissions") != null) {
            this.permissionsHandler = new SuperPerms(plugin);
            permPlugin = "bPermissions";
            WirelessRedstone.getWRLogger().info("Using bPermissions for permissions !");
        } else if (pm.getPlugin("GroupManager") != null) {
            this.permissionsHandler = new SuperPerms(plugin);
            permPlugin = "GroupManager";
            WirelessRedstone.getWRLogger().info("Using GroupManager for permissions !");
        } else {
            WirelessRedstone.getWRLogger().info("None of the supported permissions plugins has been detected! Defaulting to OP/Config files!");
            permPlugin = "Bukkit OP Permissions";
            this.permissionsHandler = new opPermissions(plugin);
        }

        WirelessRedstone.getWRLogger().debug("Loaded Permissions...");
    }

    public boolean canCreateReceiver(final Player player) {
        return permissionsHandler.hasPermission(player, canCreateReceiver);
    }

    public boolean canCreateTransmitter(final Player player) {
        return permissionsHandler.hasPermission(player, canCreateTransmitter);
    }

    public boolean canCreateScreen(final Player player) {
        return permissionsHandler.hasPermission(player, canCreateScreen);
    }

    public boolean canRemoveReceiver(final Player player) {
        return permissionsHandler.hasPermission(player, canRemoveReceiver);
    }

    public boolean canRemoveTransmitter(final Player player) {
        return permissionsHandler.hasPermission(player, canRemoveTransmitter);
    }

    public boolean canRemoveScreen(final Player player) {
        return permissionsHandler.hasPermission(player, canRemoveScreen);
    }

    public boolean isWirelessAdmin(final Player player) {
        return permissionsHandler.hasPermission(player, isWirelessAdmin);
    }

    public boolean canRemoveChannel(final Player player) {
        return permissionsHandler.hasPermission(player, canRemoveChannel);
    }

    public boolean canUseListCommand(final Player player) {
        return permissionsHandler.hasPermission(player, canUseListCommand);
    }

    public boolean canSeeHelp(final Player player) {
        return permissionsHandler.hasPermission(player, canSeeHelp);
    }

    public boolean canSeeChannelInfo(final Player player) {
        return permissionsHandler.hasPermission(player, canSeeChannelInfo);
    }

    public boolean canLockChannel(final Player player) {
        return permissionsHandler.hasPermission(player, canLockChannel);
    }

    public boolean canWipeData(final Player player) {
        return permissionsHandler.hasPermission(player, canWipeData);
    }

    public boolean canBackupData(final Player player) {
        return permissionsHandler.hasPermission(player, canBackupData);
    }

    public boolean canPurgeData(final Player player) {
        return permissionsHandler.hasPermission(player, canPurgeData);
    }

    public boolean canActivateChannel(final Player player) {
        return permissionsHandler.hasPermission(player, canActivateChannel);
    }

    public boolean canSeeVersion(final Player player) {
        return permissionsHandler.hasPermission(player, canSeeVersion);
    }

    public boolean canTeleportToSign(final Player player) {
        return permissionsHandler.hasPermission(player, canTeleportToSign);
    }
}
