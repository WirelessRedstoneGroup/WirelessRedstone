package net.licks92.WirelessRedstone.Signs;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("WirelessReceiverSwitch")
public class WirelessReceiverSwitch extends WirelessReceiver {

    public WirelessReceiverSwitch(Map<String, Object> map) {
        super(map);
        setFirstState((Boolean) map.get("state"));
    }

    public WirelessReceiverSwitch(final boolean state) {
        super();
        try {
            Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    setFirstState(state); //The plugin must be fully loaded to call this method, otherwise you get a nullpointers
                }
            }, 1L);
        } catch (Exception ignored) {}
    }

    public WirelessReceiverSwitch() {
        super();
        setFirstState(false);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        boolean state;
        if (WirelessRedstone.getSignManager().switchState.get(getLocation()) != null)
            state = WirelessRedstone.getSignManager().switchState.get(getLocation());
        else
            state = false;
        map.put("state", state);
        return map;
    }

    @Override
    public void turnOn(String channelName) {
        boolean state;
        if (WirelessRedstone.getSignManager().switchState.get(getLocation()) != null)
            state = WirelessRedstone.getSignManager().switchState.get(getLocation());
        else
            state = false;

        if (state) {
            superTurnOff(channelName);
        } else {
            superTurnOn(channelName);
        }
        setState(!state);
    }

    private void superTurnOn(String channelName) {
        super.turnOn(channelName);
    }

    @Override
    public void turnOff(String channelName) {
        //Nothing, the turnOn function enables and disables the receiver
    }

    @Override
    public void changeSignContent(Block block, String channelName) {
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(0, WirelessRedstone.getStrings().tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.getStrings().tagsReceiverSwitchType.get(0));
        sign.update();
    }

    private void superTurnOff(String channelName) {
        super.turnOff(channelName);
    }

    public void setState(boolean state) {
        WirelessRedstone.getSignManager().switchState.put(getLocation(), state);
    }


    public boolean getState() {
        return WirelessRedstone.getSignManager().switchState.get(getLocation());
    }

    public void setFirstState(boolean state) {
        if (WirelessRedstone.getSignManager().switchState.get(getLocation()) == null)
            WirelessRedstone.getSignManager().switchState.put(getLocation(), state);
    }
}
