package net.licks92.WirelessRedstone.Commands.Admin;

import net.licks92.WirelessRedstone.Commands.CommandInfo;
import net.licks92.WirelessRedstone.Commands.WirelessCommand;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

@CommandInfo(description = "Get all channels", usage = "[page]", aliases = {""},
        permission = "list", canUseInConsole = true, canUseInCommandBlock = false)
public class AdminList extends WirelessCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Integer page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                Utils.sendFeedback(Main.getStrings().pageNumberInferiorToZero, sender, true);
                return;
            }
        }

        ArrayList<String> channelList = new ArrayList<>();
        for (WirelessChannel channel : Main.getStorage().getAllChannels()) {
            channelList.add(ChatColor.GRAY + "- " + ChatColor.GREEN + channel.getName());
        }

        Integer channelListLength = Main.getStorage().getAllChannels().size();
        Integer maxItemsPerPage = 8;
        Integer totalPages = 1;

        for (Integer i = 0; i < channelListLength / maxItemsPerPage; i++)
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

        Utils.sendFeedback(ChatColor.WHITE + "WirelessRedstone channels", sender, false);
        Utils.sendFeedback(ChatColor.WHITE + "Page " + page + " of " + totalPages, sender, false);

        if (totalPages == 0)
            Utils.sendFeedback(Main.getStrings().listEmpty, sender, true);
        else {
            for (Integer i = currentItem; i < (currentItem + maxItemsPerPage); i++) {
                if (!(i >= channelListLength))
                    Utils.sendCommandFeedback(channelList.get(i), sender, false);
            }
        }
    }
}
