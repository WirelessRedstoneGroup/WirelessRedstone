package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Toggle/set locked state for WirelessChannel", usage = "<channel> [state]", aliases = {"lock"},
        permission = "commands.lock", canUseInConsole = true, canUseInCommandBlock = true)
public class Lock extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
            return;
        }

        String cname = args[0];
        WirelessChannel channel = Main.getStorage().getWirelessChannel(cname);
        if (channel == null) {
            Utils.sendFeedback(Main.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        Boolean newState = !channel.isLocked();
        if(args.length >= 2) {
            if(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))
                newState = args[1].equalsIgnoreCase("true");
        }

        channel.setLocked(newState);
        Main.getStorage().updateChannel(args[0], channel);
        Utils.sendFeedback("Successfully changed locked state to " + newState + ".", sender, false); //TODO: Add this string to the stringloader
    }
}
