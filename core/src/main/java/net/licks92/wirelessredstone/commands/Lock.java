package net.licks92.wirelessredstone.commands;

import net.licks92.wirelessredstone.signs.WirelessChannel;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Toggle/set locked state for WirelessChannel", usage = "<channel> [state]", aliases = {"lock"},
        tabCompletion = {WirelessCommandTabCompletion.CHANNEL, WirelessCommandTabCompletion.BOOL},
        permission = "lock", canUseInConsole = true, canUseInCommandBlock = true)
public class Lock extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        String cname = args[0];
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(cname);
        if (channel == null) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNotFound, sender, true);
            return;
        }

        boolean newState = !channel.isLocked();
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))
                newState = args[1].equalsIgnoreCase("true");
        }

        channel.setLocked(newState);
        WirelessRedstone.getStorage().updateChannel(args[0], channel);
        Utils.sendFeedback(newState ? WirelessRedstone.getStrings().channelLocked : WirelessRedstone.getStrings().channelUnlocked, sender, false);
    }
}