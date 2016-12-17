package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Remove WirelessChannel", usage = "<channel>", aliases = {"remove"},
        permission = "remove", canUseInConsole = true, canUseInCommandBlock = false)
public class Remove extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().tooFewArguments, sender, true);
            return;
        }

        if (WirelessRedstone.getStorage().getWirelessChannel(args[0]) == null) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        if(!hasAccessToChannel(sender, args[0])){
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            return;
        }

        WirelessRedstone.getStorage().removeWirelessChannel(args[0]);
        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelRemoved, sender, false);
    }
}
