package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SignManager {

    public boolean hasAccessToChannel(Player player, String channelName) {
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);

        return channel.getOwners().contains(player.getName()) || player.hasPermission(Permissions.isWirelessAdmin);
    }

    public void placeSign(String channelName, Location location, SignType type) {
        placeSign(channelName, location, type, 0);
    }

    public void placeSign(String channelName, Location location, SignType type, int extraData) {
        if (!(location.getBlock().getType() == CompatMaterial.SIGN.getMaterial()
                || location.getBlock().getType() == CompatMaterial.WALL_SIGN.getMaterial()))
            location.getBlock().setType(CompatMaterial.SIGN.getMaterial());

        Sign sign = (Sign) location.getBlock().getState();
        sign.setLine(1, channelName);

        switch (type) {
            case TRANSMITTER:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsTransmitter.get(0));
                sign.update();
                break;
            case SCREEN:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsScreen.get(0));
                sign.update();
                break;
            case RECEIVER:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverDefaultType.get(0));
                sign.update();
                break;
            case RECEIVER_INVERTER:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverInverterType.get(0));
                sign.update();
                break;
            case RECEIVER_SWITCH:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverSwitchType.get(0));
                sign.update();
                break;
            case RECEIVER_DELAYER:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverDelayerType.get(0));
                sign.setLine(3, Integer.toString(extraData));
                sign.update();
                break;
            case RECEIVER_CLOCK:
                sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
                sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverClockType.get(0));
                sign.setLine(3, Integer.toString(extraData));
                sign.update();
                break;
        }
    }

}
