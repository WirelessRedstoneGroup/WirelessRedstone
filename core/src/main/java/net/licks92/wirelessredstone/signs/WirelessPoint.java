package net.licks92.wirelessredstone.signs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.Objects;

public abstract class WirelessPoint {

    int x, y, z;
    String owner, world;
    BlockFace direction;
    boolean isWallSign = false;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public String getOwner() {
        return owner;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public boolean isWallSign() {
        return isWallSign;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setDirection(BlockFace direction) {
        this.direction = direction;
    }

    public void setWallSign(boolean wallSign) {
        isWallSign = wallSign;
    }

    @Override
    public String toString() {
        return "WirelessPoint{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", owner='" + owner + '\'' +
                ", world='" + world + '\'' +
                ", direction=" + direction +
                ", isWallSign=" + isWallSign +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WirelessPoint that = (WirelessPoint) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (z != that.z) return false;
        if (!Objects.equals(owner, that.owner)) return false;
        return Objects.equals(world, that.world);
    }
}
