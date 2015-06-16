package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@SerializableAs("WirelessReceiverClock")
public class WirelessReceiverClock extends WirelessReceiver {
    int delay, task;

    public WirelessReceiverClock(int delay) {
        super();
        this.delay = delay;
        this.task = 0;
    }

    public WirelessReceiverClock(Map<String, Object> map) {
        super(map);
    }

    @Override
    public void turnOn(final String channelName) {
        Bukkit.broadcastMessage("Clock runner started:" + channelName);
        this.task = Bukkit.getScheduler().runTaskTimer(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (!getState()) {
                    superTurnOff(channelName);
                } else {
                    superTurnOn(channelName);
                }
            }
        }, 0L, delay * 10).getTaskId();
    }

    private void superTurnOn(String channelName) {
        super.turnOn(channelName);
    }

    @Override
    public void turnOff(final String channelName) {
        Bukkit.getScheduler().cancelTask(this.task);
        Bukkit.broadcastMessage("Clock runner stopped:" + channelName);
        superTurnOff(channelName);
        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                Sign sign = (Sign) getLocation().getBlock().getState();
                sign.setLine(2, WirelessRedstone.strings.tagsReceiverClockType.get(0));
                sign.setLine(3, Integer.toString(delay));
                sign.update();
            }
        }, 2L);
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
