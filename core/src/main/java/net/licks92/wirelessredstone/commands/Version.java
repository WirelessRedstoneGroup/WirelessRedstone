package net.licks92.wirelessredstone.commands;

import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Get WirelessRedstone version", usage = "", aliases = {"version", "v"},
        permission = "version", canUseInConsole = true, canUseInCommandBlock = true)
public class Version extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Utils.sendFeedback(WirelessRedstone.getStrings().commandVersion.replaceAll("%%VERSION", WirelessRedstone.getInstance().getDescription().getVersion()),
                sender, false);
    }
}