package net.licks92.WirelessRedstone.Signs;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("WirelessReceiverDelayer")
public class WirelessReceiverDelayer extends WirelessReceiver {

    private Integer delay;

    public WirelessReceiverDelayer(Map<String,Object> map) {
        super(map);
        setDelay((Integer) map.get("delay"));
    }

    public WirelessReceiverDelayer(Integer delay) {
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

    private void superTurnOn(String channelName) {
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
    public void changeSignContent(Block block, String channelName){
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(0, WirelessRedstone.getStrings().tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.getStrings().tagsReceiverDelayerType.get(0));
        sign.setLine(3, Integer.toString(delay));
        sign.update();
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
