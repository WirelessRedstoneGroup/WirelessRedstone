package net.licks92.wirelessredstone.commands;

import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class WirelessCommand {

    public abstract void onCommand(CommandSender sender, String[] args);

    public boolean hasAccessToChannel(CommandSender sender, String channelName) {
        return !(sender instanceof Player) || WirelessRedstone.getSignManager().hasAccessToChannel((Player) sender, channelName); //If it's console or commandBlock, it has access to channel.
    }

}
