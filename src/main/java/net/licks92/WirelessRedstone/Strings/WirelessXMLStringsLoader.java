package net.licks92.WirelessRedstone.Strings;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.licks92.WirelessRedstone.WirelessRedstone;

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
					case "tags" : // Load the tags
						final Element tagsElement = (Element) rootNodes.item(i);
						WirelessRedstone.getWRLogger().debug(
								"Loading the tags...");

						String chatTag = tagsElement
								.getElementsByTagName("chatTag").item(0)
								.getTextContent();

						if (checkString(chatTag))
							this.chatTag = chatTag + " ";
						break;

					case "playermessages" : // Load the PM strings
						Element PMElement = (Element) rootNodes.item(i);
						WirelessRedstone.getWRLogger().debug(
								"Loading the player messages ..");

						String backupDone = PMElement
								.getElementsByTagName("backupDone").item(0)
								.getTextContent();
						String backupFailed = PMElement
								.getElementsByTagName("backupFailed").item(0)
								.getTextContent();
						String channelDoesNotExist = PMElement
								.getElementsByTagName("channelDoesNotExist").item(0)
								.getTextContent();
						String channelLocked = PMElement
								.getElementsByTagName("channelLocked").item(0)
								.getTextContent();
						String channelNameContainsInvalidCaracters = PMElement
								.getElementsByTagName("channelNameContainsInvalidCaracters").item(0)
								.getTextContent();
						String channelRemoved = PMElement
								.getElementsByTagName("channelRemoved").item(0)
								.getTextContent();
						String channelRemovedCauseNoSign = PMElement
								.getElementsByTagName("channelRemovedCauseNoSign").item(0)
								.getTextContent();
						String channelUnlocked = PMElement
								.getElementsByTagName("channelUnlocked").item(0)
								.getTextContent();
						String commandDoesNotExist = PMElement
								.getElementsByTagName("commandDoesNotExist").item(0)
								.getTextContent();
						String commandForNextPage = PMElement
								.getElementsByTagName("commandForNextPage").item(0)
								.getTextContent();
						String customizedLanguageSuccessfullyLoaded = PMElement
								.getElementsByTagName("customizedLanguageSuccessfullyLoaded").item(0)
								.getTextContent();
						String DBAboutToBeDeleted = PMElement
								.getElementsByTagName("DBAboutToBeDeleted").item(0)
								.getTextContent();
						String DBDeleted = PMElement
								.getElementsByTagName("DBDeleted").item(0)
								.getTextContent();
						String DBNotDeleted = PMElement
								.getElementsByTagName("DBNotDeleted").item(0)
								.getTextContent();
						String forMoreInfosPerformWRInfo = PMElement
								.getElementsByTagName("forMoreInfosPerformWRInfo").item(0)
								.getTextContent();
						String listEmpty = PMElement
								.getElementsByTagName("listEmpty").item(0)
								.getTextContent();
						String ownersOfTheChannelAre = PMElement
								.getElementsByTagName("ownersOfTheChannelAre").item(0)
								.getTextContent();
						String pageEmpty = PMElement
								.getElementsByTagName("pageEmpty").item(0)
								.getTextContent();
						String pageNumberInferiorToZero = PMElement
								.getElementsByTagName("pageNumberInferiorToZero").item(0)
								.getTextContent();
						String playerCannotCreateChannel = PMElement
								.getElementsByTagName("playerCannotCreateChannel").item(0)
								.getTextContent();
						String playerCannotCreateReceiverOnBlock = PMElement
								.getElementsByTagName("playerCannotCreateReceiverOnBlock").item(0)
								.getTextContent();
						String playerCannotCreateSign = PMElement
								.getElementsByTagName("playerCannotCreateSign").item(0)
								.getTextContent();
						String playerCannotDestroyReceiverTorch = PMElement
								.getElementsByTagName("playerCannotDestroyReceiverTorch").item(0)
								.getTextContent();
						String playerCannotDestroySign = PMElement
								.getElementsByTagName("playerCannotDestroySign").item(0)
								.getTextContent();
						String playerCreatedChannel = PMElement
								.getElementsByTagName("playerCreatedChannel").item(0)
								.getTextContent();
						String playerDoesntHaveAccessToChannel = PMElement
								.getElementsByTagName("playerDoesntHaveAccessToChannel").item(0)
								.getTextContent();
						String playerDoesntHavePermission = PMElement
								.getElementsByTagName("playerDoesntHavePermission").item(0)
								.getTextContent();
						String playerExtendedChannel = PMElement
								.getElementsByTagName("playerExtendedChannel").item(0)
								.getTextContent();
						String signDestroyed = PMElement
								.getElementsByTagName("signDestroyed").item(0)
								.getTextContent();
						String subCommandDoesNotExist = PMElement
								.getElementsByTagName("subCommandDoesNotExist").item(0)
								.getTextContent();
						String thisChannelContains = PMElement
								.getElementsByTagName("thisChannelContains").item(0)
								.getTextContent();
						String tooFewArguments = PMElement
								.getElementsByTagName("tooFewArguments").item(0)
								.getTextContent();

						if (checkString(backupDone))
							this.backupDone = backupDone;
						if (checkString(backupFailed))
							this.backupFailed = backupFailed;
						if (checkString(channelDoesNotExist))
							this.channelDoesNotExist = channelDoesNotExist;
						if (checkString(channelLocked))
							this.channelLocked = channelLocked;
						if (checkString(channelNameContainsInvalidCaracters))
							this.channelNameContainsInvalidCaracters = channelNameContainsInvalidCaracters;
						if (checkString(channelRemoved))
							this.channelRemoved = channelRemoved;
						if (checkString(channelRemovedCauseNoSign))
							this.channelRemovedCauseNoSign = channelRemovedCauseNoSign;
						if (checkString(channelUnlocked))
							this.channelUnlocked = channelUnlocked;
						if (checkString(commandDoesNotExist))
							this.commandDoesNotExist = commandDoesNotExist;
						if (checkString(commandForNextPage))
							this.commandForNextPage = commandForNextPage;
						if (checkString(customizedLanguageSuccessfullyLoaded))
							this.customizedLanguageSuccessfullyLoaded = customizedLanguageSuccessfullyLoaded;
						if (checkString(DBAboutToBeDeleted))
							this.DBAboutToBeDeleted = DBAboutToBeDeleted;
						if (checkString(DBDeleted))
							this.DBDeleted = DBDeleted;
						if (checkString(DBNotDeleted))
							this.DBNotDeleted = DBNotDeleted;
						if (checkString(forMoreInfosPerformWRInfo))
							this.forMoreInfosPerformWRInfo = forMoreInfosPerformWRInfo;
						if (checkString(listEmpty))
							this.listEmpty = listEmpty;
						if (checkString(ownersOfTheChannelAre))
							this.ownersOfTheChannelAre = ownersOfTheChannelAre;
						if (checkString(pageEmpty))
							this.pageEmpty = pageEmpty;
						if (checkString(pageNumberInferiorToZero))
							this.pageNumberInferiorToZero = pageNumberInferiorToZero;
						if (checkString(playerCannotCreateChannel))
							this.playerCannotCreateChannel = playerCannotCreateChannel;
						if (checkString(playerCannotCreateReceiverOnBlock))
							this.playerCannotCreateReceiverOnBlock = playerCannotCreateReceiverOnBlock;
						if (checkString(playerCannotCreateSign))
							this.playerCannotCreateSign = playerCannotCreateSign;
						if (checkString(playerCannotDestroyReceiverTorch))
							this.playerCannotDestroyReceiverTorch = playerCannotDestroyReceiverTorch;
						if (checkString(playerCannotDestroySign))
							this.playerCannotDestroySign = playerCannotDestroySign;
						if (checkString(playerCreatedChannel))
							this.playerCreatedChannel = playerCreatedChannel;
						if (checkString(playerDoesntHaveAccessToChannel))
							this.playerDoesntHaveAccessToChannel = playerDoesntHaveAccessToChannel;
						if (checkString(playerDoesntHavePermission))
							this.playerDoesntHavePermission = playerDoesntHavePermission;
						if (checkString(playerExtendedChannel))
							this.playerExtendedChannel = playerExtendedChannel;
						if (checkString(signDestroyed))
							this.signDestroyed = signDestroyed;
						if (checkString(subCommandDoesNotExist))
							this.subCommandDoesNotExist = subCommandDoesNotExist;
						if (checkString(thisChannelContains))
							this.thisChannelContains = thisChannelContains;
						if (checkString(tooFewArguments))
							this.tooFewArguments = tooFewArguments;
						break;

					case "logmessages" : // Load the LM strings
						Element LMElement = (Element) rootNodes.item(i);
						WirelessRedstone.getWRLogger().debug(
								"Loading the log messages ..");

						String newUpdateAvailable = LMElement
								.getElementsByTagName("newUpdateAvailable").item(0)
								.getTextContent();

						if (checkString(newUpdateAvailable))
							this.newUpdateAvailable = newUpdateAvailable;
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

	private boolean checkString(final String string) {
		if ((string != null)) {
			if (!string.equalsIgnoreCase(""))
				return true;
		}
		return false;
	}
}
