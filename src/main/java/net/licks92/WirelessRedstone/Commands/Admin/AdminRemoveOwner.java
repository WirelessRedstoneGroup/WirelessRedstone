package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Remove owner from WirelessChannel", usage = "<channel> <playername>", aliases = {"removeowner"},
        permission = "removeOwner", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminRemoveOwner extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.sendFeedback(WirelessRedstone.getStrings().tooFewArguments, sender, true);
            return;
        }

        String channelName = args[0];
        String playerName = args[1];

        if (!hasAccessToChannel(sender, channelName)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(channelName);
        if (channel == null) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        if(!channel.getOwners().contains(playerName)){
            Utils.sendFeedback("Player is not an owner.", sender, true); //TODO: Add string to stringloader
            return;
        }

        channel.removeOwner(playerName);
        WirelessRedstone.getStorage().updateChannel(channelName, channel);

        WirelessRedstone.getWRLogger().info("Channel " + channelName + " has been updated. Player " + playerName + " has been removed to the owner list.");
        Utils.sendFeedback("User " + playerName + " is remove from channel " + channelName, sender, false);  //TODO: Add string to stringloader
    }
}
