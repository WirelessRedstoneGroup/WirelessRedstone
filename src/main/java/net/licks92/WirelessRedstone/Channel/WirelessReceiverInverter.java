package net.licks92.WirelessRedstone.Channel;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class WirelessReceiverInverter extends WirelessReceiver {

    public WirelessReceiverInverter() {
        super();
    }


    @Override
    public void turnOn(final String channelName) {
        super.turnOff(channelName);
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.strings.tagsReceiverInverterType.get(0));
        sign.update();
    }

    @Override
    public void changeSignContent(final Block block, final String channelName){
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(2, WirelessRedstone.strings.tagsReceiverInverterType.get(0));
        sign.update();
    }

    @Override
    public void turnOff(final String channelName) {
        super.turnOn(channelName);
    }
}
