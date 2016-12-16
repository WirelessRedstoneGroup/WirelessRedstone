package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;

public class BlockListener implements Listener {

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            if (Main.getSignManager().getSignType(event.getLine(0)) == null)
                return;

            if (event.getLine(1) == null) {
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                Utils.sendFeedback("No channelname given!", event.getPlayer(), true); //TODO: Add string to stringloader
                return;
            }

            if (event.getLine(1).equalsIgnoreCase("")) {
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                Utils.sendFeedback("No channelname given!", event.getPlayer(), true); //TODO: Add string to stringloader
                return;
            }

            String cname = event.getLine(1);

            if (!Main.getSignManager().hasAccessToChannel(event.getPlayer(), event.getLine(1))) {
                event.setCancelled(true);
                Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), true);
                return;
            }

            //Bukkit/Spigot first call the SignChangeEvent then register the block. If you don't add the runnable you can't access the block via getBlock()
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    switch (Main.getSignManager().getSignType(event.getLine(0), event.getLine(2))) {
                        case TRANSMITTER:
                            if (!Main.getPermissionsManager().canCreateTransmitter(event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                                Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), true);
                                return;
                            }

                            if (!Main.getSignManager().addWirelessTransmitter(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                    event.getBlock(), event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                            }
                            break;
                        case SCREEN:
                            if (!Main.getPermissionsManager().canCreateScreen(event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                                Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), true);
                                return;
                            }

                            if (!Main.getSignManager().addWirelessScreen(autoAssign(event.getPlayer(), event.getBlock(), event.getLine(1)),
                                    event.getBlock(), event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                            }
                            break;
                        case RECEIVER_NORMAL:
                            if (!Main.getPermissionsManager().canCreateReceiver(event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                                Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), true);
                                return;
                            }

                            if (!Main.getSignManager().addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(),
                                    event.getLine(1)), event.getBlock(), event.getPlayer(), WirelessReceiver.Type.DEFAULT)) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                            }
                            break;
                        case RECEIVER_INVERTER:
                            if (!Main.getPermissionsManager().canCreateReceiver(event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                                Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), true);
                                return;
                            }

                            if (!Main.getSignManager().addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(),
                                    event.getLine(1)), event.getBlock(), event.getPlayer(), WirelessReceiver.Type.INVERTER)) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                            }
                            break;
                        case RECEIVER_DELAYER:
                            if (!Main.getPermissionsManager().canCreateReceiver(event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                                Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), true);
                                return;
                            }

                            if (!Main.getSignManager().addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(),
                                    event.getLine(1)), event.getBlock(), event.getPlayer(), WirelessReceiver.Type.DELAYER)) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                            }
                            break;
                        case RECEIVER_CLOCK:
                            if (!Main.getPermissionsManager().canCreateReceiver(event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                                Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), true);
                                return;
                            }

                            if (!Main.getSignManager().addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(),
                                    event.getLine(1)), event.getBlock(), event.getPlayer(), WirelessReceiver.Type.CLOCK)) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                            }
                            break;
                        case RECEIVER_SWITCH:
                            if (!Main.getPermissionsManager().canCreateReceiver(event.getPlayer())) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                                Utils.sendFeedback(Main.getStrings().playerCannotCreateSign, event.getPlayer(), true);
                                return;
                            }

                            if (!Main.getSignManager().addWirelessReceiver(autoAssign(event.getPlayer(), event.getBlock(),
                                    event.getLine(1)), event.getBlock(), event.getPlayer(), WirelessReceiver.Type.SWITCH)) {
                                event.setCancelled(true);
                                event.getBlock().setType(Material.AIR);
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            Sign signObject = (Sign) event.getBlock().getState();

            if (Main.getSignManager().getSignType(signObject.getLine(0)) == null)
                return;

            if (!Main.getSignManager().hasAccessToChannel(event.getPlayer(), signObject.getLine(1))) {
                event.setCancelled(true);
                Utils.sendFeedback(Main.getStrings().playerCannotDestroySign, event.getPlayer(), true);
                return;
            }

            switch (Main.getSignManager().getSignType(signObject.getLine(0))) {
                case TRANSMITTER:
                    if (!Main.getPermissionsManager().canRemoveTransmitter(event.getPlayer())) {
                        event.setCancelled(true);
                        Utils.sendFeedback(Main.getStrings().playerCannotDestroySign, event.getPlayer(), true);
                        return;
                    }

                    if (Main.getSignManager().removeWirelessTransmitter(signObject.getLine(1), event.getBlock().getLocation())) {
                        Utils.sendFeedback(Main.getStrings().signDestroyed, event.getPlayer(), false);

                        if (Main.getStorage().isChannelEmpty(Main.getStorage().getWirelessChannel(signObject.getLine(1)))) {
                            Main.getStorage().removeWirelessChannel(signObject.getLine(1));
                            Utils.sendFeedback(Main.getStrings().channelRemovedCauseNoSign, event.getPlayer(), false);
                        }
                    } else {
                        Main.getWRLogger().debug("Receiver wasn't found in the config, but the sign has been successfuly removed!");
                    }
                    break;
                case SCREEN:
                    if (!Main.getPermissionsManager().canRemoveScreen(event.getPlayer())) {
                        event.setCancelled(true);
                        Utils.sendFeedback(Main.getStrings().playerCannotDestroySign, event.getPlayer(), true);
                        return;
                    }

                    if (Main.getSignManager().removeWirelessScreen(signObject.getLine(1), event.getBlock().getLocation())) {
                        Utils.sendFeedback(Main.getStrings().signDestroyed, event.getPlayer(), false);

                        if (Main.getStorage().isChannelEmpty(Main.getStorage().getWirelessChannel(signObject.getLine(1)))) {
                            Main.getStorage().removeWirelessChannel(signObject.getLine(1));
                            Utils.sendFeedback(Main.getStrings().channelRemovedCauseNoSign, event.getPlayer(), false);
                        }
                    } else {
                        Main.getWRLogger().debug("Receiver wasn't found in the config, but the sign has been successfuly removed!");
                    }
                    break;
                case RECEIVER:
                    if (!Main.getPermissionsManager().canRemoveReceiver(event.getPlayer())) {
                        event.setCancelled(true);
                        Utils.sendFeedback(Main.getStrings().playerCannotDestroySign, event.getPlayer(), true);
                        return;
                    }

                    if (Main.getSignManager().removeWirelessReceiver(signObject.getLine(1), event.getBlock().getLocation())) {
                        Utils.sendFeedback(Main.getStrings().signDestroyed, event.getPlayer(), false);

                        if (Main.getStorage().isChannelEmpty(Main.getStorage().getWirelessChannel(signObject.getLine(1)))) {
                            Main.getStorage().removeWirelessChannel(signObject.getLine(1));
                            Utils.sendFeedback(Main.getStrings().channelRemovedCauseNoSign, event.getPlayer(), false);
                        }
                    } else {
                        Main.getWRLogger().debug("Receiver wasn't found in the config, but the sign has been successfuly removed!");
                    }
                    break;
                default:
                    break;
            }
        } else {
            for (BlockFace blockFace : Utils.getEveryBlockFace(false)) {
                if (event.getBlock().getRelative(blockFace).getType() == Material.WALL_SIGN) {
                    Sign signObject = (Sign) event.getBlock().getRelative(blockFace).getState();

                    if (Main.getSignManager().getSignType(signObject.getLine(0)) != null) {
                        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) event.getBlock().getRelative(blockFace).getState().getData();
                        if (sign.getFacing() == blockFace) {
                            event.setCancelled(true);
                            Utils.sendFeedback(Main.getStrings().playerCannotDestroyBlockAttachedToSign, event.getPlayer(), true, true);
                            break;
                        }
                    }
                }
            }

            if (event.getBlock().getRelative(BlockFace.UP).getState() instanceof Sign) {
                Sign signObject = (Sign) event.getBlock().getRelative(BlockFace.UP).getState();

                if (Main.getSignManager().getSignType(signObject.getLine(0)) != null) {
                    if (event.getBlock().getRelative(BlockFace.UP).getType() != Material.WALL_SIGN) {
                        event.setCancelled(true);
                        Utils.sendFeedback(Main.getStrings().playerCannotDestroyBlockAttachedToSign, event.getPlayer(), true, true);
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

            if (Main.getSignManager().getSignType(signObject.getLine(0)) != SignType.TRANSMITTER)
                return;

            if (signObject.getLine(1) == null)
                return;

            if (signObject.getLine(1).equalsIgnoreCase(""))
                return;

            WirelessChannel channel = Main.getStorage().getWirelessChannel(signObject.getLine(1));

            if (channel == null) {
                Main.getWRLogger().debug("The transmitter at location " + signObject.getX() + "," + signObject.getY()
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

        for (BlockFace blockFace : Utils.getEveryBlockFace(true)) {
            if (event.getBlock().getRelative(blockFace).getState() instanceof Sign) {
                Sign signObject = (Sign) event.getBlock().getRelative(blockFace).getState();

                if (Main.getSignManager().getSignType(signObject.getLine(0)) == SignType.TRANSMITTER) {
                    e = new BlockRedstoneEvent(signObject.getBlock(), event.getOldCurrent(), event.getNewCurrent());
                    callLater(e);
                }
            }
        }
    }

    private void callLater(final BlockRedstoneEvent event) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(),
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
                if (Main.getStorage().getWirelessChannel("ch-" + i) == null) {
                    Sign sign = (Sign) b.getState();
                    sign.setLine(1, "ch-" + i);
                    sign.update(true);
                    Utils.sendFeedback(Main.getStrings().automaticAssigned.replaceAll("%%NAME", "ch-" + i), p, false);
                    name = "ch-" + i;
                    break;
                }
            }
        }
        return name;
    }
}
