package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class AdminCommandManager implements CommandExecutor {

    private ArrayList<WirelessCommand> cmds;

    public AdminCommandManager() {
        cmds = new ArrayList<>();
        cmds.add(new AdminAddOwner());
        cmds.add(new AdminRemoveOwner());
        cmds.add(new AdminBackup());
        cmds.add(new AdminPurge());
        cmds.add(new AdminWipeData());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wra") || cmd.getName().equalsIgnoreCase("wradmin")) {
            if (args.length == 0) {
                int timer = 0;
                for (WirelessCommand gcmd : cmds) {
                    if (timer == 0)
                        Utils.sendFeedback(ChatColor.WHITE + "WirelessRedstone admin help menu", sender, false);
                    if (timer >= 8) {
                        Utils.sendFeedback("Use /wra help 2 for the next page.", sender, false); //TODO: Add this string to the stringloader
                        break;
                    }

                    CommandInfo info = gcmd.getClass().getAnnotation(CommandInfo.class);
                    if (sender.hasPermission("wirelessredstone.admin." + info.permission())) {
                        Utils.sendFeedback(ChatColor.GRAY + "- " + ChatColor.GREEN + "/wra "
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

            if (!sender.hasPermission("wirelessredstone.admin." + wanted.getClass().getAnnotation(CommandInfo.class).permission())) {
                Utils.sendFeedback(Main.getStrings().playerDoesntHavePermission, sender, true, true);
                return true;
            }

            if (!(sender instanceof Player || sender instanceof ConsoleCommandSender)) {
                Main.getWRLogger().info("Commandblocks are not allowed to run command: /wradmin " + args[0]);
                return true;
            }

            if (!wanted.getClass().getAnnotation(CommandInfo.class).canUseInConsole() && sender instanceof ConsoleCommandSender) {
                Utils.sendFeedback("Only in-game players can use this command.", sender, true); //TODO: Add this string to the stringloader
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

    private String getCommandUsage(CommandInfo info) {
        if (info.usage().equalsIgnoreCase(""))
            return "";
        else
            return " " + info.usage();
    }

}
