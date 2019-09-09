package net.licks92.wirelessredstone.commands.Admin;

import net.licks92.wirelessredstone.commands.CommandInfo;
import net.licks92.wirelessredstone.commands.WirelessCommand;
import net.licks92.wirelessredstone.commands.WirelessCommandTabCompletion;
import net.licks92.wirelessredstone.signs.WirelessChannel;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Remove owner from WirelessChannel", usage = "<channel> <playername>", aliases = {"removeowner"},
        tabCompletion = {WirelessCommandTabCompletion.CHANNEL, WirelessCommandTabCompletion.PLAYER},
        permission = "removeOwner", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminRemoveOwner extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        String channelName = args[0];
        String playerName = args[1];

        if (!hasAccessToChannel(sender, channelName)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, sender, true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        if (channel == null) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNotFound, sender, true);
            return;
        }

        if (!channel.getOwners().contains(playerName)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelAlreadyOwner, sender, true);
            return;
        }

        channel.removeOwner(playerName);
        WirelessRedstone.getStorage().updateChannel(channelName, channel);

        WirelessRedstone.getWRLogger().info("Channel " + channelName + " has been updated. Player " + playerName + " has been removed to the owner list.");
        Utils.sendFeedback(WirelessRedstone.getStrings().channelOwnerRemoved.replaceAll("%%PLAYERNAME", playerName), sender, false);
    }
}
