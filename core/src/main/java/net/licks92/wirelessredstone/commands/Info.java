package net.licks92.wirelessredstone.commands;

import net.licks92.wirelessredstone.signs.SignType;
import net.licks92.wirelessredstone.signs.WirelessChannel;
import net.licks92.wirelessredstone.signs.WirelessReceiver;
import net.licks92.wirelessredstone.signs.WirelessScreen;
import net.licks92.wirelessredstone.signs.WirelessTransmitter;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(description = "Shows WirelessChannel information", usage = "<channel> [signtype]", aliases = {"info", "i"},
        tabCompletion = {WirelessCommandTabCompletion.CHANNEL, WirelessCommandTabCompletion.SIGNTYPE},
        permission = "info", canUseInConsole = true, canUseInCommandBlock = true)
public class Info extends WirelessCommand {

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

        SignType signType = null;

        if (args.length >= 2)
            signType = Utils.getType(args[1]);

        if (signType == null) {
            Utils.sendFeedback(ChatColor.GRAY + "---- " + ChatColor.GREEN + "WirelessChannel " + channel.getName() + ChatColor.GRAY + " ----",
                    sender, false);
            Utils.sendFeedback(ChatColor.GRAY + "Is active: " +
                    ((channel.isActive()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"), sender, false);
            Utils.sendFeedback(ChatColor.GRAY + "Is locked: " +
                    ((channel.isLocked()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"), sender, false);
            Utils.sendFeedback(ChatColor.GRAY + "Sign Types:", sender, false);
            Utils.sendFeedback(ChatColor.GRAY.toString() + channel.getTransmitters().size() + ChatColor.GREEN + " transmitters, "
                    + ChatColor.GRAY + channel.getReceivers().size() + ChatColor.GREEN + " receivers, "
                    + ChatColor.GRAY + channel.getScreens().size() + ChatColor.GREEN + " screens", sender, false);
        } else {
            if (!(sender instanceof Player)) {
                Utils.sendFeedback(WirelessRedstone.getStrings().commandOnlyInGame, sender, true);
                return;
            }

            int index;
            switch (signType) {
                case TRANSMITTER:
                    if (channel.getTransmitters().size() == 0) {
                        Utils.sendFeedback(WirelessRedstone.getStrings().commandSignNotFound, sender, true);
                        return;
                    }

                    index = 0;
                    for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "transmitter")
                                .replaceAll("%%WORLD", transmitter.getWorld()).replaceAll("%%XCOORD", transmitter.getX() + "")
                                .replaceAll("%%YCOORD", transmitter.getY() + "").replaceAll("%%ZCOORD", transmitter.getZ() + "")
                                .replaceAll("%%COMMAND", "/wr tp " + channel.getName() + " transmitter " + index));
                        index++;
                    }
                    break;
                case RECEIVER:
                    if (channel.getReceivers().size() == 0) {
                        Utils.sendFeedback(WirelessRedstone.getStrings().commandSignNotFound, sender, true);
                        return;
                    }

                    index = 0;
                    for (WirelessReceiver receiver : channel.getReceivers()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "receiver")
                                .replaceAll("%%WORLD", receiver.getWorld()).replaceAll("%%XCOORD", receiver.getX() + "")
                                .replaceAll("%%YCOORD", receiver.getY() + "").replaceAll("%%ZCOORD", receiver.getZ() + "")
                                .replaceAll("%%COMMAND", "/wr tp " + channel.getName() + " receiver " + index));
                        index++;
                    }
                    break;
                case SCREEN:
                    if (channel.getScreens().size() == 0) {
                        Utils.sendFeedback(WirelessRedstone.getStrings().commandSignNotFound, sender, true);
                        return;
                    }

                    index = 0;
                    for (WirelessScreen screen : channel.getScreens()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "screen")
                                .replaceAll("%%WORLD", screen.getWorld()).replaceAll("%%XCOORD", screen.getX() + "")
                                .replaceAll("%%YCOORD", screen.getY() + "").replaceAll("%%ZCOORD", screen.getZ() + "")
                                .replaceAll("%%COMMAND", "/wr tp " + channel.getName() + " screen " + index));
                        index++;
                    }
                    break;
                default:
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandSignNotFound, sender, true);
                    break;
            }
        }
    }
}