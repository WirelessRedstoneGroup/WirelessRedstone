package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.CompatMaterial;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.Redstone;
import org.bukkit.material.TripwireHook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void on(BlockRedstoneEvent event) {
        handleRedstoneEvent(event.getBlock());
    }

    @SuppressWarnings("ConstantConditions")
    @EventHandler
    public void on(SignChangeEvent event) {
        SignType signType = Utils.getSignType(event.getLine(0));
        if (signType == null) {
            return;
        }

        if (signType == SignType.TRANSMITTER) {
            if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                handlePlaceCancelled(event.getBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true, true);
                return;
            }
        } else if (signType == SignType.RECEIVER) {
            if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                handlePlaceCancelled(event.getBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true, true);
                return;
            }
        } else if (signType == SignType.SCREEN) {
            if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                handlePlaceCancelled(event.getBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true, true);
                return;
            }
        } else {
            return;
        }

        signType = Utils.getSignType(event.getLine(0), event.getLine(2));

        if (event.getLine(1).equalsIgnoreCase("")) {
            handlePlaceCancelled(event.getBlock());
            Utils.sendFeedback(WirelessRedstone.getStrings().noChannelName, event.getPlayer(), true);
            return;
        }

        String channelName = event.getLine(1);

        if (!WirelessRedstone.getSignManager().hasAccessToChannel(event.getPlayer(), channelName)) {
            handlePlaceCancelled(event.getBlock());
            Utils.sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true, true);
            return;
        }

        int delay = 0;
        try {
            delay = Integer.parseInt(event.getLine(3));
        } catch (NumberFormatException ignored) {
            if (signType == SignType.RECEIVER_DELAYER) {
                handlePlaceCancelled(event.getBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().delayNumberOnly, event.getPlayer(), true);
                return;
            } else if (signType == SignType.RECEIVER_CLOCK) {
                handlePlaceCancelled(event.getBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().intervalNumberOnly, event.getPlayer(), true);
                return;
            }
        }

        if (delay < 50 && signType == SignType.RECEIVER_DELAYER) {
            handlePlaceCancelled(event.getBlock());
            Utils.sendFeedback(WirelessRedstone.getStrings().commandDelayMin, event.getPlayer(), true);
            return;
        } else if (delay < 50 && signType == SignType.RECEIVER_CLOCK) {
            handlePlaceCancelled(event.getBlock());
            Utils.sendFeedback(WirelessRedstone.getStrings().commandIntervalMin, event.getPlayer(), true);
            return;
        }

        //TODO: #registerSign Implement error message if failed
        int finalDelay = delay;
        Bukkit.getScheduler().runTask(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                Sign sign = (Sign) event.getBlock().getState();
                BlockFace signDirection = ((Directional) sign.getData()).getFacing();

                int result = WirelessRedstone.getSignManager().registerSign(
                        channelName,
                        event.getBlock(),
                        Utils.getSignType(sign.getLine(0), sign.getLine(2)),
                        signDirection,
                        Collections.singletonList(event.getPlayer().getUniqueId().toString()),
                        finalDelay
                );

                if (result == 0) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().channelExtended, event.getPlayer(), false);
                } else if (result == 1) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().channelCreated, event.getPlayer(), false);
                } else if (result == -1) {
                    handlePlaceCancelled(event.getBlock());
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandDelayMin, event.getPlayer(), true);
                } else if (result == -2) {
                    handlePlaceCancelled(event.getBlock());
                    Utils.sendFeedback(WirelessRedstone.getStrings().commandIntervalMin, event.getPlayer(), true);
                }
            }
        });
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) event.getBlock().getState();

            SignType signType = Utils.getSignType(sign.getLine(0));
            if (signType == null) {
                return;
            }

            if (signType == SignType.TRANSMITTER) {
                if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().permissionDestroySign, event.getPlayer(), true, true);
                    event.setCancelled(true);
                    return;
                }
            } else if (signType == SignType.RECEIVER) {
                if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().permissionDestroySign, event.getPlayer(), true, true);
                    event.setCancelled(true);
                    return;
                }
            } else if (signType == SignType.SCREEN) {
                if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().permissionDestroySign, event.getPlayer(), true, true);
                    event.setCancelled(true);
                    return;
                }
            } else {
                return;
            }

            String channelName = sign.getLine(1);

            if (!WirelessRedstone.getSignManager().hasAccessToChannel(event.getPlayer(), channelName)) {
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionDestroySign, event.getPlayer(), true, true);
                event.setCancelled(true);
                return;
            }

            WirelessRedstone.getSignManager().removeSign(channelName, event.getBlock().getLocation());
            Utils.sendFeedback(WirelessRedstone.getStrings().signDestroyed, event.getPlayer(), false);
        }
    }

    private void handleRedstoneEvent(Block block) {
        Block nextTickBlock = null;
        Collection<BlockFace> blockFaces = Utils.getAxisBlockFaces();
        Material type = block.getType();

        if (type == CompatMaterial.REPEATER.getMaterial() || type == CompatMaterial.REPEATER_ON.getMaterial() || type == CompatMaterial.REPEATER_OFF.getMaterial() ||
                type == CompatMaterial.COMPARATOR.getMaterial() || type == CompatMaterial.COMPARATOR_ON.getMaterial() || type == CompatMaterial.COMPARATOR_OFF.getMaterial()) {
            Directional directional = (Directional) block.getState().getData();

            if (block.getRelative(directional.getFacing()).getType().isSolid() &&
                    !block.getRelative(directional.getFacing()).getType().isInteractable()) {
                nextTickBlock = block.getRelative(directional.getFacing());
            }

            blockFaces = Collections.singletonList(directional.getFacing());
        } else if (type == Material.DAYLIGHT_DETECTOR || type == Material.DETECTOR_RAIL || CompatMaterial.IS_PREASURE_PLATE.isMaterial(type)) {
            nextTickBlock = block.getRelative(BlockFace.DOWN);
        } else if (block.getState() != null) {
            if (block.getState().getData() instanceof Attachable && block.getState().getData() instanceof Redstone &&
                    !(block.getState().getData() instanceof TripwireHook)) {
                Attachable attachable = (Attachable) block.getState().getData();

                //TODO: Skip UP and DOWN due to a Spigot bug: https://hub.spigotmc.org/jira/browse/SPIGOT-4506
                if (!(attachable.getAttachedFace() == BlockFace.UP || attachable.getAttachedFace() == BlockFace.DOWN)) {
                    nextTickBlock = block.getRelative(attachable.getAttachedFace());
                }
            }
        }

        if (nextTickBlock != null) {
            final Block finalNextTickBlock = nextTickBlock;
            Bukkit.getScheduler().runTask(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    handleRedstoneEvent(finalNextTickBlock);
                }
            });
        }

        List<Location> locations = new ArrayList<>();
        for (BlockFace blockFace : blockFaces) {
            if (block.getRelative(blockFace).getState() instanceof Sign) {
                locations.add(block.getRelative(blockFace).getLocation());
            }
        }

        if (!locations.isEmpty()) {
            Bukkit.getScheduler().runTask(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    for (Location location : locations) {
                        Sign sign = (Sign) location.getBlock().getState();

                        updateRedstonePower(sign, block.isBlockPowered() || block.isBlockIndirectlyPowered());
                    }
                }
            });
        }
    }

    private void handlePlaceCancelled(Block block) {
        block.setType(Material.AIR);

        if (ConfigManager.getConfig().getDropSignBroken()) {
            block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.SIGN));
        }
    }

    private void updateRedstonePower(Sign sign, boolean powered) {
        if (Utils.getSignType(sign.getLine(0)) != SignType.TRANSMITTER)
            return;

        if (sign.getLine(1) == null)
            return;

        if (sign.getLine(1).equalsIgnoreCase(""))
            return;

        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(sign.getLine(1));
        if (channel == null) {
            return;
        }

        if (powered) {
            channel.turnOn();
        } else {
            channel.turnOff();
        }
    }

}
