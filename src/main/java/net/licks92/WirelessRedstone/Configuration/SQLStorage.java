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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.Location;

public class SQLStorage implements IWirelessStorageConfiguration
{	
	private File sqlFile;
	private Connection connection;
	
	private final String sql_iswallsign = "iswallsign";
	private final String sql_direction = "direction";
	private final String sql_channelname = "name";
	private final String sql_channellocked = "locked";
	private final String sql_channelowners = "owners";
	private final String sql_signowner = "signowner";
	private final String sql_signworld = "world";
	private final String sql_signx = "x";
	private final String sql_signy = "y";
	private final String sql_signz = "z";
	private final String sql_signtype = "signtype";
	
	File channelFolder;
	
	public SQLStorage(File r_channelFolder)
	{
		channelFolder = r_channelFolder;
		
		sqlFile = new File(channelFolder.getAbsolutePath() + File.separator + "channels.db");
	}
	
	public boolean init()
	{
		WirelessRedstone.getStackableLogger().debug("Establishing connection to database...");
		
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + sqlFile.getAbsolutePath());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		WirelessRedstone.getStackableLogger().debug("Connection to SQL Database has been established!");
		
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
				if(rs.getString("name").equals(name))
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
			
		WirelessRedstone.getStackableLogger().info("Channels saved in archive : " + zipName);
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
				channels.add(rs.getString("name"));
			}
			rs.close(); //Always close the ResultSet
			
			for(String channelName : channels)
			{
				if(channelName.equals(r_channelName))
				{
					//Get the ResultSet from the table we want
					ResultSet rs2 = statement.executeQuery("SELECT * FROM " + channelName);
					if(rs2.getString("name") == null) //If the table is empty
					{
						statement.executeUpdate("DROP TABLE " + channelName);
						rs2.close();
						statement.close();
						return new WirelessChannel();
					}
					
					//Create an empty WirelessChannel
					WirelessChannel channel = new WirelessChannel();
					
					//Set the Id, the name, and the locked variable
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
						owners.add(rs2.getString(sql_channelowners));
					}
					channel.setOwners(owners);
					
					//Set the wireless signs
					ArrayList<WirelessReceiver> receivers = new ArrayList<WirelessReceiver>();
					ArrayList<WirelessTransmitter> transmitters = new ArrayList<WirelessTransmitter>();
					ArrayList<WirelessScreen> screens = new ArrayList<WirelessScreen>();
					while(rs2.next())
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
					}
					channel.setReceivers(receivers);
					channel.setTransmitters(transmitters);
					channel.setScreens(screens);
					
					//Done. Return channel
					rs2.close();
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

	
	public boolean close()
	{
		try {
			//Close
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		WirelessRedstone.getStackableLogger().debug("Connection to SQL Database has been successfully closed!");
		return true;
	}

	@Override
	public void createWirelessChannel(String channelName, WirelessChannel channel)
	{
		if(!sqlTableExists(channelName)) //Create the channel
		{
			//Get the type of the sign that has been created
			String signtype;
			IWirelessPoint wirelesspoint;
			int iswallsign;
			if(!channel.getReceivers().isEmpty())
			{
				signtype = "receiver";
				wirelesspoint = channel.getReceivers().get(0);
			}
			else if(!channel.getTransmitters().isEmpty())
			{
				signtype = "transmitter";
				wirelesspoint = channel.getTransmitters().get(0);
			}
			else if(!channel.getScreens().isEmpty())
			{
				signtype = "screen";
				wirelesspoint = channel.getScreens().get(0);
			}
			else
			{
				WirelessRedstone.getStackableLogger().severe("Channel created with no IWirelessPoint in, stopping the creation of the channel.");
				removeWirelessChannel(channelName);
				return;
			}
			if(wirelesspoint.getisWallSign())
			{
				iswallsign = 1;
			}
			else
			{
				iswallsign = 0;
			}
			
			try
			{
				//Create the table
				Statement statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE " + channelName + " ( "
					
					//First columns are for the channel
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
				statement.executeUpdate("INSERT INTO " + channelName + " (" + sql_channelname + "," + sql_channellocked + "," + sql_channelowners + ") "
					+ "VALUES ('" + channel.getName() + "'," //name
					+ "0" + "," //locked
					+ "'" + channel.getOwners().get(0)
					+ "')"); //The first owner
			
				//Create the sign that caused the channel to create
				statement.executeUpdate("INSERT INTO " + channelName + " (" + sql_signtype + "," + sql_signx + "," + sql_signy + "," + sql_signz + "," + sql_direction + "," + sql_signowner + "," + sql_signworld + "," + sql_iswallsign + ") "
					+ "VALUES ('" + signtype + "'," //Type of the wireless point
					+ wirelesspoint.getX() + ","
					+ wirelesspoint.getY() + ","
					+ wirelesspoint.getZ() + ","
					+ wirelesspoint.getDirection() + ","
					+ "'" + wirelesspoint.getOwner() + "',"
					+ "'" + wirelesspoint.getWorld() + "',"
					+ iswallsign
					+ " ) ");
			
				//Finished!
				statement.close();
				return;
			}
			catch(SQLException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void removeWirelessChannel(String channelName)
	{
		try
		{
			if(!sqlTableExists(channelName))
				return;
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE " + channelName);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}

	@Override
	public Collection<WirelessChannel> getAllChannels()
	{
		try
		{
			Statement statement = connection.createStatement();
			ArrayList<WirelessChannel> channels = new ArrayList<WirelessChannel>();
			
			ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
			ArrayList<String> channelNames = new ArrayList<String>();
			while(rs.next())
			{
				channelNames.add(rs.getString("name"));
			}
			rs.close(); //Always close the ResultSet
			
			for(String channelName : channelNames)
			{
				//Get the ResultSet from the table we want
				ResultSet rs2 = statement.executeQuery("SELECT * FROM " + channelName);
				if(rs2.getString("name") == null) //If the table is empty
				{
					statement.executeUpdate("DROP TABLE " + channelName);
					break;
				}
				
				//Create an empty WirelessChannel
				WirelessChannel channel = new WirelessChannel();
				
				//Set the Id, the name, and the locked variable
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
					owners.add(rs2.getString(sql_channelowners));
				}
				channel.setOwners(owners);
				
				//Set the wireless signs
				ArrayList<WirelessReceiver> receivers = new ArrayList<WirelessReceiver>();
				ArrayList<WirelessTransmitter> transmitters = new ArrayList<WirelessTransmitter>();
				ArrayList<WirelessScreen> screens = new ArrayList<WirelessScreen>();
				while(rs2.next())
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
				}
				channel.setReceivers(receivers);
				channel.setTransmitters(transmitters);
				channel.setScreens(screens);
				
				rs2.close();
				channels.add(channel);
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
	public void createWirelessPoint(String channelName, IWirelessPoint point)
	{
		if(!sqlTableExists(channelName))
		{
			WirelessRedstone.getStackableLogger().severe("Could not create this wireless point in the channel " + channelName + ", it does not exist!");
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
			statement.executeUpdate("INSERT INTO " + channelName + " (" + sql_signtype + "," + sql_signx + "," + sql_signy + "," + sql_signz + "," + sql_direction + "," + sql_signowner + "," + sql_signworld + "," + sql_iswallsign + ") "
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
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		return;
	}

	@Override
	public void updateChannel(String channelName, WirelessChannel channel)
	{
		return;
	}

	@Override
	public boolean removeWirelessReceiver(String channelName, Location loc)
	{
		return false;
	}

	@Override
	public boolean removeWirelessTransmitter(String channelName, Location loc)
	{
		return false;
	}

	@Override
	public boolean removeWirelessScreen(String channelName, Location loc)
	{
		return false;
	}
}
