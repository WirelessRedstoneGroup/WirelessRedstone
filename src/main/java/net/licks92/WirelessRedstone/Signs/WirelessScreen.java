package net.licks92.WirelessRedstone.Signs;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("WirelessScreen")
public class WirelessScreen implements ConfigurationSerializable, IWirelessPoint {

    private String owner;
    private int x;
    private int y;
    private int z;
    private String world;
    private BlockFace direction = BlockFace.SELF;
    private boolean isWallSign = false;

    public WirelessScreen() {
    }

    public WirelessScreen(Map<String, Object> map) {
        owner = (String) map.get("owner");
        world = (String) map.get("world");
        isWallSign = (Boolean) map.get("isWallSign");
        x = (Integer) map.get("x");
        y = (Integer) map.get("y");
        z = (Integer) map.get("z");


        try {
            direction = (BlockFace) BlockFace.valueOf(map.get("direction").toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                Integer directionInt = Integer.parseInt(map.get("direction").toString());
                if (isWallSign) //This is maybe redundent, needs more testing
                    direction = WirelessRedstone.getUtils().intToBlockFaceSign(directionInt);
                else
                    direction = WirelessRedstone.getUtils().intToBlockFaceSign(directionInt);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public boolean getIsWallSign() {
        return this.isWallSign;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public void setWorld(String world) {
        this.world = world;
    }

    @Override
    public void setDirection(int direction) {
        if (isWallSign)
            this.direction = WirelessRedstone.getUtils().intToBlockFaceWallSign(direction);
        else
            this.direction = WirelessRedstone.getUtils().intToBlockFaceSign(direction);
    }

    @Override
    public void setDirection(BlockFace face) {
        this.direction = face;
    }

    @Override
    public void setIsWallSign(boolean isWallSign) {
        this.isWallSign = isWallSign;
    }

    @Override
    public String getOwner() {
        return this.owner;
    }

    @Override
    public String getWorld() {
        return this.world;
    }

    @Override
    public BlockFace getDirection() {
        return this.direction;
    }

    @Override
    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("direction", this.direction.name().toUpperCase());
        map.put("isWallSign", getIsWallSign());
        map.put("owner", getOwner());
        map.put("world", getWorld());
        map.put("x", getX());
        map.put("y", getY());
        map.put("z", getZ());
        return map;
    }

    public void deserialize(Map<String, Object> map) {
        this.setDirection((BlockFace) BlockFace.valueOf(map.get("direction").toString().toUpperCase()));
        this.setIsWallSign((Boolean) map.get("isWallSign"));
        this.setOwner((String) map.get("owner"));
        this.setWorld((String) map.get("world"));
        this.setX((Integer) map.get("x"));
        this.setY((Integer) map.get("y"));
        this.setZ((Integer) map.get("z"));
    }

    public void turnOn() {
        String str = ChatColor.GREEN + "ACTIVE";
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(2, str);
        sign.update();
    }

    public void turnOff() {
        String str = ChatColor.RED + "INACTIVE";
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(2, str);
        sign.update();
    }

}
