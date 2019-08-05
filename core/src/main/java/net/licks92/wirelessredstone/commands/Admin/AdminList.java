package net.licks92.wirelessredstone.commands.Admin;

import net.licks92.wirelessredstone.commands.CommandInfo;
import net.licks92.wirelessredstone.commands.WirelessCommand;
import net.licks92.wirelessredstone.signs.WirelessChannel;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

@CommandInfo(description = "Get all channels", usage = "[page]", aliases = {"list", "l"},
        permission = "list", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminList extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                Utils.sendFeedback(WirelessRedstone.getStrings().commandInferiorZero, sender, true);
                return;
            }
        }

        ArrayList<String> channelList = new ArrayList<>();
        for (WirelessChannel channel : WirelessRedstone.getStorageManager().getChannels()) {
            channelList.add(ChatColor.GRAY + "- " + ChatColor.GREEN + channel.getName());
        }

        int channelListLength = WirelessRedstone.getStorageManager().getChannels().size();
        int maxItemsPerPage = 10;
        int totalPages = 1;

        for (int i = 0; i < channelListLength / maxItemsPerPage; i++) {
            totalPages++;
        }

        if (channelListLength % maxItemsPerPage == 0) {
            totalPages--;
        }

        if (page > totalPages) {
            if (totalPages > 1)
                Utils.sendFeedback("There are only " + totalPages + " pages.", sender, true);
            else
                Utils.sendFeedback("There is only 1 page.", sender, true);
            return;
        }

        int currentItem = ((page * maxItemsPerPage) - maxItemsPerPage);
        // 2*3 = 6 ; 6 - 3 = 3

        Utils.sendFeedback(ChatColor.WHITE + "WirelessRedstone channels", sender, false);
        Utils.sendFeedback(ChatColor.WHITE + "Page " + page + " of " + totalPages, sender, false);

        if (totalPages == 0) {
            Utils.sendFeedback(WirelessRedstone.getStrings().commandNoData, sender, true);
        } else {
            for (int i = currentItem; i < (currentItem + maxItemsPerPage); i++) {
                if (!(i >= channelListLength))
                    Utils.sendCommandFeedback(channelList.get(i), sender, false);
            }
        }
    }
}
