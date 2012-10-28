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
			return performWRlist(sender, args, player);
		}
		else if (commandName.equals("wrlock"))
		{
			return performLockChannel(sender, args, player);
		}
		return true;
	}

	public ArrayList<String> generateCommandList(Player player)
	{
		ArrayList<String> commands = new ArrayList<String>(); 

		if (plugin.permissions.canCreateTransmitter(player))
		{
			commands.add("/wr transmitter channelname - Creates transmitter sign.");
		}

		if (plugin.permissions.canCreateReceiver(player))
		{
			commands.add("/wr receiver channelname - Creates receiver sign.");
		}
		
		if (plugin.permissions.canCreateScreen(player))
		{
			commands.add("/wr screen channelname - Creates screen sign.");
		}

		if (plugin.permissions.canRemoveChannel(player))
		{
			commands.add("/wr remove channel - Removes a channel.");
		}

		if (plugin.permissions.isWirelessAdmin(player))
		{
			commands.add("/wr admin - Channel admin commands. Execute for more info.");
		}

		if (plugin.permissions.canUseListCommand(player))
		{
			commands.add("/wr list - Lists all the channels with the owners.");
		}

		return commands;
	}
	
	private boolean performWR(CommandSender sender, String[] r_args, Player player)
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
			else if(commandName.equals("admin")
					||commandName.equals("a"))
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
				return performWRlist(sender, args, player);
			}
			else if (commandName.equals("info"))
			{
				return performShowInfo(sender,args, player);
			}
			else if (commandName.equals("lock")
					||commandName.equals("unlock"))
			{
				return performLockChannel(sender, args, player);
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
	
	private boolean performLockChannel(CommandSender sender, String[] args, Player player)
	{
		if(args.length==0)
		{
			player.sendMessage(WirelessRedstone.strings.tooFewArguments);
		}
		if(!plugin.permissions.canLockChannel(player))
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		if(args.length>=1)
		{
			WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(args[0]);
			if(channel.isLocked())
			{
				channel.setLocked(false);
				player.sendMessage(WirelessRedstone.strings.channelUnlocked);
			}
			else
			{
				channel.setLocked(true);
				player.sendMessage(WirelessRedstone.strings.channelLocked);
			}
		}
		return false;
	}

	private boolean performWRlist(CommandSender sender, String[] args, Player player)
	{
		if (!plugin.permissions.canUseListCommand(player))
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		ArrayList<String> list = new ArrayList<String>();
		try
		{
			for (WirelessChannel channel : WirelessRedstone.config.getAllChannels())
			{
				//Show Name of each channel and his activity
				if(channel != null)
				{
					String item = channel.getName() + " : ";
					if(plugin.WireBox.isActive(channel))
						item += ChatColor.GREEN + "ACTIVE";
					else
						item += ChatColor.RED + "INACTIVE";
					list.add(item);
				}
			}
		}
		catch(NullPointerException ex)
		{
			WirelessRedstone.getStackableLogger().severe("Unable to get the list of channels ! Stack trace ==>");
			ex.printStackTrace();
		}

		if (args.length >= 1)
		{
			int pagenumber;
			try
			{
				pagenumber = Integer.parseInt(args[0]);
			}
			catch (Exception e)
			{
				player.sendMessage("This page number is not a number!");
				return true;
			}
			player.sendMessage(ChatColor.AQUA + "WirelessRedstone Channel List(" + WirelessRedstone.config.getAllChannels().size() + " channel(s) )");
			ShowList(list, pagenumber, player);
			player.sendMessage(WirelessRedstone.strings.forMoreInfosPerformWRInfo);
			player.sendMessage(WirelessRedstone.strings.nextPage);
			return true;
		}
		else if (args.length == 0)
		{
			player.sendMessage(ChatColor.AQUA + "WirelessRedstone Channel List(" + WirelessRedstone.config.getAllChannels().size() + " channel(s) )");
			ShowList(list, 1, player);
			player.sendMessage(WirelessRedstone.strings.forMoreInfosPerformWRInfo);
			player.sendMessage(WirelessRedstone.strings.nextPage);
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean performChannelAdmin(CommandSender sender, String[] args,Player player)
	{
		if(!plugin.permissions.isWirelessAdmin(player))
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		if (args.length > 0)
		{
			String subCommand = args[0];

			if (subCommand.equalsIgnoreCase("addowner"))
			{
				String channelName = args[1];
				String playername = args[2];
				if (plugin.WireBox.hasAccessToChannel(player, channelName))
				{
					WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(channelName);
					channel.addOwner(playername);
					WirelessRedstone.config.updateChannel(channelName, channel);
					WirelessRedstone.getStackableLogger().debug(playername + " has been added to the list of owners of " + channelName);
					return true;
				}
				else
				{
					player.sendMessage(WirelessRedstone.strings.playerHasNotAccessToChannel);
				}
			}
			
			else if (subCommand.equalsIgnoreCase("removeowner"))
			{
				String channelName = args[1];
				String playername = args[2];
				if (plugin.WireBox.hasAccessToChannel(player, channelName))
				{
					WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(channelName);
					channel.removeOwner(playername);
					WirelessRedstone.config.updateChannel(channelName, channel);
					WirelessRedstone.getStackableLogger().debug(playername + " has been removed from the list of owners of " + channelName);
					return true;
				}
				else
				{
					player.sendMessage(WirelessRedstone.strings.playerHasNotAccessToChannel);
				}
			}
			
			else if (subCommand.equalsIgnoreCase("wipedata"))
			{
				return performWipeData(sender, args, player);
			}
			
			else
			{
				player.sendMessage("[WirelessRedstone] Unknown sub command!");
			}
		}
		else
		{
			player.sendMessage("Channel Admin Commands:");
			player.sendMessage("/wr admin addowner channelname playername - Add a player to channel.");
			player.sendMessage("/wr admin removeowner channelname playername - Add a player to channel.");
			player.sendMessage("/wr admin wipedata - Erase the database! Don't do it if you don't know what you're doing!");
		}
		return true;

	}

	private boolean performHelp(CommandSender sender, String[] args, Player player)
	{
		if (!plugin.permissions.canSeeHelp(player))
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
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

	private boolean performCreateTransmitter(CommandSender sender, String[] args, Player player)
	{
		if (plugin.permissions.canCreateTransmitter(player))
		{
			if (args.length >= 1)
			{
				String channelname = args[0];
				if (plugin.WireBox.hasAccessToChannel(player, channelname))
				{
					player.getLocation().getBlock();
					player.getLocation().getBlock().setType(Material.SIGN_POST);
					Sign sign = (Sign) player.getLocation().getBlock().getState();
					sign.setLine(0, WirelessRedstone.strings.tagsTransmitter.get(0));
					sign.setLine(1, channelname);
					sign.update(true);
					plugin.WireBox.addWirelessTransmitter(channelname, player.getLocation().getBlock(), player);
				}
				else
				{
					player.sendMessage(WirelessRedstone.strings.playerHasNotAccessToChannel);
				}
			}
			else if(args.length == 0)
			{
				player.sendMessage(WirelessRedstone.strings.tooFewArguments);
				return true;
			}
		}
		else
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		return true;
	}

	private boolean performCreateReceiver(CommandSender sender, String[] args,Player player)
	{
		if (plugin.permissions.canCreateReceiver(player))
		{
			if (args.length >= 1)
			{
				String channelname = args[0];
				if (plugin.WireBox.hasAccessToChannel(player, channelname))
				{
					player.getLocation().getBlock().setType(Material.SIGN_POST);
					Sign sign = (Sign) player.getLocation().getBlock().getState();
					sign.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
					sign.setLine(1, channelname);
					sign.update(true);
					if(!plugin.WireBox.addWirelessReceiver(channelname, player.getLocation().getBlock(), player))
					{
						sign.getBlock().breakNaturally();
					}
					return true;
				}
				else
				{
					player.sendMessage(WirelessRedstone.strings.playerHasNotAccessToChannel);
				}
			}
			else if(args.length == 0)
			{
				player.sendMessage(WirelessRedstone.strings.tooFewArguments);
				return true;
			}
		}
		else
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		return true;
	}
	
	private boolean performCreateScreen(CommandSender sender, String[] args, Player player)
	{
		if (plugin.permissions.canCreateScreen(player))
		{
			if (args.length >= 1)
			{
				String channelname = args[0];
				if (plugin.WireBox.hasAccessToChannel(player, channelname))
				{
					player.getLocation().getBlock();
					player.getLocation().getBlock().setType(Material.SIGN_POST);
					Sign sign = (Sign) player.getLocation().getBlock().getState();
					sign.setLine(0, WirelessRedstone.strings.tagsScreen.get(0));
					sign.setLine(1, channelname);
					sign.update(true);
					plugin.WireBox.addWirelessScreen(channelname, player.getLocation().getBlock(), player);
				}
				else
				{
					player.sendMessage(WirelessRedstone.strings.playerHasNotAccessToChannel);
				}
			}
			else if(args.length == 0)
			{
				player.sendMessage(WirelessRedstone.strings.tooFewArguments);
				return true;
			}
		}
		else
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		return true;
	}
	
	private boolean performRemoveChannel(CommandSender sender, String[] args, Player player)
	{
		if (plugin.permissions.canRemoveChannel(player))
		{
			if (args.length >= 1)
			{
				if (plugin.WireBox.hasAccessToChannel(player, args[0]))
				{
					plugin.WireBox.removeChannel(args[0]);
					player.sendMessage("Channel has been removed !");
				}
				else
				{
					player.sendMessage(WirelessRedstone.strings.playerHasNotAccessToChannel);
				}
			}
			else if(args.length == 0)
			{
				player.sendMessage(WirelessRedstone.strings.tooFewArguments);
				return true;
			}
		}
		else
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		return true;
	}
	
	private boolean performWipeData(CommandSender sender, String[] args, Player player)
	{
		/*
		 * To-do list:
		 * - Make a backup before.
		 * - Remove all the signs of every channel.
		 */
		
		if(!plugin.permissions.canWipeData(player))
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		if(WirelessRedstone.config.wipeData())
		{
			player.sendMessage("Database has been succesfully wiped!");
			return true;
		}
		else
		{
			player.sendMessage("Database hasn't been wiped.");
			return true;
		}
	}

	private boolean performShowInfo(CommandSender sender, String[] args, Player player)
	{
		/*
		 * This method shows the status of a WirelessChannel.
		 * At the moment, it shows :
		 * - if the channel is active or not.
		 * - how many signs of each kind is in each channel.
		 */
		if (!plugin.permissions.canSeeChannelInfo(player))
		{
			player.sendMessage(WirelessRedstone.strings.playerHasNotPermission);
			return true;
		}
		if(args.length == 0)
		{
			player.sendMessage(WirelessRedstone.strings.tooFewArguments);
			return true;
		}
		if(args.length == 1)
		{
			if (plugin.WireBox.hasAccessToChannel(player, args[0]))
			{
				if(WirelessRedstone.config.getWirelessChannel(args[0]) == null)
				{
					player.sendMessage("This channel doesn't exists");
					return false;
				}
				WirelessChannel tempChannel = WirelessRedstone.config.getWirelessChannel(args[0]);
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
				 * Counting signs of the channel.
				 */
				player.sendMessage(WirelessRedstone.strings.thisChannelContains);

				player.sendMessage(" - " + tempChannel.getReceivers().size() + " receivers.");
				player.sendMessage(" - " + tempChannel.getTransmitters().size() + " transmitters.");
				player.sendMessage(" - " + tempChannel.getScreens().size() + " screens.");
				
				/*
				 * Showing the owners
				 */
				
				player.sendMessage(WirelessRedstone.strings.ownersOfTheChannelAre);
				for(String owner : tempChannel.getOwners())
				{
					player.sendMessage(" - " +  owner);
				}
				
				return true;
			}
			else
			{
				player.sendMessage(WirelessRedstone.strings.playerHasNotAccessToChannel);
			}
		}
		return false;
	}
	
	public void ShowList(ArrayList<String> list, int cpage, Player player)
	{ 
		/*
		 * Show a page from list of Strings
		 * Where maxitems is the maximum items on each page
		 */
		
		if(cpage < 0)
		{
			player.sendMessage(WirelessRedstone.strings.pageNumberInferiorToZero);
		}
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
			player.sendMessage(WirelessRedstone.strings.noItemOnList);
		}
		else
		{
			for (int i = currentitem; i < (currentitem + maxitems); i++)
			{
				if(!(i >= itemsonlist))
				{
					player.sendMessage(" - " + list.get(i));
				}
			}
		}

	}


}
