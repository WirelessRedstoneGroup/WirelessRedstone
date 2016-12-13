package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Storage.StorageType;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

@CommandInfo(description = "Convert all channels to another database", usage = "<storage type>", aliases = {"convert"},
        permission = "convertdata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminConvert extends WirelessCommand {

    private ArrayList<String> confirmation = new ArrayList<>();

    @Override
    public void onCommand(final CommandSender sender, String[] args) {
        if (args.length == 0) {
            Utils.sendFeedback(Main.getStrings().tooFewArguments, sender, true);
            return;
        }

        if (!confirmation.contains(sender.getName())) {
            Utils.sendFeedback(ChatColor.BOLD + Main.getStrings().convertContinue.replaceAll("%%STORAGETYPE", args[0]), sender, true);

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

        switch (args[0].toUpperCase()) {
            case "YML":
            case "YAML": {
                if (ConfigManager.getConfig().getStorageType() == StorageType.YAML) {
                    Utils.sendFeedback(Main.getStrings().convertSameType, sender, true);
                    return;
                }

                Main.getSignManager().stopAllClocks();

                ConfigManager.getConfig().setStorageType(StorageType.YAML);
//                Main.getStorage().backupData("yml");
                Main.getStorage().close();

                Main.getInstance().resetStorageManager();
                Main.getStorage().initStorage();
                break;
            }

            case "SQL":
            case "SQLITE": {
                if (ConfigManager.getConfig().getStorageType() == StorageType.SQLITE) {
                    Utils.sendFeedback(Main.getStrings().convertSameType, sender, true);
                    return;
                }

                Main.getSignManager().stopAllClocks();

                ConfigManager.getConfig().setStorageType(StorageType.SQLITE);
//                Main.getStorage().backupData("db");
                Main.getStorage().close();

                Main.getInstance().resetStorageManager();
                Main.getStorage().initStorage();
                break;
            }

            default:
                Utils.sendFeedback(Main.getStrings().convertFailed, sender, true);
                return;
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                Main.getStorage().initStorage();
            }
        }, 1L);

        Utils.sendFeedback(Main.getStrings().convertDone, sender, false);
    }
}
