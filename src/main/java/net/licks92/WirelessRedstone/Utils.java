package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Signs.IWirelessPoint;
import net.licks92.WirelessRedstone.Signs.SignType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    //Currently blocking all bad characters from EVERY config
    public static char[] badCharacters = {'|', '-', '*', '/', '<', '>', ' ', '=', '~',
            '!', '^', '(', ')', ':', '`', '.'};

    public static String getBukkitVersion() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static boolean sameLocation(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ() && loc1.getWorld() == loc2.getWorld();
    }

    public static boolean isCompatible() {
        try {
            String[] pieces = getBukkitVersion().substring(1).split("_");

            return Integer.parseInt(pieces[0]) >= 1 && Integer.parseInt(pieces[1]) >= 8;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static void sendFeedback(String message, CommandSender sender, boolean error) {
        sendFeedback(message, sender, error, false);
    }

    public static void sendCommandFeedback(String message, CommandSender sender, boolean error) {
        sendCommandFeedback(message, sender, error, false);
    }

    public static void sendFeedback(String message, CommandSender sender, boolean error, boolean checkSilent) {
        if (ConfigManager.getConfig().getSilentMode() && checkSilent)
            return;
        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "WirelessRedstone" + ChatColor.GRAY + "] "
                + (error ? ChatColor.RED : ChatColor.GREEN) + message);
    }

    public static void sendCommandFeedback(String message, CommandSender sender, boolean error, boolean checkSilent) {
        if (ConfigManager.getConfig().getSilentMode() && checkSilent)
            return;
        sender.sendMessage((error ? ChatColor.RED : ChatColor.GREEN) + message);
    }

    public static boolean containsBadChar(String string) {
        //Check if string contains something different then a-z 0-9. It also checks if it contains a tab char
        Pattern p = Pattern.compile("([^a-z0-9-_]|[\\t])", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);

        return m.find();
    }

    /*
    * 1.8 - 1.10
    * Torch Facing
    * 0 - Air
    * 1 - East
    * 2 - West
    * 3 - South
    * 4 - North
    * */

    public static int torchFaceToInt(BlockFace face) {
        switch (face) {
            case NORTH:
                return 4;
            case EAST:
                return 1;
            case SOUTH:
                return 3;
            case WEST:
                return 2;
            default:
                return 0;
        }
    }

    public static BlockFace intToBlockFaceTorch(Integer dir) {
        switch (dir) {
            case 4:
                return BlockFace.NORTH;
            case 1:
                return BlockFace.EAST;
            case 3:
                return BlockFace.SOUTH;
            case 2:
                return BlockFace.WEST;
            default:
                return BlockFace.SELF;
        }
    }

    /*
    * 1.8 - 1.10
    * Wall Sign facing
    * 0 - North
    * 3 - South
    * 4 - West
    * 5 - East
    * */

    public static int wallSignFaceToInt(BlockFace face) {
        switch (face) {
            case NORTH:
                return 0;
            case EAST:
                return 5;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            default:
                return 0;
        }
    }

    public static BlockFace intToBlockFaceWallSign(Integer dir) {
        switch (dir) {
            case 0:
                return BlockFace.NORTH;
            case 5:
                return BlockFace.EAST;
            case 3:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            default:
                return BlockFace.NORTH;
        }
    }

    /*
    * 1.8 - 1.10
    * Sign facing
    * 0 - South
    * 4 - West
    * 8 - North
    * 12 - East
    * */

    public static int signFaceToInt(BlockFace face) {
        switch (face) {
            case NORTH:
                return 8;
            case EAST:
                return 12;
            case SOUTH:
                return 0;
            case WEST:
                return 4;
            default:
                return 0;
        }
    }

    public static BlockFace intToBlockFaceSign(Integer dir) {
        switch (dir) {
            case 8:
                return BlockFace.NORTH;
            case 12:
                return BlockFace.EAST;
            case 0:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            default:
                return BlockFace.SOUTH;
        }
    }

    public static boolean isValidWallLocation(Block block) {
        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) block
                .getState().getData();
        BlockFace face = sign.getAttachedFace();
        Block tempBlock = block.getRelative(face);

        return !(tempBlock.getType() == Material.AIR
                || tempBlock.getType() == Material.PISTON_BASE
                || tempBlock.getType() == Material.PISTON_EXTENSION
                || tempBlock.getType() == Material.PISTON_MOVING_PIECE
                || tempBlock.getType() == Material.PISTON_STICKY_BASE
                || tempBlock.getType() == Material.GLOWSTONE
                || tempBlock.getType() == Material.REDSTONE_LAMP_ON
                || tempBlock.getType() == Material.REDSTONE_LAMP_OFF
                || tempBlock.getType() == Material.LEAVES
                || tempBlock.getType() == Material.WOOD_STAIRS
                || tempBlock.getType() == Material.COBBLESTONE_STAIRS
                || tempBlock.getType() == Material.RED_SANDSTONE_STAIRS
                || tempBlock.getType() == Material.SANDSTONE_STAIRS
                || tempBlock.getType() == Material.FENCE
                || tempBlock.getType() == Material.ACACIA_FENCE
                || tempBlock.getType() == Material.DARK_OAK_FENCE
                || tempBlock.getType() == Material.JUNGLE_FENCE
                || tempBlock.getType() == Material.BIRCH_FENCE
                || tempBlock.getType() == Material.WOOD_DOOR
                || tempBlock.getType() == Material.WOODEN_DOOR
                || tempBlock.getType() == Material.IRON_DOOR_BLOCK
                || tempBlock.getType() == Material.IRON_DOOR
                || tempBlock.getType() == Material.GLASS
                || tempBlock.getType() == Material.THIN_GLASS
                || tempBlock.getType() == Material.STAINED_GLASS
                || tempBlock.getType() == Material.STAINED_GLASS_PANE
                || tempBlock.getType() == Material.COBBLE_WALL
                || tempBlock.getType() == Material.ICE
                || tempBlock.getType() == Material.WOOD_STEP
                || tempBlock.getType() == Material.STEP
                || tempBlock.getType() == Material.TNT
                || tempBlock.getType() == Material.SEA_LANTERN
                || (tempBlock.getTypeId() >= 219 && tempBlock.getTypeId() <= 234) //We could use Material but we need to specify every color
        );
    }

    public static boolean isValidLocation(Block block) {
        if (block == null)
            return false;

        Block tempBlock = block.getRelative(BlockFace.DOWN);

        return !(tempBlock.getType() == Material.AIR
                || tempBlock.getType() == Material.PISTON_BASE
                || tempBlock.getType() == Material.PISTON_EXTENSION
                || tempBlock.getType() == Material.PISTON_MOVING_PIECE
                || tempBlock.getType() == Material.PISTON_STICKY_BASE
                || tempBlock.getType() == Material.GLOWSTONE
                || tempBlock.getType() == Material.REDSTONE_LAMP_ON
                || tempBlock.getType() == Material.REDSTONE_LAMP_OFF
                || tempBlock.getType() == Material.LEAVES
                || tempBlock.getType() == Material.TNT
                || tempBlock.getType() == Material.SEA_LANTERN
                || (tempBlock.getTypeId() >= 219 && tempBlock.getTypeId() <= 234) //We could use Material but we need to specify every color
        );
    }

    public static void signWarning(Block block, Integer code) {
        Sign sign = (Sign) block.getState();
        switch (code) {
            case 1:
                sign.setLine(2, "Bad block");
                sign.setLine(3, "Behind sign");
                sign.update();
                break;

            default:
                break;
        }
    }

    public static List<BlockFace> getEveryBlockFace(boolean addUpAndDown) {
        ArrayList<BlockFace> possibleBlockface = new ArrayList<BlockFace>();
        possibleBlockface.add(BlockFace.NORTH);
        possibleBlockface.add(BlockFace.EAST);
        possibleBlockface.add(BlockFace.SOUTH);
        possibleBlockface.add(BlockFace.WEST);

        if (addUpAndDown) {
            possibleBlockface.add(BlockFace.UP);
            possibleBlockface.add(BlockFace.DOWN);
        }

        return possibleBlockface;
    }

    public static void loadChunks() {
        if (ConfigManager.getConfig().getCancelChunkUnload()) {
            for (IWirelessPoint point : Main.getGlobalCache().getAllSigns()) {
                Location location = point.getLocation();
                if (location.getWorld() == null)
                    continue; // world currently not loaded.

                Chunk center = location.getBlock().getChunk();
                World world = center.getWorld();
                int range = ConfigManager.getConfig().getCancelChunkUnloadRange();
                for (int dx = -(range); dx <= range; dx++) {
                    for (int dz = -(range); dz <= range; dz++) {
                        Chunk chunk = world.getChunkAt(center.getX() + dx,
                                center.getZ() + dz);
                        world.loadChunk(chunk);
                    }
                }
            }
        }
    }

    public static SignType getSignType(String string) {
        switch (string.toUpperCase()) {
            case "TRANSMITTER":
            case "TRANSMITTERS":
            case "T":
                return SignType.TRANSMITTER;
            case "SCREEN":
            case "SCREENS":
            case "S":
                return SignType.SCREEN;
            case "RECEIVER":
            case "RECEIVERS":
            case "R":
                return SignType.RECEIVER_NORMAL;
            case "INVERTER":
            case "I":
                return SignType.RECEIVER_INVERTER;
            case "CLOCK":
            case "C":
                return SignType.RECEIVER_CLOCK;
            case "SWITCH":
                return SignType.RECEIVER_SWITCH;
            case "DELAYER":
            case "DELAY":
            case "D":
                return SignType.RECEIVER_DELAYER;
            default:
                return null;
        }
    }

    public static String getDatabaseFriendlyName(String normalName) {
        try {
            Integer.parseInt(normalName);
            normalName = "num_" + normalName;
        } catch (NumberFormatException ignored) {
        }

        for (char character : badCharacters) {
            if (normalName.contains(String.valueOf(character))) {
                String ascii = "" + (int) character;
                String code = "_char_" + ascii + "_";
                normalName = normalName.replace(String.valueOf(character), code);
            }
        }
        return normalName;
    }

    public static boolean isSpigot() {
        return Bukkit.getVersion().contains("Spigot");
    }

    public static String getTeleportString(String playerName) {
        return "tellraw " + playerName + " " + "[\"\",{\"text\":\"[\",\"color\":\"gray\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\",\"color\":\"gray\"}]}}},{\"text\":\"\\\u27A4\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\",\"color\":\"gray\"}]}}},{\"text\":\"] \",\"color\":\"gray\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\",\"color\":\"gray\"}]}}},{\"text\":\"Name %%NAME, type: %%TYPE, world: %%WORLD, x: %%XCOORD, y: %%YCOORD, z: %%ZCOORD\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\",\"color\":\"gray\"}]}}}]";
    }

    public static String getDownloadUrl(String playerName) {
        return  "tellraw " + playerName + " " + "[\"\",{\"text\":\"%%TEXT\",\"color\":\"blue\",\"italic\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%%LINK\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%HOVERTEXT\",\"color\":\"gray\"}]}}}]";
    }
}
