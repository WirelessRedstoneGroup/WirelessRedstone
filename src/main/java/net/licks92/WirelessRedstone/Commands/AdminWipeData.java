package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

@CommandInfo(description = "Wipe all channels", usage = "", aliases = {"wipedata"},
        permission = "wipeData", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminWipeData extends WirelessCommand {

    private ArrayList<String> confirmation = new ArrayList<>();

    @Override
    public void onCommand(final CommandSender sender, String[] args) {
        if (!confirmation.contains(sender.getName())) {
            Utils.sendFeedback(Main.getStrings().DBAboutToBeDeleted, sender, true);

            confirmation.add(sender.getName());

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    confirmation.remove(sender.getName());
                }
            }, 20 * 15);

            return;
        }

        confirmation.remove(sender.getName());

        if (Main.getStorage().wipeData()) {
            Utils.sendFeedback(Main.getStrings().DBDeleted, sender, false);
        } else {
            Utils.sendFeedback(Main.getStrings().DBNotDeleted, sender, true);
        }
    }
}
