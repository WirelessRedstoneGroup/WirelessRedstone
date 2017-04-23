package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Storage.StorageType;
import net.licks92.WirelessRedstone.WirelessRedstone;
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
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        if (!confirmation.contains(sender.getName())) {
            WirelessRedstone.getUtils().sendFeedback(ChatColor.BOLD + WirelessRedstone.getStrings()
                    .dbConvertConfirm.replaceAll("%%STORAGETYPE", args[0]), sender, true);

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

        switch (args[0].toUpperCase()) {
            case "YML":
            case "YAML": {
                if (ConfigManager.getConfig().getStorageType() == StorageType.YAML) {
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().dbConvertSameType, sender, true);
                    return;
                }

                WirelessRedstone.getSignManager().stopAllClocks();

                ConfigManager.getConfig().setStorageType(StorageType.YAML);
//                WirelessRedstone.getStorage().backupData("yml");
                WirelessRedstone.getStorage().close();

                WirelessRedstone.getInstance().resetStorageManager();
                WirelessRedstone.getStorage().initStorage();
                break;
            }

            case "SQL":
            case "SQLITE": {
                if (ConfigManager.getConfig().getStorageType() == StorageType.SQLITE) {
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().dbConvertSameType, sender, true);
                    return;
                }

                WirelessRedstone.getSignManager().stopAllClocks();

                ConfigManager.getConfig().setStorageType(StorageType.SQLITE);
//                WirelessRedstone.getStorage().backupData("db");
                WirelessRedstone.getStorage().close();

                WirelessRedstone.getInstance().resetStorageManager();
                WirelessRedstone.getStorage().initStorage();
                break;
            }

            default:
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().dbConvertFailed, sender, true);
                return;
        }

        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
            @Override
            public void run() {
                WirelessRedstone.getStorage().initStorage();
            }
        }, 1L);

        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().dbConvertDone, sender, false);
    }
}
