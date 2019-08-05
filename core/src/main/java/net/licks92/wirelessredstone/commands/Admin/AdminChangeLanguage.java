package net.licks92.wirelessredstone.commands.Admin;

import net.licks92.wirelessredstone.commands.CommandInfo;
import net.licks92.wirelessredstone.commands.WirelessCommand;
import net.licks92.wirelessredstone.ConfigManager;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Change response language", usage = "<language>", aliases = {"changelanguage", "changel"},
        permission = "changelanguage", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminChangeLanguage extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        ConfigManager.getConfig().setValue(ConfigManager.ConfigPaths.LANGUAGE, args[0]);
        WirelessRedstone.getInstance().resetStrings();

        Utils.sendFeedback(WirelessRedstone.getStrings().commandLanguageChanged, sender, false);
    }
}
