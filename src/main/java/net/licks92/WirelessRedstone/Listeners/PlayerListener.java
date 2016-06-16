package net.licks92.WirelessRedstone.Listeners;

import net.gravitydevelopment.updater.Updater;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.*;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener{

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (Main.getPermissionsManager().isWirelessAdmin(event.getPlayer())) {
            if (Main.getUpdater().getResult() == Updater.UpdateResult.UPDATE_AVAILABLE
                    && ConfigManager.getConfig().getUpdateCheck()) {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendFeedback(Main.getStrings().newUpdateAvailable, event.getPlayer(), false);
                    }
                }, 2L); //This runnable makes sure this is the last message if the player joins
            }
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
        if (Main.getSignManager().isTransmitter(sign.getLine(0)))
            type = SignType.TRANSMITTER;
        else if (Main.getSignManager().isScreen(sign.getLine(0)))
            type = SignType.SCREEN;
        else if (Main.getSignManager().isReceiver(sign.getLine(0)))
            type = Main.getSignManager().getReceiverType(sign.getLine(2));

        WirelessChannel channel = Main.getStorage().getWirelessChannel(sign.getLine(1));

        if (!signAlreadyExist(event.getClickedBlock().getLocation(), sign.getLine(1))) {
            if(type != null){
                if (sign.getLine(1) == null) {
                    event.getClickedBlock().setType(Material.AIR);
                    event.getPlayer().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(),
                            new ItemStack(Material.SIGN, 1));
                    Utils.sendFeedback("No channel name found", event.getPlayer(), true); //TODO: Add this string to the stringloader
                    return;
                }

                String cname = sign.getLine(1);

                if (!Main.getSignManager().hasAccessToChannel(event.getPlayer(), cname)) {
                    event.getClickedBlock().setType(Material.AIR);
                    event.getPlayer().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(),
                            new ItemStack(Material.SIGN, 1));
                    Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), false);
                    return;
                }

                switch (type) {
                    case TRANSMITTER:
                        if (!Main.getSignManager().addWirelessTransmitter(cname, event.getClickedBlock(), event.getPlayer())) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                        break;
                    case SCREEN:
                        if (!Main.getSignManager().addWirelessScreen(cname, event.getClickedBlock(), event.getPlayer())) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                        break;
                    case RECEIVER_NORMAL:
                        if (!Main.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                                event.getPlayer(), WirelessReceiver.Type.DEFAULT)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                        break;
                    case RECEIVER_INVERTER:
                        if (!Main.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                                event.getPlayer(), WirelessReceiver.Type.INVERTER)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                        break;
                    case RECEIVER_DELAYER:
                        if (!Main.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                                event.getPlayer(), WirelessReceiver.Type.DELAYER)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                        break;
                    case RECEIVER_CLOCK:
                        if (!Main.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                                event.getPlayer(), WirelessReceiver.Type.CLOCK)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                        break;
                    case RECEIVER_SWITCH:
                        if (!Main.getSignManager().addWirelessReceiver(cname, event.getClickedBlock(),
                                event.getPlayer(), WirelessReceiver.Type.SWITCH)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (channel == null) {
            return;
        }

        if (Main.getPermissionsManager().canActivateChannel(event.getPlayer())) {
            if (type == SignType.TRANSMITTER)
                channel.turnOn(ConfigManager.getConfig().getInteractTransmitterTime());
            else if (type == SignType.SCREEN)
                event.getPlayer().performCommand("wri " + channel.getName());
        } else {
            Utils.sendFeedback(Main.getStrings().playerDoesntHavePermission, event.getPlayer(), true, true);
        }
    }

    private boolean signAlreadyExist(Location loc, String schannel) {
        boolean exist = false;
        WirelessChannel channel = Main.getStorage().getWirelessChannel(schannel);
        if (channel == null)
            return false;

        for (WirelessReceiver receiver : channel.getReceivers()) {
            if (Utils.sameLocation(loc, receiver.getLocation())) {
                exist = true;
                break;
            }
        }

        if(!exist) {
            for (WirelessTransmitter transmitter : channel.getTransmitters()) {
                if (Utils.sameLocation(loc, transmitter.getLocation())) {
                    exist = true;
                    break;
                }
            }
        }

        if(!exist) {
            for (WirelessScreen screen : channel.getScreens()) {
                if (Utils.sameLocation(loc, screen.getLocation())) {
                    exist = true;
                    break;
                }
            }
        }
        return exist;
    }
}
