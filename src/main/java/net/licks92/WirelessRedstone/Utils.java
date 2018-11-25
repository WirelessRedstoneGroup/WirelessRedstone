package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Signs.SignType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collection;

public class Utils {

    private static final BlockFace[] axis = { BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SELF };
    private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

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

    public static boolean newMaterialSystem() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            String[] pieces = bukkitVersion.substring(1).split("_");

            return Integer.parseInt(pieces[0]) >= 1 && Integer.parseInt(pieces[1]) >= 13;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static void sendFeedback(String message, CommandSender sender, boolean error) {
        sendFeedback(message, sender, error, false);
    }

    public static void sendFeedback(String message, CommandSender sender, boolean error, boolean checkSilent) {
        if (ConfigManager.getConfig().getSilentMode() && checkSilent)
            return;
        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "WirelessRedstone" + ChatColor.GRAY + "] "
                + (error ? ChatColor.RED : ChatColor.GREEN) + message);
    }

    public static void sendCommandFeedback(String message, CommandSender sender, boolean error) {
        sendCommandFeedback(message, sender, error, false);
    }

    public static void sendCommandFeedback(String message, CommandSender sender, boolean error, boolean checkSilent) {
        if (ConfigManager.getConfig().getSilentMode() && checkSilent)
            return;
        sender.sendMessage((error ? ChatColor.RED : ChatColor.GREEN) + message);
    }

    public static BlockFace getBlockFace(boolean isWallSign, int direction) {
        BlockFace blockFace = BlockFace.SELF;

        if (isWallSign) {
            switch (direction) {
                case 2:
                    blockFace = BlockFace.NORTH;
                    break;
                case 3:
                    blockFace = BlockFace.SOUTH;
                    break;
                case 4:
                    blockFace = BlockFace.WEST;
                    break;
                case 5:
                    blockFace = BlockFace.EAST;
                    break;
                default:
                    blockFace = BlockFace.NORTH;
                    break;
            }
        } else {
            switch (direction) {
                case 0:
                    blockFace = BlockFace.SOUTH;
                    break;
                case 1:
                    blockFace = BlockFace.SOUTH_SOUTH_WEST;
                    break;
                case 2:
                    blockFace = BlockFace.SOUTH_WEST;
                    break;
                case 3:
                    blockFace = BlockFace.WEST_SOUTH_WEST;
                    break;
                case 4:
                    blockFace = BlockFace.WEST;
                    break;
                case 5:
                    blockFace = BlockFace.WEST_NORTH_WEST;
                    break;
                case 6:
                    blockFace = BlockFace.NORTH_WEST;
                    break;
                case 7:
                    blockFace = BlockFace.NORTH_NORTH_WEST;
                    break;
                case 8:
                    blockFace = BlockFace.NORTH;
                    break;
                case 9:
                    blockFace = BlockFace.NORTH_NORTH_EAST;
                    break;
                case 10:
                    blockFace = BlockFace.NORTH_EAST;
                    break;
                case 11:
                    blockFace = BlockFace.EAST_NORTH_EAST;
                    break;
                case 12:
                    blockFace = BlockFace.EAST;
                    break;
                case 13:
                    blockFace = BlockFace.EAST_SOUTH_EAST;
                    break;
                case 14:
                    blockFace = BlockFace.SOUTH_EAST;
                    break;
                case 15:
                    blockFace = BlockFace.SOUTH_SOUTH_EAST;
                    break;
            }
        }

        return blockFace;
    }

    public static Collection<BlockFace> getAxisBlockFaces() {
        return Arrays.asList(axis);
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle<br>
     * This includes the NORTH_WEST faces
     *
     * @param yaw angle
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw) {
        return yawToFace(yaw, true);
    }
    /**
     * Gets the horizontal Block Face from a given yaw angle
     *
     * @param yaw angle
     * @param useSubCardinalDirections setting, True to allow NORTH_WEST to be returned
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7];
        } else {
            return axis[Math.round(yaw / 90f) & 0x3];
        }
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

    public static SignType getSignType(String firstLine) {
        return getSignType(firstLine, "");
    }

    public static SignType getSignType(String firstLine, String secondLine) {
        if (WirelessRedstone.getStringManager().tagsTransmitter.contains(firstLine)) {
            return SignType.TRANSMITTER;
        } else if (WirelessRedstone.getStringManager().tagsScreen.contains(firstLine)) {
            return SignType.SCREEN;
        } else if (WirelessRedstone.getStringManager().tagsReceiver.contains(firstLine)) {
            if (WirelessRedstone.getStringManager().tagsReceiverInverterType.contains(secondLine)) {
                return SignType.RECEIVER_DELAYER;
            } else if (WirelessRedstone.getStringManager().tagsReceiverSwitchType.contains(secondLine)) {
                return SignType.RECEIVER_SWITCH;
            } else if (WirelessRedstone.getStringManager().tagsReceiverClockType.contains(secondLine)) {
                return SignType.RECEIVER_CLOCK;
            } else if (WirelessRedstone.getStringManager().tagsReceiverDelayerType.contains(secondLine)) {
                return SignType.RECEIVER_DELAYER;
            }

            return SignType.RECEIVER;
        }

        return null;
    }

    public static String getTeleportString(String playerName) {
        return "tellraw " + playerName + " " + "[\"\",{\"text\":\"[\",\"color\":\"gray\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\"}]}}},{\"text\":\"\\u27A4\",\"color\":\"aqua\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\"}]}}},{\"text\":\"] \",\"color\":\"gray\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\"}]}},\"bold\":false},{\"text\":\"Name %%NAME, type: %%TYPE, world: %%WORLD, x: %%XCOORD, y: %%YCOORD, z: %%ZCOORD\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\"}]}}}]";
    }

}
