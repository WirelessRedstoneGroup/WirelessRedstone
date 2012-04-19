package net.licks92.WirelessRedstone.Configuration;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import net.licks92.WirelessRedstone.WirelessRedstone;

public class WirelessStringProvider
{
	private NewWirelessConfiguration config;
	private WirelessRedstone plugin;
	public String playerExtendedChannel;
	public String playerCreatedChannel;
	public String playerCannotCreateChannel;
	public String playerCannotCreateReceiverOnBlock;
	public String playerHaveNotPermission;
	public String playerHaveNotAccessToChannel;
	public String noItemOnList;
	public String tooFewArguments;
	public List<String> tagsTransmitter;
	public List<String> tagsReceiver;
	public List<String> tagsScreen;
	
	public WirelessStringProvider(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		config = WirelessRedstone.config;
		
		//If useSystemLanguage is true, then the language argument is not used
		loadStrings();
	}
	
	private boolean loadStrings()
	{
		try
		{
			playerExtendedChannel = ChatColor.GREEN + "[WirelessRedstone] You just extended a channel!";
			playerCreatedChannel = ChatColor.GREEN + "[WirelessRedstone] You just created a new channel!";
			playerCannotCreateChannel = ChatColor.RED + "[WirelessRedstone] You cannot create a channel";
			playerCannotCreateReceiverOnBlock = ChatColor.RED + "[WirelessRedstone] You cannot create a wireless receiver on this block !";
			playerHaveNotPermission = ChatColor.RED + "You don't have the permissions to use this command.";
			playerHaveNotAccessToChannel = ChatColor.RED + "[WirelessRedstone] You don't have access to this channel.";
			tooFewArguments = ChatColor.RED +  "[WirelessRedstone] Too few arguments !";
			noItemOnList = "There are no items on this list!";
			tagsTransmitter = new ArrayList<String>();
			tagsTransmitter.add("[transmitter]");
			tagsTransmitter.add("[wrt]");
			tagsReceiver = new ArrayList<String>();
			tagsReceiver.add("[receiver]");
			tagsReceiver.add("[wrr]");
			tagsScreen = new ArrayList<String>();
			tagsScreen.add("[screen]");
			tagsScreen.add("[wrs]");
		}
		catch (Exception ex)
		{
			WirelessRedstone.getStackableLogger().severe("Could not load strings !");
			return false;
		}
		return true;
	}
}
