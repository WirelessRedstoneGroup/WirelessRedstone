package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Get admin help page", usage = "<page>", aliases = {"help", "h"},
        permission = "isAdmin", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminHelp extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {

    }
}
