package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;
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
    private int direction = 0;
    private boolean isWallSign = false;

    public enum Type {
        Default, Inverter, Delayer, Clock;
    }

    /**
     * IMPORTANT : You shouldn't have to create a WirelessReceiver with this method.
     * It's used by the bukkit serialization system.
     */
    public WirelessReceiver(Map<String, Object> map) {
        owner = (String) map.get("owner");
        world = (String) map.get("world");
        direction = (Integer) map.get("direction");
        isWallSign = (Boolean) map.get("isWallSign");
        x = (Integer) map.get("x");
        y = (Integer) map.get("y");
        z = (Integer) map.get("z");
    }

    public WirelessReceiver() {

    }

    /**
     * This method should be called ONLY by the bukkit serialization system!
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("direction", this.direction);
        map.put("isWallSign", getIsWallSign());
        map.put("owner", getOwner());
        map.put("world", getWorld());
        map.put("x", getX());
        map.put("y", getY());
        map.put("z", getZ());
        return map;
    }

    @Override
    public String getOwner() {
        return this.owner;
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
    public String getWorld() {
        return this.world;
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
    public BlockFace getDirection() {
        return WirelessRedstone.WireBox.intToBlockFaceSign(direction);
    }

    @Override
    /**
     * You should ALWAYS use the setDirection(BlockFace) method.
     *
     * @param direction
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public void setDirection(BlockFace face) {
        setDirection(WirelessRedstone.WireBox.signFaceToInt(face));
    }

    @Override
    public boolean getIsWallSign() {
        return isWallSign;
    }

    @Override
    public void setIsWallSign(boolean iswallsign) {
        this.isWallSign = iswallsign;
    }

    /**
     * @return A Location object made with the receiver data.
     */
    public Location getLocation() {
        Location loc = new Location(Bukkit.getWorld(world), x, y, z);
        return loc;
    }

    public void turnOn(String channelName) {
        if (getLocation().getWorld() == null) // If the world is not loaded or doesn't exist
            return;

        Block block = getLocation().getBlock();

        if (block.getType() == Material.SIGN_POST) {
            if (!WirelessRedstone.WireBox.isValidLocation(block)) {
                WirelessRedstone.WireBox.signWarning(block, 1);
            } else {
                block.setType(Material.REDSTONE_TORCH_ON);
                block.getState().update();
            }
        } else {
            if (block.getType() == Material.WALL_SIGN) {
                if (!WirelessRedstone.WireBox.isValidWallLocation(block)) {
                    WirelessRedstone.WireBox.signWarning(block, 1);
                } else {
                    byte directionByte;

                    if (WirelessRedstone.getBukkitVersion().contains("v1_8")) {
                        switch (getDirection()) {
                            case EAST:
                                directionByte = 1;
                                break;

                            case WEST:
                                directionByte = 2;
                                break;

                            case SOUTH:
                                directionByte = 3;
                                break;

                            case NORTH:
                                directionByte = 4;
                                break;

                            default:
                                directionByte = 1;

                        }
                    } else {
                        switch (getDirection()) {
                            case EAST:
                                directionByte = 0;
                                break;

                            case WEST:
                                directionByte = 2;
                                break;

                            case SOUTH:
                                directionByte = 3;
                                break;

                            case NORTH:
                                directionByte = 4;
                                break;

                            default:
                                directionByte = 5;

                        }
                    }

                    block.setTypeIdAndData(76, directionByte, true);
                }
            }
        }
    }

    public void turnOff(String channelName) {
        if (getLocation().getWorld() == null)
            return;

        byte directionByte;
        Block block = getLocation().getBlock();
        int blockID = getIsWallSign() ? 68 : 63;

        if (getIsWallSign()) {
            switch (getDirection()) {
                case NORTH:
                    directionByte = 2;
                    break;

                case SOUTH:
                    directionByte = 3;
                    break;

                case WEST:
                    directionByte = 4;
                    break;

                case EAST:
                    directionByte = 5;
                    break;

                default:
                    directionByte = 2;
            }
        } else {
            directionByte = (byte) WirelessRedstone.WireBox.signFaceToInt(getDirection());
        }

        if(WirelessRedstone.getBukkitVersion().contains("v1_8"))
            block.setTypeIdAndData(blockID, directionByte, true);
        else {
            block.setType(Material.AIR);
            block.setTypeIdAndData(blockID, directionByte, true);
            block.getState().update();
        }

        if (block.getState() instanceof Sign) {
            changeSignContent(block, channelName);
        }
    }

    public void changeSignContent(Block block, String channelName) {
        Sign signtemp = (Sign) block.getState();
        signtemp.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
        signtemp.setLine(1, channelName);
        signtemp.setLine(2, WirelessRedstone.strings.tagsReceiverDefaultType.get(0));
        signtemp.update(true);
    }
}