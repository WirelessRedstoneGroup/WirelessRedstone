package net.licks92.WirelessRedstone.Compat;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;
import com.sk89q.worldedit.world.World;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class InternalWorldEditLogger_6 extends AbstractLoggingExtent {

    private final Actor eventActor;
    private final World eventWorld;

    public InternalWorldEditLogger_6(Actor actor, World world, Extent extent) {
        super(extent);
        this.eventActor = actor;
        this.eventWorld = world;
    }

    protected void onBlockChange(Vector position, BaseBlock baseBlock) {
        if (!(this.eventWorld instanceof BukkitWorld)) {
            return;
        }

        org.bukkit.World world = ((BukkitWorld) this.eventWorld).getWorld();
        if (world == null || position == null) {
            return;
        }

        Block block = world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        if (WirelessRedstone.getSignManager().isWirelessRedstoneSign(block)) {
            Sign sign = (Sign) block.getState();
            String channelName = sign.getLine(1);
            WirelessRedstone.getSignManager().removeSign(channelName, block.getLocation());
            WirelessRedstone.getWRLogger().debug("Removed sign at " + block.getLocation() + " because it was edited by WorldEdit");
        }
    }
}
