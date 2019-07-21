package net.licks92.WirelessRedstone.WorldEdit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;

public class WorldEditHooker {

    @Subscribe
    public void wrapForLogging(EditSessionEvent event) {
        Actor actor = event.getActor();
        World world = event.getWorld();
        if ((actor != null) && (event.getStage().equals(EditSession.Stage.BEFORE_CHANGE))) {
            event.setExtent(new WorldEditLogger(actor, world, event.getExtent()));
        }
    }

    public static void register() {
        try {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(WirelessRedstone.getInstance(), new Runnable() {
                public void run() {
                    try {
                        WorldEditHooker worldEditHooker = new WorldEditHooker();
                        WirelessRedstone.getInstance().setWorldEditHooker(worldEditHooker);
                        WorldEdit.getInstance().getEventBus().register(worldEditHooker);
                    } catch (Exception e) {
                        WirelessRedstone.getWRLogger().severe("Error while hooking worldedit");
                    }
                }
            }, 0L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unRegister(){
        try {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(WirelessRedstone.getInstance(), new Runnable() {
                public void run() {
                    try {
                        WorldEdit.getInstance().getEventBus().unregister(WirelessRedstone.getInstance().getWorldEditHooker());
                    } catch (Exception e) {
                        WirelessRedstone.getWRLogger().severe("Error while unhooking worldedit");
                    }
                }
            }, 0L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
