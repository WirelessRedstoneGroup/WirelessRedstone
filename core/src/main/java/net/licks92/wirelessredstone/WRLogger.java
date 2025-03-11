package net.licks92.wirelessredstone;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class WRLogger {

    private final Audience audience;
    private final Component prefixComponent;
    private final boolean debug;
    private final boolean color;

    /**
     * Creates an instance of WRLogger.
     *
     * @param prefix  This is added before all messages.
     * @param console ConsoleCommandSender for console access.
     * @param debug   Enable debug mode.
     * @param color   Enable colored messages.
     */
    public WRLogger(String prefix, ConsoleCommandSender console, boolean debug, boolean color) {
        this.debug = debug;
        this.color = color;
        this.audience = console != null ? Audience.audience(console) : Audience.empty(); // Use Adventure's Audience

        // Add color to the prefix if applicable
        if (color) {
            this.prefixComponent = Component.text()
                    .append(Component.text(prefix, NamedTextColor.RED))
                    .append(Component.space())
                    .build();
        } else {
            this.prefixComponent = Component.text(prefix);
        }
    }

    /**
     * Displays an informational message to the console.
     *
     * @param msg The informational message.
     */
    public void info(String msg) {
        log(msg, NamedTextColor.GREEN);
    }

    /**
     * Displays a debug message to the console if debug mode is enabled.
     *
     * @param msg The debug message.
     */
    public void debug(String msg) {
        if (debug) {
            log("[Debug] " + msg, NamedTextColor.GOLD);
        }
    }

    /**
     * Displays a severe error message to the console.
     *
     * @param msg The severe message.
     */
    public void severe(String msg) {
        log("[SEVERE] " + msg, NamedTextColor.DARK_RED);
    }

    /**
     * Displays a warning message to the console.
     *
     * @param msg The warning message.
     */
    public void warning(String msg) {
        log("[WARNING] " + msg, NamedTextColor.YELLOW);
    }

    /**
     * Logs the message with the appropriate color and level.
     *
     * @param msg   The message to log.
     * @param color The text color (or null for no color).
     */
    private void log(String msg, TextColor color) {
        Component messageComponent = formatMessage(msg, color);

        // Send to audience (Adventure API)
        audience.sendMessage(messageComponent);

        // Fallback to Bukkit Logger in case audience isn't available
        if (audience == Audience.empty()) {
            Bukkit.getLogger().info(stripColors(messageComponent));
        }
    }

    /**
     * Formats the message by applying prefix and color if needed.
     *
     * @param msg   The message to format.
     * @param color The text color (null for no color).
     * @return A formatted Component with the message content.
     */
    private Component formatMessage(String msg, TextColor color) {
        Component msgComponent = Component.text(msg);
        if (color != null && this.color) {
            msgComponent = msgComponent.color(color);
        }
        return prefixComponent.append(msgComponent);
    }

    /**
     * Strips colors from a Component.
     * This is a fallback for plain-text logging via Bukkit Logger.
     *
     * @param component The component to strip.
     * @return A plain-text representation of the component.
     */
    private String stripColors(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component);
    }
}