package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
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
                Utils.sendFeedback(Main.getStrings().backupFailed, sender, true);
                return;
        }

        if (Main.getStorage().backupData(extension)) {
            Utils.sendFeedback(Main.getStrings().backupDone, sender, false);
        } else {
            Utils.sendFeedback(Main.getStrings().backupFailed, sender, true);
        }
    }
}
