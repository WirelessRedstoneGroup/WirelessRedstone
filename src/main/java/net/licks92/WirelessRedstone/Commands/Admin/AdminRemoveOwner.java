package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Remove owner from WirelessChannel", usage = "<channel> <playername>", aliases = {"removeowner"},
        permission = "removeOwner", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminRemoveOwner extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
            return;
        }

        String channelName = args[0];
        String playerName = args[1];

        if (!hasAccessToChannel(sender, channelName)) {
            Utils.sendFeedback(Main.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            return;
        }

        WirelessChannel channel = Main.getStorage().getWirelessChannel(channelName);
        if (channel == null) {
            Utils.sendFeedback(Main.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        if(!channel.getOwners().contains(playerName)){
            Utils.sendFeedback("Player is not an owner.", sender, true); //TODO: Add string to stringloader
            return;
        }

        channel.removeOwner(playerName);
        Main.getStorage().updateChannel(channelName, channel);

        Main.getWRLogger().info("Channel " + channelName + " has been updated. Player " + playerName + " has been removed to the owner list.");
        Utils.sendFeedback("User " + playerName + " is remove from channel " + channelName, sender, false);  //TODO: Add string to stringloader
    }
}
