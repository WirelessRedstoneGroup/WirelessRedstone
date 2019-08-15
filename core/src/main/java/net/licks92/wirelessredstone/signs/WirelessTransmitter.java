package net.licks92.wirelessredstone.signs;

import net.licks92.wirelessredstone.Utils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("WirelessTransmitter")
public class WirelessTransmitter extends WirelessPoint implements ConfigurationSerializable {

    public WirelessTransmitter(int x, int y, int z, String world, boolean isWallSign, BlockFace direction, String owner) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.isWallSign = isWallSign;
        this.direction = direction;
        this.owner = owner;
    }

    public WirelessTransmitter(Map<String, Object> map) {
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

    public boolean isPowered() {
        Location loc = getLocation();
        if (loc == null) {
            return false;
        }

        // MC <= 1.12 #getBlock can be NULL
        if (loc.getBlock() == null) {
            return false;
        }

        return loc.getBlock().isBlockIndirectlyPowered() || loc.getBlock().isBlockPowered();
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
        return "WirelessTransmitter{" +
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
