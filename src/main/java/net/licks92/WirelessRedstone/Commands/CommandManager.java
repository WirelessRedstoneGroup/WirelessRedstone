package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class CommandManager implements CommandExecutor, TabCompleter {

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
                        Utils.sendFeedback(ChatColor.WHITE + "WirelessRedstone help menu", sender, false); //TODO: Add this string to the stringloader
                    if (timer >= 8) {
                        Utils.sendFeedback("Use /wr help 2 for the next page.", sender, false); //TODO: Add this string to the stringloader
                        break;
                    }

                    CommandInfo info = gcmd.getClass().getAnnotation(CommandInfo.class);
                    if (sender.hasPermission("wirelessredstone.commands." + info.permission())) {
                        Utils.sendCommandFeedback(ChatColor.GRAY + "- " + ChatColor.GREEN + "/wr "
                                + StringUtils.join(info.aliases(), " | ") + getCommandUsage(info) + ChatColor.WHITE + " - "
                                + ChatColor.GRAY + info.description(), sender, false);
                        timer++;
                    }
                }
                if (timer == 0) {
                    Utils.sendFeedback(WirelessRedstone.getStrings().permissionGeneral, sender, true, true);
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
                Utils.sendFeedback(WirelessRedstone.getStrings().commandNotFound, sender, true, true);
                return true;
            }

            if (!sender.hasPermission("wirelessredstone.commands." + wanted.getClass().getAnnotation(CommandInfo.class).permission())) {
                Utils.sendFeedback(WirelessRedstone.getStrings().permissionGeneral, sender, true, true);
                return true;
            }

            if (!wanted.getClass().getAnnotation(CommandInfo.class).canUseInCommandBlock() && !(sender instanceof Player || sender instanceof ConsoleCommandSender)) {
                WirelessRedstone.getWRLogger().info("Commandblocks are not allowed to run command: /wr " + args[0]);
                return true;
            }

            if (!wanted.getClass().getAnnotation(CommandInfo.class).canUseInConsole() && sender instanceof ConsoleCommandSender) {
                Utils.sendFeedback(WirelessRedstone.getStrings().commandOnlyInGame, sender, true);
                return true;
            }

            Vector<String> a = new Vector<String>(Arrays.asList(args));
            a.remove(0);
            args = a.toArray(new String[a.size()]);

            wanted.onCommand(sender, args);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp() || sender.hasPermission("wirelessredstone.admin.isAdmin") ||
                sender.hasPermission("wirelessredstone.commands." + args[0])) {
            List<String> completions = new ArrayList<>();

            if (args.length == 1) {
                String partialCommand = args[0];
                List<String> commands = new ArrayList<>();

                for (WirelessCommand cmd : cmds) {
                    commands.add(cmd.getClass().getAnnotation(CommandInfo.class).aliases()[0]);
                }

                StringUtil.copyPartialMatches(partialCommand, commands, completions);
            }

            if (args.length == 2) {
                String partialChannel = args[1];
                List<String> channels = new ArrayList<>();

                for (WirelessChannel channel : WirelessRedstone.getStorageManager().getChannels()) {
                    channels.add(channel.getName());
                }

                StringUtil.copyPartialMatches(partialChannel, channels, completions);
            }

            Collections.sort(completions);

            return completions;

        }

        return null;
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
