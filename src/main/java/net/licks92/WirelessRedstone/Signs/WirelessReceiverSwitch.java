package net.licks92.WirelessRedstone.Signs;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("WirelessReceiverSwitch")
public class WirelessReceiverSwitch extends WirelessReceiver {

    private boolean isActive = false;

    public WirelessReceiverSwitch() {
        super();
    }

    public WirelessReceiverSwitch(Map<String, Object> map) {
        super(map);
        isActive = (Boolean) map.get("state");
    }

    @Override
    public void turnOn(String channelName) {
        if (isActive) {
            super.turnOff(channelName);
        } else {
            super.turnOn(channelName);
        }
    }

    @Override
    public void turnOff(String channelName) {
    }

    @Override
    public void changeSignContent(Block block, String channelName) {
        Sign sign = (Sign) block.getState();
        sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverSwitchType.get(0));
        sign.update(true);
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("state", isActive);
        return map;
    }

}
