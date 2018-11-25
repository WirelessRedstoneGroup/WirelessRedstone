package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.CompatMaterial;
import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Directional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void on(BlockRedstoneEvent event) {
        handleRedstoneEvent(event.getBlock());
    }

    private void handleRedstoneEvent(Block block) {
        Block nextTickBlock = null;
        Collection<BlockFace> blockFaces = new ArrayList<>();
        Material type = block.getType();

        if (type == CompatMaterial.REPEATER.getMaterial() || type == CompatMaterial.REPEATER_ON.getMaterial() || type == CompatMaterial.REPEATER_OFF.getMaterial()) {
            Directional directional = (Directional) block.getState().getData();

            if (block.getRelative(directional.getFacing()).getType().isSolid() &&
                    !block.getRelative(directional.getFacing()).getType().isInteractable()) {
                nextTickBlock = block.getRelative(directional.getFacing());
            }

            blockFaces.add(directional.getFacing());
        } else {
            blockFaces = Utils.getAxisBlockFaces();
        }

        if (nextTickBlock != null) {
            final Block finalNextTickBlock = nextTickBlock;
            Bukkit.getScheduler().runTask(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    handleRedstoneEvent(finalNextTickBlock);
                }
            });
        }

        List<Location> locations = new ArrayList<>();
        for (BlockFace blockFace : blockFaces) {
            if (block.getRelative(blockFace).getState() instanceof Sign) {
                locations.add(block.getRelative(blockFace).getLocation());
            }
        }

        if (!locations.isEmpty()) {
            Bukkit.getScheduler().runTask(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    for (Location location : locations) {
                        Sign sign = (Sign) location.getBlock().getState();

                        updateRedstonePower(sign, block.isBlockPowered() || block.isBlockIndirectlyPowered());
                    }
                }
            });
        }
    }

    private void updateRedstonePower(Sign sign, boolean powered) {
        if (Utils.getSignType(sign.getLine(0)) != SignType.TRANSMITTER)
            return;

        if (sign.getLine(1) == null)
            return;

        if (sign.getLine(1).equalsIgnoreCase(""))
            return;

        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(sign.getLine(1));

        if (powered) {
            channel.turnOn();
        } else {
            channel.turnOff();
        }
    }

}
