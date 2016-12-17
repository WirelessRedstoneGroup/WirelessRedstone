package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.WirelessRedstone;
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

public class CommandManager implements CommandExecutor {

    private ArrayList<WirelessCommand> cmds;

    public CommandManager() {
        cmds = new ArrayList<>();
        cmds.add(new Help());
        cmds.add(new Version());
        cmds.add(new Create());
        cmds.add(new Remove());
        cmds.add(new Info());
        cmds.add(new Activate());
        cmds.add(new Teleport());
        cmds.add(new Lock());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wr") || cmd.getName().equalsIgnoreCase("wredstone")
                || cmd.getName().equalsIgnoreCase("wifi") || cmd.getName().equalsIgnoreCase("wirelessredstone")) {
            if (args.length == 0) {
                int timer = 0;
                for (WirelessCommand gcmd : cmds) {
                    if (timer == 0)
                        WirelessRedstone.getUtils().sendFeedback(ChatColor.WHITE + "WirelessRedstone help menu", sender, false); //TODO: Add this string to the stringloader
                    if (timer >= 8) {
                        WirelessRedstone.getUtils().sendFeedback("Use /wr help 2 for the next page.", sender, false); //TODO: Add this string to the stringloader
                        break;
                    }

                    CommandInfo info = gcmd.getClass().getAnnotation(CommandInfo.class);
                    if (sender.hasPermission("wirelessredstone." + info.permission())) {
                        WirelessRedstone.getUtils().sendCommandFeedback(ChatColor.GRAY + "- " + ChatColor.GREEN + "/wr "
                                + StringWirelessRedstone.getUtils().join(info.aliases(), "|") + getCommandUsage(info) + ChatColor.WHITE + " - "
                                + ChatColor.GRAY + info.description(), sender, false);
                        timer++;
                    }
                }
                if (timer == 0) {
                    WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().playerDoesntHavePermission, sender, true, true);
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
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().subCommandDoesNotExist, sender, true, true);
                return true;
            }

            if (!sender.hasPermission("wirelessredstone.commands." + wanted.getClass().getAnnotation(CommandInfo.class).permission())) {
                WirelessRedstone.getUtils().sendFeedback(WirelessRedstone.getStrings().playerDoesntHavePermission, sender, true, true);
                return true;
            }

            if(!wanted.getClass().getAnnotation(CommandInfo.class).canUseInCommandBlock() && !(sender instanceof Player || sender instanceof ConsoleCommandSender)) {
                WirelessRedstone.getWRLogger().info("Commandblocks are not allowed to run command: /wr " + args[0]);
                return true;
            }

            if (!wanted.getClass().getAnnotation(CommandInfo.class).canUseInConsole() && sender instanceof ConsoleCommandSender) {
                WirelessRedstone.getUtils().sendFeedback("Only in-game players can use this command.", sender, true); //TODO: Add this string to the stringloader
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