package net.licks92.wirelessredstone.listeners;

import net.licks92.wirelessredstone.ConfigManager;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import net.licks92.wirelessredstone.compat.InternalProvider;
import net.licks92.wirelessredstone.materiallib.data.CrossMaterial;
import net.licks92.wirelessredstone.signs.SignType;
import net.licks92.wirelessredstone.signs.WirelessChannel;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(BlockRedstoneEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE || event.getBlock().getType() == Material.REDSTONE_WIRE) {
            if (event.getNewCurrent() < event.getOldCurrent() && event.getNewCurrent() != 0) {
                return;
            }

            boolean skipLocation = false;
            if (Utils.isNewMaterialSystem()) {
                if (event.getOldCurrent() < 15) {
                    skipLocation = true;
                }
            }

            handleRedstoneEvent(event.getBlock(), event.getNewCurrent() > 0, skipLocation, false); // skipLocation: true
        } else {
            handleRedstoneEvent(event.getBlock(), event.getNewCurrent() > 0, false, event.getNewCurrent() == 0);
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

        if (!InternalProvider.getCompatBlockData().isPowerable(event.getBlock())) {
            return;
        }

        // Testing for better performance
//        if (event.getBlock().getType() == Material.REDSTONE || event.getBlock().getType() == Material.REDSTONE_WIRE) {
//            return;
//        }

        boolean isPowered = InternalProvider.getCompatBlockData().isPowered(event.getBlock());

        // Testing to handle only dispowering or all events
        if (!isPowered) {
            handleRedstoneEvent(event.getBlock(), isPowered, false, false);
        }
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
        Bukkit.getScheduler().runTask(WirelessRedstone.getInstance(), () -> {
            if (!(event.getBlock().getState() instanceof Sign)) {
                return;
            }

            Sign sign = (Sign) event.getBlock().getState();
            BlockFace signDirection = InternalProvider.getCompatBlockData().getSignRotation(event.getBlock());

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
        });
    }

    @EventHandler
    public void on(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE_BLOCK || CrossMaterial.REDSTONE_TORCH.equals(event.getBlock().getType())) {
            handleRedstoneEvent(event.getBlock(), true, false, false);
        }
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE_BLOCK || CrossMaterial.REDSTONE_TORCH.equals(event.getBlock().getType())) {
            handleRedstoneEvent(event.getBlock(), false, true, false);
        }

        // MC <= 1.12 #getBlock can be NULL
        if (event.getBlock() == null) {
            return;
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

    private void handleRedstoneEvent(Block block, boolean powered, boolean skipLocation, boolean useScheduler) {
        Collection<BlockFace> blockFaces = Utils.getAxisBlockFaces();
        List<Location> locations = new ArrayList<>();
        Material type = block.getType();

        if (CrossMaterial.REPEATER.equals(type) || CrossMaterial.REPEATER_ON.equals(type) || CrossMaterial.REPEATER_OFF.equals(type) ||
                CrossMaterial.COMPARATOR.equals(type) || CrossMaterial.COMPARATOR_ON.equals(type) || CrossMaterial.COMPARATOR_OFF.equals(type)) {
            if (Utils.isNewMaterialSystem()) {
//                org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) block.getBlockData();
                BlockFace direction = InternalProvider.getCompatBlockData().getDirectionalFacing(block);

                if (block.getRelative(direction.getOppositeFace()).getType().isOccluding() &&
                        !block.getRelative(direction.getOppositeFace()).getType().isInteractable()) {
                    Block relBlock = block.getRelative(direction.getOppositeFace());
                    locations = Utils.getAxisBlockFaces().stream()
                            .map(axisBlockFace -> relBlock.getRelative(axisBlockFace).getLocation())
                            .collect(Collectors.toList());
                }

                locations.add(block.getRelative(direction.getOppositeFace()).getRelative(direction.getOppositeFace()).getLocation());
                blockFaces = Collections.singletonList(direction.getOppositeFace());
            } else {
//                Directional directional = (Directional) block.getState().getData();
                BlockFace direction = InternalProvider.getCompatBlockData().getDirectionalFacing(block);

                if (block.getRelative(direction).getType().isOccluding()) {
                    Block relBlock = block.getRelative(direction);
                    locations = Utils.getAxisBlockFaces().stream()
                            .map(axisBlockFace -> relBlock.getRelative(axisBlockFace).getLocation())
                            .collect(Collectors.toList());
                }

                locations.add(block.getRelative(direction).getRelative(direction).getLocation());
                blockFaces = Collections.singletonList(direction);
            }
        } else if (type == Material.DAYLIGHT_DETECTOR || type == Material.DETECTOR_RAIL
                || type.name().toUpperCase().endsWith("_PLATE")) {
            locations.add(block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getLocation());
        } else {
            if (InternalProvider.getCompatBlockData().isRedstoneSwitch(block)) {
                BlockFace direction = InternalProvider.getCompatBlockData().getRedstoneSwitchFacing(block);

                Block relBlock = block.getRelative(direction);
                for (BlockFace axisBlockFace : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN)) {
                    locations.add(relBlock.getRelative(axisBlockFace).getLocation());
                }

                locations.add(block.getRelative(direction).getRelative(direction).getLocation());
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

                if (useScheduler) {
                    Bukkit.getScheduler().runTask(WirelessRedstone.getInstance(), () -> updateRedstonePower(sign, powered, skipLocation));
                } else {
                    updateRedstonePower(sign, powered, skipLocation);
                }
            }
        }
    }

    private void handlePlaceCancelled(Block block) {
        block.setType(Material.AIR);

        if (ConfigManager.getConfig().getDropSignBroken()) {
            CrossMaterial.SIGN.getHandle()
                    .ifPresent(materialHandler -> block.getWorld().dropItem(block.getLocation(), new ItemStack(materialHandler.getType())));
        }
    }

    private void updateRedstonePower(Sign sign, boolean powered, boolean skipLocation) {
        if ("TRUE".equalsIgnoreCase(System.getProperty("mc.development"))) {
            WirelessRedstone.getWRLogger().debug("Redstone power update (" + powered + "): " + sign.getLocation());
        }

        if (Utils.getSignType(sign.getLine(0)) != SignType.TRANSMITTER)
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
            channel.turnOff(skipLocation ? sign.getLocation() : null);
        }
    }

}
