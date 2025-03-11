package net.licks92.wirelessredstone.commands.Admin;

import net.licks92.wirelessredstone.commands.CommandInfo;
import net.licks92.wirelessredstone.commands.WirelessCommand;
import net.licks92.wirelessredstone.commands.WirelessCommandTabCompletion;
import net.licks92.wirelessredstone.signs.WirelessChannel;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(
        description = "Add owner to WirelessChannel",
        usage = "<channel> <playername>",
        aliases = {"addowner"},
        tabCompletion = {WirelessCommandTabCompletion.CHANNEL, WirelessCommandTabCompletion.PLAYER},
        permission = "addOwner",
        canUseInConsole = true,
        canUseInCommandBlock = false
)
public class AdminAddOwner extends WirelessCommand {

    /**
     * Handles the execution of the "addowner" command, which adds a player as an owner
     * to a specified wireless channel. The command ensures proper validation of arguments,
     * permissions, and checks before adding the owner.
     *
     * @param sender The entity executing the command. This could be a player, console, or other valid command sender.
     * @param args   An array of command arguments. The first argument is the channel name,
     *               and the second is the player's name to be added as an owner.
     */
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // Validate arguments
        if (args == null || args.length < 2) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true, null, false);
            return;
        }

        String channelName = args[0];
        String playerName = args[1];

        // Validate sender permissions and channel access
        if (!hasAccessToChannel(sender, channelName)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, sender, true, null, false);
            return;
        }

        // Fetch the channel by name
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        if (channel == null) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNotFound(channelName), sender, true, null, false);
            return;
        }

        // Check if the specified player is already an owner
        if (channel.getOwners().contains(playerName)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelAlreadyOwner(playerName), sender, true, null, false);
            return;
        }

        // Attempt to add the owner to the channel
        if (addOwnerToChannel(channel, playerName)) {
            WirelessRedstone.getStorage().updateChannel(channelName, channel);

            // Notify the sender of successful addition
            Utils.sendFeedback(
                    WirelessRedstone.getStrings().channelOwnerAdded(playerName, channelName), sender, false, null, false);
        } else {
            // Notify the sender of failure
            Utils.sendFeedback("Failed to add the owner. Please try again later.", sender, true, null, false);
        }
    }

    /**
     * Adds an owner to the channel if not already an owner.
     *
     * @param channel    The WirelessChannel object.
     * @param playerName The name of the player to add.
     * @return True if the player was successfully added, false otherwise.
     */
    private boolean addOwnerToChannel(WirelessChannel channel, String playerName) {
        try {
            channel.addOwner(playerName); // Add the player as an owner
            return true;
        } catch (IllegalStateException e) {
            WirelessRedstone.getWRLogger()
                    .warning("Failed to add '" + playerName + "' as an owner: " + e.getMessage());
            return false;
        }
    }
}