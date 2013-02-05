package net.licks92.WirelessRedstone.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

/**
 * 
 * @author licks92
 * 
 * 
 * Allows to get the strings in order to use them in other classes.
 *
 */
@SerializableAs("WirelessStrings")
public class WirelessStrings implements ConfigurationSerializable
{	
	//Strings in alphabetical order.
	public String chatTag;
	public String channelDoesNotExist;
	public String channelLocked;
	public String channelNameContainsInvalidCaracters;
	public String channelRemovedCauseNoSign;
	public String channelUnlocked;
	public String commandForNextPage;
	public String customizedLanguageSuccessfullyLoaded;
	public String forMoreInfosPerformWRInfo;
	public String noItemOnPage;
	public String ownersOfTheChannelAre;
	public String pageNumberInferiorToZero;
	public String playerCannotCreateChannel;
	public String playerCannotCreateReceiverOnBlock;
	public String playerCannotCreateSign;
	public String playerCannotDestroySign;
	public String playerCreatedChannel;
	public String playerDoesntHaveAccessToChannel;
	public String playerDoesntHavePermission;
	public String playerExtendedChannel;
	public String signDestroyed;
	public String thisChannelContains;
	public String tooFewArguments;
	
	public List<String> tagsTransmitter = new ArrayList<String>();
	public List<String> tagsReceiver = new ArrayList<String>();
	public List<String> tagsScreen = new ArrayList<String>();
	
	public WirelessStrings(Map<String, Object> lang)
	{
		chatTag = "[" + lang.get("chatTag") + "] ";
		channelDoesNotExist = ChatColor.RED + chatTag + lang.get("channelDoesNotExist");
		channelLocked = ChatColor.GREEN + chatTag + lang.get("channelLocked");
		channelNameContainsInvalidCaracters = ChatColor.RED + chatTag + lang.get("channelNameContainsInvalidCaracters");
		channelRemovedCauseNoSign = ChatColor.GREEN + chatTag + lang.get("channelRemovedCauseNoSign");
		channelUnlocked = ChatColor.GREEN + chatTag + lang.get("channelUnlocked");
		commandForNextPage = ChatColor.GREEN + chatTag + lang.get("commandForNextPage");
		customizedLanguageSuccessfullyLoaded = ChatColor.GREEN + chatTag + lang.get("customizedLanguageSuccessfullyLoaded");
		forMoreInfosPerformWRInfo = ChatColor.GREEN + chatTag + lang.get("forMoreInfosPerformWRInfo");
		noItemOnPage = ChatColor.RED + chatTag + lang.get("noItemOnList");
		ownersOfTheChannelAre = chatTag + lang.get("ownersOfTheChannelAre");
		pageNumberInferiorToZero = ChatColor.RED + chatTag + lang.get("pageNumberInferiorToZero");
		playerCannotCreateChannel = ChatColor.RED + chatTag + lang.get("playerCannotCreateChannel");
		playerCannotCreateReceiverOnBlock = ChatColor.RED + chatTag + lang.get("playerCannotCreateReceiverOnBlock");
		playerCreatedChannel = ChatColor.GREEN + chatTag + lang.get("playerCreatedChannel");
		playerDoesntHaveAccessToChannel = ChatColor.RED + chatTag + lang.get("playerDoesntHaveAccessToChannel");
		playerDoesntHavePermission = ChatColor.RED + chatTag + lang.get("playerDoesntHavePermission");
		playerExtendedChannel = ChatColor.GREEN + chatTag + lang.get("playerExtendedChannel");
		signDestroyed = ChatColor.GREEN + chatTag + lang.get("signDestroyed");
		thisChannelContains = chatTag + lang.get("thisChannelContains");
		tooFewArguments = ChatColor.RED + chatTag + lang.get("thisChannelContains");
		
		//The signtags must be always the same
		tagsTransmitter.add("[transmitter]");
		tagsTransmitter.add("[wrt]");
		tagsReceiver.add("[receiver]");
		tagsReceiver.add("[wrr]");
		tagsScreen.add("[screen]");
		tagsScreen.add("[wrs]");
	}
	
	@Override
	public Map<String, Object> serialize()
	{
		return null;
	}
}
