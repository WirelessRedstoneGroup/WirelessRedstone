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
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.Redstone;
import org.bukkit.material.TripwireHook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(BlockRedstoneEvent event) {
        if (event.getNewCurrent() < event.getOldCurrent() && event.getNewCurrent() != 0) {
            return;
        }

        if (event.getBlock().getType() == Material.REDSTONE || event.getBlock().getType() == Material.REDSTONE_WIRE) {
            handleRedstoneEvent(event.getBlock(), event.getNewCurrent() > 0, true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(BlockPhysicsEvent event) {
        if (event.getBlock() == null) {
            return;
        }

        if (event.getBlock().getState() == null) {
            return;
        }

        if (event.getBlock().getState().getData() == null) {
            return;
        }

        if (Utils.isNewMaterialSystem()) {
            if (!(event.getBlock().getBlockData() instanceof org.bukkit.block.data.Powerable)) {
                return;
            }
        } else {
            if (!(event.getBlock().getState().getData() instanceof Redstone)) {
                return;
            }
        }

        if (event.getBlock().getType() == Material.REDSTONE || event.getBlock().getType() == Material.REDSTONE_WIRE) {
            return;
        }

        boolean isPowered = false;
        if (Utils.isNewMaterialSystem()) {
            isPowered = ((org.bukkit.block.data.Powerable) event.getBlock().getBlockData()).isPowered();
        } else {
            isPowered = ((Redstone) event.getBlock().getState().getData()).isPowered();
        }

        handleRedstoneEvent(event.getBlock(), isPowered, false);
    }

    @EventHandler
    public void on(final SignChangeEvent event) {
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

        final String channelName = event.getLine(1);

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
        final int finalDelay = delay;
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
    public void on(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE_BLOCK || event.getBlock().getType() == CompatMaterial.REDSTONE_TORCH.getMaterial()) {
            handleRedstoneEvent(event.getBlock(), true, false);
        }
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE_BLOCK || event.getBlock().getType() == CompatMaterial.REDSTONE_TORCH.getMaterial()) {
            handleRedstoneEvent(event.getBlock(), false, true);
        }

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

    private void handleRedstoneEvent(Block block, boolean powered, boolean skipLocation) {
        Collection<BlockFace> blockFaces = Utils.getAxisBlockFaces();
        List<Location> locations = new ArrayList<>();
        Material type = block.getType();

        if (type == CompatMaterial.REPEATER.getMaterial() || type == CompatMaterial.REPEATER_ON.getMaterial() || type == CompatMaterial.REPEATER_OFF.getMaterial() ||
                type == CompatMaterial.COMPARATOR.getMaterial() || type == CompatMaterial.COMPARATOR_ON.getMaterial() || type == CompatMaterial.COMPARATOR_OFF.getMaterial()) {
            if (Utils.isNewMaterialSystem()) {
                org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) block.getBlockData();

                if (block.getRelative(directional.getFacing().getOppositeFace()).getType().isOccluding() &&
                        !block.getRelative(directional.getFacing().getOppositeFace()).getType().isInteractable()) {
                    Block relBlock = block.getRelative(directional.getFacing().getOppositeFace());
                    for (BlockFace axisBlockFace : Utils.getAxisBlockFaces()) {
                        locations.add(relBlock.getRelative(axisBlockFace).getLocation());
                    }

                }

                locations.add(block.getRelative(directional.getFacing().getOppositeFace()).getRelative(directional.getFacing().getOppositeFace()).getLocation());

                blockFaces = Collections.singletonList(directional.getFacing().getOppositeFace());
            } else {
                Directional directional = (Directional) block.getState().getData();

                if (block.getRelative(directional.getFacing()).getType().isOccluding() &&
                        !block.getRelative(directional.getFacing()).getType().isInteractable()) {
                    Block relBlock = block.getRelative(directional.getFacing());
                    for (BlockFace axisBlockFace : Utils.getAxisBlockFaces()) {
                        locations.add(relBlock.getRelative(axisBlockFace).getLocation());
                    }

                }

                locations.add(block.getRelative(directional.getFacing()).getRelative(directional.getFacing()).getLocation());

                blockFaces = Collections.singletonList(directional.getFacing());
            }
        } else if (type == Material.DAYLIGHT_DETECTOR || type == Material.DETECTOR_RAIL || CompatMaterial.IS_PREASURE_PLATE.isMaterial(type)) {
            locations.add(block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getLocation());
        } else {
            if (Utils.isNewMaterialSystem()) {
                if (block.getBlockData() != null) {
                    if (block.getBlockData() instanceof org.bukkit.block.data.type.Switch) {
                        org.bukkit.block.data.type.Switch switchBlock = (org.bukkit.block.data.type.Switch) block.getBlockData();
                        BlockFace blockFace = switchBlock.getFacing().getOppositeFace();

                        if (switchBlock.getFace() == org.bukkit.block.data.type.Switch.Face.FLOOR) {
                            blockFace = BlockFace.DOWN;
                        } else if (switchBlock.getFace() == org.bukkit.block.data.type.Switch.Face.CEILING) {
                            blockFace = BlockFace.UP;
                        }

                        Block relBlock = block.getRelative(blockFace);
                        for (BlockFace axisBlockFace : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
                            locations.add(relBlock.getRelative(axisBlockFace).getLocation());
                        }
                        locations.add(block.getRelative(blockFace).getRelative(blockFace).getLocation());
                    }
                }
            } else {
                if (block.getState() != null) {
                    if (block.getState().getData() instanceof Attachable && block.getState().getData() instanceof Redstone &&
                            !(block.getState().getData() instanceof TripwireHook)) {
                        Attachable attachable = (Attachable) block.getState().getData();

                        Block relBlock = block.getRelative(attachable.getAttachedFace());
                        for (BlockFace axisBlockFace : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
                            locations.add(relBlock.getRelative(axisBlockFace).getLocation());
                        }
                        locations.add(block.getRelative(attachable.getAttachedFace()).getRelative(attachable.getAttachedFace()).getLocation());
                    }
                }
            }
        }

        for (BlockFace blockFace : blockFaces) {
            if (block.getRelative(blockFace).getState() instanceof Sign) {
                locations.add(block.getRelative(blockFace).getLocation());
            }
        }

        if (!locations.isEmpty()) {
            for (Location location : locations) {
                if (!(location.getBlock().getState() instanceof Sign)) {
                    continue;
                }

                Sign sign = (Sign) location.getBlock().getState();
                updateRedstonePower(sign, powered, skipLocation);
            }
        }
    }

    private void handlePlaceCancelled(Block block) {
        block.setType(Material.AIR);

        if (ConfigManager.getConfig().getDropSignBroken()) {
            block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.SIGN));
        }
    }

    private void updateRedstonePower(Sign sign, boolean powered, boolean skipLocation) {
        WirelessRedstone.getWRLogger().debug("Redstone power update: " + sign.getLocation());

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
            channel.turnOff(skipLocation ? sign.getLocation() : null, false);
        }
    }

}
