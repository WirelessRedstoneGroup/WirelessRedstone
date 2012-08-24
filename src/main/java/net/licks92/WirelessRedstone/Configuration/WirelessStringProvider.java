package net.licks92.WirelessRedstone.Configuration;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import net.licks92.WirelessRedstone.WirelessRedstone;

public class WirelessStringProvider
{
	public String playerExtendedChannel;
	public String playerCreatedChannel;
	public String playerCannotCreateChannel;
	public String playerCannotCreateReceiverOnBlock;
	public String playerHaveNotPermission;
	public String playerHaveNotAccessToChannel;
	public String playerCannotCreateSign;
	public String signDestroyed;
	public String channelRemovedCauseNoSign;
	public String channelNameContainsInvalidCaracters;
	public String playerCannotDestroySign;
	public String noItemOnList;
	public String tooFewArguments;
	public List<String> tagsTransmitter = new ArrayList<String>();
	public List<String> tagsReceiver = new ArrayList<String>();
	public List<String> tagsScreen = new ArrayList<String>();
	
	public WirelessStringProvider()
	{	
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
			playerCannotCreateSign = ChatColor.RED + "[WirelessRedstone] You don't have the permission to create this sign!";
			signDestroyed = ChatColor.GREEN + "[WirelessRedstone] Succesfully removed this sign !";
			channelRemovedCauseNoSign = ChatColor.GREEN + "[WirelessRedstone] Channel removed, no more signs in the worlds.";
			channelNameContainsInvalidCaracters = "[WirelessRedstone] " + playerCannotCreateChannel + " : Name contains invalid caracters !";
			tooFewArguments = ChatColor.RED + "[WirelessRedstone] Too few arguments !";
			noItemOnList = ChatColor.RED + "[WirelessRedstone] There are no items on this list!";
			playerCannotDestroySign = ChatColor.RED + "[WirelessRedstone] You are not allowed to destroy this sign!" ;
			tagsTransmitter.add("[transmitter]");
			tagsTransmitter.add("[wrt]");
			tagsReceiver.add("[receiver]");
			tagsReceiver.add("[wrr]");
			tagsScreen.add("[screen]");
			tagsScreen.add("[wrs]");
		}
		catch (Exception ex)
		{
			WirelessRedstone.getStackableLogger().severe("Could not load strings !");
			WirelessRedstone.getStackableLogger().severe("Exception stacktrace : ");
			ex.printStackTrace();
			return false;
		}
		return true;
	}
}
