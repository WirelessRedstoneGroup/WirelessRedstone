package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Activate a channel (for a certain amount of ms)", usage = "<channel> [time]", aliases = {"activate", "a"},
        permission = "commands.activate", canUseInConsole = true)
public class Activate extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
            return;
        }

        WirelessChannel channel = Main.getStorage().getWirelessChannel(args[0]);
        if(channel == null){
            Utils.sendFeedback(Main.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        if(!hasAccessToChannel(sender, args[0])){
            Utils.sendFeedback(Main.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            return;
        }

        Integer time = ConfigManager.getConfig().getInteractTransmitterTime();

        if (args.length >= 2) {
            try {
                time = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex){
                Utils.sendFeedback("That is not a number.", sender, true); //TODO: Add this string to the stringloader
                return;
            }
        }

        if(time < 50) {
            Utils.sendFeedback("The activation time must be at least 50ms.", sender, true); //TODO: Add this string to the stringloader
            return;
        }

        channel.turnOn(time);
        Utils.sendFeedback("Successfully executed command.", sender, false); //TODO: Add this string to the stringloader
    }
}
