package net.licks92.WirelessRedstone;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class WRLogger {

    private ConsoleCommandSender console;
    private String prefix;
    private boolean debug;
    private boolean color;

    /**
     * Creates an instance of WRLogger.
     *
     * @param prefix This is added before all messages
     * @param console Console reference from bukkit/spigot
     * @param debug Enable debug mode
     * @param color Enable color messages
     */
    public WRLogger(String prefix, ConsoleCommandSender console, boolean debug, boolean color) {
        this.debug = debug;
        this.color = color;
        this.console = console;
        if (color) this.prefix = ChatColor.RED + prefix + ChatColor.RESET;
        else this.prefix = prefix;
    }

    /**
     * Display a info message to the console.
     *
     * @param msg Message
     */
    public void info(String msg) {
        console.sendMessage(prefix + " " + msg);
    }

    /**
     * Display a debug message to the console if debug mode is enabled.
     *
     * @param msg Message
     */
    public void debug(String msg) {
        if (debug) {
            if (color) console.sendMessage(prefix + ChatColor.GOLD + "[Debug] " + ChatColor.RESET + msg);
            else console.sendMessage(prefix + "[Debug] " + msg);
        }
    }

    /**
     * Display a severe message to the console.
     *
     * @param msg Message
     */
    public void severe(String msg) {
        if (color) console.sendMessage(prefix + ChatColor.DARK_RED + "[SEVERE] " + ChatColor.RESET + msg);
        else console.sendMessage(prefix + "[SEVERE] " + msg);
    }

    /**
     * Display a warning message to the console.
     *
     * @param msg Message
     */
    public void warning(String msg) {
        if (color) console.sendMessage(prefix + ChatColor.RED + "[WARNING] " + ChatColor.RESET + msg);
        else console.sendMessage(prefix + "[WARNING] " + msg);
    }

}
