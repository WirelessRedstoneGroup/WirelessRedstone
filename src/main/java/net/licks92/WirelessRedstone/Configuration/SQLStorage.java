package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class SQLStorage implements IWirelessStorageConfiguration
{	
	private File sqlFile;
	private Connection connection;
	
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
	
	public SQLStorage(File r_channelFolder, WirelessRedstone r_plugin)
	{
		channelFolder = r_channelFolder;
		plugin = r_plugin;
		
		sqlFile = new File(channelFolder.getAbsolutePath() + File.separator + "channels.db");
	}
	
	public boolean init()
	{
		return init(true);
	}
	
	public boolean init(boolean allowConvert)
	{
		WirelessRedstone.getWRLogger().debug("Establishing connection to database...");
		
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + sqlFile.getAbsolutePath());
		} catch (SQLException e) {
			WirelessRedstone.getWRLogger().severe("Something wrong happened during the connection to the database! Error log :");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			WirelessRedstone.getWRLogger().severe("Class org.sqlite.JDBC not found, cannot connect to database. Error log :");
			e.printStackTrace();
		}
		
		WirelessRedstone.getWRLogger().debug("Connection to SQL Database has been established!");
		
		if(canConvert() && allowConvert)
		{
			WirelessRedstone.getWRLogger().info("WirelessRedstone found one or many channels in .yml files.");
			WirelessRedstone.getWRLogger().info("Beginning data transfer... (from Yaml files to SQL Database)");
			if(convert())
			{
				WirelessRedstone.getWRLogger().info("Done ! All the channels are now stored in the SQL Database.");
			}
		}
		
		return true;
	}
	
	public String getNormalName(String asciiName)
	{
		for(char character : WirelessRedstone.config.badCharacters)
		{
			String ascii = "" + (int)character;
			String code = "_char_" + ascii + "_";
			if(asciiName.contains(code))
			{
				asciiName = asciiName.replace(code, String.valueOf(character));
			}
		}
		return asciiName;
	}
	
	public String getAsciiName(String normalName)
	{
		for(char character : WirelessRedstone.config.badCharacters)
		{
			if(normalName.contains(String.valueOf(character)))
			{
				String ascii = "" + (int)character;
				String code = "_char_" + ascii + "_";
				normalName = normalName.replace(String.valueOf(character), code);
			}
		}
		return normalName;
	}
	
	public boolean close()
	{
		try {
			//Close
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		WirelessRedstone.getWRLogger().debug("Connection to SQL Database has been successfully closed!");
		return true;
	}
	
	public boolean canConvert()
	{
		for(File file : channelFolder.listFiles())
		{
			if(file.getName().contains(".yml"))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean convert()
	{
		WirelessRedstone.getWRLogger().info("Backuping the channels/ folder before transfer.");
		if(!backupData())
		{
			WirelessRedstone.getWRLogger().severe("Backup failed ! Data transfer abort...");
		}
		else
		{
			WirelessRedstone.getWRLogger().info("Backup done. Starting data transfer...");
			
			YamlStorage yaml = new YamlStorage(channelFolder, plugin);
			yaml.init(false);
			for(WirelessChannel channel : yaml.getAllChannels())
			{
				createWirelessChannel(channel.getName(), channel);
			}
			yaml.close();
			for(File f : channelFolder.listFiles())
			{
				if(f.getName().contains(".yml"))
				{
					f.delete();
				}
			}
		}
		return true;
	}
	
	private boolean sqlTableExists(String name)
	{
		try
		{
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
			
			while(rs.next())
			{
				if(getNormalName(rs.getString("name")).equals(name))
				{
					rs.close();
					statement.close();
					return true;
				}
			}

			rs.close();
			statement.close();
			return false;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean wipeData()
	{
		//Backup before wiping
		backupData();
		try
		{
			//Get the names of all the tables
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
			ArrayList<String> tables = new ArrayList<String>();
			while(rs.next())
			{
				tables.add(rs.getString("name"));
			}
			rs.close();
			statement.close();
			
			//Erase all the tables
			for(String channelName : tables)
			{
				removeWirelessChannel(channelName);
			}
			
			return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean backupData()
	{
		try
		{
			String zipName = "WRBackup "
					+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
					+ Calendar.getInstance().get(Calendar.MONTH)
					+ Calendar.getInstance().get(Calendar.YEAR) + "-"
					+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
					+ Calendar.getInstance().get(Calendar.MINUTE)
					+ Calendar.getInstance().get(Calendar.SECOND);
			FileOutputStream fos = new FileOutputStream((channelFolder.getCanonicalPath().split(channelFolder.getName())[0]) + zipName + ".zip");
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (File file : channelFolder.listFiles())
			{
				if (!file.isDirectory() && file.getName().contains(".db"))
				{
					FileInputStream fis = new FileInputStream(file);
					
					ZipEntry zipEntry = new ZipEntry(file.getName());
					zos.putNextEntry(zipEntry);
					
					byte[] bytes = new byte[1024];
					int length;
					
					while ((length = fis.read(bytes)) >= 0)
					{
						zos.write(bytes, 0, length);
					}

					zos.closeEntry();
					fis.close();
				}
			}

			zos.close();
			fos.close();
			
		WirelessRedstone.getWRLogger().info("Channels saved in archive : " + zipName);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public WirelessChannel getWirelessChannel(String r_channelName)
	{
		try
		{
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
			ArrayList<String> channels = new ArrayList<String>();
			
			while(rs.next())
			{
				channels.add(getNormalName(rs.getString("name")));
				WirelessRedstone.getWRLogger().debug("Added" + getNormalName(rs.getString("name")) + "to the list of channels.");
			}
			rs.close(); //Always close the ResultSet
			
			for(String channelName : channels)
			{
				if(channelName.equals(r_channelName))
				{
					//Get the ResultSet from the table we want
					ResultSet rs2 = statement.executeQuery("SELECT * FROM " + getAsciiName(channelName));
					if(rs2.getString("name") == null) //If the table is empty
					{
						statement.executeUpdate("DROP TABLE " + getAsciiName(channelName));
						rs2.close();
						statement.close();
						return new WirelessChannel();
					}
					
					//Create an empty WirelessChannel
					WirelessChannel channel = new WirelessChannel();
					
					//Set the Id, the name, and the locked variable
					channel.setId(rs2.getInt(sql_channelid));
					channel.setName(rs2.getString(sql_channelname));
					if(rs2.getInt(sql_channellocked) == 1)
						channel.setLocked(true);
					else if(rs2.getInt(sql_channellocked) == 0)
						channel.setLocked(false);
					else
						channel.setLocked(false);
					
					//Set the owners
					ArrayList<String> owners = new ArrayList<String>();
					while(rs2.next())
					{
						if(rs2.getString(sql_channelowners) != null)
							owners.add(rs2.getString(sql_channelowners));
					}
					channel.setOwners(owners);
					rs2.close();
					
					//Because a SQLite ResultSet is TYPE_FORWARD only, we have to create a third ResultSet and close the second
					ResultSet rs3 = statement.executeQuery("SELECT * FROM " + getAsciiName(channelName));
					
					//Set the wireless signs
					ArrayList<WirelessReceiver> receivers = new ArrayList<WirelessReceiver>();
					ArrayList<WirelessTransmitter> transmitters = new ArrayList<WirelessTransmitter>();
					ArrayList<WirelessScreen> screens = new ArrayList<WirelessScreen>();
					rs3.next();//Because first row does not contain a wireless sign
					while(rs3.next())
					{
						if(rs3.getString(sql_signtype).equals("receiver"))
						{
							WirelessReceiver receiver = new WirelessReceiver();
							receiver.setDirection(rs3.getInt(sql_direction));
							receiver.setisWallSign(rs3.getBoolean(sql_iswallsign));
							receiver.setOwner(rs3.getString(sql_signowner));
							receiver.setWorld(rs3.getString(sql_signworld));
							receiver.setX(rs3.getInt(sql_signx));
							receiver.setY(rs3.getInt(sql_signy));
							receiver.setZ(rs3.getInt(sql_signz));
							receivers.add(receiver);
						}
						if(rs3.getString(sql_signtype).equals("transmitter"))
						{
							WirelessTransmitter transmitter = new WirelessTransmitter();
							transmitter.setDirection(rs3.getInt(sql_direction));
							transmitter.setisWallSign(rs3.getBoolean(sql_iswallsign));
							transmitter.setOwner(rs3.getString(sql_signowner));
							transmitter.setWorld(rs3.getString(sql_signworld));
							transmitter.setX(rs3.getInt(sql_signx));
							transmitter.setY(rs3.getInt(sql_signy));
							transmitter.setZ(rs3.getInt(sql_signz));
							transmitters.add(transmitter);
						}
						if(rs3.getString(sql_signtype).equals("screen"))
						{
							WirelessScreen screen = new WirelessScreen();
							screen.setDirection(rs3.getInt(sql_direction));
							screen.setisWallSign(rs3.getBoolean(sql_iswallsign));
							screen.setOwner(rs3.getString(sql_signowner));
							screen.setWorld(rs3.getString(sql_signworld));
							screen.setX(rs3.getInt(sql_signx));
							screen.setY(rs3.getInt(sql_signy));
							screen.setZ(rs3.getInt(sql_signz));
							screens.add(screen);
						}
					}
					channel.setReceivers(receivers);
					channel.setTransmitters(transmitters);
					channel.setScreens(screens);
					
					//Done. Return channel
					rs3.close();
					statement.close();
					return channel;
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null; //Channel not found
	}

	@Override
	public boolean createWirelessChannel(String channelName, WirelessChannel channel)
	{
		if(!sqlTableExists(channelName)) //Check if channel already exists
		{
			//Get the type of the sign that has been created
			if(!channel.getReceivers().isEmpty() && !channel.getTransmitters().isEmpty() && !channel.getScreens().isEmpty())
			{
				WirelessRedstone.getWRLogger().severe("Channel created with no IWirelessPoint in, stopping the creation of the channel.");
				return false;
			}
			
			try
			{
				//Create the table
				Statement statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE " + getAsciiName(channelName) + " ( "
					
					//First columns are for the channel
					+ sql_channelid + " int,"
					+ sql_channelname + " char(64),"
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
				statement.executeUpdate("INSERT INTO " + getAsciiName(channelName) + " (" + sql_channelid + "," + sql_channelname + "," + sql_channellocked + "," + sql_channelowners + ") "
					+ "VALUES ("
					+ channel.getId() + ","
					+ "'" + channel.getName() + "'," //name
					+ "0" + "," //locked
					+ "'" + channel.getOwners().get(0)
					+ "')"); //The first owner
				//Finished this part
				statement.close();
				
				//Create the wireless points
				ArrayList<IWirelessPoint> points = new ArrayList<IWirelessPoint>();
				points.add((IWirelessPoint) channel.getReceivers());
				points.add((IWirelessPoint) channel.getTransmitters());
				points.add((IWirelessPoint) channel.getScreens());
				for(IWirelessPoint ipoint : points)
				{
					createWirelessPoint(channelName, ipoint);
				}
				plugin.WireBox.UpdateCache();
				return true;
			}
			catch(SQLException ex)
			{
				ex.printStackTrace();
			}
		}
		WirelessRedstone.getWRLogger().debug("Tried to create a channel that already exists in the database");
		return false;
	}
	
	public boolean renameWirelessChannel(String channelName, String newChannelName)
	{
		WirelessChannel channel = getWirelessChannel(channelName);
		
		List<IWirelessPoint> signs = new ArrayList<IWirelessPoint>();
		
		signs.addAll(channel.getReceivers());
		signs.addAll(channel.getTransmitters());
		signs.addAll(channel.getScreens());
		
		for(IWirelessPoint sign : signs)
		{
			Location loc = new Location(Bukkit.getWorld(sign.getWorld()), sign.getX(), sign.getY(), sign.getZ());
			Sign signBlock = (Sign) loc.getBlock();
			signBlock.setLine(1, newChannelName);
		}
		
		try
		{
			Statement statement = connection.createStatement();
			
			//Remove the old channel in the config
			statement.executeUpdate("DROP TABLE " + getAsciiName(channelName));
			
			statement.close();
			
			//Set a new channel - HAVE TO FIND A BETTER WAY THAN JUST REMOVING THE TABLE AND CREATE AN OTHER
			createWirelessChannel(newChannelName, channel);
		}
		
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void removeWirelessChannel(String channelName)
	{
		try
		{
			plugin.WireBox.removeSigns(getWirelessChannel(channelName));
			if(!sqlTableExists(channelName))
				return;
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE " + getAsciiName(channelName));
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			plugin.WireBox.UpdateCache();
		}
		return;
	}

	@Override
	public Collection<WirelessChannel> getAllChannels()
	{
		Statement statement;
		try
		{
			statement = connection.createStatement();
			ArrayList<WirelessChannel> channels = new ArrayList<WirelessChannel>();
			
			ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
			ArrayList<String> channelNames = new ArrayList<String>();
			while(rs.next())
			{
				channelNames.add(getNormalName(rs.getString("name")));
			}
			rs.close();
			statement.close();
			
			for(String channelName : channelNames)
			{
				channels.add(getWirelessChannel(channelName));
			}
			return channels;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null; //Channel not found
	}

	@Override
	public boolean createWirelessPoint(String channelName, IWirelessPoint point)
	{
		if(!sqlTableExists(channelName))
		{
			WirelessRedstone.getWRLogger().severe("Could not create this wireless point in the channel " + channelName + ", it does not exist!");
		}
		
		int iswallsign;
		String signtype;
		
		if(point instanceof WirelessReceiver)
		{
			signtype = "receiver";
		}
		else if(point instanceof WirelessTransmitter)
		{
			signtype = "transmitter";
		}
		else //if WirelessScreen
		{
			signtype = "screen";
		}
		
		if(point.getisWallSign())
		{
			iswallsign = 1;
		}
		else
		{
			iswallsign = 0;
		}
		
		try
		{
			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO " + getAsciiName(channelName) + " (" + sql_signtype + "," + sql_signx + "," + sql_signy + "," + sql_signz + "," + sql_direction + "," + sql_signowner + "," + sql_signworld + "," + sql_iswallsign + ") "
					+ "VALUES ('" + signtype + "'," //Type of the wireless point
					+ point.getX() + ","
					+ point.getY() + ","
					+ point.getZ() + ","
					+ point.getDirection() + ","
					+ "'" + point.getOwner() + "',"
					+ "'" + point.getWorld() + "',"
					+ iswallsign
					+ " ) ");
			statement.close();
			plugin.WireBox.UpdateCache();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		return true;
	}

	@Override
	public void updateChannel(String channelName, WirelessChannel channel)
	{
		try
		{
			int locked = (channel.isLocked()) ? 1 : 0;
			Statement statement = connection.createStatement();
			
			//Update name and lock status
			statement.executeUpdate("UPDATE " + getAsciiName(channelName)
					+ " SET "
					+ sql_channelname + "='" + channel.getName() + "' "
					+ sql_channellocked + "=" + locked + " "
					+ "WHERE " + sql_channelid + "=" + channel.getId());
			
			//Then update the owners
			statement.executeUpdate("ALTER TABLE " + getAsciiName(channelName) + " DROP COLUMN " + sql_channelowners);
			statement.executeUpdate("ALTER TABLE " + getAsciiName(channelName) + " ADD COLUMN " + sql_channelowners);
			for(String owner : channel.getOwners())
			{
				statement.executeUpdate("INSERT INTO " + getAsciiName(channelName) + " (" + sql_channelowners + ") VALUES " + owner);
			}
			statement.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}

	@Override
	public boolean removeWirelessReceiver(String channelName, Location loc)
	{
		WirelessChannel channel = getWirelessChannel(channelName);
		if(channel!=null)
		{
			channel.removeReceiverAt(loc);
			return removeWirelessPoint(channelName, loc);
		}
		else
			return false;
	}

	@Override
	public boolean removeWirelessTransmitter(String channelName, Location loc)
	{
		WirelessChannel channel = getWirelessChannel(channelName);
		if(channel!=null)
		{
			channel.removeTransmitterAt(loc);
			return removeWirelessPoint(channelName, loc);
		}
		else
			return false;
	}

	@Override
	public boolean removeWirelessScreen(String channelName, Location loc)
	{
		WirelessChannel channel = getWirelessChannel(channelName);
		if(channel!=null)
		{
			channel.removeScreenAt(loc);
			return removeWirelessPoint(channelName, loc);
		}
		else
			return false;
	}
	
	private boolean removeWirelessPoint(String channelName, Location loc)
	{
		try
		{
			Statement statement = connection.createStatement();
			String sql = "DELETE FROM " + getAsciiName(channelName) + " WHERE "
					+ sql_signx + "=" + loc.getBlockX() + " AND "
					+ sql_signy + "=" + loc.getBlockY() + " AND "
					+ sql_signz + "=" + loc.getBlockZ() + " AND "
					+ sql_signworld + "='" + loc.getWorld().getName() + "'";
			statement.executeUpdate(sql);
			WirelessRedstone.getWRLogger().debug("Statement to delete wireless sign : " + sql);
			statement.close();
			plugin.WireBox.UpdateCache();
		} catch (SQLException ex) {
			WirelessRedstone.getWRLogger().debug(ex.getMessage());
			return false;
		}
		return true;
	}
}
