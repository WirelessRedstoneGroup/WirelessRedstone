package net.licks92.WirelessRedstone.Commands;

import net.licks92.WirelessRedstone.Signs.WirelessReceiver;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
                                + StringUtils.join(info.aliases(), "|") + getCommandUsage(info) + ChatColor.WHITE + " - "
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

            if (!wanted.getClass().getAnnotation(CommandInfo.class).canUseInCommandBlock() && !(sender instanceof Player || sender instanceof ConsoleCommandSender)) {
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

        if (cmd.getName().equalsIgnoreCase("wrt") || cmd.getName().equalsIgnoreCase("wrr") || cmd.getName().equalsIgnoreCase("wrs")) {
            if (args.length == 0) {
                WirelessRedstone.getUtils().sendCommandFeedback(WirelessRedstone.getStrings().tooFewArguments, sender, true);
                return true;
            }

            if (!(sender instanceof Player)) {
                WirelessRedstone.getUtils().sendFeedback("Only in-game players can use this command.", sender, true);
                return true;
            }

            Player player = (Player) sender;

            String channelName = args[0];
            if (WirelessRedstone.getSignManager().hasAccessToChannel(player, channelName)) {
                player.getLocation().getBlock();
                player.getLocation().getBlock().setType(Material.SIGN_POST);

                Sign sign = (Sign) player.getLocation().getBlock().getState();
                sign.setLine(1, channelName);

                if (cmd.getName().equalsIgnoreCase("wrt"))
                    sign.setLine(0, WirelessRedstone.getStrings().tagsTransmitter.get(0));
                else if (cmd.getName().equalsIgnoreCase("wrr")) {
                    sign.setLine(0, WirelessRedstone.getStrings().tagsReceiver.get(0));
                    if (args.length > 1) {
                        String type = args[1];
                        switch (type) {
                            case "inverter":
                            case "inv": {
                                sign.setLine(2, WirelessRedstone.getStrings().tagsReceiverInverterType.get(0));
                                sign.update();

                                if (!WirelessRedstone.getSignManager().addWirelessReceiver(
                                        channelName, player.getLocation().getBlock(),
                                        player, WirelessReceiver.Type.INVERTER)) {
                                    sign.getBlock().setType(Material.AIR);
                                    return true;
                                }
                                break;
                            }
                            case "delayer":
                            case "delay":
                            case "del": {
                                if (args.length >= 3) {
                                    int delay;
                                    try {
                                        delay = Integer.parseInt(args[2]);
                                    } catch (NumberFormatException ex) {
                                        WirelessRedstone.getUtils().sendCommandFeedback("The delay must be a number!", sender, true);
                                        sign.getBlock().setType(Material.AIR);
                                        return true;
                                    }

                                    sign.setLine(2, WirelessRedstone.getStrings().tagsReceiverDelayerType.get(0));
                                    sign.setLine(3, Integer.toString(delay));
                                    sign.update();

                                    if (!WirelessRedstone.getSignManager().addWirelessReceiver(
                                            channelName, player.getLocation()
                                                    .getBlock(), player, WirelessReceiver.Type.DELAYER)) {
                                        sign.getBlock().setType(Material.AIR);
                                        return true;
                                    }
                                }
                                if (args.length < 3) {
                                    WirelessRedstone.getUtils().sendCommandFeedback(WirelessRedstone.getStrings().tooFewArguments, sender, true);
                                }
                                break;
                            }
                            case "clock":
                                if (args.length >= 3) {
                                    int delay;
                                    try {
                                        delay = Integer.parseInt(args[2]);
                                    } catch (NumberFormatException ex) {
                                        WirelessRedstone.getUtils().sendCommandFeedback("The interval must be a number!", sender, true);
                                        sign.getBlock().setType(Material.AIR);
                                        return true;
                                    }

                                    sign.setLine(2, WirelessRedstone.getStrings().tagsReceiverClockType.get(0));
                                    sign.setLine(3, Integer.toString(delay));
                                    sign.update();

                                    if (!WirelessRedstone.getSignManager().addWirelessReceiver(
                                            channelName, player.getLocation().getBlock(), player, WirelessReceiver.Type.CLOCK)) {
                                        sign.getBlock().setType(Material.AIR);
                                        return true;
                                    }
                                }
                                if (args.length < 3) {
                                    WirelessRedstone.getUtils().sendCommandFeedback(WirelessRedstone.getStrings().tooFewArguments, sender, true);
                                }
                                break;
                            default:
                                if (!WirelessRedstone.getSignManager().addWirelessReceiver(channelName, player.getLocation().getBlock(),
                                        player, WirelessReceiver.Type.DEFAULT)) {
                                    sign.getBlock().setType(Material.AIR);
                                    return true;
                                }
                                break;
                        }
                    } else {
                        if (!WirelessRedstone.getSignManager().addWirelessReceiver(channelName, player.getLocation().getBlock(),
                                player, WirelessReceiver.Type.DEFAULT)) {
                            sign.getBlock().setType(Material.AIR);
                            return true;
                        }
                    }
                } else if (cmd.getName().equalsIgnoreCase("wrs"))
                    sign.setLine(0, WirelessRedstone.getStrings().tagsScreen.get(0));

                org.bukkit.material.Sign dataSign = new org.bukkit.material.Sign();
                dataSign.setFacingDirection(BlockFace.SOUTH);
                sign.setData(dataSign);
                sign.update(true);

                if (cmd.getName().equalsIgnoreCase("wrt"))
                    WirelessRedstone.getSignManager().addWirelessTransmitter(channelName, player.getLocation().getBlock(), player);
                else if (cmd.getName().equalsIgnoreCase("wrs"))
                    WirelessRedstone.getSignManager().addWirelessScreen(channelName, player.getLocation().getBlock(), player);
            } else {
                WirelessRedstone.getUtils().sendCommandFeedback(WirelessRedstone.getStrings().playerDoesntHaveAccessToChannel, sender, true);
            }
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