package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.Compat.InternalProvider;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Permissions;
import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.UpdateChecker;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.materiallib.data.CrossMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class PlayerListener implements Listener {

    @EventHandler
    public void on(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        if (!(event.getClickedBlock().getState() instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) event.getClickedBlock().getState();
        SignType signType = Utils.getSignType(sign.getLine(0));
        if (signType == null) {
            return;
        }

        if (signType == SignType.TRANSMITTER) {
            if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                handlePlaceCancelled(event.getClickedBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true, true);
                return;
            }
        } else if (signType == SignType.RECEIVER) {
            if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                handlePlaceCancelled(event.getClickedBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true, true);
                return;
            }
        } else if (signType == SignType.SCREEN) {
            if (!WirelessRedstone.getSignManager().canPlaceSign(event.getPlayer(), signType)) {
                handlePlaceCancelled(event.getClickedBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionCreateSign, event.getPlayer(), true, true);
                return;
            }
        } else {
            return;
        }

        signType = Utils.getSignType(sign.getLine(0), sign.getLine(2));

        if (sign.getLine(1).equalsIgnoreCase("")) {
            handlePlaceCancelled(event.getClickedBlock());
            Utils.sendFeedback(WirelessRedstone.getStrings().noChannelName, event.getPlayer(), true);
            return;
        }

        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(sign.getLine(1));
        if (channel != null) {
            if (!WirelessRedstone.getSignManager().hasAccessToChannel(event.getPlayer(), sign.getLine(1))) {
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionChannelAccess, event.getPlayer(), true);
                handlePlaceCancelled(event.getClickedBlock());
                return;
            }
        }

        if (WirelessRedstone.getSignManager().isSignRegistred(event.getClickedBlock().getLocation())) {
            return;
        }

        int delay = 0;
        try {
            delay = Integer.parseInt(sign.getLine(3));
        } catch (NumberFormatException ignored) {
            if (signType == SignType.RECEIVER_DELAYER) {
                handlePlaceCancelled(event.getClickedBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().delayNumberOnly, event.getPlayer(), true);
                return;
            } else if (signType == SignType.RECEIVER_CLOCK) {
                handlePlaceCancelled(event.getClickedBlock());
                Utils.sendFeedback(WirelessRedstone.getStrings().intervalNumberOnly, event.getPlayer(), true);
                return;
            }
        }

        BlockFace signDirection = InternalProvider.getCompatBlockData().getSignRotation(sign.getBlock());

        int result = WirelessRedstone.getSignManager().registerSign(
                sign.getLine(1),
                event.getClickedBlock(),
                Utils.getSignType(sign.getLine(0), sign.getLine(2)),
                signDirection,
                Collections.singletonList(event.getPlayer().getUniqueId().toString()),
                delay
        );

        if (result == 0) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelExtended, event.getPlayer(), false);
        } else if (result == 1) {
            Utils.sendFeedback(WirelessRedstone.getStrings().channelCreated, event.getPlayer(), false);
        } else if (result == -1) {
            handlePlaceCancelled(event.getClickedBlock());
            Utils.sendFeedback(WirelessRedstone.getStrings().commandDelayMin, event.getPlayer(), true);
        } else if (result == -2) {
            handlePlaceCancelled(event.getClickedBlock());
            Utils.sendFeedback(WirelessRedstone.getStrings().commandIntervalMin, event.getPlayer(), true);
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(Permissions.isWirelessAdmin)) {
            return;
        }

        UpdateChecker updateChecker = UpdateChecker.init(WirelessRedstone.getInstance());

        if (updateChecker.getLastResult() == null) {
            return;
        }

        if (!updateChecker.getLastResult().updateAvailable()) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(),
                () -> Utils.sendFeedback(WirelessRedstone.getStrings().newUpdate
                        .replaceAll("%%NEWVERSION", updateChecker.getLastResult().getNewestVersion())
                        .replaceAll("%%URL", updateChecker.getLastResult().getUrl()), event.getPlayer(), false),
                1L);
    }

    private void handlePlaceCancelled(Block block) {
        block.setType(Material.AIR);

        if (ConfigManager.getConfig().getDropSignBroken()) {
            CrossMaterial.SIGN.getHandle()
                    .ifPresent(materialHandler -> block.getWorld().dropItem(block.getLocation(), new ItemStack(materialHandler.getType())));
        }
    }

}
