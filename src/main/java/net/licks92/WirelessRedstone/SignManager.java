package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessPoint;
import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverClock;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverDelayer;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverInverter;
import net.licks92.WirelessRedstone.Signs.WirelessReceiverSwitch;
import net.licks92.WirelessRedstone.Signs.WirelessScreen;
import net.licks92.WirelessRedstone.Signs.WirelessTransmitter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;

public class SignManager {

    public boolean hasAccessToChannel(Player player, String channelName) {
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);

        return channel.getOwners().contains(player.getName()) || player.hasPermission(Permissions.isWirelessAdmin);
    }

    public boolean canPlaceSign(Player player, SignType signType) {
        if (player.isOp()) {
            return true;
        }

        if (signType == SignType.TRANSMITTER) {
            return player.hasPermission(Permissions.canCreateTransmitter);
        } else if (signType == SignType.SCREEN) {
            return player.hasPermission(Permissions.canCreateScreen);
        } else {
            return player.hasPermission(Permissions.canCreateReceiver);
        }
    }

    public boolean placeSign(String channelName, Location location, SignType type) {
        return placeSign(channelName, location, type, 0);
    }

    public boolean placeSign(String channelName, Location location, SignType type, int extraData) {
        if (location.getBlock().getType() != Material.AIR) {
            return false;
        }

        if (!(location.getBlock().getType() == CompatMaterial.SIGN.getMaterial()
                || location.getBlock().getType() == CompatMaterial.WALL_SIGN.getMaterial())) {
            location.getBlock().setType(CompatMaterial.SIGN.getMaterial());
        }

        Sign sign = (Sign) location.getBlock().getState();
        org.bukkit.material.Sign signData = new org.bukkit.material.Sign(Material.SIGN);
        signData.setFacingDirection(Utils.yawToFace(location.getYaw(), false));
        sign.setData(signData);
        sign.update();
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

        return true;
    }

    /**
     * Save a WirelessRedstone sign to the database.
     *
     * @param channelName Name of the WirelessRedstone channel
     * @param block Place where the block is located
     * @param type What WirelessPoint type
     * @param direction Which direction is the sign facing
     * @param owners All the owners of the channel
     * @param delay Amount of delay for clock & delayer; this can be 0 if it is not one of these types
     * @return Return value<br>
     *  0 - Success; extended a channel
     *  1 - Success; created a channel
     */
    public int registerSign(String channelName, Block block, SignType type, BlockFace direction, List<String> owners, int delay) {
        int result = 0;
        if (WirelessRedstone.getStorageManager().getChannel(channelName) == null) {
            result = 1;
            WirelessRedstone.getStorage().createChannel(new WirelessChannel(channelName, owners));
        }

        boolean isWallSign = block.getType() == CompatMaterial.WALL_SIGN.getMaterial();

        WirelessPoint point = null;
        switch (type) {
            case TRANSMITTER:
                point = new WirelessTransmitter(
                        block.getLocation().getBlockX(), 
                        block.getLocation().getBlockY(), 
                        block.getLocation().getBlockZ(), 
                        block.getLocation().getWorld().getName(), 
                        isWallSign,
                        direction,
                        owners.get(0)
                );
                break;
            case SCREEN:
                point = new WirelessScreen(
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY(),
                        block.getLocation().getBlockZ(),
                        block.getLocation().getWorld().getName(),
                        isWallSign,
                        direction,
                        owners.get(0)
                );
                break;
            case RECEIVER:
                point = new WirelessReceiver(
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY(),
                        block.getLocation().getBlockZ(),
                        block.getLocation().getWorld().getName(),
                        isWallSign,
                        direction,
                        owners.get(0)
                );
                break;
            case RECEIVER_INVERTER:
                point = new WirelessReceiverInverter(
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY(),
                        block.getLocation().getBlockZ(),
                        block.getLocation().getWorld().getName(),
                        isWallSign,
                        direction,
                        owners.get(0)
                );
                break;
            case RECEIVER_SWITCH:
                point = new WirelessReceiverSwitch(
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY(),
                        block.getLocation().getBlockZ(),
                        block.getLocation().getWorld().getName(),
                        isWallSign,
                        direction,
                        owners.get(0)
                );
                break;
            case RECEIVER_DELAYER:
                point = new WirelessReceiverDelayer(
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY(),
                        block.getLocation().getBlockZ(),
                        block.getLocation().getWorld().getName(),
                        isWallSign,
                        direction,
                        owners.get(0),
                        delay
                );
                break;
            case RECEIVER_CLOCK:
                point = new WirelessReceiverClock(
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY(),
                        block.getLocation().getBlockZ(),
                        block.getLocation().getWorld().getName(),
                        isWallSign,
                        direction,
                        owners.get(0),
                        delay
                );
                break;
        }

        WirelessRedstone.getStorage().createWirelessPoint(channelName, point);
        return result;
    }

}
