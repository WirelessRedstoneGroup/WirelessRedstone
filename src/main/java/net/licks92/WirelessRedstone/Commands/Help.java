package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

@CommandInfo(aliases = {"help", "h"}, description = "Show all available commands", permission = "commands.help", usage = "[page]", canUseInConsole = true)
public class Help extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) { //TODO: Add all these strings to the stringloader
        Integer page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                Utils.sendFeedback(Main.getStrings().pageNumberInferiorToZero, sender, true);
                return;
            }
        }

        ArrayList<String> commandList = new ArrayList<>();
        for (WirelessCommand gcmd : Main.getCommandManager().getCommands()) {
            CommandInfo info = gcmd.getClass().getAnnotation(CommandInfo.class);
            commandList.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "/wr "
                    + StringUtils.join(info.aliases(), ":") + " "
                    + info.usage() + ChatColor.WHITE + " - "
                    + ChatColor.GRAY + info.description());
        }

        Integer commandListLength = Main.getCommandManager().getCommands().size();
        Integer maxItemsPerPage = 8;
        Integer totalPages = 1;

        for (Integer i = 0; i < commandListLength / maxItemsPerPage; i++)
            totalPages++;

        if (page > totalPages) {
            if (totalPages > 1)
                Utils.sendFeedback("There are only " + totalPages + " pages.", sender, true);
            else
                Utils.sendFeedback("There is only 1 page.", sender, true);
            return;
        }

        Integer currentItem = ((page * maxItemsPerPage) - maxItemsPerPage);
        // 2*3 = 6 ; 6 - 3 = 3

        Utils.sendFeedback(ChatColor.WHITE + "WirelessRedstone help menu", sender, false);
        Utils.sendFeedback(ChatColor.WHITE + "Page " + page + " of " + totalPages, sender, false);

        if (totalPages == 0)
            Utils.sendFeedback(Main.getStrings().listEmpty, sender, true);
        else {
            for (Integer i = currentItem; i < (currentItem + maxItemsPerPage); i++) {
                if (!(i >= commandListLength))
                    Utils.sendFeedback(commandList.get(i), sender, false);
            }
        }
    }
}
