package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.*;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(description = "Shows WirelessChannel information", usage = "<channel> [signtype]", aliases = {"info", "i"},
        permission = "commands.info", canUseInConsole = true, canUseInCommandBlock = true)
public class Info extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0){
            Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
            return;
        }

        WirelessChannel channel = Main.getStorage().getWirelessChannel(args[0]);
        if(channel == null){
            Utils.sendFeedback(Main.getStrings().channelDoesNotExist, sender, true);
            return;
        }

        SignType signType = null;

        if(args.length >= 2)
            signType = Utils.getSignType(args[1]);

        if(signType == null){
            Utils.sendFeedback(ChatColor.GRAY + "---- " + ChatColor.GREEN + "WirelessChannel " + channel.getName() + ChatColor.GRAY + " ----",
                    sender, false);
            Utils.sendFeedback(ChatColor.GRAY + "Is active: " + ((channel.isActive()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"), sender, false);
            Utils.sendFeedback(ChatColor.GRAY + "Is locked: " + ((channel.isLocked()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"), sender, false);
            Utils.sendFeedback(ChatColor.GRAY + "Sign Types:", sender, false);
            Utils.sendFeedback(ChatColor.GRAY.toString() + channel.getTransmitters().size() + ChatColor.GREEN + " transmitters, "
                    + ChatColor.GRAY + channel.getReceivers().size() + ChatColor.GREEN + " receivers, "
                    + ChatColor.GRAY + channel.getScreens().size() + ChatColor.GREEN + " screens", sender, false); //Java why the fuck do I need to add toString()
        } else {
            if(!(sender instanceof Player)){
                Utils.sendFeedback("Only in-game players can use this command.", sender, true); //TODO: Add this string to the stringloader (-> CommandManager)
                return;
            }

            switch (signType) { //TODO: Replace %%COMMAND
                case TRANSMITTER:
                    if(channel.getTransmitters().size() == 0){
                        Utils.sendFeedback("No signs found.", sender, true); //TODO: Add this string to the stringloader
                        return;
                    }

                    for(WirelessTransmitter transmitter : channel.getTransmitters()){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "transmitter")
                                .replaceAll("%%WORLD", transmitter.getWorld()).replaceAll("%%XCOORD", transmitter.getX() + "")
                                .replaceAll("%%YCOORD", transmitter.getY() + "").replaceAll("%%ZCOORD", transmitter.getZ() + ""));
                    }
                    break;
                case RECEIVER:
                    if(channel.getReceivers().size() == 0){
                        Utils.sendFeedback("No signs found.", sender, true); //TODO: Add this string to the stringloader
                        return;
                    }

                    for(WirelessReceiver receiver : channel.getReceivers()){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "receiver")
                                .replaceAll("%%WORLD", receiver.getWorld()).replaceAll("%%XCOORD", receiver.getX() + "")
                                .replaceAll("%%YCOORD", receiver.getY() + "").replaceAll("%%ZCOORD", receiver.getZ() + ""));
                    }
                    break;
                case SCREEN:
                    if(channel.getScreens().size() == 0){
                        Utils.sendFeedback("No signs found.", sender, true); //TODO: Add this string to the stringloader
                        return;
                    }

                    for(WirelessScreen screen : channel.getScreens()){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.getTeleportString(sender.getName())
                                .replaceAll("%%HOVERTEXT", "Click me to teleport to the sign location!")
                                .replaceAll("%%NAME", channel.getName()).replaceAll("%%TYPE", "screen")
                                .replaceAll("%%WORLD", screen.getWorld()).replaceAll("%%XCOORD", screen.getX() + "")
                                .replaceAll("%%YCOORD", screen.getY() + "").replaceAll("%%ZCOORD", screen.getZ() + ""));
                    }
                    break;
            }
        }
    }
}
