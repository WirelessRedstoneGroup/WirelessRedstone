package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(description = "Teleport to specific sign", usage = "<channel> <type> <signID>", aliases = {"teleport", "tp"},
        permission = "commands.teleport", canUseInConsole = false, canUseInCommandBlock = false)
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
                try {
                    Location locTransmitter = channel.getTransmitters().get(index).getLocation().add(0.5, 0, 0.5);
                    locTransmitter.setYaw(player.getLocation().getYaw());
                    locTransmitter.setPitch(player.getLocation().getPitch());
                    player.teleport(locTransmitter);
                } catch (IndexOutOfBoundsException ex) {
                    Utils.sendFeedback("Sign not found!", player, true); //TODO: Add this string to the stringloader
                }
                break;
            case "RECEIVER":
            case "RECEIVERS":
            case "R":
                try {
                    Location locReceiver = channel.getReceivers().get(index).getLocation().add(0.5, 0, 0.5);
                    locReceiver.setYaw(player.getLocation().getYaw());
                    locReceiver.setPitch(player.getLocation().getPitch());
                    player.teleport(locReceiver);
                } catch (IndexOutOfBoundsException ex) {
                    Utils.sendFeedback("Sign not found!", player, true); //TODO: Add this string to the stringloader
                }
                break;
            case "SCREEN":
            case "SCREENS":
            case "S":
                try {
                    Location locScreen = channel.getScreens().get(index).getLocation().add(0.5, 0, 0.5);
                    locScreen.setYaw(player.getLocation().getYaw());
                    locScreen.setPitch(player.getLocation().getPitch());
                    player.teleport(locScreen);
                } catch (IndexOutOfBoundsException ex) {
                    Utils.sendFeedback("Sign not found!", player, true); //TODO: Add this string to the stringloader
                }
                break;
        }
    }
}
