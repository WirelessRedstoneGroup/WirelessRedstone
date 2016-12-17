package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Purge database", usage = "", aliases = {"purge"},
        permission = "purgedata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminPurge extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (WirelessRedstone.getStorage().purgeData()) {
            Utils.sendFeedback(WirelessRedstone.getStrings().purgeDataDone, sender, false);
        } else {
            Utils.sendFeedback(WirelessRedstone.getStrings().purgeDataFailed, sender, true);
        }
    }
}
