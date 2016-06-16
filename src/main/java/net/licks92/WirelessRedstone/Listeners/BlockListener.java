package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            Sign signObject = (Sign) event.getBlock().getState();

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
