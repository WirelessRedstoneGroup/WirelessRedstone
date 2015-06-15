package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@SerializableAs("WirelessReceiverClock")
public class WirelessReceiverClock extends WirelessReceiver {
    private static final long serialVersionUID = -2955411933245551990L;
    int delay;
    BukkitTask task;

    public WirelessReceiverClock(int delay) {
        super();
        this.delay = delay;
    }

    public WirelessReceiverClock(Map<String, Object> map) {
        super(map);
    }

    @Override
    public void turnOn(final String channelName) {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                if(getState()){
                    superTurnOff(channelName);
                } else {
                    superTurnOn(channelName);
                }
            }
        }, 0L, delay);
    }

    private void superTurnOn(String channelName) {
        super.turnOn(channelName);
    }

    @Override
    public void turnOff(final String channelName) {
        task.cancel();
        task = null;
        superTurnOff(channelName);
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
