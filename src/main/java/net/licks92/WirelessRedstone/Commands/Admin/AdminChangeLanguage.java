package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.command.CommandSender;

@CommandInfo(description = "Change response language", usage = "<language>", aliases = {"changelanguage", "changel"},
        permission = "changelanguage", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminChangeLanguage extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandTooFewArguments, sender, true);
            return;
        }

        ConfigManager.getConfig().setValue(ConfigManager.ConfigPaths.LANGUAGE, args[0]);
        WirelessRedstone.getInstance().resetStrings();

        WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().commandLanguageChanged, sender, false);
    }
}
