package net.licks92.WirelessRedstone;

import java.util.ArrayList;

import net.licks92.WirelessRedstone.channel.WirelessChannel;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WirelessCommands implements CommandExecutor
{
	private final WirelessRedstone plugin;

	public WirelessCommands(WirelessRedstone plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		String commandName = command.getName().toLowerCase();
		Player player;

		if (!(sender instanceof Player))
		{
			WirelessRedstone.getStackableLogger().info("Only in-game players can use this command.");
			return true;
		}

		player = (Player) sender;

		if (commandName.equals("wrhelp"))
		{
			return performHelp(sender, args, player);
		}
		else if(commandName.equals("wrt"))
		{
			return performCreateTransmitter(sender, args, player);
		}
		else if(commandName.equals("wrr"))
		{
			return performCreateReceiver(sender, args, player);
		}
		else if(commandName.equals("wrc"))
		{
			return performChannelAdmin(sender, args, player);
		}
		else if (commandName.equals("wrremove"))
		{
			return performRemoveChannel(sender, args, player);
		}
		else if (commandName.equals("wrlist"))
		{
			player.sendMessage(ChatColor.RED + "Function not implemented yet !");
			return true;
			//return performWRlist(sender, args, player);
		}
		else if (commandName.equals("wri"))
		{
			return performShowInfo(sender,args,player);
		}
		/*else if (commandName.equals("wr") || commandName.equals("wirelessredstone") || commandName.equals("wstone"))
		{
			
		}*/
		return true;
	}
	public ArrayList<String> generateCommandList(Player player)
	{
		ArrayList<String> commands = new ArrayList<String>(); 

		if (plugin.permissionsHandler.hasPermission(player, "WirelessRedstone.commands.wrt") || plugin.permissionsHandler.hasPermission(player, "WirelessRedstone.basics"))
		{
			commands.add("/WRt channelname - Creates transmitter sign.");
		}

		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrr")
				|| plugin.permissionsHandler.hasPermission(player,
						"WirelessRedstone.basics")) {
			commands.add("/WRr channelname - Creates receiver sign.");
		}

		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrremove")
				|| plugin.permissionsHandler.hasPermission(player,
						"WirelessRedstone.basics")) {
			commands.add("/WRremove channelname - Removes a channel.");
		}

		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrc")
				|| plugin.permissionsHandler.hasPermission(player,
						"WirelessRedstone.basics")) {
			commands.add("/WRc - Channel admin commands. Execute for more info.");
		}

		if (plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.commands.wrlist"))
		{
			commands.add("/WRlist - Lists all the channels with the owners.");
		}

		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrcleanup")) {
			commands.add("/WRcleanup - cleansup the database for errors and other things.");
		}

		return commands;
	}

	@SuppressWarnings("unused")
	private boolean performWRlist(CommandSender sender, String[] args, Player player)
	{
		if (!plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.basics")
				|| !plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.commands.list"))
		{
			player.sendMessage("You don't have the permissions to use this command.");
			return true;
		}
		ArrayList<String> list = new ArrayList<String>();
		try
		{
			for (WirelessChannel channel : plugin.WireBox.getChannels())
			{
				if(channel != null)
				{
					String item = channel.getName() + " : ";
					for (String owner : channel.getOwners())
					{
						item += owner + ", ";
					}
					list.add(item);
				}
				list.add("Receivers: " + channel.getReceivers().size() + " | Transmitters: " + channel.getTransmitters().size()); //Bug NullPointerExcetion
			}
		} catch(NullPointerException ex) {
			WirelessRedstone.getStackableLogger().severe("Unable to get the list of channels ! Stack trace ==>");
			ex.printStackTrace();
		}

		if (args.length == 1)
		{
			int pagenumber;
			try
			{
				pagenumber = Integer.parseInt(args[0]);
			}
			catch (Exception e)
			{
				player.sendMessage("This page number is not an number!");
				return true;
			}
			player.sendMessage(ChatColor.AQUA + "WirelessRedstone Channel List(" + plugin.WireBox.getChannels().size() + " channel(s) )");
			ShowList(list, pagenumber, player);
			player.sendMessage("\n/wrlist pagenumber for next page!");
			return true;
		}
		else if (args.length == 0)
		{
			player.sendMessage(ChatColor.AQUA + "WirelessRedstone Channel List(" + plugin.WireBox.getChannels().size() + " channel(s) )");
			ShowList(list, 1, player);
			player.sendMessage("\n/wrlist pagenumber for next page!");
			return true;
		}
		else if(args.length > 1)
		{
			player.sendMessage("Too Many Arguments !");
			return false;
		}
		else
		{
			return false;
		}
	}

	private boolean performChannelAdmin(CommandSender sender, String[] args,Player player)
	{
		if (args.length >= 3) {
			String channelname = args[0];
			String subcommand = args[1];
			String playername = args[2];

			if (subcommand.equalsIgnoreCase("addowner"))
			{
				if (plugin.WireBox.hasAccessToChannel(player, channelname))
				{
					WirelessChannel channel = plugin.WireBox.getChannel(channelname);
					channel.addOwner(playername);
					plugin.WireBox.SaveChannel(channel);
					return true;
				}
				else
				{
					player.sendMessage("[WirelessRedstone] You don't have access to this channel.");
				}
			} else if (subcommand.equalsIgnoreCase("removeowner")) {
				if (plugin.WireBox.hasAccessToChannel(player, channelname)) {
					WirelessChannel channel = plugin.WireBox
							.getChannel(channelname);
					channel.removeOwner(playername);
					plugin.WireBox.SaveChannel(channel);
					return true;
				} else {
					player.sendMessage("[WirelessRedstone] You don't have access to this channel.");
				}
			} else {
				player.sendMessage("[WirelessRedstone] Unknown sub command!");
			}
		} else {
			player.sendMessage("Channel Admin Commands:");
			player.sendMessage("/WRc channelname addowner playername - Add a player to channel.");
			player.sendMessage("/WRc channelname removeowner playername - Add a player to channel.");
		}
		return true;

	}

	private boolean performHelp(CommandSender sender, String[] args,
			Player player) {

		if (!plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.basics")
				|| !plugin.permissionsHandler.hasPermission(player,
						"WirelessRedstone.commands.help")) {
			player.sendMessage("You don't have the permissions to use this command.");
			return true;
		}

		ArrayList<String> commands = generateCommandList(player);

		if (args.length >= 1)
		{
			int pagenumber;
			try
			{
				pagenumber = Integer.parseInt(args[0]);
			}
			catch (Exception e)
			{
				player.sendMessage("This page number is not an number!");
				return true;
			}
			player.sendMessage(ChatColor.AQUA + "WirelessRedstone User Commands:");
			ShowList(commands, pagenumber, player);
			player.sendMessage("/WRhelp " + Integer.toString(pagenumber + 1) + " for next page!");
			return true;
		}
		else
		{
			player.sendMessage(ChatColor.AQUA + "WirelessRedstone User Commands:");
			ShowList(commands, 1, player);
			player.sendMessage("/WRhelp 2 for next page!");
		}
		return true;
	}

	public boolean performCreateTransmitter(CommandSender sender,
			String[] args, Player player) {
		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrt")
				|| plugin.permissionsHandler.hasPermission(player,
						"WirelessRedstone.basics")) {
			if (args.length >= 1) {
				String channelname = args[0];
				if (plugin.WireBox.hasAccessToChannel(player, channelname)) {
					player.getLocation().getBlock();
					player.getLocation().getBlock().setType(Material.SIGN_POST);
					Sign sign = (Sign) player.getLocation().getBlock()
							.getState();
					sign.setLine(0, "[WRt]");
					sign.setLine(1, channelname);
					sign.update(true);
					plugin.WireBox.addWirelessTransmitter(channelname, player
							.getLocation().getBlock(), player);
				}
			}
		}
		return true;
	}

	public boolean performRemoveChannel(CommandSender sender, String[] args, Player player)
	{
		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrremove")
				|| plugin.permissionsHandler.hasPermission(player,
						"WirelessRedstone.basics"))
		{
			if (args.length >= 1)
			{
				if (plugin.WireBox.hasAccessToChannel(player, args[0]))
				{
					plugin.WireBox.removeChannel(args[0]);
				}
			}
		}
		player.sendMessage("Channel has been removed !");
		return true;
	}

	public boolean performCreateReceiver(CommandSender sender, String[] args,Player player)
	{
		if (plugin.permissionsHandler.hasPermission(player, "WirelessRedstone.commands.wrr") || plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.basics"))
		{
			if (args.length >= 1)
			{
				String channelname = args[0];
				if (plugin.WireBox.hasAccessToChannel(player, channelname))
				{
					player.getLocation().getBlock();
					player.getLocation().getBlock().setType(Material.SIGN_POST);
					Sign sign = (Sign) player.getLocation().getBlock().getState();
					sign.setLine(0, "[WRr]");
					sign.setLine(1, channelname);
					sign.update(true);
					plugin.WireBox.addWirelessReceiver(channelname, player.getLocation().getBlock(), player);
				}
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	public boolean performShowInfo(CommandSender sender, String[] args, Player player)
	{
		/*
		 * This method shows the status of a WirelessChannel.
		 * At the moment, it only shows if the channel is active or not...
		 */
		if (!plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.basics")
				|| !plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.commands.info"))
		{
			player.sendMessage("You don't have the permissions to use this command.");
			return true;
		}
		if(args.length == 0)
		{
			player.sendMessage("Too few arguments !");
			return false;
		}
		if(args.length == 1)
		{
			if(plugin.WireBox.getChannel(args[0]) == null)
			{
				player.sendMessage("This channel doesn't exists");
				return false;
			}
			WirelessChannel tempChannel = plugin.WireBox.getChannel(args[0]);
			player.sendMessage("STATUS OF " + tempChannel.getName());
			/*
			 * Checking for active transmitters
			 */
			if(plugin.WireBox.isActive(tempChannel))
			{
				player.sendMessage("Is Activated : " + ChatColor.GREEN + "YES");
			}
			else
			{
				player.sendMessage("Is Activated : " + ChatColor.RED + "NO");
			}
			/*
			 * Send the final message
			 */
			return true;
		}
		if(args.length >= 2)
		{
			player.sendMessage("Too many arguments !");
			return false;
		}
		return false;
	}
	
	public void ShowList(ArrayList<String> list, int cpage, Player player)
	{ 
		/*
		 * Show a page from list of Strings
		 * Where maxitems is the maximum items on each page
		 */
		int itemsonlist = list.size();
		int maxitems = 5;
		int currentpage = cpage;
		int totalpages = 0;
		for(int i = 0; i <= itemsonlist/maxitems; i++)
		{
			totalpages = i + 1;
		}

		int currentitem = ((cpage * maxitems) - maxitems);
		// 2*3 = 6 ; 6 - 3 = 3
		player.sendMessage("Page " + currentpage + " on " + totalpages);
		if (totalpages == 0)
		{
			player.sendMessage("There are no items on this list!");
		}
		else
		{
			for (int i = currentitem; i < (currentitem + maxitems); i++)
			{
				if(!(i >= itemsonlist))
				{
					player.sendMessage(list.get(i));
				}
			}
		}

	}


}
