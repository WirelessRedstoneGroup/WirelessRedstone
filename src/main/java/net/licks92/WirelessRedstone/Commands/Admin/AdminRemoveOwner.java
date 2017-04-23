package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Remove owner from WirelessChannel", usage = "<channel> <playername>", aliases = {"removeowner"},
        permission = "removeOwner", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminRemoveOwner extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        String channelName = args[0];
        String playerName = args[1];

        if (!hasAccessToChannel(sender, channelName)) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, sender, true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(channelName);
        if (channel == null) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelNotFound, sender, true);
            return;
        }

        if(!channel.getOwners().contains(playerName)){
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelAlreadyOwner, sender, true);
            return;
        }

        channel.removeOwner(playerName);
        WirelessRedstone.getStorage().updateChannel(channelName, channel);

        WirelessRedstone.getWRLogger().info("Channel " + channelName + " has been updated. Player " + playerName + " has been removed to the owner list.");
        WirelessRedstone.getUtils()
                .sendFeedback(WirelessRedstone.getStrings().channelOwnerRemoved.replaceAll("%%PLAYERNAME", playerName), sender, false);
    }
}
