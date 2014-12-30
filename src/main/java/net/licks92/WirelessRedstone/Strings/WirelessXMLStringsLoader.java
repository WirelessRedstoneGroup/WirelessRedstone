package net.licks92.WirelessRedstone.Strings;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.licks92.WirelessRedstone.WirelessRedstone;

/**
 * Loads the strings from a specific file. If some strings are missing, it will replace them by the one in the default english file.
 */
public class WirelessXMLStringsLoader
{
	private WirelessStrings strings;
	private final String STRINGS_FOLDER = "languages/";
	private final String defaultLanguage = "en";
	
	public WirelessXMLStringsLoader(WirelessRedstone plugin, String language)
	{
		this.strings = WirelessRedstone.strings;
		try {
			InputStream stream = plugin.getResource(STRINGS_FOLDER + language + "/strings.xml");
			if(stream != null)
				loadFromStream(stream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//Well.
		}
	}
	
	private void loadFromStream(InputStream stream)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(stream);
			
			final Element root = document.getDocumentElement();
			final NodeList rootNodes = root.getChildNodes();
			for(int i = 0; i < rootNodes.getLength(); i++) {
				switch(rootNodes.item(i).getNodeName())
				{
				case "tags": // Load the tags
					final Element tagsElement = (Element)rootNodes.item(i);
					WirelessRedstone.getWRLogger().debug("Loading the tags strings...");
					
					strings.chatTag = tagsElement.getElementsByTagName("chatTag").item(0).getTextContent();
					WirelessRedstone.getWRLogger().debug(strings.chatTag);
					break;
					
				case "playermessages": // Load the PM strings
					Element PMElement = (Element)rootNodes.item(i);
					WirelessRedstone.getWRLogger().debug("Loading the player messages strings...");
					
					strings.backupDone = PMElement.getElementsByTagName("backupDone").item(0).getTextContent();
					strings.backupFailed = PMElement.getElementsByTagName("backupFailed").item(0).getTextContent();
					strings.channelDoesNotExist = PMElement.getElementsByTagName("channelDoesNotExist").item(0).getTextContent();
					strings.channelLocked = PMElement.getElementsByTagName("channelLocked").item(0).getTextContent();
					strings.channelNameContainsInvalidCaracters = PMElement.getElementsByTagName("channelNameContainsInvalidCaracters").item(0).getTextContent();
					strings.channelRemoved = PMElement.getElementsByTagName("channelRemoved").item(0).getTextContent();
					strings.channelRemovedCauseNoSign = PMElement.getElementsByTagName("channelRemovedCauseNoSign").item(0).getTextContent();
					strings.channelUnlocked = PMElement.getElementsByTagName("channelUnlocked").item(0).getTextContent();
					strings.commandDoesNotExist = PMElement.getElementsByTagName("commandDoesNotExist").item(0).getTextContent();
					strings.commandForNextPage = PMElement.getElementsByTagName("commandForNextPage").item(0).getTextContent();
					strings.customizedLanguageSuccessfullyLoaded = PMElement.getElementsByTagName("customizedLanguageSuccessfullyLoaded").item(0).getTextContent();
					strings.DBAboutToBeDeleted = PMElement.getElementsByTagName("DBAboutToBeDeleted").item(0).getTextContent();
					strings.DBDeleted = PMElement.getElementsByTagName("DBDeleted").item(0).getTextContent();
					strings.DBNotDeleted = PMElement.getElementsByTagName("DBNotDeleted").item(0).getTextContent();
					strings.forMoreInfosPerformWRInfo = PMElement.getElementsByTagName("forMoreInfosPerformWRInfo").item(0).getTextContent();
					strings.listEmpty = PMElement.getElementsByTagName("listEmpty").item(0).getTextContent();
					strings.ownersOfTheChannelAre = PMElement.getElementsByTagName("ownersOfTheChannelAre").item(0).getTextContent();
					strings.pageEmpty = PMElement.getElementsByTagName("pageEmpty").item(0).getTextContent();
					strings.pageNumberInferiorToZero = PMElement.getElementsByTagName("pageNumberInferiorToZero").item(0).getTextContent();
					strings.playerCannotCreateChannel = PMElement.getElementsByTagName("playerCannotCreateChannel").item(0).getTextContent();
					strings.playerCannotCreateReceiverOnBlock = PMElement.getElementsByTagName("playerCannotCreateReceiverOnBlock").item(0).getTextContent();
					strings.playerCannotCreateSign = PMElement.getElementsByTagName("playerCannotCreateSign").item(0).getTextContent();
					strings.playerCannotDestroyReceiverTorch = PMElement.getElementsByTagName("playerCannotDestroyReceiverTorch").item(0).getTextContent();
					strings.playerCannotDestroySign = PMElement.getElementsByTagName("playerCannotDestroySign").item(0).getTextContent();
					strings.playerCreatedChannel = PMElement.getElementsByTagName("playerCreatedChannel").item(0).getTextContent();
					strings.playerDoesntHaveAccessToChannel = PMElement.getElementsByTagName("playerDoesntHaveAccessToChannel").item(0).getTextContent();
					strings.playerDoesntHavePermission = PMElement.getElementsByTagName("playerDoesntHavePermission").item(0).getTextContent();
					strings.playerExtendedChannel = PMElement.getElementsByTagName("playerExtendedChannel").item(0).getTextContent();
					strings.signDestroyed = PMElement.getElementsByTagName("signDestroyed").item(0).getTextContent();
					strings.subCommandDoesNotExist = PMElement.getElementsByTagName("subCommandDoesNotExist").item(0).getTextContent();
					strings.thisChannelContains = PMElement.getElementsByTagName("thisChannelContains").item(0).getTextContent();
					strings.tooFewArguments = PMElement.getElementsByTagName("tooFewArguments").item(0).getTextContent();
					break;
					
				case "logmessages": // Load the LM strings
					Element LMElement = (Element)rootNodes.item(i);
					WirelessRedstone.getWRLogger().debug("Loading the log messages strings...");
					
					strings.newUpdateAvailable = LMElement.getElementsByTagName("newUpdateAvailable").item(0).getTextContent();
					break;
				}
			}
		}  catch (ParserConfigurationException e) {
			WirelessRedstone.getWRLogger().severe("Error while loading the xml parser.");
		} catch (SAXException | IOException e) {
			WirelessRedstone.getWRLogger().severe("Error while parsing the xml file.");
		} catch (NullPointerException e) {
			WirelessRedstone.getWRLogger().severe("Your strings file is not correctly written.");
			e.printStackTrace();
		}
		
		//Here we load the tags
		/*strings.tagsTransmitter.add("[transmitter]");
		strings.tagsTransmitter.add("[wrt]");
		strings.tagsReceiver.add("[receiver]");
		strings.tagsReceiver.add("[wrr]");
		strings.tagsScreen.add("[screen]");
		strings.tagsScreen.add("[wrs]");*/
	}
	
	public WirelessStrings getStrings() {
		return strings;
	}
}
