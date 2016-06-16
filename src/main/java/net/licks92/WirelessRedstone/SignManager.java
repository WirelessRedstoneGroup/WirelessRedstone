package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Signs.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class SignManager {

    public HashMap<Integer, String> clockTasks = new HashMap<Integer, String>();
    public HashMap<Location, Boolean> switchState = new HashMap<Location, Boolean>();
    public ArrayList<String> activeChannels = new ArrayList<String>();

    public boolean addWirelessReceiver(String cname, Block cblock, Player player, WirelessReceiver.Type type) {
        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) cblock
                .getState().getData();
        Main.getWRLogger().debug("Adding a receiver at location "
                + cblock.getLocation().getBlockX() + ","
                + cblock.getLocation().getBlockY() + ","
                + cblock.getLocation().getBlockZ() + ", facing "
                + sign.getFacing() + " in the world "
                + cblock.getLocation().getWorld().getName()
                + " with the channel name " + cname
                + " and with the type " + type + " by the player "
                + player.getName());

        Location loc = cblock.getLocation();
        Boolean isWallSign = (cblock.getType() == Material.WALL_SIGN);

        if (Utils.containsBadChar(cname)) {
            Utils.sendFeedback(Main.getStrings().channelNameContainsInvalidCaracters, player, true);
            return false;
        }

        WirelessChannel channel = Main.getStorage().getWirelessChannel(cname);

        if (isWallSign) {
            isWallSign = true;
            if (!Utils.isValidWallLocation(cblock)) {
                Utils.sendFeedback(Main.getStrings().playerCannotCreateReceiverOnBlock, player, true);
                return false;
            }
        } else {
            if (!Utils.isValidLocation(cblock)) {
                Utils.sendFeedback(Main.getStrings().playerCannotCreateReceiverOnBlock, player, true);
                return false;
            }
        }

        Boolean newChannel = false;
        if (channel == null) {
            Main.getWRLogger().debug("The channel doesn't exist. Creating it and adding the receiver in it.");

            channel = new WirelessChannel(cname);
            channel.addOwner(player.getName());
            newChannel = true;
        }

        WirelessReceiver receiver;
        switch (type) {
            case DEFAULT:
                receiver = new WirelessReceiver();
                break;

            case INVERTER:
                receiver = new WirelessReceiverInverter();
                break;

            case SWITCH:
                String stateStr = ((Sign) (cblock.getState())).getLine(3);
                boolean state;
                try {
                    state = Boolean.parseBoolean(stateStr);
                } catch (NumberFormatException ex) {
                    receiver = new WirelessReceiverSwitch();
                    break;
                }
                receiver = new WirelessReceiverSwitch(state);
                break;

            case DELAYER:
                String delayStr = ((Sign) (cblock.getState())).getLine(3);
                int delay;
                try {
                    delay = Integer.parseInt(delayStr);
                } catch (NumberFormatException ex) {
                    Utils.sendFeedback("The delay must be a number!", player, true); //TODO: Add these strings to the stringloader
                    return false;
                }
                if (delay < 50) {
                    Utils.sendFeedback("The delay must be at least 50ms", player, true);
                    return false;
                }
                receiver = new WirelessReceiverDelayer(delay);
                break;

            case CLOCK:
                String clockDelayStr = ((Sign) (cblock.getState()))
                        .getLine(3);
                int clockDelay;
                try {
                    clockDelay = Integer.parseInt(clockDelayStr);
                } catch (NumberFormatException ex) {
                    Utils.sendFeedback("The delay must be a number!", player, true); //TODO: Add these strings to the stringloader
                    return false;
                }
                if (clockDelay < 50) {
                    Utils.sendFeedback("The delay must be at least 50ms", player, true);
                    return false;
                }
                receiver = new WirelessReceiverClock(clockDelay);
                break;

            default:
                receiver = new WirelessReceiver();
                break;
        }

        receiver.setOwner(player.getName());
        receiver.setWorld(loc.getWorld().getName());
        receiver.setX(loc.getBlockX());
        receiver.setY(loc.getBlockY());
        receiver.setZ(loc.getBlockZ());
        BlockFace bfaceDirection = sign.getFacing();
        receiver.setDirection(bfaceDirection);
        receiver.setIsWallSign(isWallSign);
        channel.addReceiver(receiver);

        if (newChannel) {
            if (!Main.getStorage().createWirelessChannel(channel)) {
                Utils.sendFeedback(Main.getStrings().channelNameContainsInvalidCaracters, player, true);
                return false;
            }
            Utils.sendFeedback(Main.getStrings().playerCreatedChannel, player, false);
        } else {
            Main.getStorage().createWirelessPoint(cname, receiver);
            Utils.sendFeedback(Main.getStrings().playerExtendedChannel, player, false);
        }

        Main.getGlobalCache().update();
        return true;

    }
}
