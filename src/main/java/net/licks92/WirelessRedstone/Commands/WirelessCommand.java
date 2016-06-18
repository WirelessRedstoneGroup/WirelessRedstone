package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class WirelessCommand {

	public abstract void onCommand(CommandSender sender, String[] args);

	public boolean hasAccessToChannel(CommandSender sender, String channelName) {
		return !(sender instanceof Player) || Main.getSignManager().hasAccessToChannel((Player) sender, channelName); //If it's console or commandBlock, it has access to channel.
	}
}