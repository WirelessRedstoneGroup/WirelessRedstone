package net.licks92.WirelessRedstone.Strings;

import java.util.ArrayList;
import java.util.List;

import net.licks92.WirelessRedstone.WirelessRedstone;

public class WirelessStrings {
    // Strings in alphabetical order.
    public String chatTag;
    public String backupDone;
    public String backupFailed;
    public String channelDoesNotExist;
    public String channelLocked;
    public String channelNameContainsInvalidCaracters;
    public String channelRemoved;
    public String channelRemovedCauseNoSign;
    public String channelUnlocked;
    public String commandDoesNotExist;
    public String commandForNextPage;
    public String customizedLanguageSuccessfullyLoaded;
    public String DBAboutToBeDeleted;
    public String DBDeleted;
    public String DBNotDeleted;
    public String forMoreInfosPerformWRInfo;
    public String listEmpty;
    public String newUpdateAvailable;
    public String ownersOfTheChannelAre;
    public String pageEmpty;
    public String pageNumberInferiorToZero;
    public String playerCannotCreateChannel;
    public String playerCannotCreateReceiverOnBlock;
    public String playerCannotCreateSign;
    public String playerCannotDestroyReceiverTorch;
    public String playerCannotDestroySign;
    public String playerCreatedChannel;
    public String playerDoesntHaveAccessToChannel;
    public String playerDoesntHavePermission;
    public String playerExtendedChannel;
    public String signDestroyed;
    public String subCommandDoesNotExist;
    public String thisChannelContains;
    public String tooFewArguments;

    public List<String> tagsTransmitter;
    public List<String> tagsReceiver;
    public List<String> tagsScreen;
    public List<String> tagsReceiverDefaultType;
    public List<String> tagsReceiverInverterType;
    public List<String> tagsReceiverDelayerType;
    public List<String> tagsReceiverClockType;

    public WirelessStrings() {
        tagsTransmitter = new ArrayList<String>();
        tagsReceiver = new ArrayList<String>();
        tagsScreen = new ArrayList<String>();
        tagsReceiverDefaultType = new ArrayList<String>();
        tagsReceiverInverterType = new ArrayList<String>();
        tagsReceiverDelayerType = new ArrayList<String>();
        tagsReceiverClockType = new ArrayList<String>();

        WirelessRedstone.getWRLogger().debug("Loading the tags...");
        chatTag = "WirelessRedstone";
        backupDone = "A backup has been created into the plugin folder!";
        backupFailed = "Backup failed!";
        channelDoesNotExist = "This channel doesn't exist!";
        channelLocked = "Channel locked !";
        channelNameContainsInvalidCaracters = "This channel name contains invalid caracters !";
        channelRemoved = "This channel has been removed!";
        channelRemovedCauseNoSign = "Channel removed, no more signs in the worlds";
        channelUnlocked = "Channel unlocked !";
        commandDoesNotExist = "This command does not exist!";
        commandForNextPage = "Type /wr list pagenumber for next page!";
        customizedLanguageSuccessfullyLoaded = "You've successfully loaded your customized language for Wireless Redstone!";
        DBAboutToBeDeleted = "You are about to delete the entire database. A backup will be done before you do it. If you are sure to do it, you have 15 seconds to type this command again.";
        DBDeleted = "Database has been succesfully wiped!";
        DBNotDeleted = "Database hasn't been wiped.";
        forMoreInfosPerformWRInfo = "To get more informations about a channel, perform /wr info [channel]";
        listEmpty = "The list in empty!";
        ownersOfTheChannelAre = "The owners of this channel are";
        pageEmpty = "This page is empty!";
        pageNumberInferiorToZero = "Page number cannot be inferior to 0!";
        playerCannotCreateChannel = "You are not allowed to create a channel.";
        playerCannotCreateReceiverOnBlock = "You cannot create a wireless receiver on this block !";
        playerCannotCreateSign = "You don't have the permission to create this sign!";
        playerCannotDestroyReceiverTorch = "You cannot break my magic torches my friend!";
        playerCannotDestroySign = "You are not allowed to destroy this sign!";
        playerCreatedChannel = "You just created a new channel!";
        playerDoesntHaveAccessToChannel = "You don't have access to this channel.";
        playerDoesntHavePermission = "You don't have the permissions to do this.";
        playerExtendedChannel = "You just extended a channel!";
        signDestroyed = "Succesfully removed this sign !";
        subCommandDoesNotExist = "This subcommand doesn't exist!";
        thisChannelContains = "This channel contains";
        tooFewArguments = "Too few arguments !";

        tagsTransmitter.add("[transmitter]");
        tagsTransmitter.add("[wrt]");
        tagsReceiver.add("[receiver]");
        tagsReceiver.add("[wrr]");
        tagsScreen.add("[screen]");
        tagsScreen.add("[wrs]");
        tagsReceiverDefaultType.add("[default]");
        tagsReceiverDefaultType.add("[normal]");
        tagsReceiverInverterType.add("[inverter]");
        tagsReceiverInverterType.add("[inv]");
        tagsReceiverDelayerType.add("[delayer]");
        tagsReceiverDelayerType.add("[delay]");
        tagsReceiverDelayerType.add("[clock]");
    }
}

/*
 * //The signtags must be always the same tagsTransmitter.add("[transmitter]");
 * tagsTransmitter.add("[wrt]"); tagsReceiver.add("[receiver]");
 * tagsReceiver.add("[wrr]"); tagsScreen.add("[screen]");
 * tagsScreen.add("[wrs]"); tagsReceiverDefaultType.add("[default]");
 * tagsReceiverDefaultType.add("[normal]");
 * tagsReceiverInverterType.add("[inverter]");
 * tagsReceiverInverterType.add("[inv]");
 * tagsReceiverDelayerType.add("[delayer]");
 * tagsReceiverDelayerType.add("[delay]");
 */