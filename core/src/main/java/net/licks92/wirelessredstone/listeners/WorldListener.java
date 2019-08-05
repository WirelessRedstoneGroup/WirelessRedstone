package net.licks92.wirelessredstone.listeners;

import org.bukkit.event.Listener;

public class WorldListener implements Listener {

    // Method borrowed from MinecraftMania! Credits to Afforess!
    // https://github.com/Afforess/MinecartMania/blob/master/src/com/afforess/minecartmaniacore/api/MinecartManiaCoreWorldListener.java
//    @EventHandler
//    public void on(ChunkUnloadEvent event) {
//        if (!event.isCancelled()) {
//            if (ConfigManager.getConfig().getCancelChunkUnload()) {
//                try {
//                    Collection<WirelessPoint> points = WirelessRedstone.getStorageManager().getAllSigns();
//                    for (WirelessPoint point : points) {
//                        if (Math.abs(event.getChunk().getX() - point.getLocation().getBlock().getChunk().getX()) > ConfigManager.getConfig().getCancelChunkUnloadRange()) {
//                            continue;
//                        }
//                        if (Math.abs(event.getChunk().getZ() - point.getLocation().getBlock().getChunk().getZ()) > ConfigManager.getConfig().getCancelChunkUnloadRange()) {
//                            continue;
//                        }
//
//                        event.setCancelled(true);
//                        return;
//                    }
//                } catch (Exception e) {
//                    WirelessRedstone.getWRLogger().debug(e.toString());
//                }
//            }
//        }
//    }

}
