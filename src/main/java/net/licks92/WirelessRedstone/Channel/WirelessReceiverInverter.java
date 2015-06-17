package net.licks92.WirelessRedstone.Channel;

import java.util.Map;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("WirelessReceiverInverter")
public class WirelessReceiverInverter extends WirelessReceiver implements IWirelessPoint {
    private static final long serialVersionUID = 1362491648424270188L;

    public WirelessReceiverInverter() {
        super();
    }

    public WirelessReceiverInverter(Map<String, Object> map) {
        super(map);
    }

    @Override
    public void turnOn(String channelName) {
        super.turnOff(channelName);
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.strings.tagsReceiverInverterType.get(0));
        sign.update();
    }

    @Override
    public void changeSignContent(Block block, String channelName){
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(2, WirelessRedstone.strings.tagsReceiverInverterType.get(0));
        sign.update();
    }

    @Override
    public void turnOff(String channelName) {
        super.turnOn(channelName);
    }
}
