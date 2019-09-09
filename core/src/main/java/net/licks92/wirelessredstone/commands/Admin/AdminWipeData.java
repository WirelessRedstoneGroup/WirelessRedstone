package net.licks92.wirelessredstone.commands.Admin;

import net.licks92.wirelessredstone.commands.CommandInfo;
import net.licks92.wirelessredstone.commands.WirelessCommand;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

@CommandInfo(description = "Wipe all channels", usage = "", aliases = {"wipedata"},
        permission = "wipeData", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminWipeData extends WirelessCommand {

    private final ArrayList<UUID> confirmation = new ArrayList<>();

    @Override
    public void onCommand(final CommandSender sender, String[] args) {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        }

        if (!confirmation.contains(uuid)) {
            Utils.sendFeedback(ChatColor.BOLD + WirelessRedstone.getStrings().dbDeleteConfirm, sender, true);

            confirmation.add(uuid);

            final UUID finalUuid = uuid;
            Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), () -> confirmation.remove(finalUuid), 20 * 15);
            return;
        }

        confirmation.remove(uuid);

        if (WirelessRedstone.getStorage().wipeData()) {
            Utils.sendFeedback(WirelessRedstone.getStrings().dbDeleteDone, sender, false);
        } else {
            Utils.sendFeedback(WirelessRedstone.getStrings().dbDeleteFailed, sender, true);
        }
    }
}