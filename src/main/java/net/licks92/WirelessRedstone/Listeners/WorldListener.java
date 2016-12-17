package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Signs.IWirelessPoint;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.List;

public class WorldListener implements Listener {

    // Method borrowed from MinecraftMania! Credits to Afforess!
    // https://github.com/Afforess/MinecartMania/blob/master/src/com/afforess/minecartmaniacore/api/MinecartManiaCoreWorldListener.java
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!event.isCancelled()) {
            if (ConfigManager.getConfig().getCancelChunkUnload()) {
                try {
                    List<IWirelessPoint> points = WirelessRedstone.getGlobalCache().getAllSigns();
                    for (IWirelessPoint point : points) {
                        if (Math.abs(event.getChunk().getX() - point.getLocation().getBlock().getChunk().getX())
                                > ConfigManager.getConfig().getCancelChunkUnloadRange()) {
                            continue;
                        }
                        if (Math.abs(event.getChunk().getZ() - point.getLocation().getBlock().getChunk().getZ())
                                > ConfigManager.getConfig().getCancelChunkUnloadRange()) {
                            continue;
                        }

                        event.setCancelled(true);
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

}
