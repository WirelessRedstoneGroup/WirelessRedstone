package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class WirelessReceiver implements IWirelessPoint {
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

    public WirelessReceiver() {

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
        return WirelessRedstone.WireBox.intDirectionToBlockFace(direction);
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
        setDirection(WirelessRedstone.WireBox.blockFace2IntDirection(face));
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
        block.setTypeIdAndData(blockID, directionByte, true);

        if (block.getState() instanceof Sign) {
            changeSignContent(block, channelName);
        }
    }

    public void changeSignContent(Block block, String channelName){
        Sign signtemp = (Sign) block.getState();
        signtemp.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
        signtemp.setLine(1, channelName);
        signtemp.setLine(2, WirelessRedstone.strings.tagsReceiverDefaultType.get(0));
        signtemp.update(true);
    }
}