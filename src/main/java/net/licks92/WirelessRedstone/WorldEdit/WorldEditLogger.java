package net.licks92.WirelessRedstone.WorldEdit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;
import com.sk89q.worldedit.world.World;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Location;
import org.bukkit.block.Block;

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

        if (WirelessRedstone.getGlobalCache() == null)
            return;

        for (Location loc : WirelessRedstone.getGlobalCache().getAllSignLocations()) {
            if (Utils.sameLocation(loc, block.getLocation())) {
                String channelName = WirelessRedstone.getStorage().getWirelessChannelName(loc);
                if (WirelessRedstone.getStorage().removeIWirelessPoint(channelName, loc)) {
                    WirelessRedstone.getStorage().checkChannel(channelName);
                    WirelessRedstone.getWRLogger().debug("Removed sign because of WorldEdit");
                } else {
                    WirelessRedstone.getWRLogger().debug("Couldn't remove sign. WorldEdit was the remover");
                }
            }
        }
    }
}
