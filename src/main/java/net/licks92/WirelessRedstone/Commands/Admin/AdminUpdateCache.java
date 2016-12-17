package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Update cache", usage = "", aliases = {"updatecache", "updatec"},
        permission = "updatecache", canUseInConsole = true, canUseInCommandBlock = true)
public class AdminUpdateCache extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        WirelessRedstone.getGlobalCache().update(false);

        Utils.sendFeedback("Cache updated", sender, false);
    }
}
