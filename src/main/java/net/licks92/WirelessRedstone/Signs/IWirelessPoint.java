package net.licks92.WirelessRedstone.Signs;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public interface IWirelessPoint {

    int getX();

    int getY();

    int getZ();

    boolean getIsWallSign();

    void setOwner(String owner);

    void setX(int x);

    void setY(int y);

    void setZ(int z);

    void setWorld(String world);

    void setDirection(int direction);

    void setDirection(BlockFace face);

    void setIsWallSign(boolean isWallSign);

    String getOwner();

    String getWorld();

    BlockFace getDirection();

    Location getLocation();
}
