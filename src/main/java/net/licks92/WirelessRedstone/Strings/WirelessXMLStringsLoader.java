package net.licks92.WirelessRedstone.Strings;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loads the strings from a specific file. If some strings are missing, it will
 * replace them by the one in the default english file.
 */
public class WirelessXMLStringsLoader extends WirelessStrings {
    private final String STRINGS_FOLDER = "languages/";
    private final String defaultLanguage = "en";

    public WirelessXMLStringsLoader(final WirelessRedstone plugin,
                                    final String language) {

        String defaultLocation = STRINGS_FOLDER + defaultLanguage
                + "/strings.xml";
        InputStream stream = plugin.getResource(STRINGS_FOLDER + language
                + "/strings.xml");
        if (stream != null) {
            loadFromStream(plugin.getResource(defaultLocation));
            loadFromStream(stream);
        } else {
            WirelessRedstone
                    .getWRLogger()
                    .warning(
                            "You've set the language to "
                                    + language
                                    + " in your configuration. This language is not available. The plugin will now load the default english strings.");
            stream = plugin.getResource(defaultLocation);
            if (stream != null) {
                loadFromStream(plugin.getResource(defaultLocation));
                loadFromStream(stream);
            } else
                WirelessRedstone
                        .getWRLogger()
                        .severe("Could not load the strings in "
                                + defaultLocation
                                + ". Your jar file is probably corrupted. Please download it again from dev.bukkit.org/bukkit-plugins/wireless-redstone/");
        }
    }

