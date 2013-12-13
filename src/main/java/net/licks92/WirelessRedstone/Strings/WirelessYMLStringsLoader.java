package net.licks92.WirelessRedstone.Strings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import net.licks92.WirelessRedstone.WirelessRedstone;

/**
 * 
 * Loads the strings that are in a specific .yml file. If there isn't any file, then load the default strings.
 * 
 */
public class WirelessYMLStringsLoader
{
	private WirelessRedstone plugin;
	private WirelessStrings strings;
	
	private final String STRINGS_FOLDER = "/languages";
	private File stringsFolder;
	private final String defaultLanguage = "default";
	
	private enum LoadingError
	{
		FileNotFound , MissingStrings , NoError , UnknownError
	}
	
	public WirelessYMLStringsLoader(WirelessRedstone plugin, String language)
	{
		this.plugin = plugin;
		this.strings = WirelessRedstone.strings;
		ConfigurationSerialization.registerClass(WirelessStrings.class, "WirelessStrings");
		try {
			stringsFolder = new File(plugin.getDataFolder(), STRINGS_FOLDER);
			stringsFolder.mkdirs();
			
			if(stringsFolder.listFiles().length == 0) //If Strings folder does not contain any strings file.
			{
				createDefaultFile();
				WirelessRedstone.getWRLogger().info("No language was found in the strings folder. Creating a new one by default.");
			}
			
			switch(loadFromYaml(language))
			{
			case FileNotFound:
				createDefaultFile();
				loadFromYaml(defaultLanguage);
				WirelessRedstone.getWRLogger().warning("The language file " + language + ".yml couldn't be found in the strings folder. Now using the default language.");
				break;
				
			case MissingStrings:
				createDefaultFile();
				loadFromYaml(defaultLanguage);
				WirelessRedstone.getWRLogger().warning("There are missing strings in the file : " + language + ".yml, so we can't load it. Now using the default language.");
				break;
				
			case NoError:
				if(!language.equals(defaultLanguage))
					WirelessRedstone.getWRLogger().info(strings.customizedLanguageSuccessfullyLoaded);
				else
					WirelessRedstone.getWRLogger().debug("Successfully loaded the default language.");
				break;
				
			case UnknownError:
				WirelessRedstone.getWRLogger().warning("An unknow error happened during the loading of the language file. Now using the default language.");
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private LoadingError loadFromYaml(String language)
	{
		FileConfiguration lang = new YamlConfiguration();
		File languageFile = new File(stringsFolder, language + ".yml");
		try {
			lang.load(languageFile);
			Object obj = lang.get("Strings");
			if(!(obj instanceof WirelessStrings))
			{
				return LoadingError.MissingStrings;
			}
			else
			{
				strings = (WirelessStrings)obj;
			}
		} catch (FileNotFoundException e)
		{
			return LoadingError.FileNotFound;
		} catch (IOException e) {
			e.printStackTrace();
			return LoadingError.UnknownError;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return LoadingError.UnknownError;
		}
		
		return LoadingError.NoError;
	}
	
	private void createDefaultFile() throws IOException
	{
		InputStream in = plugin.getResource("languages/default.yml");
		String fileName = stringsFolder.getCanonicalPath() + "/" + defaultLanguage + ".yml";
		FileOutputStream fos = new FileOutputStream(fileName);
		int i = 0;
		
		while((i = in.read()) != -1) {
			fos.write(i);
		}
		fos.flush();
		fos.close();
	}
	
	/**
	 * 
	 * @deprecated
	 * @return false if an error happened, else true.
	 */
	@SuppressWarnings("unused")
	private boolean loadStrings()
	{
		try
		{
			strings.playerExtendedChannel = ChatColor.GREEN + "[WirelessRedstone] You just extended a channel!";
			strings.playerCreatedChannel = ChatColor.GREEN + "[WirelessRedstone] You just created a new channel!";
			strings.playerCannotCreateChannel = ChatColor.RED + "[WirelessRedstone] You cannot create a channel";
			strings.playerCannotCreateReceiverOnBlock = ChatColor.RED + "[WirelessRedstone] You cannot create a wireless receiver on this block !";
			strings.playerDoesntHavePermission = ChatColor.RED + "You don't have the permissions to use this command.";
			strings.playerDoesntHaveAccessToChannel = ChatColor.RED + "[WirelessRedstone] You don't have access to this channel.";
			strings.playerCannotCreateSign = ChatColor.RED + "[WirelessRedstone] You don't have the permission to create this sign!";
			strings.signDestroyed = ChatColor.GREEN + "[WirelessRedstone] Succesfully removed this sign !";
			strings.channelRemovedCauseNoSign = ChatColor.GREEN + "[WirelessRedstone] Channel removed, no more signs in the worlds.";
			strings.channelNameContainsInvalidCaracters = strings.playerCannotCreateChannel + " : Name contains invalid caracters : a dot '.'!";
			strings.channelDoesNotExist = ChatColor.RED + "[WirelessRedstone] This channel doesn't exist!";
			strings.tooFewArguments = ChatColor.RED + "[WirelessRedstone] Too few arguments !";
			strings.pageEmpty = ChatColor.RED + "[WirelessRedstone] There are no items on this list!";
			strings.playerCannotDestroySign = ChatColor.RED + "[WirelessRedstone] You are not allowed to destroy this sign!";
			strings.ownersOfTheChannelAre = "The owners of this channel are : ";
			strings.thisChannelContains = "This channel contains :";
			strings.pageNumberInferiorToZero = ChatColor.RED + "[WirelessRedstone] Page number cannot be inferior to 0!";
			strings.channelLocked = ChatColor.GREEN + "[WirelessRedstone] Channel locked !";
			strings.channelUnlocked = ChatColor.GREEN + "[WirelessRedstone] Channel unlocked !";
			strings.commandForNextPage = ChatColor.GREEN + "\n/wr list pagenumber for next page!";
			strings.forMoreInfosPerformWRInfo = ChatColor.AQUA + "For more informations about a channel, perform /wr info <channel>";
			//Here we load the tags
			strings.tagsTransmitter.add("[transmitter]");
			strings.tagsTransmitter.add("[wrt]");
			strings.tagsReceiver.add("[receiver]");
			strings.tagsReceiver.add("[wrr]");
			strings.tagsScreen.add("[screen]");
			strings.tagsScreen.add("[wrs]");
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

	public WirelessStrings getStrings()
	{
		return strings;
	}
}
