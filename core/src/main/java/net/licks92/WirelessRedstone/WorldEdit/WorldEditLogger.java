package net.licks92.WirelessRedstone.WorldEdit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;
import com.sk89q.worldedit.world.World;
import net.licks92.WirelessRedstone.Signs.WirelessPoint;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class WorldEditLogger extends AbstractLoggingExtent {
    private final Actor eventActor;
    private final World eventWorld;

    public WorldEditLogger(Actor actor, World world, Extent extent) {
        super(extent);
        this.eventActor = actor;
        this.eventWorld = world;
    }

    protected void onBlockChange(Vector position, BaseBlock baseBlock) {
        if (!(this.eventWorld instanceof BukkitWorld)) {
            return;
        }

        org.bukkit.World world = ((BukkitWorld) this.eventWorld).getWorld();

        if (world == null || position == null)
            return;

        Block block = world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) block.getState();

        if (Utils.getSignType(sign.getLine(0)) == null || sign.getLine(1).equalsIgnoreCase("")) {
            return;
        }

        if (WirelessRedstone.getStorageManager().getAllSigns() == null) {
            return;
        }

        for (WirelessPoint point : WirelessRedstone.getStorageManager().getAllSigns()) {
            if (Utils.sameLocation(point.getLocation(), block.getLocation())) {
                String channelName = sign.getLine(1);
                WirelessRedstone.getSignManager().removeSign(channelName, block.getLocation());
                WirelessRedstone.getWRLogger().debug("Removed sign because of WorldEdit");
            }
        }
    }
}
