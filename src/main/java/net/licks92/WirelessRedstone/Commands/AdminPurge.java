package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Purge database", usage = "", aliases = {"purge"},
        permission = "purgedata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminPurge extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (Main.getStorage().purgeData()) {
            Utils.sendFeedback(Main.getStrings().purgeDataDone, sender, false);
        } else {
            Utils.sendFeedback(Main.getStrings().purgeDataFailed, sender, true);
        }
    }
}
