package net.licks92.wirelessredstone.signs;

import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import net.licks92.wirelessredstone.compat.InternalProvider;
import net.licks92.wirelessredstone.materiallib.data.CrossMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("WirelessReceiver")
public class WirelessReceiver extends WirelessPoint implements ConfigurationSerializable {

    public WirelessReceiver(int x, int y, int z, String world, boolean isWallSign, BlockFace direction, String owner) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.isWallSign = isWallSign;
        this.direction = direction;
        this.owner = owner;
    }

    public WirelessReceiver(Map<String, Object> map) {
        owner = (String) map.get("owner");
        world = (String) map.get("world");
        isWallSign = (Boolean) map.get("isWallSign");
        x = (Integer) map.get("x");
        y = (Integer) map.get("y");
        z = (Integer) map.get("z");

        try {
            direction = BlockFace.valueOf(map.get("direction").toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                int directionInt = Integer.parseInt(map.get("direction").toString());
                direction = Utils.getBlockFace(false, directionInt); // In the past normal signs and wall signs where saved under one direction
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void turnOn(String channelName) {
        changeState(true, channelName);
    }

    public void turnOff(String channelName) {
        changeState(false, channelName);
    }

    protected void changeState(boolean newState, String channelName) {
        if (getLocation() == null) {
            return;
        }

        if (getLocation().getWorld() == null) {
            return;
        }

        getLocation().getWorld().loadChunk(getLocation().getChunk());
        Block block = getLocation().getBlock();

        if (isWallSign()) {
            BlockFace blockFace = null;

            if (block.getRelative(direction.getOppositeFace()).getType() != Material.AIR) {
                blockFace = direction;
            } else if (getAvailableWallFace(getLocation()) != null) {
                blockFace = getAvailableWallFace(getLocation());
            }

            if (blockFace == null) {
                block.setType(Material.AIR);
                WirelessRedstone.getWRLogger().warning("Receiver " + toString() + " is in a invalid position!");
                return;
            }

            if (!Utils.getAxisBlockFaces(false).contains(blockFace)) {
                block.setType(Material.AIR);
                WirelessRedstone.getWRLogger().warning("Receiver " + toString() + " has an invalid BlockFace! " +
                        "The BlockFace needs to be one of these values values=[north, south, west, east]");
                return;
            }

//            WirelessRedstone.getWRLogger().debug("Is solid " + (block.getRelative(direction.getOppositeFace()).getType() != Material.AIR));
//            WirelessRedstone.getWRLogger().debug("Location " + block.getRelative(direction.getOppositeFace()).getLocation());
//            WirelessRedstone.getWRLogger().debug("Face " + direction + " Available face " + availableBlockFace);

            if (newState) {
                InternalProvider.getCompatBlockData().setRedstoneWallTorch(block, blockFace, direction);
            } else {
                InternalProvider.getCompatBlockData().setSignWall(block, blockFace, direction);
                changeSignContent(block, channelName);
            }
        } else {
            if (newState) {
                CrossMaterial.REDSTONE_TORCH.setMaterial(block);
            } else {
                CrossMaterial.SIGN.setMaterial(block);

                if (!(block.getState() instanceof Sign)) {
                    WirelessRedstone.getWRLogger().warning("Receiver " + toString() + " is not a Sign but the plugin does expect it to be a Sign. " +
                            "Is the sign at a valid location?");
                    return;
                }

                if (Arrays.asList(BlockFace.UP, BlockFace.DOWN).contains(direction)) {
                    WirelessRedstone.getWRLogger().warning("Receiver " + toString() + " has an invalid BlockFace! " +
                            "The BlockFace values=[up, down] are invalid, using default BlockFace");
                } else {
                    InternalProvider.getCompatBlockData().setSignRotation(block, direction);
                }

                changeSignContent(block, channelName);
            }
        }
    }

    public void changeSignContent(Block block, String channelName) {
        if (!(block.getState() instanceof Sign)) {
            WirelessRedstone.getWRLogger().warning("Receiver " + toString() + " is not a Sign but the plugin does expect it to be a Sign. " +
                    "Is the sign at a valid location?");
            return;
        }

        Sign sign = (Sign) block.getState();
        sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverDefaultType.get(0));
        sign.update();
    }

    private BlockFace getAvailableWallFace(Location location) {
        for (BlockFace blockFace : Utils.getAxisBlockFaces(false)) {
            Block relative = location.getBlock().getRelative(blockFace);
            if (relative.getType().isSolid()) {
                return blockFace.getOppositeFace();
            }
        }

        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("direction", getDirection().name().toUpperCase());
        map.put("isWallSign", isWallSign());
        map.put("owner", getOwner());
        map.put("world", getWorld());
        map.put("x", getX());
        map.put("y", getY());
        map.put("z", getZ());
        return map;
    }


    @Override
    public String toString() {
        return "WirelessReceiver{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", owner='" + owner + '\'' +
                ", world='" + world + '\'' +
                ", direction=" + direction +
                ", isWallSign=" + isWallSign +
                '}';
    }
}
