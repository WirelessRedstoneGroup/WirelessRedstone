package net.licks92.WirelessRedstone.Configuration;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

import net.licks92.WirelessRedstone.WirelessRedstone;

public class LanguageConfiguration
{
	private static File folder;
	private static WirelessRedstone plugin;
	public FileConfiguration config;
	
	public LanguageConfiguration(WirelessRedstone r_plugin, boolean useSystemLanguage, String language)
	{
		plugin = r_plugin;
		//folder = new File(plugin.getDataFolder().toString() + "languages");
		
		//If useSystemLanguage is true, then the language argument is not used
	}
}
