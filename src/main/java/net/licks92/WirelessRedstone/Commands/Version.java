package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Get wirelessredstone version", usage = "", aliases = {"version", "v"},
        permission = "version", canUseInConsole = true, canUseInCommandBlock = true)
public class Version extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        WirelessRedstone.getUtils().sendFeedback(
                WirelessRedstone.getStrings().commandVersion.replaceAll("%%VERSION", WirelessRedstone.getInstance().getDescription().getVersion()),
                sender, false);
    }
}
