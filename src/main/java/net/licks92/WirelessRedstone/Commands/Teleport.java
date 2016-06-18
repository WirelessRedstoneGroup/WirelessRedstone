package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(description = "Teleport to specific sign", usage = "<channel> <type> <signID>", aliases = {"tp"},
        permission = "commands.teleport", canUseInConsole = false)
public class Teleport extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
            return;
        }

        WirelessChannel channel = Main.getStorage().getWirelessChannel(args[0]);
        if (channel == null) {
            Utils.sendFeedback(Main.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        if (!hasAccessToChannel(sender, args[0])) {
            Utils.sendFeedback(Main.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            return;
        }

        Integer index = 0;

        try {
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            Utils.sendFeedback("That is not a number.", sender, true); //TODO: Add this string to the stringloader (-> Activate)
            return;
        }

        Player player = (Player) sender;

        switch (args[1].toUpperCase()) {
            case "TRANSMITTER":
            case "TRANSMITTERS":
            case "T":
                Location locTransmitter = channel.getTransmitters().get(index).getLocation().add(0.5, 0, 0.5);
                player.teleport(locTransmitter);
                break;
            case "RECEIVER":
            case "RECEIVERS":
            case "R":
                Location locReceiver = channel.getReceivers().get(index).getLocation().add(0.5, 0, 0.5);
                player.teleport(locReceiver);
                break;
            case "SCREEN":
            case "SCREENS":
            case "S":
                Location locScreen = channel.getScreens().get(index).getLocation().add(0.5, 0, 0.5);
                player.teleport(locScreen);
                break;
        }
    }
}
