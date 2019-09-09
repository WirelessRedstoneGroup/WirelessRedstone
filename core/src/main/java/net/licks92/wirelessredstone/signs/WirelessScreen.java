package net.licks92.wirelessredstone.signs;

import net.licks92.wirelessredstone.Utils;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("WirelessScreen")
public class WirelessScreen extends WirelessPoint implements ConfigurationSerializable {

    public WirelessScreen(int x, int y, int z, String world, boolean isWallSign, BlockFace direction, String owner) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.isWallSign = isWallSign;
        this.direction = direction;
        this.owner = owner;
    }

    public WirelessScreen(Map<String, Object> map) {
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

    public void turnOn() {
        updateSign(true);
    }

    public void turnOff() {
        updateSign(false);
    }

    public void updateSign(boolean isChannelOn) {
        if (getLocation() == null)
            return;

        getLocation().getWorld().loadChunk(getLocation().getChunk());

        if (!(getLocation().getBlock().getState() instanceof Sign)) {
            return;
        }

        String str;
        if (isChannelOn)
            str = ChatColor.GREEN + "ACTIVE";
        else
            str = ChatColor.RED + "INACTIVE";

        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(2, str);
        sign.update();
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
        return "WirelessScreen{" +
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
