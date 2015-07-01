package net.licks92.WirelessRedstone.Strings;

import net.licks92.WirelessRedstone.WirelessRedstone;

import java.util.ArrayList;
import java.util.List;

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
    public String purgeDataDone;
    public String purgeDataFailed;
    public String signDestroyed;
    public String subCommandDoesNotExist;
    public String thisChannelContains;
    public String tooFewArguments;
    public String allTransmittersGone;
    public String playerCannotDestroyBlockAttachedToSign;
    public String convertDone;
    public String convertFailed;
    public String convertSameType;
    public String convertContinue;
    public String automaticAssigned;
    public String restoreDataDone;
    public String restoreDataFailed;

    public String tellRawString;

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

        tellRawString = "tellraw %%PLAYER [\"\",{\"text\":\"[\",\"color\":\"dark_blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%TEXT\",\"color\":\"gray\"}]}}},{\"text\":\"\\u27A4\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%TEXT\",\"color\":\"gray\"}]}}},{\"text\":\"] \",\"color\":\"dark_blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%TEXT\",\"color\":\"gray\"}]}}},{\"text\":\"Name %%NAME, type: %%TYPE, world: %%WORLD, x: %%XCOORD, y: %%YCOORD, z: %%ZCOORD\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%%COMMAND\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"%%TEXT\",\"color\":\"gray\"}]}}}]";

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
        tagsReceiverClockType.add("[clock]");
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