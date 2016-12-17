package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Add owner to WirlessChannel", usage = "<channel> <playername>", aliases = {"addowner"},
        permission = "addOwner", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminAddOwner extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().tooFewArguments, sender, true);
            return;
        }

        String channelName = args[0];
        String playerName = args[1];

        if (!hasAccessToChannel(sender, channelName)) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(channelName);
        if (channel == null) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        if(channel.getOwners().contains(playerName)){
            WirelessRedstone.getUtils().sendFeedback("Player is already an owner.", sender, true); //TODO: Add string to stringloader
            return;
        }

        channel.addOwner(playerName);
        WirelessRedstone.getStorage().updateChannel(channelName, channel);

        WirelessRedstone.getWRLogger().info("Channel " + channelName + " has been updated. Player " + playerName + " has been added to the owner list.");
        WirelessRedstone.getUtils().sendFeedback("User " + playerName + " is added to channel " + channelName, sender, false);  //TODO: Add string to stringloader
    }
}
