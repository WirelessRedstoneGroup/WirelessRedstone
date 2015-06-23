package net.licks92.WirelessRedstone;

import net.licks92.WirelessRedstone.Channel.*;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WirelessCommands implements CommandExecutor {
    private final WirelessRedstone plugin;

    private boolean wipeDataConfirm = false, convertDataConfirm = false;

    public WirelessCommands(final WirelessRedstone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
                             final String commandLabel, final String[] args) {
        String commandName = command.getName().toLowerCase();

        //Commands for players, console, commandblocks

        switch (commandName) {
            case "wirelessredstone":
                return performWR(sender, args);

            case "wrlock":
                return performLockChannel(sender, args);

            case "wractivate":
                return performActivateChannel(sender, args);
        }

        //Commands allowed for players and console

        if(!(sender instanceof Player || sender instanceof ConsoleCommandSender)) {
            WirelessRedstone.getWRLogger().info("A command block tried to use a command he is not allowed to. " +
                    "Commands blocks are only allowed to use /wrlock and /wractivate");
            return true;
        }

        switch (commandName) {
            case "wrhelp":
                return performHelp(sender, args);

            case "wri":
                return performShowInfo(sender, args);

            case "wra":
                return performChannelAdmin(sender, args);

            case "wrremove":
                return performRemoveChannel(sender, args);

            case "wrlist":
                return performWRlist(sender, args);

            case "wrversion":
                return performWRVersion(sender, args);
        }

        //Commands for players only

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only in-game players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        switch (commandName) {
            case "wrt":
                return performCreateTransmitter(player, args);

            case "wrr":
                return performCreateReceiver(player, args);

            case "wrs":
                return performCreateScreen(player, args);

            case "wrtp":
                return performTeleport(player, args);
        }
        return true;
    }

    public ArrayList<String> generateCommandList(final CommandSender sender) {
        ArrayList<String> commands = new ArrayList<>();

        if ((sender instanceof Player) ? plugin.permissions.canCreateTransmitter((Player)sender) : false) {
            commands.add("/wr transmitter <channelname> - Creates transmitter sign.");
        }

        if ((sender instanceof Player) ? plugin.permissions.canCreateReceiver((Player)sender) : false) {
            commands.add("/wr receiver <channelname> - Creates receiver sign.");
        }

        if ((sender instanceof Player) ? plugin.permissions.canCreateScreen((Player)sender) : false) {
            commands.add("/wr screen <channelname> - Creates screen sign.");
        }

        if ((sender instanceof Player) ? plugin.permissions.canRemoveChannel((Player)sender) : true) {
            commands.add("/wr remove <channel> - Removes a channel.");
        }

        if ((sender instanceof Player) ? plugin.permissions.isWirelessAdmin((Player)sender) : true) {
            commands.add("/wr admin - Channel admin commands. Execute for more info.");
        }

        if ((sender instanceof Player) ? plugin.permissions.canUseListCommand((Player)sender) : true) {
            commands.add("/wr list [page] - Lists all the channels with the owners.");
        }

        if ((sender instanceof Player) ? plugin.permissions.canSeeChannelInfo((Player)sender) : true) {
            commands.add("/wr info <channel> <signCategory> - Get some informations about a channel. TIP: if you specify a signcategory you can click on the text to teleport to te sign");
        }

        if ((sender instanceof Player) ? plugin.permissions.canLockChannel((Player)sender) : true) {
            commands.add("/wr lock/unlock <channel> - Locks/Unlocks a channel.");
        }

        if ((sender instanceof Player) ? plugin.permissions.canActivateChannel((Player)sender) : true) {
            commands.add("/wr activate <channel> <time> - Turns on a channel for a given time in ms.");
        }

        if ((sender instanceof Player) ? plugin.permissions.canSeeHelp((Player)sender) : true) {
            commands.add("/wr help [page] - Shows a list of commands you can use.");
        }

        if ((sender instanceof Player) ? plugin.permissions.canTeleportToSign((Player)sender) : false) {
            commands.add("/wr tp <channel> <signCategory> <index> - Teleport to a sign.");
        }

        return commands;
    }

    private boolean performWR(final CommandSender sender,
                              final String[] r_args) {
        // If a command is sent after the /wr, perform it. Else, perform help.
        if (r_args.length >= 1) {
            String commandName = r_args[0];
            List<String> temp = new ArrayList<>();
            temp.addAll(Arrays.asList(r_args).subList(1, r_args.length));
            String[] args = temp.toArray(new String[temp.size()]);

            //Commands for console, players, commandblocks

            switch (commandName) {
                case "lock":
                case "unlock": {
                    return performLockChannel(sender, args);
                }
                case "activate":
                case "toggle": {
                    return performActivateChannel(sender, args);
                }
            }

            //Commands for console and players only

            if(!(sender instanceof Player || sender instanceof ConsoleCommandSender)) {
                WirelessRedstone.getWRLogger().info("A command block tried to use a command he is not allowed to. " +
                        "Commands blocks are only allowed to use /wrlock and /wractivate");
                return true;
            }

            switch (commandName) {
                case "help":
                    return performHelp(sender, args);
                case "admin":
                case "a": {
                    return performChannelAdmin(sender, args);
                }
                case "remove":
                case "delete": {
                    return performRemoveChannel(sender, args);
                }
                case "list":
                    return performWRlist(sender, args);
                case "info":
                    return performShowInfo(sender, args);
                case "version":
                    return performWRVersion(sender, args);
            }

            //Commands for players only

            if (!(sender instanceof Player)) {
                sender.sendMessage("Only in-game players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            switch (commandName) {
                case "transmitter":
                case "t": {
                    return performCreateTransmitter(player, args);
                }
                case "receiver":
                case "r": {
                    return performCreateReceiver(player, args);
                }
                case "screen":
                case "s": {
                    return performCreateScreen(player, args);
                }
                case "tp":
                    return performTeleport(player, args);
                default:
                    sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + WirelessRedstone.strings.commandDoesNotExist);
                    return true;
            }
        } else if (!(sender instanceof BlockCommandSender)) {
            return performHelp(sender, r_args);
        } else {
            return true;
        }
    }

    private boolean performConvert(CommandSender sender, String[] args) {
        if ((sender instanceof Player) ? (!plugin.permissions.canConvertData((Player) sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }

        if(args.length < 2){
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.tooFewArguments);
            return true;
        }

        if (!convertDataConfirm) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD
                    + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.convertContinue.replaceAll("%%STORAGETYPE", args[1]));
            convertDataConfirm = true;
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    convertDataConfirm = false;
                }
            }, 20 * 15);
            return true;
        }
        convertDataConfirm = false;

        switch(WirelessRedstone.config.changeStorage(args[1])){
            case 0:
                sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.convertFailed);
                break;
            case 1:
                sender.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.convertDone);
                break;
            case 2:
                sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag +
                        WirelessRedstone.strings.convertSameType);
                break;
        }
        return true;
    }

    private boolean performLockChannel(final CommandSender sender,
                                       final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.tooFewArguments);
        }
        if ((sender instanceof Player) ? (!plugin.permissions.canLockChannel((Player)sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        if (args.length >= 1) {
            WirelessChannel channel = WirelessRedstone.config
                    .getWirelessChannel(args[0]);
            if (channel == null) {
                sender.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.channelDoesNotExist);
                return true;
            }
            if (channel.isLocked()) {
                channel.setLocked(false);
                WirelessRedstone.config.updateChannel(args[0], channel);
                sender.sendMessage(ChatColor.GREEN
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.channelUnlocked);
                return true;
            } else {
                channel.setLocked(true);
                WirelessRedstone.config.updateChannel(args[0], channel);
                sender.sendMessage(ChatColor.GREEN
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.channelLocked);
                return true;
            }
        }
        return false;
    }

    private boolean performWRlist(final CommandSender sender,
                                  final String[] args) {
        if ((sender instanceof Player) ? (!plugin.permissions.canUseListCommand((Player)sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        ArrayList<String> list = new ArrayList<String>();
        try {
            for (WirelessChannel channel : WirelessRedstone.config
                    .getAllChannels()) {
                // Show Name of each channel and his activity
                if (channel != null) {
                    String item = channel.getName() + " : ";
                    if (channel.isActive())
                        item += ChatColor.GREEN + "ACTIVE";
                    else
                        item += ChatColor.RED + "INACTIVE";
                    list.add(item);
                }
            }
        } catch (NullPointerException ex) {
            WirelessRedstone.getWRLogger().severe(
                    "Unable to get the list of channels ! Stack trace ==>");
            ex.printStackTrace();
        }

        if (args.length >= 1) {
            int pagenumber;
            try {
                pagenumber = Integer.parseInt(args[0]);
            } catch (Exception e) {
                sender.sendMessage("This page number is not a number!");
                return true;
            }
            sender.sendMessage(ChatColor.AQUA
                    + "WirelessRedstone Channel List("
                    + WirelessRedstone.config.getAllChannels().size()
                    + " channel(s) )");
            showList(list, pagenumber, sender);
            sender.sendMessage(WirelessRedstone.strings.forMoreInfosPerformWRInfo);
            sender.sendMessage(WirelessRedstone.strings.commandForNextPage);
            return true;
        } else if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "Channel List ("
                    + WirelessRedstone.config.getAllChannels().size()
                    + " channel(s))");
            showList(list, 1, sender);
            sender.sendMessage(WirelessRedstone.strings.forMoreInfosPerformWRInfo);
            sender.sendMessage(WirelessRedstone.strings.commandForNextPage);
            return true;
        } else {
            return false;
        }
    }

    private boolean performChannelAdmin(final CommandSender sender,
                                        final String[] args) {
        if ((sender instanceof Player) ? (!plugin.permissions.isWirelessAdmin((Player)sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        if (args.length > 0) {
            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("addowner") && args.length > 2) {
                String channelName = args[1];
                String playername = args[2];
                if (hasAccessToChannel(sender,
                        channelName)) {
                    WirelessChannel channel = WirelessRedstone.config
                            .getWirelessChannel(channelName);
                    channel.addOwner(playername);
                    WirelessRedstone.config.updateChannel(channelName, channel);
                    WirelessRedstone
                            .getWRLogger()
                            .info(playername
                                    + " has been added to the list of owners of "
                                    + channelName);
                    sender.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag + playername
                            + " has been added to the list of owners of "
                            + channelName);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED
                            + WirelessRedstone.strings.chatTag
                            + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
                }
            } else if (subCommand.equalsIgnoreCase("removeowner")
                    && args.length > 2) {
                String channelName = args[1];
                String playername = args[2];
                if (hasAccessToChannel(sender,
                        channelName)) {
                    WirelessChannel channel = WirelessRedstone.config
                            .getWirelessChannel(channelName);
                    channel.removeOwner(playername);
                    WirelessRedstone.config.updateChannel(channelName, channel);
                    WirelessRedstone
                            .getWRLogger()
                            .info(playername
                                    + " has been removed from the list of owners of "
                                    + channelName);
                    sender.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag + playername
                            + " has been removed from the list of owners of "
                            + channelName);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED
                            + WirelessRedstone.strings.chatTag
                            + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
                }
            } else if (subCommand.equalsIgnoreCase("wipedata")) {
                return performWipeData(sender, args);
            } else if (subCommand.equalsIgnoreCase("backup")) {
                return performBackupData(sender, args);
            } else if (subCommand.equalsIgnoreCase("purge")) {
                return performPurgeData(sender, args);
            } else if (subCommand.equalsIgnoreCase("convert")) {
                return performConvert(sender, args);
            } else {
                sender.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.subCommandDoesNotExist);
            }
        } else {
            sender.sendMessage("Channel Admin Commands:");
            sender.sendMessage("/wr admin addowner channelname playername - Add a player to channel.");
            sender.sendMessage("/wr admin removeowner channelname playername - Add a player to channel.");
            sender.sendMessage("/wr admin purge - Removes channels with nothing inside it and removes signs when the world doesn't exist");
            sender.sendMessage("/wr admin wipedata - Erase the database! Don't do it if you don't know what you're doing!");
            sender.sendMessage("/wr admin backup - Backup the database. You should use it before to update in order to recover it if an error occurs.");
        }
        return true;
    }

    private boolean performHelp(final CommandSender sender,
                                final String[] args) {
        if ((sender instanceof Player) ? (!plugin.permissions.canSeeHelp((Player)sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
            return true;
        }

        ArrayList<String> commands = generateCommandList(sender);

        if (args.length >= 1) {
            int pagenumber;
            try {
                pagenumber = Integer.parseInt(args[0]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "This page number is not an number!");
                return true;
            }
            sender.sendMessage(ChatColor.AQUA
                    + "WirelessRedstone User Commands:");
            showList(commands, pagenumber, sender);
            return true;
        } else {
            sender.sendMessage(ChatColor.AQUA
                    + "WirelessRedstone User Commands:");
            showList(commands, 1, sender);
            sender.sendMessage(ChatColor.BOLD + "/wr help 2 for next page!");
        }
        return true;
    }

    private boolean performCreateTransmitter(final Player player,
                                             final String[] args) {
        if (plugin.permissions.canCreateTransmitter(player)) {
            if (args.length >= 1) {
                String channelname = args[0];
                if (WirelessRedstone.WireBox.hasAccessToChannel(player,
                        channelname)) {
                    player.getLocation().getBlock();
                    player.getLocation().getBlock().setType(Material.SIGN_POST);
                    Sign sign = (Sign) player.getLocation().getBlock()
                            .getState();
                    sign.setLine(0,
                            WirelessRedstone.strings.tagsTransmitter.get(0));
                    sign.setLine(1, channelname);
                    org.bukkit.material.Sign dataSign = new org.bukkit.material.Sign();
                    dataSign.setFacingDirection(getPlayerDirection(player)
                            .getOppositeFace());
                    sign.setData(dataSign);
                    sign.update(true);
                    WirelessRedstone.WireBox.addWirelessTransmitter(
                            channelname, player.getLocation().getBlock(),
                            player);
                } else {
                    player.sendMessage(ChatColor.RED
                            + WirelessRedstone.strings.chatTag
                            + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
                }
            } else if (args.length == 0) {
                player.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.tooFewArguments);
                return true;
            }
        } else {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        return true;
    }

    private boolean performCreateReceiver(final Player player,
                                          final String[] args) {
        if (plugin.permissions.canCreateReceiver(player)) {
            if (args.length == 1) {
                String channelname = args[0];
                if (WirelessRedstone.WireBox.hasAccessToChannel(player,
                        channelname)) {
                    player.getLocation().getBlock().setType(Material.SIGN_POST);
                    Sign sign = (Sign) player.getLocation().getBlock()
                            .getState();
                    sign.setLine(0,
                            WirelessRedstone.strings.tagsReceiver.get(0));
                    sign.setLine(1, channelname);
                    org.bukkit.material.Sign dataSign = new org.bukkit.material.Sign();
                    dataSign.setFacingDirection(getPlayerDirection(player)
                            .getOppositeFace());
                    sign.setData(dataSign);
                    sign.update(true);
                    if (!WirelessRedstone.WireBox.addWirelessReceiver(
                            channelname, player.getLocation().getBlock(),
                            player, Type.Default)) {
                        sign.getBlock().breakNaturally();
                    }
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED
                            + WirelessRedstone.strings.chatTag
                            + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
                }
            }
            if (args.length >= 2) {
                String channelName = args[0];
                String type = args[1];

                if (WirelessRedstone.WireBox.hasAccessToChannel(player,
                        channelName)) {
                    player.getLocation().getBlock().setType(Material.SIGN_POST);
                    Sign sign = (Sign) player.getLocation().getBlock()
                            .getState();
                    sign.setLine(0,
                            WirelessRedstone.strings.tagsReceiver.get(0));
                    sign.setLine(1, channelName);

                    org.bukkit.material.Sign dataSign = new org.bukkit.material.Sign();
                    dataSign.setFacingDirection(getPlayerDirection(player)
                            .getOppositeFace());
                    sign.setData(dataSign);
                    sign.update(true);
                    switch (type) {
                        case "inverter":
                        case "inv": {
                            sign.setLine(
                                    2,
                                    WirelessRedstone.strings.tagsReceiverInverterType
                                            .get(0));
                            sign.update();
                            if (!WirelessRedstone.WireBox.addWirelessReceiver(
                                    channelName, player.getLocation().getBlock(),
                                    player, Type.Inverter)) {
                                sign.getBlock().breakNaturally();
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
                                    player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "The delay must be a number!");
                                    delay = 1000;
                                }
                                sign.setLine(
                                        2,
                                        WirelessRedstone.strings.tagsReceiverDelayerType
                                                .get(0));
                                sign.setLine(3, Integer.toString(delay));
                                sign.update();
                                if (!WirelessRedstone.WireBox.addWirelessReceiver(
                                        channelName, player.getLocation()
                                                .getBlock(), player, Type.Delayer)) {
                                    sign.getBlock().breakNaturally();
                                }
                            }
                            if (args.length < 3) {
                                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "Delayer created with the default value of 1000 ms!");
                                sign.setLine(3, Integer.toString(1000));
                                if (!WirelessRedstone.WireBox.addWirelessReceiver(
                                        channelName, player.getLocation()
                                                .getBlock(), player, Type.Delayer)) {
                                    sign.getBlock().breakNaturally();
                                }
                            }
                            break;
                        }
                        case "clock":
                            if (args.length >= 3) {
                                int delay;
                                try {
                                    delay = Integer.parseInt(args[2]);
                                } catch (NumberFormatException ex) {
                                    player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "The delay must be a number!");
                                    sign.getBlock().breakNaturally();
                                    break;
                                }
                                sign.setLine(
                                        2,
                                        WirelessRedstone.strings.tagsReceiverClockType
                                                .get(0));
                                sign.setLine(3, Integer.toString(delay));
                                sign.update();
                                if (!WirelessRedstone.WireBox.addWirelessReceiver(
                                        channelName, player.getLocation()
                                                .getBlock(), player, Type.Delayer)) {
                                    sign.getBlock().breakNaturally();
                                }
                            }
                            if (args.length < 3) {
                                player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "Delayer created with the default value of 1000 ms!");
                                sign.setLine(3, Integer.toString(1000));
                                if (!WirelessRedstone.WireBox.addWirelessReceiver(
                                        channelName, player.getLocation()
                                                .getBlock(), player, Type.Clock)) {
                                    sign.getBlock().breakNaturally();
                                }
                            }
                            break;
                        default:
                            if (!WirelessRedstone.WireBox.addWirelessReceiver(
                                    channelName, player.getLocation().getBlock(),
                                    player, Type.Default)) {
                                sign.getBlock().breakNaturally();
                            }
                            break;
                    }
                    return true;
                }
            } else if (args.length == 0) {
                player.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.tooFewArguments);
                return true;
            }
        } else {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        return true;
    }

    private boolean performCreateScreen(final Player player,
                                        final String[] args) {
        if (plugin.permissions.canCreateScreen(player)) {
            if (args.length >= 1) {
                String channelname = args[0];
                if (WirelessRedstone.WireBox.hasAccessToChannel(player,
                        channelname)) {
                    player.getLocation().getBlock();
                    player.getLocation().getBlock().setType(Material.SIGN_POST);
                    Sign sign = (Sign) player.getLocation().getBlock()
                            .getState();
                    sign.setLine(0, WirelessRedstone.strings.tagsScreen.get(0));
                    sign.setLine(1, channelname);
                    org.bukkit.material.Sign dataSign = new org.bukkit.material.Sign();
                    dataSign.setFacingDirection(getPlayerDirection(player)
                            .getOppositeFace());
                    sign.setData(dataSign);
                    sign.update(true);
                    WirelessRedstone.WireBox.addWirelessScreen(channelname,
                            player.getLocation().getBlock(), player);
                } else {
                    player.sendMessage(ChatColor.RED
                            + WirelessRedstone.strings.chatTag
                            + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
                }
            } else if (args.length == 0) {
                player.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.tooFewArguments);
                return true;
            }
        } else {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        return true;
    }

    private boolean performRemoveChannel(final CommandSender sender,
                                         final String[] args) {
        if ((sender instanceof Player) ? (!plugin.permissions.canRemoveChannel((Player)sender)) : false) {
            if (args.length >= 1) {
                if (hasAccessToChannel(sender, args[0])) {
                    WirelessRedstone.config.removeWirelessChannel(args[0]);
                    sender.sendMessage(ChatColor.GREEN
                            + WirelessRedstone.strings.chatTag
                            + WirelessRedstone.strings.channelRemoved);
                } else {
                    sender.sendMessage(ChatColor.RED
                            + WirelessRedstone.strings.chatTag
                            + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
                }
            } else if (args.length == 0) {
                sender.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.tooFewArguments);
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        return true;
    }

    private boolean performWipeData(final CommandSender sender,
                                    final String[] args) {
        /*
         * To-do list: - Make a backup before. - Remove all the signs of every
		 * channel.
		 */
        if ((sender instanceof Player) ? (!plugin.permissions.canWipeData((Player) sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        if (!wipeDataConfirm) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD
                    + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.DBAboutToBeDeleted);
            wipeDataConfirm = true;
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    wipeDataConfirm = false;
                }
            }, 20 * 15);
            return true;
        }
        wipeDataConfirm = false;
        if (WirelessRedstone.config.wipeData()) {
            sender.sendMessage(ChatColor.GREEN
                    + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.DBDeleted);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.DBNotDeleted);
            return true;
        }
    }

    private boolean performBackupData(final CommandSender sender,
                                      final String[] args) {
        if ((sender instanceof Player) ? (!plugin.permissions.canBackupData((Player) sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        if (WirelessRedstone.config.backupData()) {
            sender.sendMessage(ChatColor.GREEN
                    + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.backupDone);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.backupFailed);
            return true;
        }
    }

    private boolean performShowInfo(final CommandSender sender,
                                    final String[] args) {
		/*
		 * This method shows the status of a WirelessChannel. At the moment, it
		 * shows : - if the channel is active or not. - how many signs of each
		 * kind is in each channel.
		 */
        if ((sender instanceof Player) ? (!plugin.permissions.canSeeChannelInfo((Player) sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.tooFewArguments);
            return true;
        }
		/*
		 * If there's only 1 argument, it should be the channel name. In this
		 * case it will show : If the channel is active. The numbers of signs of
		 * each category in the channel. The names of the owners of the channel.
		 */
        if (args.length == 1) {
            WirelessChannel channel = WirelessRedstone.config
                    .getWirelessChannel(args[0]);
            if (channel == null) {
                sender.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.channelDoesNotExist);
                return true;
            }
            if (!hasAccessToChannel(sender, args[0])) {
                sender.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
                return true;
            }
            sender.sendMessage(ChatColor.BLUE + "----" + ChatColor.GREEN
                    + " Status of " + ChatColor.GRAY + channel.getName()
                    + ChatColor.BLUE + " ----");
			/*
			 * Checking for active transmitters
			 */
            sender.sendMessage(ChatColor.GRAY
                    + "Is activated: "
                    + ((channel.isActive())
                    ? ChatColor.GREEN + "Yes"
                    : ChatColor.RED + "No"));
            sender.sendMessage(ChatColor.GRAY
                    + "Is locked: "
                    + ((channel.isLocked())
                    ? ChatColor.GREEN + "Yes"
                    : ChatColor.RED + "No"));
			/*
			 * Counting signs of the channel.
			 */
            sender.sendMessage(ChatColor.BLUE
                    + WirelessRedstone.strings.thisChannelContains);

            sender.sendMessage(" " + ChatColor.GRAY
                    + channel.getReceivers().size() + ChatColor.GREEN
                    + " receivers, " + ChatColor.GRAY
                    + channel.getTransmitters().size() + ChatColor.GREEN
                    + " transmitters, " + ChatColor.GRAY
                    + channel.getScreens().size() + ChatColor.GREEN
                    + " screens");

			/*
			 * Showing the owners
			 */

            sender.sendMessage(ChatColor.BLUE
                    + WirelessRedstone.strings.ownersOfTheChannelAre);
            for (String owner : channel.getOwners()) {
                sender.sendMessage(" - " + owner);
            }

            return true;
        }

		/*
		 * Will be able to show more informations. The arguments will be : 'r'
		 * or 'receivers'. In this case it will show how many receivers are in
		 * the channel, and the position of each of them. 't' or 'transmitters'.
		 * In this case it will show how many transmitters are in the channel,
		 * and which ones are toggled. 's' or 'screens'.
		 *
		 * This allows also to teleport to a sign, that's why the advanced
		 * information is not available to the console and commandblocks.
		 */

        if (args.length == 2 && (sender instanceof Player)) {
            return performAdvancedShowInfo(args, (Player)sender, 1);
        }
        if (args.length == 3 && (sender instanceof Player)) {
            int page;
            try {
                page = Integer.parseInt(args[2]);
            } catch (Exception ex) {
                WirelessRedstone.getWRLogger().debug(
                        "Could not parse Integer while executing command ShowInfo. Third argument is "
                                + args[2]);
                sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "This page number is not a number!");
                return false;
            }
            if (page > 0)
                return performAdvancedShowInfo(args, (Player)sender, page);
            else {
                sender.sendMessage(ChatColor.RED
                        + WirelessRedstone.strings.chatTag
                        + WirelessRedstone.strings.pageNumberInferiorToZero);
                return false;
            }
        }
        return false;
    }

    private boolean performAdvancedShowInfo(final String[] args,
                                            final Player player, final int page) {
        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(args[0]);
        if (channel == null) {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.channelDoesNotExist);
            return true;
        }
        if (!WirelessRedstone.WireBox.hasAccessToChannel(player, args[0])) {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
            return true;
        }

        String category = args[1];

        ArrayList<String> lines = new ArrayList<String>();

        switch (category) {
            case "receivers":
            case "receiver":
            case "r": {
                // Let's show informations about receivers.
                int receiverCounter = -1;
                for (WirelessReceiver receiver : channel.getReceivers()) {
                    receiverCounter++;
                    String type = "receiver";
                    if (receiver instanceof WirelessReceiverDelayer) {
                        type = "delayer";
                    } else if (receiver instanceof WirelessReceiverInverter) {
                        type = "inverter";
                    } else if (receiver instanceof WirelessReceiverClock) {
                        type = "clock";
                    }
                    lines.add(WirelessRedstone.strings.tellRawString
                            .replaceAll("%%TEXT",
                                    "Click here to teleport to %%NAME!")
                            .replaceAll("%%NAME", channel.getName())
                            .replaceAll("%%TYPE", type)
                            .replaceAll(
                                    "%%WORLD",
                                    receiver.getLocation().getWorld().getName()
                                            + "")
                            .replaceAll("%%XCOORD",
                                    receiver.getLocation().getBlockX() + "")
                            .replaceAll("%%YCOORD",
                                    receiver.getLocation().getBlockY() + "")
                            .replaceAll("%%ZCOORD",
                                    receiver.getLocation().getBlockZ() + "")
                            .replaceAll(
                                    "%%COMMAND",
                                    "/wrtp " + args[0] + " receiver "
                                            + receiverCounter));
                }
                if (receiverCounter < 0) {
                    lines.add(ChatColor.RED
                            + "[WirelessRedstone] No signs found!");
                }
                break;
            }
            case "transmitters":
            case "transmitter":
            case "t": {
                // Let's show informations about transmitters.
                int transmitterCounter = -1;
                for (WirelessTransmitter transmitter : channel
                        .getTransmitters()) {
                    transmitterCounter++;
                    lines.add(WirelessRedstone.strings.tellRawString
                            .replaceAll("%%TEXT",
                                    "Click here to teleport to %%NAME!")
                            .replaceAll("%%NAME", channel.getName())
                            .replaceAll("%%TYPE", "transmitter")
                            .replaceAll(
                                    "%%WORLD",
                                    transmitter.getLocation().getWorld()
                                            .getName()
                                            + "")
                            .replaceAll("%%XCOORD",
                                    transmitter.getLocation().getBlockX() + "")
                            .replaceAll("%%YCOORD",
                                    transmitter.getLocation().getBlockY() + "")
                            .replaceAll("%%ZCOORD",
                                    transmitter.getLocation().getBlockZ() + "")
                            .replaceAll(
                                    "%%COMMAND",
                                    "/wrtp " + args[0] + " transmitter "
                                            + transmitterCounter));
                }
                if (transmitterCounter < 0) {
                    lines.add(ChatColor.RED
                            + "[WirelessRedstone] No signs found!");
                }
                break;
            }
            case "screens":
            case "screen":
            case "s": {
                // Let's show infos about screens.
                int screenCounter = -1;
                for (WirelessScreen screen : channel.getScreens()) {
                    screenCounter++;
                    lines.add(WirelessRedstone.strings.tellRawString
                            .replaceAll("%%TEXT",
                                    "Click here to teleport to %%NAME!")
                            .replaceAll("%%NAME", channel.getName())
                            .replaceAll("%%TYPE", "screen")
                            .replaceAll(
                                    "%%WORLD",
                                    screen.getLocation().getWorld().getName()
                                            + "")
                            .replaceAll("%%XCOORD",
                                    screen.getLocation().getBlockX() + "")
                            .replaceAll("%%YCOORD",
                                    screen.getLocation().getBlockY() + "")
                            .replaceAll("%%ZCOORD",
                                    screen.getLocation().getBlockZ() + "")
                            .replaceAll(
                                    "%%COMMAND",
                                    "/wrtp " + args[0] + " screen "
                                            + screenCounter));
                }
                if (screenCounter < 0) {
                    lines.add(ChatColor.RED
                            + "[WirelessRedstone] No signs found!");
                }
                break;
            }
        }
        showTellRawList(lines, page, player);

        return true;
    }

    private boolean performActivateChannel(final CommandSender sender,
                                           final String[] args) {
        if (!(args.length > 1)) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.tooFewArguments);
            return true;
        }
        if ((sender instanceof Player) ? (!plugin.permissions.canActivateChannel((Player) sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }

        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(args[0]);
        if (channel == null) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.channelDoesNotExist);
            return true;
        }
        if (!hasAccessToChannel(sender,
                channel.getName())) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
            return true;
        }

        int time;
        try {
            time = Integer.parseInt(args[1]);
        } catch (Exception ex) {
            return false;
        }
        channel.turnOn(time);
        return true;
    }

    private boolean performWRVersion(final CommandSender sender,
                                     final String[] args) {
        if ((sender instanceof Player) ? (!plugin.permissions.canSeeVersion((Player) sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }
        sender.sendMessage("You are currently running WirelessRedstone "
                + ChatColor.UNDERLINE + plugin.getDescription().getVersion()
                + ChatColor.RESET + " .");
        sender.sendMessage("The plugin will warn you if a newer version is released.");
        return true;
    }

    private boolean performTeleport(final Player player,
                                      final String[] args) {
        if (!(args.length > 2)) {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.tooFewArguments);
            return true;
        }
        if (!plugin.permissions.canTeleportToSign(player)) {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }

        WirelessChannel channel = WirelessRedstone.config
                .getWirelessChannel(args[0]);
        if (channel == null) {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.channelDoesNotExist);
            return true;
        }
        if (!WirelessRedstone.WireBox.hasAccessToChannel(player,
                channel.getName())) {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
            return true;
        }

        int index = 0;

        try {
            index = Integer.parseInt(args[2]);
        } catch (SecurityException ex) {
            player.sendMessage(ChatColor.RED
                    + "[WirelessRedstone] Third argument must be a number!");
            return true;
        }

        String type = args[1];
        switch (type) {
            case "receiver":
            case "r": {
                Location locReceiver = channel.getReceivers().get(index)
                        .getLocation().add(0.5, 0, 0.5);
                player.teleport(locReceiver);
                return true;
            }
            case "transmitter":
            case "t": {
                Location locTransmitter = channel.getReceivers().get(index)
                        .getLocation().add(0.5, 0, 0.5);
                player.teleport(locTransmitter);
                return true;
            }
            case "screen":
            case "s": {
                Location locScreen = channel.getReceivers().get(index)
                        .getLocation().add(0.5, 0, 0.5);
                player.teleport(locScreen);
                return true;
            }
        }
        return true;
    }

    public boolean performPurgeData(final CommandSender sender,
                                    final String[] args) {
        if ((sender instanceof Player) ? (!plugin.permissions.canPurgeData((Player) sender)) : false) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.playerDoesntHavePermission);
            return true;
        }

        if (WirelessRedstone.config.purgeData()) {
            sender.sendMessage(ChatColor.GREEN
                    + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.purgeDataDone);
        } else {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.purgeDataFailed);
        }
        return true;
    }

    /**
     * This method will show a list of lines to a player. It adds ' - ' to each
     * line.
     *
     * @param list   - A list of lines that will be showed.
     * @param cpage  - The current page, means that if there are several pages
     *               because the list is too long, it will this page (currently 5
     *               lines per page). Cannot be inferior to 1.
     * @param sender - The sender who will be showed the list.
     */
    public void showList(final ArrayList<String> list, final int cpage,
                         final CommandSender sender) {
		/*
		 * Show a page from list of Strings Where maxitems is the maximum items
		 * on each page
		 */

        int itemsonlist = list.size();
        int maxitems = 8;
        int currentpage = cpage;
        int totalpages = 1;
        if (currentpage < 1) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.pageNumberInferiorToZero);
        }
        for (int i = 0; i < itemsonlist / maxitems; i++)
            totalpages++;
        if (currentpage > totalpages) {
            sender.sendMessage(ChatColor.RED
                    + "[WirelessRedstone] There only are " + totalpages
                    + " pages.");
            return;
        }
        if (itemsonlist == 0) {
            sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.pageEmpty);
            return;
        }
        int currentitem = ((currentpage * maxitems) - maxitems);
        // 2*3 = 6 ; 6 - 3 = 3
        sender.sendMessage(ChatColor.UNDERLINE + "Page " + currentpage + " of "
                + totalpages);
        if (totalpages == 0) {
            sender.sendMessage(WirelessRedstone.strings.listEmpty);
        } else {
            for (int i = currentitem; i < (currentitem + maxitems); i++) {
                if (!(i >= itemsonlist)) {
                    sender.sendMessage("    - " + list.get(i));
                }
            }
        }

    }

    public void showTellRawList(final ArrayList<String> list, final int cpage,
                                final Player player) {
		/*
		 * Show a page from list of Strings Where maxitems is the maximum items
		 * on each page
		 */

        int itemsonlist = list.size();
        int maxitems = 8;
        int currentpage = cpage;
        int totalpages = 1;
        if (currentpage < 1) {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.pageNumberInferiorToZero);
        }
        for (int i = 0; i < itemsonlist / maxitems; i++)
            totalpages++;
        if (currentpage > totalpages) {
            player.sendMessage(ChatColor.RED
                    + "[WirelessRedstone] There only are " + totalpages
                    + " pages.");
            return;
        }
        if (itemsonlist == 0) {
            player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
                    + WirelessRedstone.strings.pageEmpty);
            return;
        }
        int currentitem = ((currentpage * maxitems) - maxitems);
        // 2*3 = 6 ; 6 - 3 = 3
        player.sendMessage(ChatColor.BLUE + "Page " + ChatColor.GRAY
                + currentpage + ChatColor.BLUE + " of " + ChatColor.GRAY
                + totalpages);
        if (totalpages == 0) {
            player.sendMessage(WirelessRedstone.strings.listEmpty);
        } else {
            for (int i = currentitem; i < (currentitem + maxitems); i++) {
                if (!(i >= itemsonlist)) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            list.get(i)
                                    .replaceAll("%%PLAYER", player.getName()));
                }
            }
        }

    }

    public BlockFace getPlayerDirection(final Player player) {

        BlockFace dir = null;

        float y = player.getLocation().getYaw();

        if (y < 0) {
            y += 360;
        }

        y %= 360;

        int i = (int) ((y + 8) / 22.5);

        if (i == 0) {
            dir = BlockFace.WEST;
        } else if (i == 1) {
            dir = BlockFace.WEST_NORTH_WEST;
        } else if (i == 2) {
            dir = BlockFace.NORTH_WEST;
        } else if (i == 3) {
            dir = BlockFace.NORTH_NORTH_WEST;
        } else if (i == 4) {
            dir = BlockFace.NORTH;
        } else if (i == 5) {
            dir = BlockFace.NORTH_NORTH_EAST;
        } else if (i == 6) {
            dir = BlockFace.NORTH_EAST;
        } else if (i == 7) {
            dir = BlockFace.EAST_NORTH_EAST;
        } else if (i == 8) {
            dir = BlockFace.EAST;
        } else if (i == 9) {
            dir = BlockFace.EAST_SOUTH_EAST;
        } else if (i == 10) {
            dir = BlockFace.SOUTH_EAST;
        } else if (i == 11) {
            dir = BlockFace.SOUTH_SOUTH_EAST;
        } else if (i == 12) {
            dir = BlockFace.SOUTH;
        } else if (i == 13) {
            dir = BlockFace.SOUTH_SOUTH_WEST;
        } else if (i == 14) {
            dir = BlockFace.SOUTH_WEST;
        } else if (i == 15) {
            dir = BlockFace.WEST_SOUTH_WEST;
        } else {
            dir = BlockFace.WEST;
        }

        return dir;

    }

    public boolean hasAccessToChannel(CommandSender sender, String channelName) {
        if(sender instanceof Player) {
            return WirelessRedstone.WireBox.hasAccessToChannel((Player) sender, channelName);
        } else {
            return true; //If it's console or commandBlock, it has access to channel.
        }
    }
}


