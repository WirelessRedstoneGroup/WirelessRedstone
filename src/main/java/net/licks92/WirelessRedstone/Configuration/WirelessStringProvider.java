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
	public String playerHasNotPermission;
	public String playerHasNotAccessToChannel;
	public String playerCannotCreateSign;
	public String ownersOfTheChannelAre;
	public String thisChannelContains;
	public String channelLocked;
	public String channelUnlocked;
	public String channelDoesNotExist;
	public String pageNumberInferiorToZero;
	public String nextPage;
	public String signDestroyed;
	public String channelRemovedCauseNoSign;
	public String channelNameContainsInvalidCaracters;
	public String playerCannotDestroySign;
	public String noItemOnList;
	public String tooFewArguments;
	public String forMoreInfosPerformWRInfo;
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
			playerHasNotPermission = ChatColor.RED + "You don't have the permissions to use this command.";
			playerHasNotAccessToChannel = ChatColor.RED + "[WirelessRedstone] You don't have access to this channel.";
			playerCannotCreateSign = ChatColor.RED + "[WirelessRedstone] You don't have the permission to create this sign!";
			signDestroyed = ChatColor.GREEN + "[WirelessRedstone] Succesfully removed this sign !";
			channelRemovedCauseNoSign = ChatColor.GREEN + "[WirelessRedstone] Channel removed, no more signs in the worlds.";
			channelNameContainsInvalidCaracters = playerCannotCreateChannel + " : Name contains invalid caracters : a dot '.'!";
			channelDoesNotExist = ChatColor.RED + "[WirelessRedstone] This channel doesn't exist!";
			tooFewArguments = ChatColor.RED + "[WirelessRedstone] Too few arguments !";
			noItemOnList = ChatColor.RED + "[WirelessRedstone] There are no items on this list!";
			playerCannotDestroySign = ChatColor.RED + "[WirelessRedstone] You are not allowed to destroy this sign!";
			ownersOfTheChannelAre = "The owners of this channel are : ";
			thisChannelContains = "This channel contains :";
			pageNumberInferiorToZero = ChatColor.RED + "[WirelessRedstone] Page number cannot be inferior to 0!";
			channelLocked = ChatColor.GREEN + "[WirelessRedstone] Channel locked !";
			channelUnlocked = ChatColor.GREEN + "[WirelessRedstone] Channel unlocked !";
			nextPage = ChatColor.GREEN + "\n/wr list pagenumber for next page!";
			forMoreInfosPerformWRInfo = ChatColor.AQUA + "For more informations about a channel, perform /wr info <channel>";
			tagsTransmitter.add("[transmitter]");
			tagsTransmitter.add("[wrt]");
			tagsReceiver.add("[receiver]");
			tagsReceiver.add("[wrr]");
			tagsScreen.add("[screen]");
			tagsScreen.add("[wrs]");
		}
		catch (Exception ex)
		{
			WirelessRedstone.getWRLogger().severe("Could not load strings !");
			WirelessRedstone.getWRLogger().severe("Exception stacktrace : ");
			ex.printStackTrace();
			return false;
		}
		return true;
	}
}
