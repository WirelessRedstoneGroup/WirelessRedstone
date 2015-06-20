package net.licks92.WirelessRedstone;

import java.util.ArrayList;
import java.util.List;

import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver.Type;
import net.licks92.WirelessRedstone.Channel.WirelessReceiverClock;
import net.licks92.WirelessRedstone.Channel.WirelessReceiverDelayer;
import net.licks92.WirelessRedstone.Channel.WirelessReceiverInverter;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WirelessCommands implements CommandExecutor {
	private final WirelessRedstone plugin;

	private boolean wipeDataConfirm = false;

	public WirelessCommands(final WirelessRedstone plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command,
			final String commandLabel, final String[] args) {
		String commandName = command.getName().toLowerCase();

		if (!(sender instanceof Player)) {
			WirelessRedstone.getWRLogger().info(
					"Only in-game players can use this command.");
			return true;
		}

		Player player = (Player) sender;
		switch (commandName) {
			case "wirelessredstone" :
				return performWR(sender, args, player);

			case "wrhelp" :
				return performHelp(sender, args, player);

			case "wrt" :
				return performCreateTransmitter(sender, args, player);

			case "wrr" :
				return performCreateReceiver(sender, args, player);

			case "wrs" :
				return performCreateScreen(sender, args, player);

			case "wri" :
				return performShowInfo(sender, args, player);

			case "wra" :
				return performChannelAdmin(sender, args, player);

			case "wrremove" :
				return performRemoveChannel(sender, args, player);

			case "wrlist" :
				return performWRlist(sender, args, player);

			case "wrlock" :
				return performLockChannel(sender, args, player);

			case "wractivate" :
				return performActivateChannel(sender, args, player);

			case "wrversion" :
				return performWRVersion(sender, args, player);

			case "wrtp" :
				return performWRTeleport(sender, args, player);
		}
		return true;
	}

	public ArrayList<String> generateCommandList(final Player player) {
		ArrayList<String> commands = new ArrayList<>();

		if (plugin.permissions.canCreateTransmitter(player)) {
			commands.add("/wr transmitter <channelname> - Creates transmitter sign.");
		}

		if (plugin.permissions.canCreateReceiver(player)) {
			commands.add("/wr receiver <channelname> - Creates receiver sign.");
		}

		if (plugin.permissions.canCreateScreen(player)) {
			commands.add("/wr screen <channelname> - Creates screen sign.");
		}

		if (plugin.permissions.canRemoveChannel(player)) {
			commands.add("/wr remove <channel> - Removes a channel.");
		}

		if (plugin.permissions.isWirelessAdmin(player)) {
			commands.add("/wr admin - Channel admin commands. Execute for more info.");
		}

		if (plugin.permissions.canUseListCommand(player)) {
			commands.add("/wr list [page] - Lists all the channels with the owners.");
		}

		if (plugin.permissions.canSeeChannelInfo(player)) {
			commands.add("/wr info <channel> <signCategory> - Get some informations about a channel. TIP: if you specify a signcategory you can click on the text to teleport to te sign");
		}

		if (plugin.permissions.canLockChannel(player)) {
			commands.add("/wr lock/unlock <channel> - Locks/Unlocks a channel.");
		}

		if (plugin.permissions.canActivateChannel(player)) {
			commands.add("/wr activate <channel> <time> - Turns on a channel for a given time in ms.");
		}

		if (plugin.permissions.canSeeHelp(player)) {
			commands.add("/wr help [page] - Shows a list of commands you can use.");
		}

		if (plugin.permissions.canTeleportToSign(player)) {
			commands.add("/wr tp <channel> <signCategory> <index> - Teleport to a sign.");
		}

		return commands;
	}

	private boolean performWR(final CommandSender sender,
			final String[] r_args, final Player player) {
		// If a command is sent after the /wr, perform it. Else, perform help.
		if (r_args.length >= 1) {
			String commandName = r_args[0];
			List<String> temp = new ArrayList<>();
			for (int i = 1; i < r_args.length; i++)
				temp.add(r_args[i]);
			String[] args = temp.toArray(new String[temp.size()]);
			switch (commandName) {
				case "help" :
					return performHelp(sender, args, player);
				case "transmitter" :
				case "t" : {
					return performCreateTransmitter(sender, args, player);
				}
				case "receiver" :
				case "r" : {
					return performCreateReceiver(sender, args, player);
				}
				case "screen" :
				case "s" : {
					return performCreateScreen(sender, args, player);
				}
				case "admin" :
				case "a" : {
					return performChannelAdmin(sender, args, player);
				}
				case "remove" :
				case "delete" : {
					return performRemoveChannel(sender, args, player);
				}
				case "list" :
					return performWRlist(sender, args, player);
				case "info" :
					return performShowInfo(sender, args, player);
				case "lock" :
				case "unlock" : {
					return performLockChannel(sender, args, player);
				}
				case "activate" :
				case "toggle" : {
					return performActivateChannel(sender, args, player);
				}
				case "version" :
					return performWRVersion(sender, args, player);
				case "tp" :
					return performWRTeleport(sender, args, player);
				default :
					player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + WirelessRedstone.strings.commandDoesNotExist);
					return true;
			}
		} else {
			return performHelp(sender, r_args, player);
		}
	}

	private boolean performLockChannel(final CommandSender sender,
			final String[] args, final Player player) {
		if (args.length == 0) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.tooFewArguments);
		}
		if (!plugin.permissions.canLockChannel(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.playerDoesntHavePermission);
			return true;
		}
		if (args.length >= 1) {
			WirelessChannel channel = WirelessRedstone.config
					.getWirelessChannel(args[0]);
			if (channel == null) {
				player.sendMessage(ChatColor.RED
						+ WirelessRedstone.strings.chatTag
						+ WirelessRedstone.strings.channelDoesNotExist);
				return true;
			}
			if (channel.isLocked()) {
				channel.setLocked(false);
				WirelessRedstone.config.updateChannel(args[0], channel);
				player.sendMessage(ChatColor.GREEN
						+ WirelessRedstone.strings.chatTag
						+ WirelessRedstone.strings.channelUnlocked);
				return true;
			} else {
				channel.setLocked(true);
				WirelessRedstone.config.updateChannel(args[0], channel);
				player.sendMessage(ChatColor.GREEN
						+ WirelessRedstone.strings.chatTag
						+ WirelessRedstone.strings.channelLocked);
				return true;
			}
		}
		return false;
	}

	private boolean performWRlist(final CommandSender sender,
			final String[] args, final Player player) {
		if (!plugin.permissions.canUseListCommand(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
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
				player.sendMessage("This page number is not a number!");
				return true;
			}
			player.sendMessage(ChatColor.AQUA
					+ "WirelessRedstone Channel List("
					+ WirelessRedstone.config.getAllChannels().size()
					+ " channel(s) )");
			showList(list, pagenumber, player);
			player.sendMessage(WirelessRedstone.strings.forMoreInfosPerformWRInfo);
			player.sendMessage(WirelessRedstone.strings.commandForNextPage);
			return true;
		} else if (args.length == 0) {
			player.sendMessage(ChatColor.AQUA + "Channel List ("
					+ WirelessRedstone.config.getAllChannels().size()
					+ " channel(s))");
			showList(list, 1, player);
			player.sendMessage(WirelessRedstone.strings.forMoreInfosPerformWRInfo);
			player.sendMessage(WirelessRedstone.strings.commandForNextPage);
			return true;
		} else {
			return false;
		}
	}

	private boolean performChannelAdmin(final CommandSender sender,
			final String[] args, final Player player) {
		if (!plugin.permissions.isWirelessAdmin(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.playerDoesntHavePermission);
			return true;
		}
		if (args.length > 0) {
			String subCommand = args[0];

			if (subCommand.equalsIgnoreCase("addowner") && args.length > 2) {
				String channelName = args[1];
				String playername = args[2];
				if (WirelessRedstone.WireBox.hasAccessToChannel(player,
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
					player.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag + playername
							+ " has been added to the list of owners of "
							+ channelName);
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ WirelessRedstone.strings.chatTag
							+ WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
				}
			} else if (subCommand.equalsIgnoreCase("removeowner")
					&& args.length > 2) {
				String channelName = args[1];
				String playername = args[2];
				if (WirelessRedstone.WireBox.hasAccessToChannel(player,
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
					player.sendMessage(ChatColor.GREEN + WirelessRedstone.strings.chatTag + playername
							+ " has been removed from the list of owners of "
							+ channelName);
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ WirelessRedstone.strings.chatTag
							+ WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
				}
			} else if (subCommand.equalsIgnoreCase("wipedata")) {
				return performWipeData(sender, args, player);
			} else if (subCommand.equalsIgnoreCase("backup")) {
				return performBackupData(sender, args, player);
			} else if (subCommand.equalsIgnoreCase("purge")) {
				return performPurgeData(sender, args, player);
			} else {
				player.sendMessage(ChatColor.RED
						+ WirelessRedstone.strings.chatTag
						+ WirelessRedstone.strings.subCommandDoesNotExist);
			}
		} else {
			player.sendMessage("Channel Admin Commands:");
			player.sendMessage("/wr admin addowner channelname playername - Add a player to channel.");
			player.sendMessage("/wr admin removeowner channelname playername - Add a player to channel.");
			player.sendMessage("/wr admin purge - Removes channels with nothing inside it and removes signs when the world doesn't exist");
			player.sendMessage("/wr admin wipedata - Erase the database! Don't do it if you don't know what you're doing!");
			player.sendMessage("/wr admin backup - Backup the database. You should use it before to update in order to recover it if an error occurs.");
		}
		return true;
	}

	private boolean performHelp(final CommandSender sender,
			final String[] args, final Player player) {
		if (!plugin.permissions.canSeeHelp(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
			return true;
		}

		ArrayList<String> commands = generateCommandList(player);

		if (args.length >= 1) {
			int pagenumber;
			try {
				pagenumber = Integer.parseInt(args[0]);
			} catch (Exception e) {
				player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "This page number is not an number!");
				return true;
			}
			player.sendMessage(ChatColor.AQUA
					+ "WirelessRedstone User Commands:");
			showList(commands, pagenumber, player);
			return true;
		} else {
			player.sendMessage(ChatColor.AQUA
					+ "WirelessRedstone User Commands:");
			showList(commands, 1, player);
			player.sendMessage(ChatColor.BOLD + "/wr help 2 for next page!");
		}
		return true;
	}

	private boolean performCreateTransmitter(final CommandSender sender,
			final String[] args, final Player player) {
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

	private boolean performCreateReceiver(final CommandSender sender,
			final String[] args, final Player player) {
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
					if (type.equals("inverter") || type.equals("inv")) {
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
					} else if (type.equals("delayer") || type.equals("delay")
							|| type.equals("del")) {
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
											.getBlock(), player, Type.Delayer))
								;
							{
								sign.getBlock().breakNaturally();
							}
						}
						if (args.length < 3) {
							player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "Delayer created with the default value of 1000 ms!");
							sign.setLine(3, Integer.toString(1000));
							if (!WirelessRedstone.WireBox.addWirelessReceiver(
									channelName, player.getLocation()
											.getBlock(), player, Type.Delayer))
								;
							{
								sign.getBlock().breakNaturally();
							}
						}
					} else {
						if (!WirelessRedstone.WireBox.addWirelessReceiver(
								channelName, player.getLocation().getBlock(),
								player, Type.Default)) {
							sign.getBlock().breakNaturally();
						}
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

	private boolean performCreateScreen(final CommandSender sender,
			final String[] args, final Player player) {
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
			final String[] args, final Player player) {
		if (plugin.permissions.canRemoveChannel(player)) {
			if (args.length >= 1) {
				if (WirelessRedstone.WireBox
						.hasAccessToChannel(player, args[0])) {
					WirelessRedstone.config.removeWirelessChannel(args[0]);
					player.sendMessage(ChatColor.GREEN
							+ WirelessRedstone.strings.chatTag
							+ WirelessRedstone.strings.channelRemoved);
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

	private boolean performWipeData(final CommandSender sender,
			final String[] args, final Player player) {
		/*
		 * To-do list: - Make a backup before. - Remove all the signs of every
		 * channel.
		 */
		if (!plugin.permissions.canWipeData(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.playerDoesntHavePermission);
			return true;
		}
		if (!wipeDataConfirm) {
			player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD
					+ WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.DBAboutToBeDeleted);
			wipeDataConfirm = true;
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					wipeDataConfirm = false;
				}
			}, 300L);
			return true;
		}
		wipeDataConfirm = false;
		if (WirelessRedstone.config.wipeData()) {
			player.sendMessage(ChatColor.GREEN
					+ WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.DBDeleted);
			return true;
		} else {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.DBNotDeleted);
			return true;
		}
	}

	private boolean performBackupData(final CommandSender sender,
			final String[] args, final Player player) {
		if (!plugin.permissions.canBackupData(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.playerDoesntHavePermission);
			return true;
		}
		if (WirelessRedstone.config.backupData()) {
			player.sendMessage(ChatColor.GREEN
					+ WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.backupDone);
			return true;
		} else {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.backupFailed);
			return true;
		}
	}

	private boolean performShowInfo(final CommandSender sender,
			final String[] args, final Player player) {
		/*
		 * This method shows the status of a WirelessChannel. At the moment, it
		 * shows : - if the channel is active or not. - how many signs of each
		 * kind is in each channel.
		 */
		if (!plugin.permissions.canSeeChannelInfo(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.playerDoesntHavePermission);
			return true;
		}
		if (args.length == 0) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
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
				player.sendMessage(ChatColor.RED
						+ WirelessRedstone.strings.chatTag
						+ WirelessRedstone.strings.channelDoesNotExist);
				return true;
			}
			if (!WirelessRedstone.WireBox.hasAccessToChannel(player, args[0])) {
				player.sendMessage(ChatColor.RED
						+ WirelessRedstone.strings.chatTag
						+ WirelessRedstone.strings.playerDoesntHaveAccessToChannel);
				return true;
			}
			player.sendMessage(ChatColor.BLUE + "----" + ChatColor.GREEN
					+ " Status of " + ChatColor.GRAY + channel.getName()
					+ ChatColor.BLUE + " ----");
			/*
			 * Checking for active transmitters
			 */
			player.sendMessage(ChatColor.GRAY
					+ "Is activated: "
					+ ((channel.isActive())
							? ChatColor.GREEN + "Yes"
							: ChatColor.RED + "No"));
			player.sendMessage(ChatColor.GRAY
					+ "Is locked: "
					+ ((channel.isLocked())
							? ChatColor.GREEN + "Yes"
							: ChatColor.RED + "No"));
			/*
			 * Counting signs of the channel.
			 */
			player.sendMessage(ChatColor.BLUE
					+ WirelessRedstone.strings.thisChannelContains);

			player.sendMessage(" " + ChatColor.GRAY
					+ channel.getReceivers().size() + ChatColor.GREEN
					+ " receivers, " + ChatColor.GRAY
					+ channel.getTransmitters().size() + ChatColor.GREEN
					+ " transmitters, " + ChatColor.GRAY
					+ channel.getScreens().size() + ChatColor.GREEN
					+ " screens");

			/*
			 * Showing the owners
			 */

			player.sendMessage(ChatColor.BLUE
					+ WirelessRedstone.strings.ownersOfTheChannelAre);
			for (String owner : channel.getOwners()) {
				player.sendMessage(" - " + owner);
			}

			return true;
		}

		/*
		 * Will be able to show more informations. The arguments will be : 'r'
		 * or 'receivers'. In this case it will show how many receivers are in
		 * the channel, and the position of each of them. 't' or 'transmitters'.
		 * In this case it will show how many transmitters are in the channel,
		 * and which ones are toggled. 's' or 'screens'.
		 */

		if (args.length == 2) {
			return performAdvancedShowInfo(args, player, 1);
		}
		if (args.length == 3) {
			int page;
			try {
				page = Integer.parseInt(args[2]);
			} catch (Exception ex) {
				WirelessRedstone.getWRLogger().debug(
						"Could not parse Integer while executing command ShowInfo. Third argument is "
								+ args[2]);
				player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag + "This page number is not a number!");
				return false;
			}
			if (page > 0)
				return performAdvancedShowInfo(args, player, page);
			else {
				player.sendMessage(ChatColor.RED
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
			case "receivers" :
			case "r" : {
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
			case "transmitters" :
			case "t" : {
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
			case "screens" :
			case "s" : {
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
			final String[] args, final Player player) {
		if (!(args.length > 1)) {
			sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.tooFewArguments);
			return true;
		}
		if (!plugin.permissions.canActivateChannel(player)) {
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
		if (!WirelessRedstone.WireBox.hasAccessToChannel(player,
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
			final String[] args, final Player player) {
		if (!plugin.permissions.canSeeVersion(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.playerDoesntHavePermission);
			return true;
		}
		player.sendMessage("You are currently running WirelessRedstone "
				+ ChatColor.UNDERLINE + plugin.getDescription().getVersion()
				+ ChatColor.RESET + " .");
		player.sendMessage("The plugin will warn you if a newer version is released.");
		return true;
	}

	private boolean performWRTeleport(final CommandSender sender,
			final String[] args, final Player player) {
		if (!(args.length > 2)) {
			sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.tooFewArguments);
			return true;
		}
		if (!plugin.permissions.canTeleportToSign(player)) {
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
		if (!WirelessRedstone.WireBox.hasAccessToChannel(player,
				channel.getName())) {
			sender.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
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
			case "receiver" :
			case "r" : {
				Location locReceiver = channel.getReceivers().get(index)
						.getLocation().add(0.5, 0, 0.5);
				player.teleport(locReceiver);
				return true;
			}
			case "transmitter" :
			case "t" : {
				Location locTransmitter = channel.getReceivers().get(index)
						.getLocation().add(0.5, 0, 0.5);
				player.teleport(locTransmitter);
				return true;
			}
			case "screen" :
			case "s" : {
				Location locScreen = channel.getReceivers().get(index)
						.getLocation().add(0.5, 0, 0.5);
				player.teleport(locScreen);
				return true;
			}
		}
		return true;
	}

	public boolean performPurgeData(final CommandSender sender,
			final String[] args, final Player player) {
		if (!plugin.permissions.canPurgeData(player)) {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.playerDoesntHavePermission);
			return true;
		}

		if (WirelessRedstone.config.purgeData()) {
			player.sendMessage(ChatColor.GREEN
					+ WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.purgeDataDone);
		} else {
			player.sendMessage(ChatColor.RED + WirelessRedstone.strings.chatTag
					+ WirelessRedstone.strings.purgeDataFailed);
		}
		return true;
	}

	/**
	 * This method will show a list of lines to a player. It adds ' - ' to each
	 * line.
	 *
	 * @param list
	 *            - A list of lines that will be showed.
	 * @param cpage
	 *            - The current page, means that if there are several pages
	 *            because the list is too long, it will this page (currently 5
	 *            lines per page). Cannot be inferior to 1.
	 * @param player
	 *            - The player who will be showed the list.
	 */
	public void showList(final ArrayList<String> list, final int cpage,
			final Player player) {
		/*
		 * Show a page from list of Strings Where maxitems is the maximum items
		 * on each page
		 */

		int itemsonlist = list.size();
		int maxitems = 10;
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
		player.sendMessage(ChatColor.UNDERLINE + "Page " + currentpage + " of "
				+ totalpages);
		if (totalpages == 0) {
			player.sendMessage(WirelessRedstone.strings.listEmpty);
		} else {
			for (int i = currentitem; i < (currentitem + maxitems); i++) {
				if (!(i >= itemsonlist)) {
					player.sendMessage("    - " + list.get(i));
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
		int maxitems = 10;
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
}
