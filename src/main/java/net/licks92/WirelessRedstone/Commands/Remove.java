package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Remove WirelessChannel", usage = "<channel>", aliases = {"remove"},
        permission = "commands.remove", canUseInConsole = true, canUseInCommandBlock = false)
public class Remove extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
            return;
        }

        if (Main.getStorage().getWirelessChannel(args[0]) == null) {
            Utils.sendFeedback(Main.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        if(!hasAccessToChannel(sender, args[0])){
            Utils.sendFeedback(Main.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            return;
        }

        Main.getStorage().removeWirelessChannel(args[0]);
        Utils.sendFeedback(Main.getStrings().channelRemoved, sender, false);
    }
}
