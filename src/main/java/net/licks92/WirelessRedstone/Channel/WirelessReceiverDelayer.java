package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("WirelessReceiverDelayer")
public class WirelessReceiverDelayer extends WirelessReceiver {
    private static final long serialVersionUID = -2955411933245551990L;
    int delay;

    public WirelessReceiverDelayer(int delay) {
        super();
        this.delay = delay;
    }

    public WirelessReceiverDelayer(Map<String, Object> map) {
        super(map);
    }

    @Override
    public void turnOn(final String channelName) {
        int delayInTicks = delay * 20;
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("WirelessRedstone"), new Runnable() {
            @Override
            public void run() {
                superTurnOn(channelName);
            }
        }, delayInTicks);
    }

    private void superTurnOn(String channelName) {
        super.turnOn(channelName);
    }

    @Override
    public void turnOff(final String channelName) {
        int delayInTicks = delay * 20;
        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                superTurnOff(channelName);
                Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        Sign sign = (Sign) getLocation().getBlock().getState();
                        sign.setLine(2, WirelessRedstone.strings.tagsReceiverDelayerType.get(0));
                        sign.setLine(3, Integer.toString(delay));
                        sign.update();
                    }
                }, 2l);
            }
        }, delayInTicks);
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
