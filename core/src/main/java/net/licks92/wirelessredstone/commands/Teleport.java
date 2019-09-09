package net.licks92.wirelessredstone.commands;

import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import net.licks92.wirelessredstone.signs.WirelessChannel;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(description = "Teleport to specific sign", usage = "<channel> <type> <signID>", aliases = {"teleport", "tp"},
        tabCompletion = {WirelessCommandTabCompletion.CHANNEL, WirelessCommandTabCompletion.SIGNTYPE},
        permission = "teleport", canUseInConsole = false, canUseInCommandBlock = false)
public class Teleport extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            //TODO: Add this to stringloader
            Utils.sendFeedback("This command is for advanced users only. Use /wr info <channel> <signtype> for more user-friendly version.", sender, true);
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

        int index;
        try {
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandNoNumber, sender, true);
            return;
        }

        Player player = (Player) sender;

        switch (args[1].toUpperCase()) {
            case "TRANSMITTER":
            case "TRANSMITTERS":
            case "T":
                try {
                    if (channel.getTransmitters().get(index).getWorld() == null) {
                        Utils.sendFeedback(WirelessRedstone.getStrings().commandInvalidLocation, player, true);
                        return;
                    }

                    Location locTransmitter = channel.getTransmitters().get(index).getLocation().add(0.5, 0, 0.5);

                    locTransmitter.setYaw(player.getLocation().getYaw());
                    locTransmitter.setPitch(player.getLocation().getPitch());
                    player.teleport(locTransmitter);
                } catch (IndexOutOfBoundsException ex) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandSignNotFound, player, true);
                } catch (IllegalArgumentException ex) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandInvalidLocation, player, true);
                }
                break;
            case "RECEIVER":
            case "RECEIVERS":
            case "R":
                try {
                    if (channel.getReceivers().get(index).getWorld() == null) {
                        Utils.sendFeedback(WirelessRedstone.getStrings().commandInvalidLocation, player, true);
                        return;
                    }

                    Location locReceiver = channel.getReceivers().get(index).getLocation().add(0.5, 0, 0.5);

                    locReceiver.setYaw(player.getLocation().getYaw());
                    locReceiver.setPitch(player.getLocation().getPitch());
                    player.teleport(locReceiver);
                } catch (IndexOutOfBoundsException ex) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandSignNotFound, player, true);
                } catch (IllegalArgumentException ex) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandInvalidLocation, player, true);
                }
                break;
            case "SCREEN":
            case "SCREENS":
            case "S":
                try {
                    if (channel.getScreens().get(index).getWorld() == null) {
                        Utils.sendFeedback(WirelessRedstone.getStrings().commandInvalidLocation, player, true);
                        return;
                    }

                    Location locScreen = channel.getScreens().get(index).getLocation().add(0.5, 0, 0.5);

                    locScreen.setYaw(player.getLocation().getYaw());
                    locScreen.setPitch(player.getLocation().getPitch());
                    player.teleport(locScreen);
                } catch (IndexOutOfBoundsException ex) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandSignNotFound, player, true);
                } catch (IllegalArgumentException ex) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandInvalidLocation, player, true);
                }
                break;
        }
    }
}
