package net.licks92.WirelessRedstone;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class WRLogger {
    ConsoleCommandSender console;
    private String prefix;
    private boolean debug;
    private boolean color;
    public static final String MINECRAFT_LOGGER = "Minecraft";

    public WRLogger(String prefix, ConsoleCommandSender console, boolean debug, boolean color) {
        this.debug = debug;
        this.color = color;
        this.console = console;
        if (color) this.prefix = ChatColor.RED + prefix + ChatColor.RESET;
        else this.prefix = prefix;
    }

    public void info(String msg) {
        console.sendMessage(prefix + " " + msg);
    }

    public void debug(String msg) {
        if (debug) {
            if (color) console.sendMessage(prefix + ChatColor.GOLD + "[Debug] " + ChatColor.RESET + msg);
            else console.sendMessage(prefix + "[Debug] " + msg);
        }
    }

    public void severe(String msg) {
        if (color) console.sendMessage(prefix + ChatColor.DARK_RED + "[SEVERE] " + ChatColor.RESET + msg);
        else console.sendMessage(prefix + "[SEVERE] " + msg);
    }

    public void warning(String msg) {
        if (color) console.sendMessage(prefix + ChatColor.RED + "[WARNING] " + ChatColor.RESET + msg);
        else console.sendMessage(prefix + "[WARNING] " + msg);
    }
}