package net.licks92.wirelessredstone.compat;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class InternalWorldEditLogger_7 extends AbstractDelegateExtent {

    private final Actor eventActor;
    private final World eventWorld;
    private final Extent eventExtent;

    protected InternalWorldEditLogger_7(Actor actor, World world, Extent extent) {
        super(extent);
        this.eventActor = actor;
        this.eventWorld = world;
        this.eventExtent = extent;
    }

    protected void onBlockChange(BlockVector3 position, BlockStateHolder<?> blockStateHolder) {
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

    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        this.onBlockChange(location, block);
        return this.eventExtent.setBlock(location, block);
    }
}
