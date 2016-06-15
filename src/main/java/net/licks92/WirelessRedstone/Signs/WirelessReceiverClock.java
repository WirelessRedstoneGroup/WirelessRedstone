package net.licks92.WirelessRedstone.Signs;

import net.licks92.WirelessRedstone.Main;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@SerializableAs("WirelessReceiverClock")
public class WirelessReceiverClock extends WirelessReceiver {

    private Integer delay;

    public WirelessReceiverClock(Map<String,Object> map) {
        super(map);
        setDelay((Integer) map.get("delay"));
    }

    public WirelessReceiverClock(int delay) {
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
        Main.getWRLogger().debug("Clock started on channel: " + channelName);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), new Runnable() {
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
        Main.getStorage().getWirelessChannel(channelName).startClock(task);
    }

    @Override
    public void turnOff(final String channelName) {
        Main.getStorage().getWirelessChannel(channelName).stopClock();
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                superTurnOff(channelName);
            }
        }, 2L);
    }

    @Override
    public void changeSignContent(Block block, String channelName){
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(0, Main.getStrings().tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, Main.getStrings().tagsReceiverClockType.get(0));
        sign.setLine(3, Integer.toString(delay));
        sign.update();
    }

    private void superTurnOn(String channelName) {
        super.turnOn(channelName);
    }

    private void superTurnOff(String channelName) {
        super.turnOff(channelName);
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return this.delay;
    }

}
