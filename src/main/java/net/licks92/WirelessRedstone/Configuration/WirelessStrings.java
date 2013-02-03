package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import net.licks92.WirelessRedstone.WirelessRedstone;

/**
 * 
 * @author licks92
 * 
 * 
 * Allows to get the strings in order to use them in other classes.
 *
 */
public class WirelessStrings
{	
	public String playerExtendedChannel;
	public String playerCreatedChannel;
	public String playerCannotCreateChannel;
	public String playerCannotCreateReceiverOnBlock;
	public String playerDoesntHavePermission;
	public String playerDoesntHaveAccessToChannel;
	public String playerCannotCreateSign;
	public String ownersOfTheChannelAre;
	public String thisChannelContains;
	public String channelLocked;
	public String channelUnlocked;
	public String channelDoesNotExist;
	public String pageNumberInferiorToZero;
	public String commandForNextPage;
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
}
