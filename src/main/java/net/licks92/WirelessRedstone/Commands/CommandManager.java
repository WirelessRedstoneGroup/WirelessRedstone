package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Signs.SignType;
import net.licks92.WirelessRedstone.Signs.WirelessChannel;
import net.licks92.WirelessRedstone.Storage.StorageType;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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
                                + StringUtils.join(info.aliases(), "|") + getCommandUsage(info) + ChatColor.WHITE + " - "
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
        if (sender.isOp() || sender.hasPermission("wirelessredstone.Admin.isAdmin") ||
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

            if (args.length >= 2) {
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

                if (wanted != null) {
                    if (args.length == 2) {
                        String partial = args[1];
                        List<String> availableCompletions = getPossibleTabCompletions(wanted, 0);
                        StringUtil.copyPartialMatches(partial, availableCompletions, completions);
                    } else if (args.length == 3) {
                        String partial = args[2];
                        List<String> availableCompletions = getPossibleTabCompletions(wanted, 1);
                        StringUtil.copyPartialMatches(partial, availableCompletions, completions);
                    }
                }
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

    private List<String> getPossibleTabCompletions(WirelessCommand command, int index) {
        List<String> availableCompletions = new ArrayList<>();

        WirelessCommandTabCompletion[] tabCompletion = command.getClass().getAnnotation(CommandInfo.class).tabCompletion();
        if (tabCompletion.length >= index + 1) {
            if (tabCompletion[index] == WirelessCommandTabCompletion.BOOL) {
                availableCompletions.add(Boolean.TRUE.toString());
                availableCompletions.add(Boolean.FALSE.toString());
            } else if (tabCompletion[index] == WirelessCommandTabCompletion.PLAYER) {
                List<String> players = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    players.add(player.getName());
                }
                availableCompletions.addAll(players);
            } else if (tabCompletion[index] == WirelessCommandTabCompletion.CHANNEL) {
                for (WirelessChannel channel : WirelessRedstone.getStorageManager().getChannels()) {
                    availableCompletions.add(channel.getName());
                }
            } else if (tabCompletion[index] == WirelessCommandTabCompletion.SIGNTYPE) {
                for (SignType type : SignType.values()) {
                    availableCompletions.add(type.name());
                }
            } else if (tabCompletion[index] == WirelessCommandTabCompletion.STORAGETYPE) {
                for (StorageType type : StorageType.values()) {
                    availableCompletions.add(type.name());
                }
            }
        }

        return availableCompletions;
    }
}
