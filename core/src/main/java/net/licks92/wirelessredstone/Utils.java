package net.licks92.wirelessredstone;

import net.licks92.wirelessredstone.signs.SignType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collection;

public class Utils {

    private static final BlockFace[] axis = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
    private static final BlockFace[] fullAxis = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN};


    /**
     * This checks if the current Minecraft server version is compatible with WirelessRedstone.
     *
     * @return If the plugin is compatible
     */
    public static boolean isCompatible() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            String[] pieces = bukkitVersion.substring(1).split("_");

            return Integer.parseInt(pieces[0]) >= 1 && Integer.parseInt(pieces[1]) >= 8;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    /**
     * This checks if the new material system is in place.
     *
     * @return If the new material system is needed
     */
    public static boolean isNewMaterialSystem() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            String[] pieces = bukkitVersion.substring(1).split("_");

            return Integer.parseInt(pieces[0]) >= 1 && Integer.parseInt(pieces[1]) >= 13;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Display a message with prefix to a specific user. This ignores the silent mode.
     *
     * @param message Text message
     * @param sender  Where send the message to
     * @param error   Is the message an error
     */
    public static void sendFeedback(String message, CommandSender sender, boolean error) {
        sendFeedback(message, sender, error, false);
    }

    /**
     * Display a message with prefix to a specific user.
     *
     * @param message     Text message
     * @param sender      Where send the message to
     * @param error       Is the message an error
     * @param checkSilent Don't display message if silent mode is on
     */
    public static void sendFeedback(String message, CommandSender sender, boolean error, boolean checkSilent) {
        if (ConfigManager.getConfig().getSilentMode() && checkSilent)
            return;
        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "WirelessRedstone" + ChatColor.GRAY + "] "
                + (error ? ChatColor.RED : ChatColor.GREEN) + message);
    }

    /**
     * Display a message to a specific user. This ignores the silent mode.
     *
     * @param message Text message
     * @param sender  Where send the message to
     * @param error   Is the message an error
     */
    public static void sendCommandFeedback(String message, CommandSender sender, boolean error) {
        sendCommandFeedback(message, sender, error, false);
    }

    /**
     * Display a message to a specific user.
     *
     * @param message     Text message
     * @param sender      Where send the message to
     * @param error       Is the message an error
     * @param checkSilent Don't display message if silent mode is on
     */
    public static void sendCommandFeedback(String message, CommandSender sender, boolean error, boolean checkSilent) {
        if (ConfigManager.getConfig().getSilentMode() && checkSilent)
            return;
        sender.sendMessage((error ? ChatColor.RED : ChatColor.GREEN) + message);
    }

    /**
     * Converts the old direction system to the new BlockFace system.
     *
     * @param isWallSign Is the sign against a wall
     * @param direction  Old sign facing id system
     * @return BlockFace
     */
    public static BlockFace getBlockFace(boolean isWallSign, int direction) {
        BlockFace blockFace;

        if (isWallSign) {
            if (direction == 2) {
                blockFace = BlockFace.NORTH;
            } else if (direction == 3) {
                blockFace = BlockFace.SOUTH;
            } else if (direction == 4) {
                blockFace = BlockFace.WEST;
            } else if (direction == 5) {
                blockFace = BlockFace.EAST;
            } else {
                blockFace = BlockFace.NORTH;
            }
        } else {
            if (direction == 0) {
                blockFace = BlockFace.SOUTH;
            } else if (direction == 1) {
                blockFace = BlockFace.SOUTH_SOUTH_WEST;
            } else if (direction == 2) {
                blockFace = BlockFace.SOUTH_WEST;
            } else if (direction == 3) {
                blockFace = BlockFace.WEST_SOUTH_WEST;
            } else if (direction == 4) {
                blockFace = BlockFace.WEST;
            } else if (direction == 5) {
                blockFace = BlockFace.WEST_NORTH_WEST;
            } else if (direction == 6) {
                blockFace = BlockFace.NORTH_WEST;
            } else if (direction == 7) {
                blockFace = BlockFace.NORTH_NORTH_WEST;
            } else if (direction == 8) {
                blockFace = BlockFace.NORTH;
            } else if (direction == 9) {
                blockFace = BlockFace.NORTH_NORTH_EAST;
            } else if (direction == 10) {
                blockFace = BlockFace.NORTH_EAST;
            } else if (direction == 11) {
                blockFace = BlockFace.EAST_NORTH_EAST;
            } else if (direction == 12) {
                blockFace = BlockFace.EAST;
            } else if (direction == 13) {
                blockFace = BlockFace.EAST_SOUTH_EAST;
            } else if (direction == 14) {
                blockFace = BlockFace.SOUTH_EAST;
            } else if (direction == 15) {
                blockFace = BlockFace.SOUTH_SOUTH_EAST;
            } else {
                blockFace = BlockFace.SOUTH;
            }
        }

        return blockFace;
    }

    /**
     * Deprecated!<br>
     * Converts BlockFace to a raw byte direction for wall signs/torches.
     *
     * @param isTorch   If the block is a torch
     * @param blockFace The direction the wall sign/torch is facing
     * @return raw byte code for direction
     */
    @Deprecated
    public static byte getRawData(boolean isTorch, BlockFace blockFace) {
        if (isTorch) {
            if (blockFace == BlockFace.NORTH)
                return (byte)4;
            else if (blockFace == BlockFace.SOUTH)
                return (byte)3;
            else if (blockFace == BlockFace.WEST)
                return (byte)2;
            else if (blockFace == BlockFace.EAST)
                return (byte)1;
            return (byte)0;
        } else {
            if (blockFace == BlockFace.NORTH)
                return (byte)2;
            else if (blockFace == BlockFace.SOUTH)
                return (byte)3;
            else if (blockFace == BlockFace.WEST)
                return (byte)4;
            else if (blockFace == BlockFace.EAST)
                return (byte)5;
            return (byte)0;
        }
    }

    /**
     * Gives a collection of adjacent BlockFaces.
     *
     * @return All the possible adjacent BlockFaces
     */
    public static Collection<BlockFace> getAxisBlockFaces() {
        return getAxisBlockFaces(true);
    }

    /**
     * Gives a collection of adjacent BlockFaces.
     *
     * @param upAndDown Include directions up and down
     * @return All the possible adjacent BlockFaces
     */
    public static Collection<BlockFace> getAxisBlockFaces(boolean upAndDown) {
        return Arrays.asList(upAndDown ? fullAxis : axis);
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle<br>
     *
     * @param yaw angle
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3];
    }

    public static SignType getType(String text) {
        switch (text.toUpperCase()) {
            case "TRANSMITTERS":
            case "TRANSMITTER":
            case "T":
                return SignType.TRANSMITTER;
            case "RECEIVERS":
            case "RECEIVER":
            case "R":
                return SignType.RECEIVER;
            case "SCREENS":
            case "SCREEN":
            case "S":
                return SignType.SCREEN;
            case "INVERTERS":
            case "INVERTER":
            case "INVERT":
            case "I":
                return SignType.RECEIVER_INVERTER;
            case "SWITCHERS":
            case "SWITCHER":
            case "SWITCHS":
            case "SWITCH":
                return SignType.RECEIVER_SWITCH;
            case "CLOCKS":
            case "CLOCK":
            case "C":
                return SignType.RECEIVER_CLOCK;
            case "DELAYERS":
            case "DELAYER":
            case "DELAY":
            case "D":
                return SignType.RECEIVER_DELAYER;
        }

        return null;
    }

    /**
     * Returns a SignType based on the first line of a sign.<br>
     * All receiver types are returned as SignType.RECEIVER
     *
     * @param firstLine First line of a sign
     * @return SignType
     */
    public static SignType getSignType(String firstLine) {
        return getSignType(firstLine, "");
    }

    /**
     * Returns a SignType based on the first and third line of a sign.<br>
     * This returns a specific receiver type.
     *
     * @param firstLine  First line of a sign
     * @param secondLine Third line of a sign
     * @return SignType
     */
    public static SignType getSignType(String firstLine, String secondLine) {
        if (WirelessRedstone.getStringManager().tagsTransmitter.contains(firstLine.toLowerCase())) {
            return SignType.TRANSMITTER;
        } else if (WirelessRedstone.getStringManager().tagsScreen.contains(firstLine.toLowerCase())) {
            return SignType.SCREEN;
        } else if (WirelessRedstone.getStringManager().tagsReceiver.contains(firstLine.toLowerCase())) {
            if (WirelessRedstone.getStringManager().tagsReceiverInverterType.contains(secondLine.toLowerCase())) {
                return SignType.RECEIVER_INVERTER;
            } else if (WirelessRedstone.getStringManager().tagsReceiverSwitchType.contains(secondLine.toLowerCase())) {
                return SignType.RECEIVER_SWITCH;
            } else if (WirelessRedstone.getStringManager().tagsReceiverClockType.contains(secondLine.toLowerCase())) {
                return SignType.RECEIVER_CLOCK;
            } else if (WirelessRedstone.getStringManager().tagsReceiverDelayerType.contains(secondLine.toLowerCase())) {
                return SignType.RECEIVER_DELAYER;
            }

            return SignType.RECEIVER;
        }

        return null;
    }

    /**
     * Check if two locations are in the same place.
     *
     * @param loc1 Location
     * @param loc2 Location
     * @return Boolean
     */
    public static boolean sameLocation(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return false;
        } else if (loc1.getWorld() == null || loc2.getWorld() == null) {
            return false;
        }

        return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ() &&
                loc1.getWorld().getName().equalsIgnoreCase(loc2.getWorld().getName());
    }

    /**
     * Mix teleport command together with a player name.
     *
     * @param playerName Player name
     * @return Command
     */
    public static String getTeleportString(String playerName) {
        return "tellraw " + playerName + " " + "[\"\",{\"text\":\"[\",\"color\":\"gray\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\"}]}}},{\"text\":\"\\u27A4\",\"color\":\"aqua\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\"}]}}},{\"text\":\"] \",\"color\":\"gray\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\"}]}},\"bold\":false},{\"text\":\"Name %%NAME, type: %%TYPE, world: %%WORLD, x: %%XCOORD, y: %%YCOORD, z: %%ZCOORD\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\"}]}}}]";
    }

}
