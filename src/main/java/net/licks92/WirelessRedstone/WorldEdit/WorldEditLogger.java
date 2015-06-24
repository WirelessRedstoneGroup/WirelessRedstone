package net.licks92.WirelessRedstone.WorldEdit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;
import com.sk89q.worldedit.world.World;
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

        org.bukkit.World world = ((BukkitWorld)this.eventWorld).getWorld();
        Block block = world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        for(Location loc : WirelessRedstone.cache.getAllSignLocations()){
            if(WirelessRedstone.sameLocation(loc, block.getLocation())){
                String channelName = WirelessRedstone.config.getWirelessChannelName(loc);
                if(WirelessRedstone.config.removeIWirelessPoint(channelName, loc)){
                    WirelessRedstone.config.checkChannel(channelName);
                    WirelessRedstone.getWRLogger().debug("Removed sign because of WE");
                } else {
                    WirelessRedstone.getWRLogger().debug("Couldn't remove sign. WE was the remover");
                }
            }
        }
    }
}
