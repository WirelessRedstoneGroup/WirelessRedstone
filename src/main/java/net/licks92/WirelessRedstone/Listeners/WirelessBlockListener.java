package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver.Type;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;

import java.util.ArrayList;

public class WirelessBlockListener implements Listener {
    private final WirelessRedstone plugin;

    public WirelessBlockListener(final WirelessRedstone r_plugin) {
        plugin = r_plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        if (WirelessRedstone.WireBox.isReceiver(event
                                .getLine(0))
                                || WirelessRedstone.WireBox.isTransmitter(event
                                .getLine(0))
                                || WirelessRedstone.WireBox.isScreen(event
                                .getLine(0))) {

                            if (!plugin.permissions.canCreateReceiver(event
                                    .getPlayer())
                                    || !plugin.permissions
                                    .canCreateTransmitter(event
                                            .getPlayer())
                                    || !plugin.permissions
                                    .canCreateScreen(event.getPlayer())) {
                                event.getBlock().setType(Material.AIR);
                                event.getPlayer()
                                        .getWorld()
                                        .dropItemNaturally(
                                                event.getBlock().getLocation(),
                                                new ItemStack(Material.SIGN, 1));
                                event.getPlayer()
                                        .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                                WirelessRedstone.strings.playerCannotCreateSign);
                                return;
                            }
                            if (event.getLine(1) == null) {
                                event.getBlock().breakNaturally();
                                event.getPlayer()
                                        .sendMessage(
                                                "[WirelessRedstone] No Channelname given!");
                                return;
                            }

                            String cname = event.getLine(1);

                            if (!WirelessRedstone.WireBox.hasAccessToChannel(
                                    event.getPlayer(), cname)) {
                                event.getBlock().setType(Material.AIR);
                                event.getPlayer()
                                        .getWorld()
                                        .dropItemNaturally(
                                                event.getBlock().getLocation(),
                                                new ItemStack(Material.SIGN, 1));
                                event.getPlayer()
                                        .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                                WirelessRedstone.strings.playerCannotCreateSign);
                                return;
                            }

