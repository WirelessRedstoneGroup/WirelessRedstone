package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Activate a channel (for a certain amount of ms)", usage = "<channel> [time]", aliases = {"activate", "a"},
        permission = "activate", canUseInConsole = true, canUseInCommandBlock = true)
public class Activate extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().tooFewArguments, sender, true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(args[0]);
        if (channel == null) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        if (!hasAccessToChannel(sender, args[0])) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            return;
        }

        Integer time = ConfigManager.getConfig().getInteractTransmitterTime();

        if (args.length >= 2) {
            try {
                time = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                WirelessRedstone.getUtils().sendFeedback("That is not a number.", sender, true); //TODO: Add this string to the stringloader
                return;
            }
        }

        if (time < 50) {
            WirelessRedstone.getUtils().sendFeedback("The activation time must be at least 50ms.", sender, true); //TODO: Add this string to the stringloader
            return;
        }

        channel.turnOn(time);
        WirelessRedstone.getUtils().sendFeedback("Successfully activated WirelessChannel.", sender, false, true); //TODO: Add this string to the stringloader
    }
}
