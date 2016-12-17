package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Storage.StorageType;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Restore database from latest backup", usage = "", aliases = {"restore"},
        permission = "restoredata", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminRestore extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (WirelessRedstone.getStorage().restoreData()) {
            case YAML: {
                if (ConfigManager.getConfig().getStorageType() != StorageType.YAML) {
                    WirelessRedstone.getSignManager().stopAllClocks();

                    ConfigManager.getConfig().setStorageType(StorageType.YAML);
                    WirelessRedstone.getStorage().close();

                    Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            WirelessRedstone.getStorage().initStorage();
                        }
                    }, 1L);
                }

                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().restoreDataDone, sender, false);
                break;
            }

            case SQLITE: {
                if (ConfigManager.getConfig().getStorageType() != StorageType.SQLITE) {
                    WirelessRedstone.getSignManager().stopAllClocks();

                    ConfigManager.getConfig().setStorageType(StorageType.SQLITE);
                    WirelessRedstone.getStorage().close();

                    Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            WirelessRedstone.getStorage().initStorage();
                        }
                    }, 1L);
                }

                break;
            }

            default:
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().restoreDataFailed, sender, true);
                break;
        }
    }
}
