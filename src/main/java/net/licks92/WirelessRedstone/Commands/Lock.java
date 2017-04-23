package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Toggle/set locked state for WirelessChannel", usage = "<channel> [state]", aliases = {"lock"},
        permission = "lock", canUseInConsole = true, canUseInCommandBlock = true)
public class Lock extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        String cname = args[0];
        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(cname);
        if (channel == null) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelNotFound, sender, true);
            return;
        }

        Boolean newState = !channel.isLocked();
        if(args.length >= 2) {
            if(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))
                newState = args[1].equalsIgnoreCase("true");
        }

        channel.setLocked(newState);
        WirelessRedstone.getStorage().updateChannel(args[0], channel);
        WirelessRedstone.getUtils()
                .sendFeedback(newState ? WirelessRedstone.getStrings().channelLocked : WirelessRedstone.getStrings().channelUnlocked, sender, false);
    }
}