                            if (WirelessRedstone.WireBox.isReceiver(event
                                    .getLine(0))) {

                                if (WirelessRedstone.WireBox
                                        .isReceiverInverter(event.getLine(2))) {
                                    if (!WirelessRedstone.WireBox
                                            .addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                                    event.getBlock(),
                                                    event.getPlayer(),
                                                    Type.Inverter)) {
                                        event.setCancelled(true);
                                        event.getBlock().breakNaturally();
                                    }
                                } else if (WirelessRedstone.WireBox
                                        .isReceiverDelayer(event.getLine(2))) {
                                    if (!WirelessRedstone.WireBox
                                            .addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                                    event.getBlock(),
                                                    event.getPlayer(),
                                                    Type.Delayer)) {
                                        event.setCancelled(true);
                                        event.getBlock().breakNaturally();
                                    }
                                } else if (WirelessRedstone.WireBox
                                        .isReceiverClock(event.getLine(2))) {
                                    if (!WirelessRedstone.WireBox
                                            .addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                                    event.getBlock(),
                                                    event.getPlayer(),
                                                    Type.Clock)) {
                                        event.setCancelled(true);
                                        event.getBlock().breakNaturally();
                                    }
                                } else if (WirelessRedstone.WireBox
                                        .isReceiverDefault(event.getLine(2))) {
                                    if (!WirelessRedstone.WireBox
                                            .addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                                    event.getBlock(),
                                                    event.getPlayer(),
                                                    Type.Default)) {
                                        event.setCancelled(true);
                                        event.getBlock().breakNaturally();
                                    }
                                } else {
                                    if (!WirelessRedstone.WireBox
                                            .addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                                    event.getBlock(),
                                                    event.getPlayer(),
                                                    Type.Default)) {
                                        event.setCancelled(true);
                                        event.getBlock().breakNaturally();
                                    }
                                }

                            } else if (WirelessRedstone.WireBox
                                    .isTransmitter(event.getLine(0))) {
                                if (!WirelessRedstone.WireBox
                                        .addWirelessTransmitter(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                                event.getBlock(),
                                                event.getPlayer())) {
                                    event.setCancelled(true);
                                    event.getBlock().breakNaturally();
                                }

                            } else if (WirelessRedstone.WireBox.isScreen(event
                                    .getLine(0))) {
                                if (!WirelessRedstone.WireBox
                                        .addWirelessScreen(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                                event.getBlock(),
                                                event.getPlayer())) {
                                    event.setCancelled(true);
                                    event.getBlock().breakNaturally();

                                }
                            }
                        }
                    }
                }, 2L);
    }

    @EventHandler
    public void onBlockPhysics(final BlockPhysicsEvent event) {
        if (event.getChangedType() == Material.REDSTONE_TORCH_ON) {
            // Why is this event here? :p
        }
    }

    @EventHandler
    public void onBlockRedstoneChange(final BlockRedstoneEvent event) {
        BlockFace bf = BlockFace.SELF;
        if (WirelessRedstone.getBukkitVersion().contains("v1_8")) {
            if (event.getBlock().getType() == Material.LEVER) {
                Lever l = (Lever) event.getBlock().getState().getData();
                Bukkit.getServer()
                        .getPluginManager()
                        .callEvent(
                                new BlockRedstoneEvent(event.getBlock()
                                        .getRelative(l.getAttachedFace()), event
                                        .getOldCurrent(), event.getNewCurrent()));
            }
            if (event.getBlock().getType() == Material.STONE_BUTTON
                    || event.getBlock().getType() == Material.WOOD_BUTTON) {
                Button b = (Button) event.getBlock().getState().getData();
                Bukkit.getServer()
                        .getPluginManager()
                        .callEvent(
                                new BlockRedstoneEvent(event.getBlock()
                                        .getRelative(b.getAttachedFace()), event
                                        .getOldCurrent(), event.getNewCurrent()));
            }
            if (event.getBlock().getType() == Material.DETECTOR_RAIL) {
                Bukkit.getServer()
                        .getPluginManager()
                        .callEvent(
                                new BlockRedstoneEvent(event.getBlock()
                                        .getRelative(BlockFace.DOWN), event
                                        .getOldCurrent(), event.getNewCurrent()));
            }
            if (event.getBlock().getType() == Material.WOOD_PLATE || event.getBlock().getType() == Material.STONE_PLATE
                    || event.getBlock().getType() == Material.IRON_PLATE || event.getBlock().getType() == Material.GOLD_PLATE) {
                Bukkit.getServer()
                        .getPluginManager()
                        .callEvent(
                                new BlockRedstoneEvent(event.getBlock()
                                        .getRelative(BlockFace.DOWN), event
                                        .getOldCurrent(), event.getNewCurrent()));
            }
//            if (event.getBlock().getType() == Material.DAYLIGHT_DETECTOR
//                    || event.getBlock().getType() == Material.DAYLIGHT_DETECTOR_INVERTED) {
//                Bukkit.getServer()
//                        .getPluginManager()
//                        .callEvent(
//                                new BlockRedstoneEvent(event.getBlock()
//                                        .getRelative(BlockFace.DOWN), event
//                                        .getOldCurrent(), event.getNewCurrent()));
//            }

            ArrayList<BlockFace> possibleBlockface = new ArrayList<BlockFace>();
            possibleBlockface.add(BlockFace.NORTH);
            possibleBlockface.add(BlockFace.EAST);
            possibleBlockface.add(BlockFace.SOUTH);
            possibleBlockface.add(BlockFace.WEST);
            possibleBlockface.add(BlockFace.UP);
            possibleBlockface.add(BlockFace.DOWN);

            for (BlockFace blockFace : possibleBlockface) {
                if (event.getBlock().getRelative(blockFace).getState() instanceof Sign) {
                    bf = blockFace;
                    break;
                }
            }

            if (bf == null)
                return;
        }

        if (!(event.getBlock().getRelative(bf).getState() instanceof Sign)) {
            return;
        }

        Sign signObject = (Sign) event.getBlock().getRelative(bf).getState();

        final WirelessChannel channel;
        // final WirelessChannel channel;

        // Lets check if the sign is a Transmitter and if the channel name not
        // is empty
        if (!WirelessRedstone.WireBox.isTransmitter(signObject.getLine(0))
                || signObject.getLine(1) == null || signObject.getLine(1).equals("")) {
            return;
        } else {
            channel = WirelessRedstone.config.getWirelessChannel(signObject
                    .getLine(1));
        }

        if (channel == null) {
            WirelessRedstone.getWRLogger().debug(
                    "The transmitter at location " + signObject.getX() + ","
                            + signObject.getY() + "," + signObject.getZ() + " "
                            + "in the world " + signObject.getWorld().getName()
                            + " is actually linked with a null channel.");
            return;
        }

        if (event.getBlock().getType() == Material.REDSTONE_WIRE && event.getNewCurrent() == 0) {
            final BlockFace finalBf = bf;
            Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    channel.toggle(event.getNewCurrent(), event.getBlock().getRelative(finalBf));
                }
            }, 1L);
        } else
            channel.toggle(event.getNewCurrent(), event.getBlock().getRelative(bf));
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if ((event.getBlock().getState() instanceof Sign)) {
            Sign signObject = (Sign) event.getBlock().getState();
            if (WirelessRedstone.WireBox.isReceiver(signObject.getLine(0))) {
                if (!WirelessRedstone.config.getSignDrop()) {
                    cancelEvent(event);
                }
                if (WirelessRedstone.WireBox.hasAccessToChannel(
                        event.getPlayer(), signObject.getLine(1))
                        && plugin.permissions.canRemoveReceiver(event
                        .getPlayer())) {
                    if (WirelessRedstone.WireBox.removeWirelessReceiver(
                            signObject.getLine(1), event.getBlock()
                                    .getLocation())) {
                        if (WirelessRedstone.config.isChannelEmpty(WirelessRedstone.config
                                .getWirelessChannel(signObject.getLine(1)))) {
                            WirelessRedstone.config
                                    .removeWirelessChannel(signObject
                                            .getLine(1));
                            event.getPlayer().sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag +
                                    WirelessRedstone.strings.signDestroyed);
                            event.getPlayer()
                                    .sendMessage(ChatColor.GRAY + WirelessRedstone.strings.chatTag +
                                            WirelessRedstone.strings.channelRemovedCauseNoSign);
                        } else {
                            event.getPlayer().sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag +
                                    WirelessRedstone.strings.signDestroyed);
                        }
                    } else {
                        WirelessRedstone
                                .getWRLogger()
                                .debug("Receiver wasn't found in the config, but the sign has been successfuly removed !");
                    }
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                            WirelessRedstone.strings.playerCannotDestroySign);
                    event.setCancelled(true);
                    signObject.update();
                }
            } else if (WirelessRedstone.WireBox.isTransmitter(signObject
                    .getLine(0))) {
                if (!WirelessRedstone.config.getSignDrop()) {
                    cancelEvent(event);
                }
                if (WirelessRedstone.WireBox.hasAccessToChannel(
                        event.getPlayer(), signObject.getLine(1))
                        && plugin.permissions.canRemoveTransmitter(event
                        .getPlayer())) {
                    if (WirelessRedstone.WireBox.removeWirelessTransmitter(
                            signObject.getLine(1), event.getBlock()
                                    .getLocation())) {
                        event.getPlayer().sendMessage(ChatColor.GREEN
                                + WirelessRedstone.strings.chatTag +
                                WirelessRedstone.strings.signDestroyed);
                        if (WirelessRedstone.config.isChannelEmpty(WirelessRedstone.config
                                .getWirelessChannel(signObject.getLine(1)))) {
                            WirelessRedstone.config
                                    .removeWirelessChannel(signObject
                                            .getLine(1));
                            event.getPlayer()
                                    .sendMessage(ChatColor.GRAY + WirelessRedstone.strings.chatTag +
                                            WirelessRedstone.strings.channelRemovedCauseNoSign);
                        } else if (WirelessRedstone.config
                                .getWirelessChannel(signObject.getLine(1))
                                .getTransmitters().size() == 0) {
                            event.getPlayer()
                                    .sendMessage(
                                            ChatColor.GREEN
                                                    + WirelessRedstone.strings.chatTag + WirelessRedstone.strings.allTransmittersGone);
                            WirelessRedstone.config.getWirelessChannel(
                                    signObject.getLine(1)).turnOff();
                        }
                    } else {
                        WirelessRedstone
                                .getWRLogger()
                                .debug("Transmitter wasn't found in the config, but the sign has been successfuly removed !");
                    }
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED
                            + WirelessRedstone.strings.chatTag +
                            WirelessRedstone.strings.playerCannotDestroySign);
                    event.setCancelled(true);
                }
            } else if (WirelessRedstone.WireBox.isScreen(signObject.getLine(0))) {
                if (!WirelessRedstone.config.getSignDrop()) {
                    cancelEvent(event);
                }
                if (WirelessRedstone.WireBox.hasAccessToChannel(
                        event.getPlayer(), signObject.getLine(1))
                        && plugin.permissions
                        .canRemoveScreen(event.getPlayer())) {
                    if (WirelessRedstone.WireBox.removeWirelessScreen(
                            signObject.getLine(1), event.getBlock()
                                    .getLocation())) {
                        event.getPlayer().sendMessage(ChatColor.GREEN
                                + WirelessRedstone.strings.chatTag +
                                WirelessRedstone.strings.signDestroyed);
                        if (WirelessRedstone.config.isChannelEmpty(WirelessRedstone.config
                                .getWirelessChannel(signObject.getLine(1)))) {
                            WirelessRedstone.config
                                    .removeWirelessChannel(signObject
                                            .getLine(1));
                            event.getPlayer()
                                    .sendMessage(ChatColor.GRAY + WirelessRedstone.strings.chatTag +
                                            WirelessRedstone.strings.channelRemovedCauseNoSign);
                        }
                    } else {
                        WirelessRedstone
                                .getWRLogger()
                                .debug("Screen wasn't found in the config, but the sign has been successfuly removed !");
                    }
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED
                            + WirelessRedstone.strings.chatTag +
                            WirelessRedstone.strings.playerCannotDestroySign);
                    event.setCancelled(true);
                }
            }
        } else if (event.getBlock().getType()
                .equals(Material.REDSTONE_TORCH_ON)) {
            for (Location loc : WirelessRedstone.cache
                    .getAllReceiverLocations()) {
                if (loc.equals(event.getBlock().getLocation())) {
                    event.getPlayer()
                            .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                    WirelessRedstone.strings.playerCannotDestroyReceiverTorch);
                    event.setCancelled(true);
                    return;
                }
            }
        } else {
            if (WirelessRedstone.cache.getAllSignLocations().contains(
                    event.getBlock().getRelative(BlockFace.UP).getLocation())) {
                event.getPlayer()
                        .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                WirelessRedstone.strings.playerCannotDestroyBlockAttachedToSign);
                event.setCancelled(true);
                return;
            }
            ArrayList<BlockFace> possibleBlockface = new ArrayList<BlockFace>();
            possibleBlockface.add(BlockFace.NORTH);
            possibleBlockface.add(BlockFace.EAST);
            possibleBlockface.add(BlockFace.SOUTH);
            possibleBlockface.add(BlockFace.WEST);

            for (BlockFace blockFace : possibleBlockface) {
                if (WirelessRedstone.cache.getAllSignLocations().contains(
                        event.getBlock().getRelative(blockFace).getLocation())) {
                    if (event.getBlock().getRelative(blockFace).getState() instanceof Sign) {
                        Sign signObject = (Sign) event.getBlock().getRelative(blockFace).getState();
                        if (WirelessRedstone.WireBox.isReceiver(signObject.getLine(0))
                                || WirelessRedstone.WireBox.isTransmitter(signObject.getLine(0))
                                || WirelessRedstone.WireBox.isScreen(signObject.getLine(0))) {
                            if (signObject.getLine(1) == null)
                                continue;
                            WirelessChannel returnedChannel = WirelessRedstone.config.getWirelessChannel(signObject.getLine(1));

                            for (WirelessReceiver receiver : returnedChannel.getReceivers()) {
                                if (!receiver.getIsWallSign())
                                    continue;

                                Location checkLoc = receiver.getLocation().getBlock().getRelative(
                                        receiver.getDirection().getOppositeFace()).getLocation();
                                if (event.getBlock().getLocation().equals(checkLoc)) {
                                    event.getPlayer()
                                            .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                                    WirelessRedstone.strings.playerCannotDestroyBlockAttachedToSign);
                                    event.setCancelled(true);
                                }
                            }

                            for (WirelessTransmitter transmitter : returnedChannel.getTransmitters()) {
                                if (!transmitter.getIsWallSign())
                                    continue;

                                Location checkLoc = transmitter.getLocation().getBlock().getRelative(
                                        transmitter.getDirection().getOppositeFace()).getLocation();
                                if (event.getBlock().getLocation().equals(checkLoc)) {
                                    event.getPlayer()
                                            .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                                    WirelessRedstone.strings.playerCannotDestroyBlockAttachedToSign);
                                    event.setCancelled(true);
                                }
                            }

                            for (WirelessScreen screen : returnedChannel.getScreens()) {
                                if (!screen.getIsWallSign())
                                    continue;

                                Location checkLoc = screen.getLocation().getBlock().getRelative(
                                        screen.getDirection().getOppositeFace()).getLocation();
                                if (event.getBlock().getLocation().equals(checkLoc)) {
                                    event.getPlayer()
                                            .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                                    WirelessRedstone.strings.playerCannotDestroyBlockAttachedToSign);
                                    event.setCancelled(true);
                                }
                            }
                        }
                    } else if (event.getBlock().getRelative(blockFace).getType() == Material.REDSTONE_TORCH_ON
                            || event.getBlock().getRelative(blockFace).getType() == Material.REDSTONE_TORCH_OFF) {
                        if (getRedstoneTorchDirection(event.getBlock().getRelative(blockFace)) == null)
                            continue;

                        Location checkLoc = event.getBlock().getRelative(blockFace)
                                .getRelative(getRedstoneTorchDirection(event.getBlock().getRelative(blockFace))
                                        .getOppositeFace()).getLocation();
                        if (event.getBlock().getLocation().equals(checkLoc)) {
                            event.getPlayer()
                                    .sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                                            WirelessRedstone.strings.playerCannotDestroyBlockAttachedToSign);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    // /**
    // * From the bukkit javadoc :
    // * Represents events with a source block and a destination block,
    // currently only applies to
    // * liquid (lava and water) and teleporting dragon eggs.
    // * If a Block From To event is cancelled, the block will not move (the
    // liquid will not flow).
    // *
    // * @param event
    // * @deprecated Because there's currently nothing that breaks a sign and
    // sends that event.
    // */
    // @EventHandler
    // public void onBlockFromTo(BlockFromToEvent event)
    // {
    // if (event.getToBlock().getType() == Material.REDSTONE_TORCH_ON)
    // {
    // for (Location loc : WirelessRedstone.cache.getAllReceiverLocations())
    // {
    // if (loc.getBlockX() == event.getToBlock().getLocation().getBlockX() ||
    // loc.getBlockY() == event.getToBlock().getLocation().getBlockY() ||
    // loc.getBlockZ() == event.getToBlock().getLocation().getBlockZ())
    // {
    // WirelessRedstone.WireBox.removeReceiverAt(loc, false);
    // return;
    // }
    // }
    // }
    // }

    private void cancelEvent(final BlockBreakEvent event) {
        /*
         * Methods cancelEvent and sendBlockBreakParticles, taken from
		 * http://www
		 * .bukkit.fr/index.php?threads/enlever-le-drop-dun-block.850/page
		 * -2#post-11582 All credits to richie3366.
		 */

        event.setCancelled(true);

        ItemStack is = event.getPlayer().getItemInHand();

        if (is.getType().getMaxDurability() > 0) {
            is.setDurability((short) (is.getDurability() + 1));

            if (is.getDurability() >= is.getType().getMaxDurability()) {
                event.getPlayer().setItemInHand(null);
            }
        }

        Block b = event.getBlock();

        Material lastType = b.getType();

        b.setType(Material.AIR);

        sendBlockBreakParticles(b, lastType, event.getPlayer());
    }

    private void sendBlockBreakParticles(final Block b,
                                         final Material lastType, final Player author) {
        try {
            int radius = 64;
            radius *= radius;

            for (Player player : b.getWorld().getPlayers()) {
                int distance = (int) player.getLocation().distanceSquared(
                        b.getLocation());
                if (distance <= radius && !player.equals(author)) {
                    player.playEffect(b.getLocation(), Effect.STEP_SOUND, null);
                }
            }
        } catch (Exception ignored) {

        }
    }

    private BlockFace getRedstoneTorchDirection(Block b) {
        if (WirelessRedstone.getBukkitVersion().contains("v1_8")) {
            switch (b.getData()) {
                case (byte) 1:
                    return BlockFace.EAST;
                case (byte) 2:
                    return BlockFace.WEST;
                case (byte) 3:
                    return BlockFace.SOUTH;
                case (byte) 4:
                    return BlockFace.NORTH;
                default:
                    return null;
            }
        } else {
            switch (b.getData()) {
                case (byte) 0:
                    return BlockFace.EAST;
                case (byte) 2:
                    return BlockFace.WEST;
                case (byte) 3:
                    return BlockFace.SOUTH;
                case (byte) 4:
                    return BlockFace.NORTH;
                default:
                    return null;
            }
        }
    }

    private String autoAssign(Player p, Block b, String line) {
        String name = line;
        if (line.equalsIgnoreCase("[auto]")) {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                if (WirelessRedstone.config.getWirelessChannel("ch-" + i) == null) {
                    Sign sign = (Sign) b.getState();
                    sign.setLine(1, "ch-" + i);
                    sign.update(true);
                    p.sendMessage(ChatColor.GRAY + WirelessRedstone.strings.chatTag
                            + WirelessRedstone.strings.automaticAssigned.replaceAll("%%NAME", "ch-" + i));
                    name = "ch-" + i;
                    break;
                }
            }
        }
        return name;
    }
}
