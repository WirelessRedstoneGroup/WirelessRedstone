package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import lib.PatPeter.SQLibrary.SQLite;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class NewWirelessConfiguration
{
	private static final String CHANNEL_FOLDER = "/channels";
	
	private final String sql_iswallsign = "iswallsign";
	private final String sql_direction = "direction";
	private final String sql_channelid = "id";
	private final String sql_channelname = "name";
	private final String sql_channellocked = "locked";
	private final String sql_channelowners = "owners";
	private final String sql_signowner = "signowner";
	private final String sql_signworld = "world";
	private final String sql_signx = "x";
	private final String sql_signy = "y";
	private final String sql_signz = "z";
	private final String sql_signtype = "signtype";
	
	private File channelFolder;
	private WirelessRedstone plugin;
	private boolean saveInSQLDatabase;

	private SQLite db;
	
	private FileConfiguration getConfig()
	{
		return plugin.getConfig();
	}
	
	public NewWirelessConfiguration(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		
		//Loading and saving
		getConfig().options().copyHeader(true);
		getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		reloadConfig();
	}
	
	public void init()
	{
		//Create the channel folder
		channelFolder = new File(plugin.getDataFolder(), CHANNEL_FOLDER);
		channelFolder.mkdir();
				
		//Storage system => SQL or config files
		if(getSQLUsage())
		{
			initSQLSave();
		}
		else
		{
			initTextSave();
		}

		//Language selection
		//To implement

		//Show debug informations about the config...
		if(getDebugMode())
			WirelessRedstone.getStackableLogger().debug("Channels stored in " + channelFolder.getAbsolutePath());
	}

	private boolean initTextSave()
	{
		saveInSQLDatabase = false;
		
		//Initialize the serialization
		ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
		ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
		ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
		ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
		
		//Try to know if the config is an OldConfiguration, and convert it
		File oldConfig = new File(plugin.getDataFolder(), "settings.yml");
		if(oldConfig.exists())
		{
			convertOldConfigToNew(oldConfig);
		}
		
		return true;
	}
	
	private boolean initSQLSave()
	{
		saveInSQLDatabase = true;
		
		db = new SQLite(Bukkit.getLogger(), "[WirelessRedstone]", "channels", channelFolder.getAbsolutePath());
		
		try
		{
			db.open();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		WirelessRedstone.getStackableLogger().debug("Connection to SQL Database has been established!");
		
		return true;
	}
	
	public boolean close()
	{
		if(saveInSQLDatabase)
		{
			db.close();
			WirelessRedstone.getStackableLogger().debug("Connection to SQL Database has been successfully closed!");
			return true;
		}
		return true;
	}
	
	public void convertOldConfigToNew(File file)
	{
		OldWirelessConfiguration oldConfiguration = new OldWirelessConfiguration(plugin.getDataFolder());
		getConfig().set("WirelessChannels", oldConfiguration.get("WirelessChannels"));
		
		WirelessRedstone.getStackableLogger().info("Old text configuration has been converted to new text configuration");
		
		file.delete();
	}

	public void reloadConfig()
	{
		plugin.reloadConfig();
	}

	public Level getLogLevel()
	{
		return Level.parse(getConfig().getString("LogLevel"));
	}

	public boolean getVaultUsage()
	{
		return getConfig().getBoolean("UseVault");
	}
	
	private boolean getSQLUsage()
	{
		return getConfig().getBoolean("UseSQL");
	}

	public void save()
	{
		plugin.saveConfig();
	}

	public boolean isCancelChunkUnloads()
	{
		return getConfig().getBoolean("cancelChunkUnloads", true);
	}

	public int getChunkUnloadRange()
	{
		return getConfig().getInt("cancelChunkUnloadRange", 4);
	}
	
	public boolean getSignDrop()
	{
		return getConfig().getBoolean("DropSignWhenBroken", true);
	}
	
	public boolean getDebugMode()
	{
		return getConfig().getBoolean("DebugMode", false);
	}
	
	private boolean sqlTableExists(String name)
	{
		ResultSet rs = db.query("SELECT name FROM sqlite_master WHERE type = \"table\"");
		try
		{
			rs.first();
			do
			{
				if(rs.getString("name").equals(name))
				{
					return true;
				}
			}while(rs.next());
			
			return false;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public WirelessChannel getWirelessChannel(String channelName)
	{
		if(saveInSQLDatabase)
		{
			try
			{
				ResultSet rs = db.query("SELECT name FROM sqlite_master WHERE type = \"table\"");
				while(rs.next())
				{
					if(rs.getString("name").equals(channelName))
					{
						//Get the ResultSet from the table we want
						ResultSet rs2 = db.query("SELECT * FROM " + rs.getString(channelName));
						if(!rs2.first()) //If the table is empty
						{
							db.query("DROP TABLE " + channelName);
							return new WirelessChannel();
						}
						
						//Create an empty WirelessChannel
						WirelessChannel channel = new WirelessChannel();
						
						//Set the Id, the name, and the locked variable
						rs2.first();
						channel.setId(rs2.getInt(sql_channelid));
						channel.setName(rs2.getString(sql_channelname));
						if(rs.getInt(sql_channellocked) == 1)
							channel.setLocked(true);
						else if(rs.getInt(sql_channellocked) == 0)
							channel.setLocked(false);
						else
							channel.setLocked(false);
						
						//Set the owners
						ArrayList<String> owners = new ArrayList<String>();
						rs.first();
						do
						{
							owners.add(rs2.getString(sql_channelowners));
						}while(rs.next());
						channel.setOwners(owners);
						
						//Set the wireless signs
						ArrayList<WirelessReceiver> receivers = new ArrayList<WirelessReceiver>();
						ArrayList<WirelessTransmitter> transmitters = new ArrayList<WirelessTransmitter>();
						ArrayList<WirelessScreen> screens = new ArrayList<WirelessScreen>();
						rs.first();
						do
						{
							if(rs2.getString(sql_signtype).equals("receiver"))
							{
								WirelessReceiver receiver = new WirelessReceiver();
								receiver.setDirection(rs2.getInt(sql_direction));
								receiver.setisWallSign(rs2.getBoolean(sql_iswallsign));
								receiver.setOwner(rs2.getString(sql_signowner));
								receiver.setWorld(rs2.getString(sql_signworld));
								receiver.setX(rs2.getInt(sql_signx));
								receiver.setY(rs2.getInt(sql_signy));
								receiver.setZ(rs2.getInt(sql_signz));
								receivers.add(receiver);
							}
							if(rs2.getString(sql_signtype).equals("transmitter"))
							{
								WirelessTransmitter transmitter = new WirelessTransmitter();
								transmitter.setDirection(rs2.getInt(sql_direction));
								transmitter.setisWallSign(rs2.getBoolean(sql_iswallsign));
								transmitter.setOwner(rs2.getString(sql_signowner));
								transmitter.setWorld(rs2.getString(sql_signworld));
								transmitter.setX(rs2.getInt(sql_signx));
								transmitter.setY(rs2.getInt(sql_signy));
								transmitter.setZ(rs2.getInt(sql_signz));
								transmitters.add(transmitter);
							}
							if(rs2.getString(sql_signtype).equals("screen"))
							{
								WirelessScreen screen = new WirelessScreen();
								screen.setDirection(rs2.getInt(sql_direction));
								screen.setisWallSign(rs2.getBoolean(sql_iswallsign));
								screen.setOwner(rs2.getString(sql_signowner));
								screen.setWorld(rs2.getString(sql_signworld));
								screen.setX(rs2.getInt(sql_signx));
								screen.setY(rs2.getInt(sql_signy));
								screen.setZ(rs2.getInt(sql_signz));
								screens.add(screen);
							}
						}while(rs.next());
						channel.setReceivers(receivers);
						channel.setTransmitters(transmitters);
						channel.setScreens(screens);
						
						//Done. Return channel
						return channel;
					}
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			WirelessRedstone.getStackableLogger().severe("Method getWirelessChannel : No channel with the given name (" + channelName + ") was found! Did you edit or remove the database?");
			return new WirelessChannel();
		}
		else
		{
			YamlConfiguration channelConfig = new YamlConfiguration();
			try
			{
				File channelFile = new File(channelFolder, channelName + ".yml");
				channelFile.createNewFile();
				channelConfig.load(channelFile);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
			
			Object channel = channelConfig.get(channelName);
			if(channel == null)
				return null; // channel not found
			else if(!(channel instanceof WirelessChannel))
			{
				plugin.getLogger().warning("Channel "+channelName+" does not seem to be of type WirelessChannel.");
				return null;
			}
			else
				return (WirelessChannel)channel;
		}
	}
	
	public void setWirelessChannel(String channelName, WirelessChannel channel)
	{
		if(saveInSQLDatabase)
		{
			if(channel == null)
			{
				db.query("DROP TABLE " + channelName);
				return;
			}
			if(!sqlTableExists(channelName))
			{
				//Create the table
				db.createTable("CREATE TABLE " + channelName + " ( "
						
						//First columns are for the channel
						+ sql_channelname + " char(64),"
						+ sql_channelid + " int,"
						+ sql_channellocked + " int (1),"
						+ sql_channelowners + " char(64),"
						
						//After there are the signs colums
						+ sql_signtype + " char(32),"
						+ sql_signx + " int,"
						+ sql_signy + " int,"
						+ sql_signz + " int,"
						+ sql_direction + " int,"
						+ sql_signowner + " char(64),"
						+ sql_signworld + " char(128),"
						+ sql_iswallsign + " int(1)"
						+ " ) ");
				
				//Fill the columns name, id and locked
				ResultSet rs = db.query("INSERT INTO " + channelName + " (" + sql_channelname + "," + sql_channelid + "," + sql_channellocked + "," + sql_channelowners + ") "
						+ "VALUES ('" + channel.getName() + "'," //name
						+ channel.getId() + "," //id
						+ "0" + ",'" //locked
						+ channel.getOwners().get(0) + "')"); //The first owner
				
				//Create the sign that caused the channel to create
			}
		}
		else
		{
			FileConfiguration channelConfig = new YamlConfiguration();
			try
			{
				File channelFile = new File(channelFolder, channelName + ".yml");
				channelFile.createNewFile();
				channelConfig.load(channelFile);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
			
			channelConfig.set(channelName, channel);
			
			try
			{
				channelConfig.save(new File(channelFolder, channelName + ".yml"));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public Collection<WirelessChannel> getAllChannels()
	{
		ArrayList<File> fileList = new ArrayList<File>();
		
		if(fileList.isEmpty())
			return new ArrayList<WirelessChannel>(0);
		
		List<WirelessChannel> channels = new ArrayList<WirelessChannel>();
		
		for(File f : channelFolder.listFiles())
		{
			fileList.add(f);
			FileConfiguration channelConfig = new YamlConfiguration();
			try
			{
				channelConfig.load(f);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
			Object channel = channelConfig.get(f.getName().split(".yml").toString());
			if(channel instanceof WirelessChannel)
			{
				channels.add((WirelessChannel)channel);
			}
			else
				plugin.getLogger().warning("Channel "+channel+" is not of type WirelessChannel.");
		}
		
		return channels;
	}
	
	public Object get(String path)
	{
		return getConfig().get(path);
	}
	
	public void set(String path, Object channel)
	{
		getConfig().set(path, channel);
	}
}