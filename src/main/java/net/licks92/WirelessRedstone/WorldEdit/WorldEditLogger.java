package net.licks92.WirelessRedstone.WorldEdit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;
import com.sk89q.worldedit.world.World;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
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
        for(Location loc : Main.getChannelManager().getAllSignLocations()){
            if(Utils.sameLocation(loc, block.getLocation())){
                String channelName = Main.getStorage().getWirelessChannelName(loc);
                if(Main.getStorage().removeIWirelessPoint(channelName, loc)){
                    Main.getStorage().checkChannel(channelName);
                    Main.getWRLogger().debug("Removed sign because of WorldEdit");
                } else {
                    Main.getWRLogger().debug("Couldn't remove sign. WorldEdit was the remover");
                }
            }
        }
    }
}
