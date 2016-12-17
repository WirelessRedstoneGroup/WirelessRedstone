package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

@CommandInfo(description = "Wipe all channels", usage = "", aliases = {"wipedata"},
        permission = "wipeData", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminWipeData extends WirelessCommand {

    private ArrayList<String> confirmation = new ArrayList<>();

    @Override
    public void onCommand(final CommandSender sender, String[] args) {
        if (!confirmation.contains(sender.getName())) {
            Utils.sendFeedback(ChatColor.BOLD + WirelessRedstone.getStrings().DBAboutToBeDeleted, sender, true);

            confirmation.add(sender.getName());

            Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                @Override
                public void run() {
                    confirmation.remove(sender.getName());
                }
            }, 20 * 15);

            return;
        }

        confirmation.remove(sender.getName());

        if (WirelessRedstone.getStorage().wipeData()) {
            Utils.sendFeedback(WirelessRedstone.getStrings().DBDeleted, sender, false);
        } else {
            Utils.sendFeedback(WirelessRedstone.getStrings().DBNotDeleted, sender, true);
        }
    }
}
