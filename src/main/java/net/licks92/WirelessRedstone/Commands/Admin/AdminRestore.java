package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Restore database from latest backup", usage = "", aliases = {"restore"},
        permission = "restoredata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminRestore extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {

    }
}
