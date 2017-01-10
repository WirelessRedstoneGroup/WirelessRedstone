package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Signs.*;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (WirelessRedstone.getPermissionsManager().isWirelessAdmin(event.getPlayer()) && WirelessRedstone.getUpdater() != null) {
            Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    WirelessRedstone.getUpdater().showUpdate(event.getPlayer());
                }
            }, 2L); //This runnable makes sure this is the last message if the player joins
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            return;
        }

        if (!(event.getClickedBlock().getState() instanceof Sign))
            return;

        Sign sign = (Sign) event.getClickedBlock().getState();

        if (sign.getLine(0) == null || sign.getLine(0).equals("")
                || sign.getLine(1) == null || sign.getLine(1).equals(""))
            return;

        SignType type = null;
        if (WirelessRedstone.getSignManager().isTransmitter(sign.getLine(0)))
            type = SignType.TRANSMITTER;
        else if (WirelessRedstone.getSignManager().isScreen(sign.getLine(0)))
            type = SignType.SCREEN;
        else if (WirelessRedstone.getSignManager().isReceiver(sign.getLine(0)))
            type = WirelessRedstone.getSignManager().getReceiverType(sign.getLine(2));

        if (type == null) {
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(sign.getLine(1));

        if (!signAlreadyExist(event.getClickedBlock().getLocation(), sign.getLine(1))) {
            if (sign.getLine(1) == null) {
                event.getClickedBlock().breakNaturally();
                WirelessRedstone.getUtils().sendFeedback("No channel name found", event.getPlayer(), true); //TODO: Add this string to the stringloader
                return;
            }

            String cname = sign.getLine(1);

            if (!WirelessRedstone.getSignManager().hasAccessToChannel(event.getPlayer(), cname)) {
                event.getClickedBlock().breakNaturally();
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().playerCannotCreateSign, event.getPlayer(), false);
                return;
            }

            switch (type) {
                case TRANSMITTER:
                    if (!WirelessRedstone.getSignManager().addWirelessTransmitter(cname, event.getClickedBlock(), event.getPlayer())) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                    break;
                case SCREEN:
                    if (!WirelessRedstone.getSignManager().addWirelessScreen(cname, event.getClickedBlock(), event.getPlayer())) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                    break;
                case RECEIVER_NORMAL:
                    if (!WirelessRedstone.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                            event.getPlayer(), WirelessReceiver.Type.DEFAULT)) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                    break;
                case RECEIVER_INVERTER:
                    if (!WirelessRedstone.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                            event.getPlayer(), WirelessReceiver.Type.INVERTER)) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                    break;
                case RECEIVER_DELAYER:
                    if (!WirelessRedstone.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                            event.getPlayer(), WirelessReceiver.Type.DELAYER)) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                    break;
                case RECEIVER_CLOCK:
                    if (!WirelessRedstone.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                            event.getPlayer(), WirelessReceiver.Type.CLOCK)) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                    break;
                case RECEIVER_SWITCH:
                    if (!WirelessRedstone.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                            event.getPlayer(), WirelessReceiver.Type.SWITCH)) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                    break;
                default:
                    break;
            }
        }

        if (channel == null) {
            return;
        }

        if (WirelessRedstone.getPermissionsManager().canActivateChannel(event.getPlayer())) {
            if (type == SignType.TRANSMITTER)
                channel.turnOn(ConfigManager.getConfig().getInteractTransmitterTime());
            else if (type == SignType.SCREEN)
                event.getPlayer().performCommand("wr i " + channel.getName());
        } else {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().playerDoesntHavePermission, event.getPlayer(), true, true);
        }
    }

    private boolean signAlreadyExist(Location loc, String schannel) {
        boolean exist = false;
        WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(schannel);
        if (channel == null)
            return false;

        for (WirelessReceiver receiver : channel.getReceivers()) {
            if (WirelessRedstone.getUtils().sameLocation(loc, receiver.getLocation())) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                if (WirelessRedstone.getUtils().sameLocation(loc, transmitter.getLocation())) {
                    exist = true;
                    break;
                }
            }
        }

        if (!exist) {
            for (WirelessScreen screen : channel.getScreens()) {
                if (WirelessRedstone.getUtils().sameLocation(loc, screen.getLocation())) {
                    exist = true;
                    break;
                }
            }
        }
        return exist;
    }
}
