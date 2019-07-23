package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Activate a channel (for a certain amount of ms)", usage = "<channel> [time] [-s]", aliases = {"activate", "a"},
        tabCompletion = {WirelessCommandTabCompletion.CHANNEL},
        permission = "activate", canUseInConsole = true, canUseInCommandBlock = true)
public class Activate extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(args[0]);
        if (channel == null) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNotFound, sender, true);
            return;
        }

        if (!hasAccessToChannel(sender, args[0])) {
            Utils.sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, sender, true);
            return;
        }

        boolean silence = false;
        Integer time = ConfigManager.getConfig().getInteractTransmitterTime();

        if (args.length >= 2) {
            int timeIndex = 1;
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-s")) {
                    silence = true;
                    timeIndex++;
                    break;
                }
            }

            try {
                time = Integer.parseInt(args[timeIndex]);
            } catch (NumberFormatException ex) {
                Utils.sendFeedback(WirelessRedstone.getStrings().commandNoNumber, sender, true);
                return;
            }
        }

        if (time < 50 && time > 0) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandActivationMin, sender, true);
            return;
        }

        if (time < 0) {
            time = 0;
        }

        channel.turnOn(time);

        if (!silence) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandActivated, sender, false, true);
        }
    }
}