package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class CommandManager implements CommandExecutor {

    private ArrayList<WirelessCommand> cmds;

    public CommandManager() {
        cmds = new ArrayList<>();
        cmds.add(new Help());
        cmds.add(new Version());
        cmds.add(new Activate());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wr") || cmd.getName().equalsIgnoreCase("wredstone")
                || cmd.getName().equalsIgnoreCase("wifi") || cmd.getName().equalsIgnoreCase("wirelessredstone")) {
            if (args.length == 0) {
                int timer = 0;
                for (WirelessCommand gcmd : cmds) {
                    if (timer == 8) {
                        Utils.sendFeedback("Use /wr help 2 for the next page", sender, false); //TODO: Add this string to the stringloader
                        break;
                    }

                    CommandInfo info = gcmd.getClass().getAnnotation(CommandInfo.class);
                    if (sender.hasPermission("wirelessredstone." + info.permission())) {
                        Utils.sendFeedback(ChatColor.GRAY + "- " + ChatColor.GREEN + "/wr "
                                + StringUtils.join(info.aliases(), "|") + getCommandUsage(info) + ChatColor.WHITE + " - "
                                + ChatColor.GRAY + info.description(), sender, false);
                        timer++;
                    }
                }
                if (timer == 0) {
                    Utils.sendFeedback(Main.getStrings().playerDoesntHavePermission, sender, true, true);
                }

                return true;
            }

            WirelessCommand wanted = null;

            for (WirelessCommand gcmd : cmds) {
                CommandInfo info = gcmd.getClass().getAnnotation(CommandInfo.class);
                for (String alias : info.aliases()) {
                    if (alias.equals(args[0])) {
                        wanted = gcmd;
                        break;
                    }
                }
            }

            if (wanted == null) {
                Utils.sendFeedback(Main.getStrings().subCommandDoesNotExist, sender, true, true);
                return true;
            }

            if (!sender.hasPermission("wirelessredstone." + wanted.getClass().getAnnotation(CommandInfo.class).permission())) {
                Utils.sendFeedback(Main.getStrings().playerDoesntHavePermission, sender, true, true);
                return true;
            }

            if (!wanted.getClass().getAnnotation(CommandInfo.class).canUseInConsole() && !(sender instanceof Player)) {
                Utils.sendFeedback("Only in-game players can use this command.", sender, false); //TODO: Add this string to the stringloader
                return true;
            }

            Vector<String> a = new Vector<String>(Arrays.asList(args));
            a.remove(0);
            args = a.toArray(new String[a.size()]);

            wanted.onCommand(sender, args);
        }

        return true;
    }

    public List<WirelessCommand> getCommands() {
        return cmds;
    }

    private String getCommandUsage(CommandInfo info){
        if(info.usage().equalsIgnoreCase(""))
            return "";
        else
            return " " + info.usage();
    }
}