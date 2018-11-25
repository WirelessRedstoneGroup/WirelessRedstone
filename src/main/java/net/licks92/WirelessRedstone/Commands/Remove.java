package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Remove WirelessChannel", usage = "<channel> [remove signs]", aliases = {"remove"},
        permission = "remove", canUseInConsole = true, canUseInCommandBlock = false)
public class Remove extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        if (WirelessRedstone.getStorageManager().getChannel(args[0]) == null) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNotFound, sender, true);
            return;
        }

        if(!hasAccessToChannel(sender, args[0])){
            Utils.sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, sender, true);
            return;
        }

        boolean removeSigns = false;
        if(args.length >= 2) {
            if(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))
                removeSigns = args[1].equalsIgnoreCase("true");
        }

        WirelessRedstone.getStorage().removeChannel(args[0], removeSigns);
        Utils.sendFeedback(WirelessRedstone.getStrings().channelRemoved, sender, false);
    }
}