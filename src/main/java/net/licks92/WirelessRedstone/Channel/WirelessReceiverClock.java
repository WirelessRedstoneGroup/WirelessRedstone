package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@SerializableAs("WirelessReceiverClock")
public class WirelessReceiverClock extends WirelessReceiver {
    int delay, taskId;
    boolean isRunning;

    public WirelessReceiverClock(int delay) {
        super();
        this.delay = delay;
        this.taskId = 0;
        this.isRunning = false;
    }

    public WirelessReceiverClock(Map<String, Object> map) {
        super(map);
    }

    @Override
    public void turnOn(final String channelName) {
        if (isRunning) {
            return;
        }
        isRunning = true;
        WirelessRedstone.getWRLogger().debug("Clock started named by: " + channelName);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                WirelessRedstone.getWRLogger().debug("Clock " + channelName + ", state is " + getState() + ", task Id " + taskId);
                if (getState()) {
                    superTurnOff(channelName);
                } else {
                    superTurnOn(channelName);
                }
            }
        }, 0L, delay * 20);
        this.taskId = task.getTaskId();
    }

    @Override
    public void turnOff(final String channelName) {
        if (!isRunning) {
            return;
        }
        Bukkit.getScheduler().cancelTask(this.taskId);
        WirelessRedstone.getWRLogger().debug("Clock stopped named by: " + channelName);
        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                superTurnOff(channelName);
            }
        }, 2L);
        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                Sign sign = (Sign) getLocation().getBlock().getState();
                sign.setLine(2, WirelessRedstone.strings.tagsReceiverClockType.get(0));
                sign.setLine(3, Integer.toString(delay));
                sign.update();
            }
        }, 4L);
        isRunning = false;
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