    private void loadFromStream(final InputStream stream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(stream);

            final Element root = document.getDocumentElement();
            final NodeList rootNodes = root.getChildNodes();
            for (int i = 0; i < rootNodes.getLength(); i++) {
                switch (rootNodes.item(i).getNodeName()) {
                    case "tags": // Load the tags
                        final Element tagsElement = (Element) rootNodes.item(i);
                        WirelessRedstone.getWRLogger().debug(
                                "Loading the tags...");

                        if (tagsElement.getElementsByTagName("chatTag").item(0) != null)
                            this.chatTag = "[" + tagsElement
                                    .getElementsByTagName("chatTag").item(0)
                                    .getTextContent()
                                    + "] ";
                        break;

                    case "playermessages": // Load the PM strings
                        Element PMElement = (Element) rootNodes.item(i);
                        WirelessRedstone.getWRLogger().debug(
                                "Loading the player messages ..");

                        if (PMElement.getElementsByTagName("backupDone").item(0) != null)
                            this.backupDone = PMElement
                                    .getElementsByTagName("backupDone").item(0)
                                    .getTextContent();
                        if (PMElement.getElementsByTagName("backupFailed").item(0) != null)
                            this.backupFailed = PMElement
                                    .getElementsByTagName("backupFailed")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("channelDoesNotExist").item(0) != null)
                            this.channelDoesNotExist = PMElement
                                    .getElementsByTagName("channelDoesNotExist")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("channelLocked").item(0) != null)
                            this.channelLocked = PMElement
                                    .getElementsByTagName("channelLocked")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("channelUnlocked").item(0) != null)
                            this.channelUnlocked = PMElement
                                    .getElementsByTagName("channelUnlocked")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("channelNameContainsInvalidCaracters").item(0) != null)
                            this.channelNameContainsInvalidCaracters = PMElement
                                    .getElementsByTagName(
                                            "channelNameContainsInvalidCaracters")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("channelRemoved").item(0) != null)
                            this.channelRemoved = PMElement
                                    .getElementsByTagName("channelRemoved")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("channelRemovedCauseNoSign").item(0) != null)
                            this.channelRemovedCauseNoSign = PMElement
                                    .getElementsByTagName(
                                            "channelRemovedCauseNoSign")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("signDestroyed").item(0) != null)
                            this.signDestroyed = PMElement
                                    .getElementsByTagName("signDestroyed")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("commandForNextPage").item(0) != null)
                            this.commandForNextPage = PMElement
                                    .getElementsByTagName("commandForNextPage")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("customizedLanguageSuccessfullyLoaded").item(0) != null)
                            this.customizedLanguageSuccessfullyLoaded = PMElement
                                    .getElementsByTagName(
                                            "customizedLanguageSuccessfullyLoaded")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("DBAboutToBeDeleted").item(0) != null)
                            this.DBAboutToBeDeleted = PMElement
                                    .getElementsByTagName("DBAboutToBeDeleted")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("DBDeleted").item(0) != null)
                            this.DBDeleted = PMElement
                                    .getElementsByTagName("DBDeleted").item(0)
                                    .getTextContent();
                        if (PMElement.getElementsByTagName("DBNotDeleted").item(0) != null)
                            this.DBNotDeleted = PMElement
                                    .getElementsByTagName("DBNotDeleted")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("forMoreInfosPerformWRInfo").item(0) != null)
                            this.forMoreInfosPerformWRInfo = PMElement
                                    .getElementsByTagName(
                                            "forMoreInfosPerformWRInfo")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("listEmpty").item(0) != null)
                            this.listEmpty = PMElement
                                    .getElementsByTagName("listEmpty").item(0)
                                    .getTextContent();
                        if (PMElement
                                .getElementsByTagName("ownersOfTheChannelAre").item(0) != null)
                            this.ownersOfTheChannelAre = PMElement
                                    .getElementsByTagName(
                                            "ownersOfTheChannelAre").item(0)
                                    .getTextContent();
                        if (PMElement.getElementsByTagName("pageEmpty").item(0) != null)
                            this.pageEmpty = PMElement
                                    .getElementsByTagName("pageEmpty").item(0)
                                    .getTextContent();
                        if (PMElement
                                .getElementsByTagName("pageNumberInferiorToZero").item(0) != null)
                            this.pageNumberInferiorToZero = PMElement
                                    .getElementsByTagName(
                                            "pageNumberInferiorToZero").item(0)
                                    .getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerCannotCreateChannel").item(0) != null)
                            this.playerCannotCreateChannel = PMElement
                                    .getElementsByTagName(
                                            "playerCannotCreateChannel")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerCannotCreateReceiverOnBlock").item(0) != null)
                            this.playerCannotCreateReceiverOnBlock = PMElement
                                    .getElementsByTagName(
                                            "playerCannotCreateReceiverOnBlock")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerCannotCreateSign").item(0) != null)
                            this.playerCannotCreateSign = PMElement
                                    .getElementsByTagName(
                                            "playerCannotCreateSign").item(0)
                                    .getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerCannotDestroyReceiverTorch").item(0) != null)
                            this.playerCannotDestroyReceiverTorch = PMElement
                                    .getElementsByTagName(
                                            "playerCannotDestroyReceiverTorch")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerCannotDestroySign").item(0) != null)
                            this.playerCannotDestroySign = PMElement
                                    .getElementsByTagName(
                                            "playerCannotDestroySign").item(0)
                                    .getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerCreatedChannel").item(0) != null)
                            this.playerCreatedChannel = PMElement
                                    .getElementsByTagName(
                                            "playerCreatedChannel").item(0)
                                    .getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerDoesntHaveAccessToChannel").item(0) != null)
                            this.playerDoesntHaveAccessToChannel = PMElement
                                    .getElementsByTagName(
                                            "playerDoesntHaveAccessToChannel")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerDoesntHavePermission").item(0) != null)
                            this.playerDoesntHavePermission = PMElement
                                    .getElementsByTagName(
                                            "playerDoesntHavePermission")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("playerExtendedChannel").item(0) != null)
                            this.playerExtendedChannel = PMElement
                                    .getElementsByTagName(
                                            "playerExtendedChannel").item(0)
                                    .getTextContent();
                        if (PMElement.getElementsByTagName("purgeDataDone").item(0) != null)
                            this.purgeDataDone = PMElement
                                    .getElementsByTagName("purgeDataDone")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("purgeDataFailed").item(0) != null)
                            this.purgeDataFailed = PMElement
                                    .getElementsByTagName("purgeDataFailed")
                                    .item(0).getTextContent();
                        if (PMElement
                                .getElementsByTagName("subCommandDoesNotExist").item(0) != null)
                            this.subCommandDoesNotExist = PMElement
                                    .getElementsByTagName(
                                            "subCommandDoesNotExist").item(0)
                                    .getTextContent();
                        if (PMElement
                                .getElementsByTagName("thisChannelContains").item(0) != null)
                            this.thisChannelContains = PMElement
                                    .getElementsByTagName("thisChannelContains")
                                    .item(0).getTextContent();

                        if (PMElement.getElementsByTagName("allTransmittersGone").item(0) != null)
                            this.allTransmittersGone = PMElement
                                    .getElementsByTagName("allTransmittersGone")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("playerCannotDestroyBlockAttachedToSign").item(0) != null)
                            this.playerCannotDestroyBlockAttachedToSign = PMElement
                                    .getElementsByTagName("playerCannotDestroyBlockAttachedToSign")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("commandDoesNotExist").item(0) != null)
                            this.commandDoesNotExist = PMElement
                                    .getElementsByTagName("commandDoesNotExist")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("tooFewArguments").item(0) != null)
                            this.tooFewArguments = PMElement
                                    .getElementsByTagName("tooFewArguments")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("convertDone").item(0) != null)
                            this.convertDone = PMElement
                                    .getElementsByTagName("convertDone")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("convertFailed").item(0) != null)
                            this.convertFailed = PMElement
                                    .getElementsByTagName("convertFailed")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("convertSameType").item(0) != null)
                            this.convertSameType = PMElement
                                    .getElementsByTagName("convertSameType")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("convertContinue").item(0) != null)
                            this.convertContinue = PMElement
                                    .getElementsByTagName("convertContinue")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("automaticAssigned").item(0) != null)
                            this.automaticAssigned = PMElement
                                    .getElementsByTagName("automaticAssigned")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("restoreDataDone").item(0) != null)
                            this.restoreDataDone = PMElement
                                    .getElementsByTagName("restoreDataDone")
                                    .item(0).getTextContent();
                        if (PMElement.getElementsByTagName("restoreDataFailed").item(0) != null)
                            this.restoreDataFailed = PMElement
                                    .getElementsByTagName("restoreDataFailed")
                                    .item(0).getTextContent();
                        break;

                    case "logmessages": // Load the LM strings
                        Element LMElement = (Element) rootNodes.item(i);
                        WirelessRedstone.getWRLogger().debug(
                                "Loading the log messages ..");

                        if (LMElement
                                .getElementsByTagName("newUpdateAvailable").item(0) != null)
                            this.newUpdateAvailable = LMElement
                                    .getElementsByTagName("newUpdateAvailable")
                                    .item(0).getTextContent();
                        break;
                }
            }
        } catch (ParserConfigurationException e) {
            WirelessRedstone.getWRLogger().severe(
                    "Error while loading the xml parser.");
        } catch (SAXException | IOException e) {
            WirelessRedstone.getWRLogger().severe(
                    "Error while parsing the xml file.");
        } catch (NullPointerException e) {
            WirelessRedstone.getWRLogger().severe(
                    "Your strings file is not correctly written.");
            e.printStackTrace();
        }

        // Here we load the tags
        /*
		 * tagsTransmitter.add("[transmitter]"); tagsTransmitter.add("[wrt]");
		 * tagsReceiver.add("[receiver]"); tagsReceiver.add("[wrr]");
		 * tagsScreen.add("[screen]"); tagsScreen.add("[wrs]");
		 */
    }
}
