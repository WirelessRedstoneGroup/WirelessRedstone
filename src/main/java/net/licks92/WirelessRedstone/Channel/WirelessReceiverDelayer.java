package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("WirelessReceiverDelayer")
public class WirelessReceiverDelayer extends WirelessReceiver {
    int delay;

    public WirelessReceiverDelayer(Map<String,Object> map) {
        super(map);
        setDelay((Integer) map.get("delay"));
    }

    public WirelessReceiverDelayer(final int delay) {
        super();
        this.delay = delay;
    }

    @Override
    public Map<String,Object> serialize(){
        Map<String,Object> map = super.serialize();
        map.put("delay", getDelay());
        return map;
    }

    @Override
    public void turnOn(final String channelName) {
        int delayInTicks = delay / 50;
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("WirelessRedstone"), new Runnable() {
            @Override
            public void run() {
                superTurnOn(channelName);
            }
        }, delayInTicks);
    }

    private void superTurnOn(final String channelName) {
        super.turnOn(channelName);
    }

    @Override
    public void turnOff(final String channelName) {
        int delayInTicks = delay / 50;
        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                superTurnOff(channelName);
            }
        }, delayInTicks);
    }

    @Override
    public void changeSignContent(final Block block, final String channelName){
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.strings.tagsReceiverDelayerType.get(0));
        sign.setLine(3, Integer.toString(delay));
        sign.update();
    }

    private void superTurnOff(final String channelName) {
        super.turnOff(channelName);
    }

    /**
     * @param delay - Sets the delay of the delayer.
     */
    public void setDelay(final int delay) {
        this.delay = delay;
    }

    /**
     * @return The delay of the delayer.
     */
    public int getDelay() {
        return this.delay;
    }
}
