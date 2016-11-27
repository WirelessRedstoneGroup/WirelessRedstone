package net.licks92.WirelessRedstone.Signs;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("WirelessReceiver")
public class WirelessReceiver implements ConfigurationSerializable, IWirelessPoint {

    private String owner;
    private int x;
    private int y;
    private int z;
    private String world;
    private BlockFace direction = BlockFace.SELF;
    private boolean isWallSign = false;

    public enum Type {
        DEFAULT, INVERTER, DELAYER, CLOCK, SWITCH
    }

    public WirelessReceiver() {
    }

    public WirelessReceiver(Map<String, Object> map) {
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
                    direction = Utils.intToBlockFaceSign(directionInt);
                else
                    direction = Utils.intToBlockFaceSign(directionInt);
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
            this.direction = Utils.intToBlockFaceWallSign(direction);
        else
            this.direction = Utils.intToBlockFaceSign(direction);
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

    public void turnOn(String channelName) {
        if (getLocation().getWorld() == null) // If the world is not loaded or doesn't exist
            return;

        Block block = getLocation().getBlock();

        if (!getIsWallSign()) {
            if (!Utils.isValidLocation(block))
                Utils.signWarning(block, 1);
            else
                block.setTypeIdAndData(76, (byte) 0, true);

        } else {
            if (block.getType() == Material.WALL_SIGN) {
                if (!Utils.isValidWallLocation(block))
                    Utils.signWarning(block, 1);
                else
                    block.setTypeIdAndData(76, (byte) Utils.torchFaceToInt(getDirection()), true);
            }
        }
    }

    public void turnOff(String channelName) {
        if (getLocation().getWorld() == null)
            return;

        byte directionByte;
        Block block = getLocation().getBlock();
        int blockID = getIsWallSign() ? 68 : 63;

        if (getIsWallSign())
            directionByte = (byte) Utils.wallSignFaceToInt(getDirection());
        else
            directionByte = (byte) Utils.signFaceToInt(getDirection());


        block.setTypeIdAndData(blockID, directionByte, true);

        if (block.getState() instanceof Sign) {
            changeSignContent(block, channelName);
        }
    }

    public void changeSignContent(Block block, String channelName) {
        Sign signtemp = (Sign) block.getState();
        signtemp.setLine(0, Main.getStrings().tagsReceiver.get(0));
        signtemp.setLine(1, channelName);
        signtemp.setLine(2, Main.getStrings().tagsReceiverDefaultType.get(0));
        signtemp.update(true);
    }
}
