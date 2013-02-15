package net.licks92.WirelessRedstone;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class WRLogger
{
	ConsoleCommandSender console;
	private String prefix;
	private boolean debug;
	public static final String MINECRAFT_LOGGER = "Minecraft";

	public WRLogger(String prefix, ConsoleCommandSender console, boolean debug)
	{
		this.prefix = ChatColor.RED + prefix + ChatColor.RESET;
		this.debug = debug;
		this.console = console;
	}
	
	public void info(String msg)
	{
		console.sendMessage(prefix + " " + msg);
	}
	
	public void debug(String msg)
	{
		if(debug)
			console.sendMessage(prefix + ChatColor.GOLD + "[Debug] " + ChatColor.RESET + msg);
	}

	public void severe(String msg)
	{
		console.sendMessage(prefix + ChatColor.DARK_RED + "[SEVERE] " + ChatColor.RESET + msg);
	}

	public void warning(String msg)
	{
		console.sendMessage(prefix + ChatColor.RED + "[WARNING] " + ChatColor.RESET + msg);
	}
}