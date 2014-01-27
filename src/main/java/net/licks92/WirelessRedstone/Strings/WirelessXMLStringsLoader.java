package net.licks92.WirelessRedstone.Strings;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import net.licks92.WirelessRedstone.WirelessRedstone;

/**
 * Loads the strings from a specific file. If some strings are missing, it will replace them by the one in the default english file.
 */
public class WirelessXMLStringsLoader
{
	private WirelessStrings strings;
	private final String STRINGS_FOLDER = "/languages";
	private File stringsFolder;
	private final String defaultLanguage = "en";
	
	public WirelessXMLStringsLoader(WirelessRedstone plugin, String language)
	{
		stringsFolder = new File(plugin.getDataFolder() + STRINGS_FOLDER);
	}
	
	private void loadFromFile(File file)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db;
		Document document;
		
		//At first let's load and parse the xml language file.
		try
		{
			db = dbf.newDocumentBuilder();
			document = db.parse(file);
		}
		catch (ParserConfigurationException ex)
		{
			WirelessRedstone.getWRLogger().severe("Error while loading the xml parser.");
		} catch (SAXException | IOException e) {
			WirelessRedstone.getWRLogger().severe("Error while parsing the xml file.");
		}
		//If the document was successfully parsed.
		finally
		{
			
		}
		
		
	}
}
