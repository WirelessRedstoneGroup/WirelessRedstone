package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Backup all wirelesschannels", usage = "", aliases = {"backup"},
        permission = "backupdata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminBackup extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String extension;

        switch (ConfigManager.getConfig().getStorageType()){
            case YAML:
                extension = "yml";
                break;
            case SQLITE:
                extension = "db";
                break;
            default:
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().dbBackupFailed, sender, true);
                return;
        }

        if (WirelessRedstone.getStorage().backupData(extension)) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().dbBackupDone, sender, false);
        } else {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().dbBackupFailed, sender, true);
        }
    }
}
