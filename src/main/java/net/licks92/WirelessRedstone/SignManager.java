package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Signs.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignManager {

    public HashMap<Integer, String> clockTasks = new HashMap<Integer, String>();
    public HashMap<Location, Boolean> switchState = new HashMap<Location, Boolean>();
    public ArrayList<String> activeChannels = new ArrayList<String>();

    //Create

    public boolean addWirelessReceiver(String cname, Block cblock, Player player, WirelessReceiver.Type type) {
        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) cblock.getState().getData();
        WirelessRedstone.getWRLogger().debug("Adding a receiver at location "
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
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNameContainsInvalidCharacters, player, true);
            return false;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(cname);

        if (isWallSign) {
            isWallSign = true;
            if (!Utils.isValidWallLocation(cblock)) {
                Utils.sendFeedback(WirelessRedstone.getStrings().playerCannotCreateReceiverOnBlock, player, true);
                return false;
            }
        } else {
            if (!Utils.isValidLocation(cblock)) {
                Utils.sendFeedback(WirelessRedstone.getStrings().playerCannotCreateReceiverOnBlock, player, true);
                return false;
            }
        }

        Boolean newChannel = false;
        if (channel == null) {
            WirelessRedstone.getWRLogger().debug("The channel doesn't exist. Creating it and adding the receiver in it.");

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
                String clockDelayStr = ((Sign) (cblock.getState())).getLine(3);
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

        if (newChannel) {
            channel.addReceiver(receiver);

            if (!WirelessRedstone.getStorage().createWirelessChannel(channel)) {
                Utils.sendFeedback(WirelessRedstone.getStrings().channelNameContainsInvalidCharacters, player, true);
                return false;
            }

            Utils.sendFeedback(WirelessRedstone.getStrings().playerCreatedChannel, player, false);
        } else {
            WirelessRedstone.getStorage().createWirelessPoint(cname, receiver);
            Utils.sendFeedback(WirelessRedstone.getStrings().playerExtendedChannel, player, false);
        }

        WirelessRedstone.getGlobalCache().update();
        return true;

    }

    public boolean addWirelessTransmitter(String cname, Block cblock, Player player) {
        Location loc = cblock.getLocation();
        Boolean isWallSign = false;
        if (cblock.getType() == Material.WALL_SIGN) {
            isWallSign = true;
        }

        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) cblock.getState().getData();

        if (Utils.containsBadChar(cname)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNameContainsInvalidCharacters, player, true);
            return false;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(cname);

        Boolean newChannel = false;
        if (channel == null) {
            WirelessRedstone.getWRLogger().debug("The channel doesn't exist. Creating it and adding the transmitter in it.");

            channel = new WirelessChannel(cname);
            channel.addOwner(player.getName());
            newChannel = true;
        }

        WirelessTransmitter transmitter = new WirelessTransmitter();
        transmitter.setOwner(player.getName());
        transmitter.setWorld(loc.getWorld().getName());
        transmitter.setX(loc.getBlockX());
        transmitter.setY(loc.getBlockY());
        transmitter.setZ(loc.getBlockZ());
        BlockFace bfaceDirection = sign.getFacing();
        transmitter.setDirection(bfaceDirection);
        transmitter.setIsWallSign(isWallSign);

        if (newChannel) {
            channel.addTransmitter(transmitter);

            if (!WirelessRedstone.getStorage().createWirelessChannel(channel)) {
                Utils.sendFeedback(WirelessRedstone.getStrings().channelNameContainsInvalidCharacters, player, true);
                return false;
            }

            Utils.sendFeedback(WirelessRedstone.getStrings().playerCreatedChannel, player, false);
        } else {
            WirelessRedstone.getStorage().createWirelessPoint(cname, transmitter);
            Utils.sendFeedback(WirelessRedstone.getStrings().playerExtendedChannel, player, false);
        }

        WirelessRedstone.getGlobalCache().update();
        return true;
    }

    public boolean addWirelessScreen(String cname, Block cblock, Player player) {
        Location loc = cblock.getLocation();
        Boolean isWallSign = false;
        if (cblock.getType() == Material.WALL_SIGN) {
            isWallSign = true;
        }

        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) cblock.getState().getData();

        if (Utils.containsBadChar(cname)) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelNameContainsInvalidCharacters, player, true);
            return false;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(cname);

        Boolean newChannel = false;
        if (channel == null) {
            WirelessRedstone.getWRLogger().debug("The channel doesn't exist. Creating it and adding the screen in it.");

            channel = new WirelessChannel(cname);
            channel.addOwner(player.getName());
            newChannel = true;
        }

        WirelessScreen screen = new WirelessScreen();
        screen.setOwner(player.getName());
        screen.setWorld(loc.getWorld().getName());
        screen.setX(loc.getBlockX());
        screen.setY(loc.getBlockY());
        screen.setZ(loc.getBlockZ());
        BlockFace bfaceDirection = sign.getFacing();
        screen.setDirection(bfaceDirection);
        screen.setIsWallSign(isWallSign);

        if (newChannel) {
            channel.addScreen(screen);

            if (!WirelessRedstone.getStorage().createWirelessChannel(channel)) {
                Utils.sendFeedback(WirelessRedstone.getStrings().channelNameContainsInvalidCharacters, player, true);
                return false;
            }

            Utils.sendFeedback(WirelessRedstone.getStrings().playerCreatedChannel, player, false);
        } else {
            WirelessRedstone.getStorage().createWirelessPoint(cname, screen);
            Utils.sendFeedback(WirelessRedstone.getStrings().playerExtendedChannel, player, false);
        }

        WirelessRedstone.getGlobalCache().update();
        return true;
    }

    //Remove

    public boolean removeWirelessReceiver(String cname, Location loc) {
        if (WirelessRedstone.getStorage().removeWirelessReceiver(cname, loc)) {
            WirelessRedstone.getGlobalCache().update();
            return true;
        } else
            return false;
    }

    public boolean removeWirelessTransmitter(String cname, Location loc) {
        if (WirelessRedstone.getStorage().removeWirelessTransmitter(cname, loc)) {
            WirelessRedstone.getGlobalCache().update();
            return true;
        } else
            return false;
    }

    public boolean removeWirelessScreen(String cname, Location loc) {
        if (WirelessRedstone.getStorage().removeWirelessScreen(cname, loc)) {
            WirelessRedstone.getGlobalCache().update();
            return true;
        } else
            return false;
    }

    /*
    * This will ONLY remove the sign. It does NOT remove it from the storage.
    * */
    public void removeSigns(WirelessChannel channel) {
        try {
            for (IWirelessPoint point : channel.getReceivers()) {
                point.getLocation().getBlock().setType(Material.AIR);
            }
        } catch (NullPointerException ignored) {
        } //When there isn't any receiver, it'll throw this exception.

        try {
            for (IWirelessPoint point : channel.getTransmitters()) {
                point.getLocation().getBlock().setType(Material.AIR);
            }
        } catch (NullPointerException ignored) {
        } //When there isn't any transmitter, it'll throw this exception.

        try {
            for (IWirelessPoint point : channel.getScreens()) {
                point.getLocation().getBlock().setType(Material.AIR);
            }
        } catch (NullPointerException ignored) {
        } //When there isn't any screen, it'll throw this exception.
    }

    //Getters

    public ArrayList<Location> getReceiverLocations(WirelessChannel channel) {
        ArrayList<Location> returnlist = new ArrayList<Location>();
        for (WirelessReceiver receiver : channel.getReceivers()) {
            returnlist.add(receiver.getLocation());
        }
        return returnlist;
    }

    public ArrayList<Location> getReceiverLocations(String channelname) {
        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(channelname);
        if (channel == null)
            return new ArrayList<Location>();

        return getReceiverLocations(channel);
    }

    public ArrayList<Location> getScreenLocations(WirelessChannel channel) {
        ArrayList<Location> returnlist = new ArrayList<Location>();
        for (WirelessScreen screen : channel.getScreens()) {
            returnlist.add(screen.getLocation());
        }
        return returnlist;
    }

    public ArrayList<Location> getScreenLocations(String channelname) {
        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(channelname);
        if (channel == null)
            return new ArrayList<Location>();

        return getScreenLocations(channel);
    }

    public SignType getSignType(String data) {
        return getSignType(data, null);
    }

    public SignType getSignType(String data, String extraData) {
        if (isTransmitter(data))
            return SignType.TRANSMITTER;

        if (isScreen(data))
            return SignType.SCREEN;

        if (isReceiver(data))
            if (extraData == null)
                return SignType.RECEIVER;
            else
                return getReceiverType(extraData);

        return null;
    }

    public SignType getReceiverType(String data) {
        if (data == null)
            return SignType.RECEIVER_NORMAL;

        if (isReceiverInverter(data))
            return SignType.RECEIVER_INVERTER;

        if (isReceiverDelayer(data))
            return SignType.RECEIVER_DELAYER;

        if (isReceiverClock(data))
            return SignType.RECEIVER_CLOCK;

        if (isReceiverSwitch(data))
            return SignType.RECEIVER_SWITCH;

        return SignType.RECEIVER_NORMAL;
    }

    //Checkers

    public boolean isTransmitter(String data) {
        for (String tag : WirelessRedstone.getStrings().tagsTransmitter) {
            if (data.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isScreen(String data) {
        for (String tag : WirelessRedstone.getStrings().tagsScreen) {
            if (data.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReceiver(String data) {
        for (String tag : WirelessRedstone.getStrings().tagsReceiver) {
            if (data.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReceiverDefault(String data) {
        for (String tag : WirelessRedstone.getStrings().tagsReceiverDefaultType) {
            if (data.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReceiverInverter(String data) {
        for (String tag : WirelessRedstone.getStrings().tagsReceiverInverterType) {
            if (data.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReceiverDelayer(String data) {
        for (String tag : WirelessRedstone.getStrings().tagsReceiverDelayerType) {
            if (data.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReceiverClock(String data) {
        for (String tag : WirelessRedstone.getStrings().tagsReceiverClockType) {
            if (data.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReceiverSwitch(String data) {
        for (String tag : WirelessRedstone.getStrings().tagsReceiverSwitchType) {
            if (data.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAccessToChannel(Player player, String channelname) {
        if (WirelessRedstone.getStorage().getWirelessChannel(channelname) != null) {
            return WirelessRedstone.getPermissionsManager().isWirelessAdmin(player)
                    || WirelessRedstone.getStorage().getWirelessChannel(channelname).getOwners().contains(player.getName());
        }
        return true;
    }

    // Utils

    public void stopAllClocks() {
        ArrayList<Integer> remove = new ArrayList<Integer>();
        for (Map.Entry<Integer, String> task : WirelessRedstone.getSignManager().clockTasks.entrySet()) {
            Bukkit.getScheduler().cancelTask(task.getKey());
            remove.add(task.getKey());
            WirelessRedstone.getWRLogger().debug("Stopped clock task " + task);
        }
        for (Integer i : remove) {
            WirelessRedstone.getSignManager().clockTasks.remove(i);
        }
        remove.clear();
    }
}
