package net.licks92.wirelessredstone.commands.Admin;

import net.licks92.wirelessredstone.commands.CommandInfo;
import net.licks92.wirelessredstone.commands.WirelessCommand;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Purge database", usage = "", aliases = {"purge"},
        permission = "purgedata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminPurge extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (WirelessRedstone.getStorage().purgeData() >= 0) {
            Utils.sendFeedback(WirelessRedstone.getStrings().dbPurgeDone, sender, false);
        } else {
            Utils.sendFeedback(WirelessRedstone.getStrings().dbPurgeFailed, sender, true);
        }
    }
}