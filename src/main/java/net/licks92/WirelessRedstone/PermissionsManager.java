package net.licks92.WirelessRedstone;

import org.bukkit.entity.Player;

public class PermissionsManager {

    //Create
    private final String canCreateReceiver = "wirelessredstone.create.receiver";
    private final String canCreateTransmitter = "wirelessredstone.create.transmitter";
    private final String canCreateScreen = "wirelessredstone.create.screen";
    //Remove
    private final String canRemoveReceiver = "wirelessredstone.remove.receiver";
    private final String canRemoveTransmitter = "wirelessredstone.remove.transmitter";
    private final String canRemoveScreen = "wirelessredstone.remove.screen";
    //Commands
    private final String canRemoveChannel = "wirelessredstone.commands.removechannel";
    private final String canUseListCommand = "wirelessredstone.commands.list";
    private final String canSeeHelp = "wirelessredstone.commands.help";
    private final String canSeeChannelInfo = "wirelessredstone.commands.info";
    private final String canLockChannel = "wirelessredstone.commands.lock";
    private final String canActivateChannel = "wirelessredstone.commands.activate";
    private final String canSeeVersion = "wirelessredstone.commands.version";
    private final String canTeleportToSign = "wirelessredstone.commands.tp";
    //Admin
    private final String isWirelessAdmin = "wirelessredstone.admin.isAdmin";
    private final String canWipeData = "wirelessredstone.admin.wipedata";
    private final String canBackupData = "wirelessredstone.admin.backupdata";
    private final String canPurgeData = "wirelessredstone.admin.purgedata";
    private final String canConvertData = "wirelessredstone.admin.convertdata";
    private final String canRestoreData = "wirelessredstone.admin.restoredata";

    public boolean canCreateReceiver(Player player) {
        return player.hasPermission(canCreateReceiver);
    }

    public boolean canCreateTransmitter(Player player) {
        return player.hasPermission(canCreateTransmitter);
    }

    public boolean canCreateScreen(Player player) {
        return player.hasPermission(canCreateScreen);
    }

    public boolean canRemoveReceiver(Player player) {
        return player.hasPermission(canRemoveReceiver);
    }

    public boolean canRemoveTransmitter(Player player) {
        return player.hasPermission(canRemoveTransmitter);
    }

    public boolean canRemoveScreen(Player player) {
        return player.hasPermission(canRemoveScreen);
    }

    public boolean isWirelessAdmin(Player player) {
        return player.hasPermission(isWirelessAdmin);
    }

    public boolean canRemoveChannel(Player player) {
        return player.hasPermission(canRemoveChannel);
    }

    public boolean canUseListCommand(Player player) {
        return player.hasPermission(canUseListCommand);
    }

    public boolean canSeeHelp(Player player) {
        return player.hasPermission(canSeeHelp);
    }

    public boolean canSeeChannelInfo(Player player) {
        return player.hasPermission(canSeeChannelInfo);
    }

    public boolean canLockChannel(Player player) {
        return player.hasPermission(canLockChannel);
    }

    public boolean canWipeData(Player player) {
        return player.hasPermission(canWipeData);
    }
    public boolean canConvertData(Player player) {
        return player.hasPermission(canConvertData);
    }

    public boolean canBackupData(Player player) {
        return player.hasPermission(canBackupData);
    }

    public boolean canPurgeData(Player player) {
        return player.hasPermission(canPurgeData);
    }

    public boolean canRestoreData(Player player) {
        return player.hasPermission(canRestoreData);
    }

    public boolean canActivateChannel(Player player) {
        return player.hasPermission(canActivateChannel);
    }

    public boolean canSeeVersion(Player player) {
        return player.hasPermission(canSeeVersion);
    }

    public boolean canTeleportToSign(Player player) {
        return player.hasPermission(canTeleportToSign);
    }
}
