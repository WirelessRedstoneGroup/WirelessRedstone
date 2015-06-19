package net.licks92.WirelessRedstone.Strings;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.ChatColor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
            loadFromStream(stream);
        } else {
            WirelessRedstone
                    .getWRLogger()
                    .warning(
                            "You've set the language to "
                                    + language
                                    + " in your configuration. This language is not available. The plugin will now load the default english strings.");
            stream = plugin.getResource(STRINGS_FOLDER + defaultLanguage
                    + "/strings.xml");
            if (stream != null)
                loadFromStream(stream);
            else
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
                        WirelessRedstone.getWRLogger().info(
                                "Loading the tags...");

                        chatTag = ChatColor.GREEN + tagsElement.getElementsByTagName("chatTag")
                                .item(0).getTextContent();
                        break;

                    case "playermessages": // Load the PM strings
                        Element PMElement = (Element) rootNodes.item(i);
                        WirelessRedstone.getWRLogger().info(
                                "Loading the player messages ..");

                        backupDone = ChatColor.GREEN + PMElement
                                .getElementsByTagName("backupDone").item(0)
                                .getTextContent();
                        backupFailed = ChatColor.RED + PMElement
                                .getElementsByTagName("backupFailed").item(0)
                                .getTextContent();
                        channelDoesNotExist = ChatColor.RED + PMElement
                                .getElementsByTagName("channelDoesNotExist")
                                .item(0).getTextContent();
                        channelLocked = ChatColor.GREEN + PMElement
                                .getElementsByTagName("channelLocked").item(0)
                                .getTextContent();
                        channelNameContainsInvalidCaracters = ChatColor.RED + PMElement
                                .getElementsByTagName(
                                        "channelNameContainsInvalidCaracters")
                                .item(0).getTextContent();
                        channelRemoved = ChatColor.GREEN + PMElement
                                .getElementsByTagName("channelRemoved").item(0)
                                .getTextContent();
                        channelRemovedCauseNoSign = ChatColor.GRAY + PMElement
                                .getElementsByTagName(
                                        "channelRemovedCauseNoSign").item(0)
                                .getTextContent();
                        channelUnlocked = ChatColor.GREEN + PMElement
                                .getElementsByTagName("channelUnlocked")
                                .item(0).getTextContent();
                        commandDoesNotExist = ChatColor.RED + PMElement
                                .getElementsByTagName("commandDoesNotExist")
                                .item(0).getTextContent();
                        commandForNextPage = ChatColor.GREEN + PMElement
                                .getElementsByTagName("commandForNextPage")
                                .item(0).getTextContent();
                        customizedLanguageSuccessfullyLoaded = ChatColor.GREEN + PMElement
                                .getElementsByTagName(
                                        "customizedLanguageSuccessfullyLoaded")
                                .item(0).getTextContent();
                        DBAboutToBeDeleted = ChatColor.RED + PMElement
                                .getElementsByTagName("DBAboutToBeDeleted")
                                .item(0).getTextContent();
                        DBDeleted = PMElement.getElementsByTagName("DBDeleted")
                                .item(0).getTextContent();
                        DBNotDeleted = ChatColor.GREEN + PMElement
                                .getElementsByTagName("DBNotDeleted").item(0)
                                .getTextContent();
                        forMoreInfosPerformWRInfo = ChatColor.GREEN + PMElement
                                .getElementsByTagName(
                                        "forMoreInfosPerformWRInfo").item(0)
                                .getTextContent();
                        listEmpty = ChatColor.GREEN + PMElement.getElementsByTagName("listEmpty")
                                .item(0).getTextContent();
                        ownersOfTheChannelAre = ChatColor.BLUE + PMElement
                                .getElementsByTagName("ownersOfTheChannelAre")
                                .item(0).getTextContent();
                        pageEmpty = ChatColor.RED + PMElement.getElementsByTagName("pageEmpty")
                                .item(0).getTextContent();
                        pageNumberInferiorToZero = ChatColor.RED + PMElement
                                .getElementsByTagName(
                                        "pageNumberInferiorToZero").item(0)
                                .getTextContent();
                        playerCannotCreateChannel = ChatColor.RED + PMElement
                                .getElementsByTagName(
                                        "playerCannotCreateChannel").item(0)
                                .getTextContent();
                        playerCannotCreateReceiverOnBlock = ChatColor.RED + PMElement
                                .getElementsByTagName(
                                        "playerCannotCreateReceiverOnBlock")
                                .item(0).getTextContent();
                        playerCannotCreateSign = ChatColor.RED + PMElement
                                .getElementsByTagName("playerCannotCreateSign")
                                .item(0).getTextContent();
                        playerCannotDestroyReceiverTorch = ChatColor.RED + PMElement
                                .getElementsByTagName(
                                        "playerCannotDestroyReceiverTorch")
                                .item(0).getTextContent();
                        playerCannotDestroySign = ChatColor.RED + PMElement
                                .getElementsByTagName("playerCannotDestroySign")
                                .item(0).getTextContent();
                        playerCreatedChannel = ChatColor.GREEN + PMElement
                                .getElementsByTagName("playerCreatedChannel")
                                .item(0).getTextContent();
                        playerDoesntHaveAccessToChannel = ChatColor.RED + PMElement
                                .getElementsByTagName(
                                        "playerDoesntHaveAccessToChannel")
                                .item(0).getTextContent();
                        playerDoesntHavePermission = ChatColor.RED + PMElement
                                .getElementsByTagName(
                                        "playerDoesntHavePermission").item(0)
                                .getTextContent();
                        playerExtendedChannel = ChatColor.GREEN + PMElement
                                .getElementsByTagName("playerExtendedChannel")
                                .item(0).getTextContent();
                        signDestroyed = ChatColor.GREEN + PMElement
                                .getElementsByTagName("signDestroyed").item(0)
                                .getTextContent();
                        subCommandDoesNotExist = ChatColor.RED + PMElement
                                .getElementsByTagName("subCommandDoesNotExist")
                                .item(0).getTextContent();
                        thisChannelContains = ChatColor.BLUE + PMElement
                                .getElementsByTagName("thisChannelContains")
                                .item(0).getTextContent();
                        tooFewArguments = ChatColor.RED + PMElement
                                .getElementsByTagName("tooFewArguments")
                                .item(0).getTextContent();
                        break;

                    case "logmessages": // Load the LM strings
                        Element LMElement = (Element) rootNodes.item(i);
                        WirelessRedstone.getWRLogger().debug(
                                "Loading the log messages ..");

                        newUpdateAvailable = ChatColor.GREEN + LMElement
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
