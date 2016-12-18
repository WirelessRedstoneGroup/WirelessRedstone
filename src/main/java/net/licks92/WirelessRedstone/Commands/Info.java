package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Signs.*;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(description = "Shows WirelessChannel information", usage = "<channel> [signtype]", aliases = {"info", "i"},
        permission = "info", canUseInConsole = true, canUseInCommandBlock = true)
public class Info extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0){
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().tooFewArguments, sender, true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(args[0]);
        if(channel == null){
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        SignType signType = null;

        if(args.length >= 2)
            signType = WirelessRedstone.getUtils().getSignType(args[1]);

        if(signType == null){
            WirelessRedstone.getUtils().sendFeedback(ChatColor.GRAY + "---- " + ChatColor.GREEN + "WirelessChannel " + channel.getName() + ChatColor.GRAY + " ----",
                    sender, false);
            WirelessRedstone.getUtils().sendFeedback(ChatColor.GRAY + "Is active: " + ((channel.isActive()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"), sender, false);
            WirelessRedstone.getUtils().sendFeedback(ChatColor.GRAY + "Is locked: " + ((channel.isLocked()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"), sender, false);
            WirelessRedstone.getUtils().sendFeedback(ChatColor.GRAY + "Sign Types:", sender, false);
            WirelessRedstone.getUtils().sendFeedback(ChatColor.GRAY.toString() + channel.getTransmitters().size() + ChatColor.GREEN + " transmitters, "
                    + ChatColor.GRAY + channel.getReceivers().size() + ChatColor.GREEN + " receivers, "
                    + ChatColor.GRAY + channel.getScreens().size() + ChatColor.GREEN + " screens", sender, false);
        } else {
            if(!(sender instanceof Player)){
                WirelessRedstone.getUtils().sendFeedback("Only in-game players can use this command.", sender, true); //TODO: Add this string to the stringloader (-> CommandManager)
                return;
            }

            Integer index;
            switch (signType) {
                case TRANSMITTER:
                    if(channel.getTransmitters().size() == 0){
                        WirelessRedstone.getUtils().sendFeedback("No signs found.", sender, true); //TODO: Add this string to the stringloader
                        return;
                    }

                    index = 0;
                    for(WirelessTransmitter transmitter : channel.getTransmitters()){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), WirelessRedstone.getUtils().getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "transmitter")
                                .replaceAll("%%WORLD", transmitter.getWorld()).replaceAll("%%XCOORD", transmitter.getX() + "")
                                .replaceAll("%%YCOORD", transmitter.getY() + "").replaceAll("%%ZCOORD", transmitter.getZ() + "")
                                .replaceAll("%%COMMAND", "/wr tp " + channel.getName() + " transmitter " + index));
                        index++;
                    }
                    break;
                case RECEIVER_NORMAL:
                    if(channel.getReceivers().size() == 0){
                        WirelessRedstone.getUtils().sendFeedback("No signs found.", sender, true); //TODO: Add this string to the stringloader
                        return;
                    }

                    index = 0;
                    for(WirelessReceiver receiver : channel.getReceivers()){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), WirelessRedstone.getUtils().getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "receiver")
                                .replaceAll("%%WORLD", receiver.getWorld()).replaceAll("%%XCOORD", receiver.getX() + "")
                                .replaceAll("%%YCOORD", receiver.getY() + "").replaceAll("%%ZCOORD", receiver.getZ() + "")
                                .replaceAll("%%COMMAND", "/wr tp " + channel.getName() + " receiver " + index));
                        index++;
                    }
                    break;
                case SCREEN:
                    if(channel.getScreens().size() == 0){
                        WirelessRedstone.getUtils().sendFeedback("No signs found.", sender, true); //TODO: Add this string to the stringloader
                        return;
                    }

                    index = 0;
                    for(WirelessScreen screen : channel.getScreens()){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), WirelessRedstone.getUtils().getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "screen")
                                .replaceAll("%%WORLD", screen.getWorld()).replaceAll("%%XCOORD", screen.getX() + "")
                                .replaceAll("%%YCOORD", screen.getY() + "").replaceAll("%%ZCOORD", screen.getZ() + "")
                                .replaceAll("%%COMMAND", "/wr tp " + channel.getName() + " screen " + index));
                        index++;
                    }
                    break;
            }
        }
    }
}
