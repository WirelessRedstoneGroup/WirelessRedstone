package net.licks92.WirelessRedstone;

import java.util.ArrayList;
import java.util.List;

import net.licks92.WirelessRedstone.Channel.WirelessChannel;

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
		if (commandName.equals("wirelessredstone"))
		{
			return performWR(sender, args, player);
		}
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
		else if (commandName.equals("wrs"))
		{
			return performCreateScreen(sender, args, player);
		}
		else if (commandName.equals("wri"))
		{
			return performShowInfo(sender,args,player);
		}
		else if(commandName.equals("wra"))
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
			commands.add("/wr receiver channelname - Creates receiver sign.");
		}

		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrremove")
				|| plugin.permissionsHandler.hasPermission(player,
						"WirelessRedstone.basics")) {
			commands.add("/wr remove channel - Removes a channel.");
		}

		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrc")
				|| plugin.permissionsHandler.hasPermission(player,
						"WirelessRedstone.basics")) {
			commands.add("/wr admin - Channel admin commands. Execute for more info.");
		}

		if (plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.commands.wrlist"))
		{
			commands.add("/wr list - Lists all the channels with the owners.");
		}

		if (plugin.permissionsHandler.hasPermission(player,
				"WirelessRedstone.commands.wrcleanup")) {
			commands.add("/wr cleanup - cleansup the database for errors and other things.");
		}

		return commands;
	}
	
	public boolean performWR(CommandSender sender, String[] r_args, Player player)
	{
		//If a command is sent after the /wr, perform it. Else, perform help.
		if(r_args.length>=1)
		{
			String commandName = r_args[0];
			List<String> temp = new ArrayList<String>();
			for(int i = 1; i < r_args.length; i ++)
			{
				temp.add(r_args[i]);
			}
			String[] args = temp.toArray(new String[0]);
			if (commandName.equals("wirelessredstone"))
			{
				return performWR(sender, args, player);
			}
			if (commandName.equals("help"))
			{
				return performHelp(sender, args, player);
			}
			else if(commandName.equals("transmitter")
					||commandName.equals("t"))
			{
				return performCreateTransmitter(sender, args, player);
			}
			else if(commandName.equals("receiver")
					||commandName.equals("r"))
			{
				return performCreateReceiver(sender, args, player);
			}
			else if(commandName.equals("screen")
					||commandName.equals("s"))
			{
				return performCreateScreen(sender, args, player);
			}
			else if(commandName.equals("admin"))
			{
				return performChannelAdmin(sender, args, player);
			}
			else if (commandName.equals("remove")
					||commandName.equals("delete"))
			{
				return performRemoveChannel(sender, args, player);
			}
			else if (commandName.equals("list"))
			{
				player.sendMessage(ChatColor.RED + "Function not implemented yet !");
				return true;
				//return performWRlist(sender, args, player);
			}
			else if (commandName.equals("info"))
			{
				return performShowInfo(sender,args,player);
			}
			else
			{
				player.sendMessage(ChatColor.RED + "This command does not exist.");
				return true;
			}
		}
		else
		{
			return performHelp(sender, r_args, player);
		}
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
		if (args.length >= 3)
		{
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
		}
		else
		{
			player.sendMessage("Channel Admin Commands:");
			player.sendMessage("/wr admin channelname addowner playername - Add a player to channel.");
			player.sendMessage("/wr admin channelname removeowner playername - Add a player to channel.");
		}
		return true;

	}

	private boolean performHelp(CommandSender sender, String[] args, Player player)
	{
		if (!plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.basics")
				|| !plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.commands.help"))
		{
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
			player.sendMessage("/wr help " + Integer.toString(pagenumber + 1) + " for next page!");
			return true;
		}
		else
		{
			player.sendMessage(ChatColor.AQUA + "WirelessRedstone User Commands:");
			ShowList(commands, 1, player);
			player.sendMessage("/wr help 2 for next page!");
		}
		return true;
	}

	public boolean performCreateTransmitter(CommandSender sender, String[] args, Player player)
	{
		if (plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.commands.wrt")
				|| plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.basics"))
		{
			if (args.length >= 1)
			{
				String channelname = args[0];
				if (plugin.WireBox.hasAccessToChannel(player, channelname))
				{
					player.getLocation().getBlock();
					player.getLocation().getBlock().setType(Material.SIGN_POST);
					Sign sign = (Sign) player.getLocation().getBlock().getState();
					sign.setLine(0, "[wrt]");
					sign.setLine(1, channelname);
					sign.update(true);
					plugin.WireBox.addWirelessTransmitter(channelname, player.getLocation().getBlock(), player);
				}
			}
			else if(args.length == 0)
			{
				player.sendMessage("[WirelessRedstone]" + ChatColor.RED + "Too few arguments ! Use /wr t channel");
				return true;
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You don't have permission to do that !");
			return true;
		}
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
					player.getLocation().getBlock().setType(Material.SIGN_POST);
					Sign sign = (Sign) player.getLocation().getBlock().getState();
					sign.setLine(0, "[WRr]");
					sign.setLine(1, channelname);
					sign.update(true);
					if(!plugin.WireBox.addWirelessReceiver(channelname, player.getLocation().getBlock(), player))
					{
						sign.getBlock().breakNaturally();
					}
					return true;
				}
			}
			else if(args.length == 0)
			{
				player.sendMessage("[WirelessRedstone]" + ChatColor.RED + "Too few arguments ! Use /wr r channel");
				return true;
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You don't have permission to do that !");
			return true;
		}
		return true;
	}
	
	public boolean performCreateScreen(CommandSender sender, String[] args, Player player)
	{
		if (plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.commands.wrs")
				|| plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.basics"))
		{
			if (args.length >= 1)
			{
				String channelname = args[0];
				if (plugin.WireBox.hasAccessToChannel(player, channelname))
				{
					player.getLocation().getBlock();
					player.getLocation().getBlock().setType(Material.SIGN_POST);
					Sign sign = (Sign) player.getLocation().getBlock().getState();
					sign.setLine(0, "[wrs]");
					sign.setLine(1, channelname);
					sign.update(true);
					plugin.WireBox.addWirelessScreen(channelname, player.getLocation().getBlock(), player);
				}
			}
			else if(args.length == 0)
			{
				player.sendMessage("[WirelessRedstone]" + ChatColor.RED + "Too few arguments ! Use /wr t channel");
				return true;
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You don't have permission to do that !");
			return true;
		}
		return true;
	}
	
	public boolean performRemoveChannel(CommandSender sender, String[] args, Player player)
	{
		if (plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.commands.wrremove")
				|| plugin.permissionsHandler.hasPermission(player,"WirelessRedstone.basics"))
		{
			if (args.length >= 1)
			{
				if (plugin.WireBox.hasAccessToChannel(player, args[0]))
				{
					plugin.WireBox.removeChannel(args[0]);
					player.sendMessage("Channel has been removed !");
				}
			}
			else if(args.length == 0)
			{
				player.sendMessage("[WirelessRedstone]" + ChatColor.RED + "Too few arguments ! Use /wr remove channel");
				return true;
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You don't have permission to do that !");
			return true;
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
			player.sendMessage("You don't have permission to do that !");
			return true;
		}
		if(args.length == 0)
		{
			player.sendMessage("[WirelessRedstone]" + ChatColor.RED + "Too few arguments ! Use /wr info channel");
			return true;
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
