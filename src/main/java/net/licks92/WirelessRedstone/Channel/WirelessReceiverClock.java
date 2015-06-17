package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@SerializableAs("WirelessReceiverClock")
public class WirelessReceiverClock extends WirelessReceiver {
    int delay;

    public WirelessReceiverClock(int delay) {
        super();
        this.delay = delay;
    }

    public WirelessReceiverClock(Map<String, Object> map) {
        super(map);
    }

    @Override
    public void turnOn(final String channelName) {
        WirelessRedstone.getWRLogger().debug("Clock started by: " + channelName);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(WirelessRedstone.getInstance(), new Runnable() {
            boolean b = false;
            @Override
            public void run() {
//                WirelessRedstone.getWRLogger().debug("Clock " + channelName + ", state is "
//                        + b);
                if (b) {
                    superTurnOff(channelName);
                } else {
                    superTurnOn(channelName);
                }
                b = !b;
            }
        }, 0L, this.delay / 50);
        WirelessRedstone.config.getWirelessChannel(channelName).startClock(task);
    }

    @Override
    public void turnOff(final String channelName) {
        WirelessRedstone.config.getWirelessChannel(channelName).stopClock();
        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                superTurnOff(channelName);
            }
        }, 2L);
    }

    @Override
    public void changeSignContent(Block block, String channelName){
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.strings.tagsReceiverClockType.get(0));
        sign.setLine(3, Integer.toString(delay));
        sign.update();
    }

    private void superTurnOn(String channelName) {
        super.turnOn(channelName);
    }

    private void superTurnOff(String channelName) {
        super.turnOff(channelName);
    }

    /**
     * @param delay - Sets the delay of the delayer.
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * @return The delay of the delayer.
     */
    public int getDelay() {
        return this.delay;
    }
}
