package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;

public class BlockListener implements Listener {

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            if (WirelessRedstone.getSignManager().getSignType(event.getLine(0)) == null)
                return;

            if (event.getLine(1) == null) {
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().noChannelName, event.getPlayer(), true);
                return;
            }

            if (event.getLine(1).equalsIgnoreCase("")) {
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().noChannelName, event.getPlayer(), true);
                return;
            }

            String cname = event.getLine(1);

            if (!WirelessRedstone.getSignManager().hasAccessToChannel(event.getPlayer(), event.getLine(1))) {
                event.setCancelled(true);
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true);
                return;
            }

            //Bukkit/Spigot first call the SignChangeEvent then register the block. If you don't add the runnable you can't access the block via getBlock()
            Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (createSign(event.getPlayer(), event.getLine(1), event.getLine(0), event.getLine(2), event))
                        event.setCancelled(true);
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            final Sign sign = (Sign) event.getBlock().getState();

            if (WirelessRedstone.getSignManager().getSignType(sign.getLine(0)) == null)
                return;

            if (sign.getLine(1) == null) {
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().noChannelName, event.getPlayer(), true);
                return;
            }

            if (sign.getLine(1).equalsIgnoreCase("")) {
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().noChannelName, event.getPlayer(), true);
                return;
            }

            String cname = sign.getLine(1);

            if (!WirelessRedstone.getSignManager().hasAccessToChannel(event.getPlayer(), sign.getLine(1))) {
                event.setCancelled(true);
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true);
                return;
            }

            if (createSign(event.getPlayer(), sign.getLine(1), sign.getLine(0), sign.getLine(2), event))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            Sign signObject = (Sign) event.getBlock().getState();

            if (WirelessRedstone.getSignManager().getSignType(signObject.getLine(0)) == null)
                return;

            if (!WirelessRedstone.getSignManager().hasAccessToChannel(event.getPlayer(), signObject.getLine(1))) {
                event.setCancelled(true);
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionDestroySign, event.getPlayer(), true);
                return;
            }

            switch (WirelessRedstone.getSignManager().getSignType(signObject.getLine(0))) {
                case TRANSMITTER:
                    if (!WirelessRedstone.getPermissionsManager().canRemoveTransmitter(event.getPlayer())) {
                        event.setCancelled(true);
                        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionDestroySign, event.getPlayer(), true);
                        return;
                    }

                    if (WirelessRedstone.getSignManager().removeWirelessTransmitter(signObject.getLine(1), event.getBlock().getLocation())) {
                        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().signDestroyed, event.getPlayer(), false);

                        if (WirelessRedstone.getStorage().isChannelEmpty(WirelessRedstone.getStorage().getWirelessChannel(signObject.getLine(1)))) {
                            WirelessRedstone.getStorage().removeWirelessChannel(signObject.getLine(1));
                            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelRemovedNoSigns, event.getPlayer(), false);
                        }
                    } else {
                        WirelessRedstone.getWRLogger().debug("Transmitter wasn't found in the config, but the sign has been successfuly removed!");
                    }
                    break;
                case SCREEN:
                    if (!WirelessRedstone.getPermissionsManager().canRemoveScreen(event.getPlayer())) {
                        event.setCancelled(true);
                        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionDestroySign, event.getPlayer(), true);
                        return;
                    }

                    if (WirelessRedstone.getSignManager().removeWirelessScreen(signObject.getLine(1), event.getBlock().getLocation())) {
                        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().signDestroyed, event.getPlayer(), false);

                        if (WirelessRedstone.getStorage().isChannelEmpty(WirelessRedstone.getStorage().getWirelessChannel(signObject.getLine(1)))) {
                            WirelessRedstone.getStorage().removeWirelessChannel(signObject.getLine(1));
                            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelRemovedNoSigns, event.getPlayer(), false);
                        }
                    } else {
                        WirelessRedstone.getWRLogger().debug("Screen wasn't found in the config, but the sign has been successfuly removed!");
                    }
                    break;
                case RECEIVER:
                    if (!WirelessRedstone.getPermissionsManager().canRemoveReceiver(event.getPlayer())) {
                        event.setCancelled(true);
                        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionDestroySign, event.getPlayer(), true);
                        return;
                    }

                    if (WirelessRedstone.getSignManager().removeWirelessReceiver(signObject.getLine(1), event.getBlock().getLocation())) {
                        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().signDestroyed, event.getPlayer(), false);

                        if (WirelessRedstone.getStorage().isChannelEmpty(WirelessRedstone.getStorage().getWirelessChannel(signObject.getLine(1)))) {
                            WirelessRedstone.getStorage().removeWirelessChannel(signObject.getLine(1));
                            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().channelRemovedNoSigns, event.getPlayer(), false);
                        }
                    } else {
                        WirelessRedstone.getWRLogger().debug("Receiver wasn't found in the config, but the sign has been successfuly removed!");
                    }
                    break;
                default:
                    break;
            }
        } else {
            for (BlockFace blockFace : WirelessRedstone.getUtils().getEveryBlockFace(false)) {
                if (event.getBlock().getRelative(blockFace).getType() == Material.WALL_SIGN) {
                    Sign signObject = (Sign) event.getBlock().getRelative(blockFace).getState();

                    if (WirelessRedstone.getSignManager().getSignType(signObject.getLine(0)) != null) {
                        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) event.getBlock().getRelative(blockFace).getState().getData();
                        if (sign.getFacing() == blockFace) {
                            event.setCancelled(true);
                            WirelessRedstone.getUtils()
                                    .sendFeedback(WirelessRedstone.getStrings().attachedToSign, event.getPlayer(), true, true);
                            break;
                        }
                    }
                }
            }

            if (event.getBlock().getRelative(BlockFace.UP).getState() instanceof Sign) {
                Sign signObject = (Sign) event.getBlock().getRelative(BlockFace.UP).getState();

                if (WirelessRedstone.getSignManager().getSignType(signObject.getLine(0)) != null) {
                    if (event.getBlock().getRelative(BlockFace.UP).getType() != Material.WALL_SIGN) {
                        event.setCancelled(true);
                        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().attachedToSign, event.getPlayer(), true, true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (event.getOldCurrent() == event.getNewCurrent())
            return;

        if (event.getBlock().getState() instanceof Sign) {
            Sign signObject = (Sign) event.getBlock().getState();

            if (WirelessRedstone.getSignManager().getSignType(signObject.getLine(0)) != SignType.TRANSMITTER)
                return;

            if (signObject.getLine(1) == null)
                return;

            if (signObject.getLine(1).equalsIgnoreCase(""))
                return;

            WirelessChannel channel = WirelessRedstone.getStorage().getWirelessChannel(signObject.getLine(1));

            if (channel == null) {
                WirelessRedstone.getWRLogger().debug("The transmitter at location " + signObject.getX() + "," + signObject.getY()
                        + "," + signObject.getZ() + " in the world " + signObject.getWorld().getName()
                        + " is actually linked with a null channel.");
                return;
            }

            if (event.getBlock().isBlockIndirectlyPowered() || event.getBlock().isBlockPowered()) {
                channel.toggle(1, event.getBlock());
            } else {
                channel.toggle(0, event.getBlock());
            }
            return;
        }

        BlockRedstoneEvent e = null;
        switch (event.getBlock().getType()) {
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
                Attachable attachable = (Attachable) event.getBlock().getState().getData();
                e = new BlockRedstoneEvent(event.getBlock().getRelative(attachable.getAttachedFace()),
                        event.getOldCurrent(), event.getNewCurrent());
                callLater(e);
                break;
            case DETECTOR_RAIL:
            case WOOD_PLATE:
            case STONE_PLATE:
            case GOLD_PLATE:
            case IRON_PLATE:
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
                e = new BlockRedstoneEvent(event.getBlock().getRelative(BlockFace.DOWN),
                        event.getOldCurrent(), event.getNewCurrent());
                callLater(e);
                break;
            default:
                break;
        }

        for (BlockFace blockFace : WirelessRedstone.getUtils().getEveryBlockFace(true)) {
            if (event.getBlock().getRelative(blockFace).getState() instanceof Sign) {
                Sign signObject = (Sign) event.getBlock().getRelative(blockFace).getState();

                if (WirelessRedstone.getSignManager().getSignType(signObject.getLine(0)) == SignType.TRANSMITTER) {
                    e = new BlockRedstoneEvent(signObject.getBlock(), event.getOldCurrent(), event.getNewCurrent());
                    callLater(e);
                }
            }
        }
    }

    private boolean createSign(Player player, String channelName, String signType, String signSubType, BlockEvent event) {
        switch (WirelessRedstone.getSignManager().getSignType(signType, signSubType)) {
            case TRANSMITTER:
                if (!WirelessRedstone.getPermissionsManager().canCreateTransmitter(player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, player, true);
                    return true;
                }

                if (!WirelessRedstone.getSignManager().addWirelessTransmitter(autoAssign(player, event.getBlock(), channelName),
                        event.getBlock(), player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    return true;
                }
                break;
            case SCREEN:
                if (!WirelessRedstone.getPermissionsManager().canCreateScreen(player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, player, true);
                    return true;
                }

                if (!WirelessRedstone.getSignManager().addWirelessScreen(autoAssign(player, event.getBlock(), channelName),
                        event.getBlock(), player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    return true;
                }
                break;
            case RECEIVER_NORMAL:
                if (!WirelessRedstone.getPermissionsManager().canCreateReceiver(player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, player, true);
                    return true;
                }

                if (!WirelessRedstone.getSignManager().addWirelessReceiver(autoAssign(player, event.getBlock(),
                        channelName), event.getBlock(), player, WirelessReceiver.Type.DEFAULT)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    return true;
                }
                break;
            case RECEIVER_INVERTER:
                if (!WirelessRedstone.getPermissionsManager().canCreateReceiver(player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, player, true);
                    return true;
                }

                if (!WirelessRedstone.getSignManager().addWirelessReceiver(autoAssign(player, event.getBlock(),
                        channelName), event.getBlock(), player, WirelessReceiver.Type.INVERTER)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    return true;
                }
                break;
            case RECEIVER_DELAYER:
                if (!WirelessRedstone.getPermissionsManager().canCreateReceiver(player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, player, true);
                    return true;
                }

                if (!WirelessRedstone.getSignManager().addWirelessReceiver(autoAssign(player, event.getBlock(),
                        channelName), event.getBlock(), player, WirelessReceiver.Type.DELAYER)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    return true;
                }
                break;
            case RECEIVER_CLOCK:
                if (!WirelessRedstone.getPermissionsManager().canCreateReceiver(player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, player, true);
                    return true;
                }

                if (!WirelessRedstone.getSignManager().addWirelessReceiver(autoAssign(player, event.getBlock(),
                        channelName), event.getBlock(), player, WirelessReceiver.Type.CLOCK)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    return true;
                }
                break;
            case RECEIVER_SWITCH:
                if (!WirelessRedstone.getPermissionsManager().canCreateReceiver(player)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, player, true);
                    return true;
                }

                if (!WirelessRedstone.getSignManager().addWirelessReceiver(autoAssign(player, event.getBlock(),
                        channelName), event.getBlock(), player, WirelessReceiver.Type.SWITCH)) {
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    private void callLater(final BlockRedstoneEvent event) {
        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        Bukkit.getServer().getPluginManager().callEvent(event);
                    }
                }, 1L);
    }

    private String autoAssign(Player p, Block b, String line) {
        String name = line;
        if (line.equalsIgnoreCase("[auto]")) {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                if (WirelessRedstone.getStorage().getWirelessChannel("ch-" + i) == null) {
                    Sign sign = (Sign) b.getState();
                    sign.setLine(1, "ch-" + i);
                    sign.update(true);
                    WirelessRedstone.getUtils()
                            .sendFeedback(WirelessRedstone.getStrings().autoAssigned.replaceAll("%%NAME", "ch-" + i), p, false);
                    name = "ch-" + i;
                    break;
                }
            }
        }
        return name;
    }
}
