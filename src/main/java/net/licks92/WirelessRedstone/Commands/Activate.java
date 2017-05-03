package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Activate a channel (for a certain amount of ms)", usage = "<channel> [time]", aliases = {"activate", "a"},
        permission = "activate", canUseInConsole = true, canUseInCommandBlock = true)
public class Activate extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        final WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(args[0]);
        if (channel == null) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelNotFound, sender, true);
            return;
        }

        if (!hasAccessToChannel(sender, args[0])) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, sender, true);
            return;
        }

        Integer time = ConfigManager.getConfig().getInteractTransmitterTime();

        if (args.length >= 2) {
            try {
                time = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandNoNumber, sender, true);
                return;
            }
        }

        if (time < 50) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandActivationMin, sender, true);
            return;
        }

        final Integer finalTime = time;
        Bukkit.getScheduler().runTask(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                channel.turnOn(finalTime);
            }
        });
        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandActivated, sender, false, true);
    }
}
