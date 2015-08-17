package net.licks92.WirelessRedstone.Channel;

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
        } catch (Exception ignored) {
        }
    }

    public WirelessReceiverSwitch() {
        super();
        setFirstState(false);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        boolean state;
        if (WirelessRedstone.WireBox.switchState.get(getLocation()) != null)
            state = WirelessRedstone.WireBox.switchState.get(getLocation());
        else
            state = false;
        map.put("state", state);
        return map;
    }

    @Override
    public void turnOn(final String channelName) {
        boolean state;
        if (WirelessRedstone.WireBox.switchState.get(getLocation()) != null)
            state = WirelessRedstone.WireBox.switchState.get(getLocation());
        else
            state = false;

        if (state) {
            superTurnOff(channelName);
        } else {
            superTurnOn(channelName);
        }
        setState(!state);
    }

    private void superTurnOn(final String channelName) {
        super.turnOn(channelName);
    }

    @Override
    public void turnOff(final String channelName) {
        //Nothing, the turnOn function enables and disables the receiver
    }

    @Override
    public void changeSignContent(final Block block, final String channelName) {
        Sign sign = (Sign) getLocation().getBlock().getState();
        sign.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.strings.tagsReceiverSwitchType.get(0));
        sign.update();
    }

    private void superTurnOff(final String channelName) {
        super.turnOff(channelName);
    }

    /**
     * @param state - Sets the state of the switch.
     */
    public void setState(final boolean state) {
        WirelessRedstone.WireBox.switchState.put(getLocation(), state);
    }


    public boolean getState() {
        return WirelessRedstone.WireBox.switchState.get(getLocation());
    }

    /**
     * @param state - Sets the state of the switch.
     */
    public void setFirstState(final boolean state) {
        if (WirelessRedstone.WireBox.switchState.get(getLocation()) == null)
            WirelessRedstone.WireBox.switchState.put(getLocation(), state);
    }
}
