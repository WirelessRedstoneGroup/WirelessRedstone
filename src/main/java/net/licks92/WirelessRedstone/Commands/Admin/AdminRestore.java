package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Storage.StorageType;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Restore database from latest backup", usage = "", aliases = {"restore"},
        permission = "restoredata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminRestore extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (Main.getStorage().restoreData()) {
            case YAML: {
                if (ConfigManager.getConfig().getStorageType() != StorageType.YAML) {
                    Main.getSignManager().stopAllClocks();

                    ConfigManager.getConfig().setStorageType(StorageType.YAML);
                    Main.getStorage().close();

                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            Main.getStorage().initStorage();
                        }
                    }, 1L);
                }

                Utils.sendFeedback(Main.getStrings().restoreDataDone, sender, false);
                break;
            }

            case SQLITE: {
                if (ConfigManager.getConfig().getStorageType() != StorageType.SQLITE) {
                    Main.getSignManager().stopAllClocks();

                    ConfigManager.getConfig().setStorageType(StorageType.SQLITE);
                    Main.getStorage().close();

                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            Main.getStorage().initStorage();
                        }
                    }, 1L);
                }

                break;
            }

            default:
                Utils.sendFeedback(Main.getStrings().restoreDataFailed, sender, true);
                break;
        }
    }
}
