package net.licks92.WirelessRedstone.Channel;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public interface IWirelessPoint {
    String getOwner();

    int getX();

    int getY();

    int getZ();

    String getWorld();

    BlockFace getDirection();

    boolean getisWallSign();

    void setOwner(String owner);

    void setX(int x);

    void setY(int y);

    void setZ(int z);

    void setWorld(String world);

    void setDirection(int direction);

    void setDirection(BlockFace face);

    void setisWallSign(boolean iswallsign);

    Location getLocation();
}
