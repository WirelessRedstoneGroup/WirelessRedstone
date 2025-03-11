package net.licks92.wirelessredstone;

import net.licks92.wirelessredstone.signs.SignType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Utils {

    private static final List<BlockFace> AXIS = List.of(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST);
    private static final List<BlockFace> FULL_AXIS = List.of(
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST,
            BlockFace.UP, BlockFace.DOWN);

    private static final Map<Integer, BlockFace> WALL_DIRECTIONS = Map.of(
            2, BlockFace.NORTH, 3, BlockFace.SOUTH, 4, BlockFace.WEST, 5, BlockFace.EAST
    );

    private static final Map<Integer, BlockFace> FLOOR_DIRECTIONS = Map.ofEntries(
            Map.entry(0, BlockFace.SOUTH), Map.entry(1, BlockFace.SOUTH_SOUTH_WEST),
            Map.entry(2, BlockFace.SOUTH_WEST), Map.entry(3, BlockFace.WEST_SOUTH_WEST),
            Map.entry(4, BlockFace.WEST), Map.entry(5, BlockFace.WEST_NORTH_WEST),
            Map.entry(6, BlockFace.NORTH_WEST), Map.entry(7, BlockFace.NORTH_NORTH_WEST),
            Map.entry(8, BlockFace.NORTH), Map.entry(9, BlockFace.NORTH_NORTH_EAST),
            Map.entry(10, BlockFace.NORTH_EAST), Map.entry(11, BlockFace.EAST_NORTH_EAST),
            Map.entry(12, BlockFace.EAST), Map.entry(13, BlockFace.EAST_SOUTH_EAST),
            Map.entry(14, BlockFace.SOUTH_EAST), Map.entry(15, BlockFace.SOUTH_SOUTH_EAST)
    );

    /**
     * Retrieves the current Minecraft version as a string using the Bukkit API.
     *
     * @return A string representing the current Minecraft version.
     */
    private static String getMinecraftVersion() {
        // Utilize Paper's API for better version handling
        return Bukkit.getMinecraftVersion();
    }

    /**
     * Parses the current Minecraft version string into a {@code Version} object.
     * Extracts the major and minor version numbers from the string and
     * encapsulates them into a {@code Version} record. If parsing the version
     * fails, an empty {@code Optional} is returned.
     *
     * @return An {@code Optional<Version>} containing the parsed version if successful,
     *         or an empty {@code Optional} if an exception occurs during parsing.
     */
    private static Optional<Version> parseVersion() {
        try {
            String version = getMinecraftVersion();
            String[] parts = version.split("\\.");
            return Optional.of(new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Determines if the current Minecraft version is compatible by checking
     * whether the major version is greater than 1, or if the major version is 1
     * and the minor version is at least 8.
     *
     * @return true if the Minecraft version is 1.8 or newer, false if parsing fails or the version is incompatible.
     */
    public static boolean isCompatible() {
        return parseVersion()
                .map(version -> version.major() > 1 || (version.major() == 1 && version.minor() >= 8))
                .orElse(false);
    }

    /**
     * Determines if the current system uses the new Minecraft material system
     * introduced in version 1.13 or higher.
     *
     * @return true if the Minecraft version is 1.13 or newer, false otherwise.
     */
    public static boolean isNewMaterialSystem() {
        return parseVersion()
                .map(version -> version.major() > 1 || (version.major() == 1 && version.minor() >= 13))
                .orElse(false);
    }

    /**
     * Sends a message to the specified {@code CommandSender} with an optional prefix and
     * formatting to indicate whether it is an error message. The message can be
     * conditionally suppressed based on a silent mode configuration.
     *
     * @param message      The message to be sent to the {@code CommandSender}.
     * @param sender       The {@code CommandSender} who will receive the message.
     * @param isError      Whether the message represents an error. If {@code true},
     *                     the message is displayed in red; otherwise, it is displayed in green.
     * @param prefix       An optional prefix string to prepend to the message. If
     *                     null, no prefix is added.
     * @param checkSilent  Whether to check for silent mode configuration. If {@code true},
     *                     the message will not be sent if silent mode is enabled.
     */
    private static void sendMessageToSender(String message, CommandSender sender, boolean isError, String prefix, boolean checkSilent) {
        if (checkSilent && ConfigManager.getConfig().getSilentMode()) return;

        String formattedMessage = Optional.ofNullable(prefix)
                .map(p -> ChatColor.GRAY + "[" + ChatColor.RED + p + ChatColor.GRAY + "] ")
                .orElse("") +
                (isError ? ChatColor.RED : ChatColor.GREEN) + message;

        sender.sendMessage(formattedMessage);
    }

    public static void sendFeedback(String message, CommandSender sender, boolean isError) {
        sendMessageToSender(message, sender, isError, "WirelessRedstone", false);
    }

    public static void sendFeedback(String message, CommandSender sender, boolean isError, boolean checkSilent) {
        sendMessageToSender(message, sender, isError, "WirelessRedstone", checkSilent);
    }

    public static void sendCommandFeedback(String message, CommandSender sender, boolean isError) {
        sendMessageToSender(message, sender, isError, null, false);
    }

    public static void sendCommandFeedback(String message, CommandSender sender, boolean isError, boolean checkSilent) {
        sendMessageToSender(message, sender, isError, null, checkSilent);
    }

    /**
     * Determines the appropriate BlockFace direction based on whether the block is a wall sign
     * or a floor-mounted block, and the provided direction value.
     *
     * @param isWallSign Indicates if the block is a wall sign. If true, the direction will be
     *                   determined using the wall directions map. If false, the floor directions map
     *                   will be used.
     * @param direction  The integer value representing the direction. This value is used as a key
     *                   to determine the corresponding BlockFace.
     * @return The corresponding BlockFace for the given block type and direction value. Defaults
     *         to BlockFace.NORTH for wall signs and BlockFace.SOUTH for floor blocks if the
     *         direction is not found in the respective map.
     */
    public static BlockFace getBlockFace(boolean isWallSign, int direction) {
        return isWallSign
                ? WALL_DIRECTIONS.getOrDefault(direction, BlockFace.NORTH)
                : FLOOR_DIRECTIONS.getOrDefault(direction, BlockFace.SOUTH);
    }

    /**
     * Generates raw data byte information based on the state of a torch and the specified BlockFace direction.
     *
     * @param isTorch   Indicates whether the object is a torch.
     * @param blockFace The direction represented by a BlockFace enum.
     * @return A byte value representing the raw data for the given torch state and BlockFace.
     * @deprecated This method is deprecated and may be removed in future updates.
     */
    @Deprecated
    public static byte getRawData(boolean isTorch, BlockFace blockFace) {
        return switch (blockFace) {
            case NORTH -> (byte) (isTorch ? 4 : 2);
            case SOUTH -> (byte) (isTorch ? 3 : 3);
            case WEST -> (byte) (isTorch ? 2 : 4);
            case EAST -> (byte) (isTorch ? 1 : 5);
            default -> (byte) 0;
        };
    }

    /**
     * Generates a Minecraft-specific teleportation command string to be used with a player.
     *
     * @param playerName The name of the player for which the teleportation string is generated.
     * @return A formatted string representing the teleportation command based on the player's name.
     */
    public static String getTeleportString(String playerName) {
        return """
            tellraw %s [
                "",
                {"text": "[", "color": "gray",
                 "clickEvent": {"action": "run_command", "value": "%%COMMAND"},
                 "hoverEvent": {"action": "show_text", "value": {"text": "", "extra": [{"text": "%%HOVERTEXT"}]}}
                },
                {"text": "\\u27A4", "color": "aqua", "bold": true,
                 "clickEvent": {"action": "run_command", "value": "%%COMMAND"},
                 "hoverEvent": {"action": "show_text", "value": {"text": "", "extra": [{"text": "%%HOVERTEXT"}]}}
                },
                {"text": "] ", "color": "gray",
                 "clickEvent": {"action": "run_command", "value": "%%COMMAND"},
                 "hoverEvent": {"action": "show_text", "value": {"text": "", "extra": [{"text": "%%HOVERTEXT"}]}}
                },
                {"text": "Name %%NAME, type: %%TYPE, world: %%WORLD, x: %%XCOORD, y: %%YCOORD, z: %%ZCOORD",
                 "color": "green",
                 "clickEvent": {"action": "run_command", "value": "%%COMMAND"},
                 "hoverEvent": {"action": "show_text", "value": {"text": "", "extra": [{"text": "%%HOVERTEXT"}]}}
                }
            ]
            """.formatted(playerName);
    }

    /**
     * Retrieves a collection of BlockFace directions that align with an axis.
     * Optionally includes the up and down BlockFaces depending on the parameter.
     *
     * @param includeUpAndDown If true, the resulting collection includes both up and down directions;
     *                         otherwise, only horizontal axis directions are included.
     * @return A collection of BlockFace directions based on the specified parameter.
     */
    public static Collection<BlockFace> getAxisBlockFaces(boolean includeUpAndDown) {
        return includeUpAndDown ? FULL_AXIS : AXIS;
    }

    /**
     * Retrieves a collection of BlockFace values that represent the primary axis directions.
     *
     * @return A collection of BlockFace values corresponding to the primary axis directions.
     */
    public static Collection<BlockFace> getAxisBlockFaces() {
        return FULL_AXIS;
    }

    /**
     * Converts a yaw angle into a corresponding BlockFace direction.
     *
     * @param yaw The yaw angle to be converted, measured in degrees. Typically ranges from -180 to 180.
     * @return The BlockFace that corresponds to the given yaw angle.
     */
    public static BlockFace yawToFace(float yaw) {
        return AXIS.get(Math.round(yaw / 90) & 0x3);
    }

    /**
     * Compares two Location objects to determine if they represent the same block location in the same world.
     *
     * @param loc1 The first Location object to compare, may be null.
     * @param loc2 The second Location object to compare, may be null.
     * @return True if both locations share the same block coordinates (x, y, z) and world name, otherwise false.
     */
    public static boolean sameLocation(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.getWorld() == null || loc2.getWorld() == null) {
            return false;
        }
        return loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ() &&
                loc1.getWorld().getName().equalsIgnoreCase(loc2.getWorld().getName());
    }

    /**
     * Determines the SignType based on the provided text and line.
     *
     * @param text The text to be evaluated, indicating the type of sign.
     *             Acceptable values include specific keywords like "TRANSMITTER", "RECEIVER", etc.
     * @param line The additional line parameter, which must not be null.
     * @return The corresponding SignType if a match is found, otherwise null.
     */
    public static SignType getType(String text, @NotNull String line) {
        return switch (text.toUpperCase()) {
            case "TRANSMITTER", "T" -> SignType.TRANSMITTER;
            case "RECEIVER", "R" -> SignType.RECEIVER;
            case "SCREEN", "S" -> SignType.SCREEN;
            case "INVERTER", "I" -> SignType.RECEIVER_INVERTER;
            case "SWITCHER" -> SignType.RECEIVER_SWITCH;
            case "CLOCK", "C" -> SignType.RECEIVER_CLOCK;
            case "DELAYER", "D" -> SignType.RECEIVER_DELAYER;
            default -> null;
        };
    }

    public static void sendFeedback(String s, CommandSender sender, boolean b, Object o, boolean b1) {
    }

    private record Version(int major, int minor) {}
}