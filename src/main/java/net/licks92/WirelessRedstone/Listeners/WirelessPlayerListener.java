package net.licks92.WirelessRedstone.Listeners;

import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.licks92.WirelessRedstone.Channel.*;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class WirelessPlayerListener implements Listener {
    private final WirelessRedstone plugin;

    public WirelessPlayerListener(final WirelessRedstone r_plugin) {
        plugin = r_plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        /*
         * Check for updates and notify the admins.
		 */

        if (plugin.permissions.isWirelessAdmin(event.getPlayer())) {
            if (plugin.updater.getResult() == UpdateResult.UPDATE_AVAILABLE
                    && WirelessRedstone.config.doCheckForUpdates()) {
                event.getPlayer().sendMessage(
                        WirelessRedstone.strings.newUpdateAvailable);
            }
        }
    }

    /**
     * Used for handling right click on a transmitter. If a player interacts
     * with a transmitter, it will turn on the channel for a given time.
     *
     * @param event
     */
    @EventHandler
    public void onPlayerRightClick(final PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            return;
        }

        if (!(event.getClickedBlock().getState() instanceof Sign))
            return;

        Sign sign = (Sign) event.getClickedBlock().getState();

        if (sign.getLine(0) == null || sign.getLine(0).equals("")
                || sign.getLine(1) == null || sign.getLine(1).equals(""))
            return;

        String type = "";
        if (WirelessRedstone.WireBox.isTransmitter(sign.getLine(0)))
            type = "transmitter";
        else if (WirelessRedstone.WireBox.isScreen(sign.getLine(0)))
            type = "screen";

        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(sign.getLine(1));

        if (!signAlreadyExist(event.getClickedBlock().getLocation(), sign.getLine(1))) {
            if (WirelessRedstone.WireBox.isReceiver(sign
                    .getLine(0))
                    || WirelessRedstone.WireBox.isTransmitter(sign
                    .getLine(0))
                    || WirelessRedstone.WireBox.isScreen(sign
                    .getLine(0))) {
                if (!plugin.permissions.canCreateReceiver(event
                        .getPlayer())
                        || !plugin.permissions
                        .canCreateTransmitter(event
                                .getPlayer())
                        || !plugin.permissions
                        .canCreateScreen(event.getPlayer())) {
                    event.getClickedBlock().setType(Material.AIR);
                    event.getPlayer()
                            .getWorld()
                            .dropItemNaturally(
                                    event.getClickedBlock().getLocation(),
                                    new ItemStack(Material.SIGN, 1));
                    event.getPlayer()
                            .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                    WirelessRedstone.strings.playerCannotCreateSign);
                    return;
                }
                if (sign.getLine(1) == null) {
                    event.getClickedBlock().setType(Material.AIR);
                    event.getPlayer()
                            .getWorld()
                            .dropItemNaturally(
                                    event.getClickedBlock().getLocation(),
                                    new ItemStack(Material.SIGN, 1));
                    event.getPlayer()
                            .sendMessage(
                                    "[WirelessRedstone] No Channelname given!");
                    return;
                }

                String cname = sign.getLine(1);

                if (!WirelessRedstone.WireBox.hasAccessToChannel(
                        event.getPlayer(), cname)) {
                    event.getClickedBlock().setType(Material.AIR);
                    event.getPlayer()
                            .getWorld()
                            .dropItemNaturally(
                                    event.getClickedBlock().getLocation(),
                                    new ItemStack(Material.SIGN, 1));
                    event.getPlayer()
                            .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                    WirelessRedstone.strings.playerCannotCreateSign);
                    return;
                }

                if (WirelessRedstone.WireBox.isReceiver(sign
                        .getLine(0))) {
                    if (WirelessRedstone.WireBox
                            .isReceiverInverter(sign.getLine(2))) {
                        if (!WirelessRedstone.WireBox
                                .addWirelessReceiver(cname,
                                        event.getClickedBlock(),
                                        event.getPlayer(),
                                        WirelessReceiver.Type.Inverter)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                    } else if (WirelessRedstone.WireBox
                            .isReceiverDelayer(sign.getLine(2))) {
                        if (!WirelessRedstone.WireBox
                                .addWirelessReceiver(cname,
                                        event.getClickedBlock(),
                                        event.getPlayer(),
                                        WirelessReceiver.Type.Delayer)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                    } else if (WirelessRedstone.WireBox
                            .isReceiverClock(sign.getLine(2))) {
                        if (!WirelessRedstone.WireBox
                                .addWirelessReceiver(cname,
                                        event.getClickedBlock(),
                                        event.getPlayer(),
                                        WirelessReceiver.Type.Clock)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                    } else if (WirelessRedstone.WireBox
                            .isReceiverDefault(sign.getLine(2))) {
                        if (!WirelessRedstone.WireBox
                                .addWirelessReceiver(cname,
                                        event.getClickedBlock(),
                                        event.getPlayer(),
                                        WirelessReceiver.Type.Default)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                    } else {
                        if (!WirelessRedstone.WireBox
                                .addWirelessReceiver(cname,
                                        event.getClickedBlock(),
                                        event.getPlayer(),
                                        WirelessReceiver.Type.Default)) {
                            event.setCancelled(true);
                            event.getClickedBlock().breakNaturally();
                        }
                    }
                } else if (WirelessRedstone.WireBox
                        .isTransmitter(sign.getLine(0))) {
                    if (!WirelessRedstone.WireBox
                            .addWirelessTransmitter(cname,
                                    event.getClickedBlock(),
                                    event.getPlayer())) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                } else if (WirelessRedstone.WireBox.isScreen(sign
                        .getLine(0))) {
                    if (!WirelessRedstone.WireBox
                            .addWirelessScreen(cname,
                                    event.getClickedBlock(),
                                    event.getPlayer())) {
                        event.setCancelled(true);
                        event.getClickedBlock().breakNaturally();
                    }
                }
            }
        }

        if (channel == null) {
            return;
        }

        if (WirelessRedstone.getInstance().permissions.canActivateChannel(event.getPlayer())) {
            if (type.equalsIgnoreCase("transmitter"))
                channel.turnOn(WirelessRedstone.config.getInteractTransmitterTime());
            else if (type.equalsIgnoreCase("screen"))
                event.getPlayer().performCommand("wri " + channel.getName());
        } else {
            if (!WirelessRedstone.config.getSilentMode())
                event.getPlayer().sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.playerDoesntHavePermission);
        }
    }

    private boolean signAlreadyExist(Location loc, String schannel) {
        boolean exist = false;
        WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(schannel);
        if (channel == null)
            return false;

        for (WirelessReceiver receiver : channel.getReceivers()) {
            if ((loc.getWorld() == receiver.getLocation().getWorld())
                    && (loc.getBlockX() == receiver.getLocation().getBlockX())
                    && (loc.getBlockY() == receiver.getLocation().getBlockY())
                    && (loc.getBlockZ() == receiver.getLocation().getBlockZ())) {
                exist = true;
            }
        }
        for (WirelessTransmitter transmitter : channel.getTransmitters()) {
            if ((loc.getWorld() == transmitter.getLocation().getWorld())
                    && (loc.getBlockX() == transmitter.getLocation().getBlockX())
                    && (loc.getBlockY() == transmitter.getLocation().getBlockY())
                    && (loc.getBlockZ() == transmitter.getLocation().getBlockZ())) {
                exist = true;
            }
        }
        for (WirelessScreen screen : channel.getScreens()) {
            if ((loc.getWorld() == screen.getLocation().getWorld())
                    && (loc.getBlockX() == screen.getLocation().getBlockX())
                    && (loc.getBlockY() == screen.getLocation().getBlockY())
                    && (loc.getBlockZ() == screen.getLocation().getBlockZ())) {
                exist = true;
            }
        }
        return exist;
    }
}
