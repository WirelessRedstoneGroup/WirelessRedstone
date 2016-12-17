package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

@CommandInfo(description = "Get admin help page", usage = "[page]", aliases = {"help", "h"},
        permission = "isAdmin", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminHelp extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Integer page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                Utils.sendFeedback(WirelessRedstone.getStrings().pageNumberInferiorToZero, sender, true);
                return;
            }
        }

        ArrayList<String> commandList = new ArrayList<>();
        for (WirelessCommand gcmd : WirelessRedstone.getAdminCommandManager().getCommands()) {
            CommandInfo info = gcmd.getClass().getAnnotation(CommandInfo.class);
            commandList.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "/wra "
                    + StringUtils.join(info.aliases(), ":") + " "
                    + info.usage() + ChatColor.WHITE + " - "
                    + ChatColor.GRAY + info.description());
        }

        Integer commandListLength = WirelessRedstone.getAdminCommandManager().getCommands().size();
        Integer maxItemsPerPage = 8;
        Integer totalPages = 1;

        for (Integer i = 0; i < commandListLength / maxItemsPerPage; i++)
            totalPages++;

        if(commandListLength % maxItemsPerPage == 0)
            totalPages--;

        if (page > totalPages) {
            if (totalPages > 1)
                Utils.sendFeedback("There are only " + totalPages + " pages.", sender, true);
            else
                Utils.sendFeedback("There is only 1 page.", sender, true);
            return;
        }

        Integer currentItem = ((page * maxItemsPerPage) - maxItemsPerPage);
        // 2*3 = 6 ; 6 - 3 = 3

        Utils.sendFeedback(ChatColor.WHITE + "WirelessRedstone admin help menu", sender, false);
        Utils.sendFeedback(ChatColor.WHITE + "Page " + page + " of " + totalPages, sender, false);

        if (totalPages == 0)
            Utils.sendFeedback(WirelessRedstone.getStrings().listEmpty, sender, true);
        else {
            for (Integer i = currentItem; i < (currentItem + maxItemsPerPage); i++) {
                if (!(i >= commandListLength))
                    Utils.sendCommandFeedback(commandList.get(i), sender, false);
            }
        }
    }
}
