package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Add owner to WirlessChannel", usage = "<channel> <playername>", aliases = {"addowner"},
        permission = "addOwner", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminAddOwner extends WirelessCommand {

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

        if(channel.getOwners().contains(playerName)){
            Utils.sendFeedback("Player is already an owner.", sender, true); //TODO: Add string to stringloader
            return;
        }

        channel.addOwner(playerName);
        Main.getStorage().updateChannel(channelName, channel);

        Main.getWRLogger().info("Channel " + channelName + " has been updated. Player " + playerName + " has been added to the owner list.");
        Utils.sendFeedback("User " + playerName + " is added to channel " + channelName, sender, false);  //TODO: Add string to stringloader
    }
}
