package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.Bukkit;

import lib.PatPeter.SQLibrary.SQLite;

public class SQLConfiguration
{
	private SQLite db;
	
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
	
	File channelFolder;
	
	WirelessRedstone plugin;
	
	public SQLConfiguration(WirelessRedstone plugin, File channelFolder)
	{
		this.plugin = plugin;
		this.channelFolder = channelFolder;
	}
	
	public boolean init()
	{
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
	
	private boolean sqlTableExists(String name)
	{
		ResultSet rs = db.query("SELECT name FROM sqlite_master WHERE type = \"table\"");
		try
		{
			do
			{
				if(rs.getString("name").equals(name))
				{
					return true;
				}
			}while(rs.next());

			rs.close();
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
		try
		{
			ResultSet rs = db.query("SELECT name FROM sqlite_master WHERE type = \"table\"");
			while(rs.next())
			{
				if(rs.getString("name").equals(channelName))
				{
					rs.close(); //Always close the ResultSet
					
					//Get the ResultSet from the table we want
					ResultSet rs2 = db.query("SELECT * FROM " + channelName);
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
					if(rs2.getInt(sql_channellocked) == 1)
						channel.setLocked(true);
					else if(rs2.getInt(sql_channellocked) == 0)
						channel.setLocked(false);
					else
						channel.setLocked(false);
					
					//Set the owners
					ArrayList<String> owners = new ArrayList<String>();
					rs2.first();
					do
					{
						owners.add(rs2.getString(sql_channelowners));
					}while(rs2.next());
					channel.setOwners(owners);
					
					//Set the wireless signs
					ArrayList<WirelessReceiver> receivers = new ArrayList<WirelessReceiver>();
					ArrayList<WirelessTransmitter> transmitters = new ArrayList<WirelessTransmitter>();
					ArrayList<WirelessScreen> screens = new ArrayList<WirelessScreen>();
					rs2.first();
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
					}while(rs2.next());
					channel.setReceivers(receivers);
					channel.setTransmitters(transmitters);
					channel.setScreens(screens);
					
					//Done. Return channel
					rs2.close();
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
	
	public void setWirelessChannel(String channelName, WirelessChannel channel)
	{
		if(channel == null)
		{
			db.query("DROP TABLE " + channelName);
			return;
		}
		if(!sqlTableExists(channelName))
		{
			//Get the type of the sign that has been created
			String signtype;
			IWirelessPoint wirelesspoint;
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
				plugin.WireBox.removeChannel(channelName);
				return;
			}
			
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
			db.query("INSERT INTO " + channelName + " (" + sql_channelname + "," + sql_channelid + "," + sql_channellocked + "," + sql_channelowners + ") "
					+ "VALUES ('" + channel.getName() + "'," //name
					+ channel.getId() + "," //id
					+ "0" + "," //locked
					+ "'" + channel.getOwners().get(0)
					+ "')"); //The first owner
			
			//Create the sign that caused the channel to create
			db.query("INSERT INTO " + channelName + " (" + sql_signtype + "," + sql_signx + "," + sql_signy + "," + sql_signz + "," + sql_direction + "," + sql_signowner + "," + sql_signworld + "," + sql_iswallsign + ") "
					+ "VALUES ('" + signtype + "'," //Type of the wireless point
					+ wirelesspoint.getX() + ","
					+ wirelesspoint.getY() + ","
					+ wirelesspoint.getZ() + ","
					+ wirelesspoint.getDirection() + ","
					+ "'" + wirelesspoint.getOwner() + "',"
					+ "'" + wirelesspoint.getWorld() + "',"
					+ wirelesspoint.getisWallSign()
					+ " ) ");
			
			//Finished!
			return;
		}
	}
	
	public boolean close()
	{
		if(WirelessRedstone.config.getSQLUsage())
		{
			db.close();
			WirelessRedstone.getStackableLogger().debug("Connection to SQL Database has been successfully closed!");
			return true;
		}
		return true;
	}

}
