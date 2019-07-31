package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Backup all wirelesschannels", usage = "", aliases = {"backup"},
        permission = "backupdata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminBackup extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (WirelessRedstone.getStorage().backupData()) {
            Utils.sendFeedback(WirelessRedstone.getStrings().dbBackupDone, sender, false);
        } else {
            Utils.sendFeedback(WirelessRedstone.getStrings().dbBackupFailed, sender, true);
        }
    }
}