package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.List;

public class WirelessWorldListener implements Listener {
    private final WirelessRedstone plugin;

    public WirelessWorldListener(final WirelessRedstone r_plugin) {
        plugin = r_plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Method borrowed from MinecraftMania! Credits to Afforess!
    // https://github.com/Afforess/MinecartMania/blob/master/src/com/afforess/minecartmaniacore/api/MinecartManiaCoreWorldListener.java
    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent event) {
        if (!event.isCancelled()) {
            if (WirelessRedstone.config.isCancelChunkUnloads()) {
                try {
                    List<IWirelessPoint> points = WirelessRedstone.cache
                            .getAllSigns();
                    for (IWirelessPoint point : points) {
                        if (Math.abs(event.getChunk().getX()
                                - point.getLocation().getBlock().getChunk()
                                .getX()) > WirelessRedstone.config
                                .getChunkUnloadRange()) {
                            continue;
                        }
                        if (Math.abs(event.getChunk().getZ()
                                - point.getLocation().getBlock().getChunk()
                                .getZ()) > WirelessRedstone.config
                                .getChunkUnloadRange()) {
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
