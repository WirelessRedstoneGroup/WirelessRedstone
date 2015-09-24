package net.licks92.WirelessRedstone.Configuration;

import com.husky.sqlite.SQLite;
import net.licks92.WirelessRedstone.Channel.*;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SQLiteStorage implements IWirelessStorageConfiguration {

	private final WirelessRedstone plugin;
	private final File channelFolder;
	private final SQLite sqLite;
	private Connection connection;

	private final String sqlIsWallSign = "iswallsign";
	private final String sqlDirection = "direction";
	private final String sqlChannelId = "id";
	private final String sqlChannelName = "name";
	private final String sqlChannelLocked = "locked";
	private final String sqlChannelOwners = "owners";
	private final String sqlSignOwner = "signowner";
	private final String sqlSignWorld = "world";
	private final String sqlSignX = "x";
	private final String sqlSignY = "y";
	private final String sqlSignZ = "z";
	private final String sqlSignType = "signtype";

	public SQLiteStorage(WirelessRedstone plugin, File channelFolder) {
		this.plugin = plugin;
		this.channelFolder = channelFolder;
		sqLite = new SQLite(plugin, channelFolder.getAbsolutePath() + File.separator + "channels.db");
	}

	@Override
	public boolean initStorage() {
		return init(true);
	}

	private boolean init(boolean allowConvert) {
		WirelessRedstone.getWRLogger().debug("Establishing connection to database...");

		try {
			connection = sqLite.openConnection();
		} catch (SQLException | ClassNotFoundException e) {
			WirelessRedstone.getWRLogger().severe("Couldn't open an connection from the database!");
			e.printStackTrace();
		}

		WirelessRedstone.getWRLogger().debug("Connection to SQLite Database has been established!");

		if (canConvert() && allowConvert) {
			WirelessRedstone.getWRLogger().info("Beginning data transfer... (Yaml -> DB)");
			if (convertFromAnotherStorage()) {
				WirelessRedstone.getWRLogger().info("Done! All the channels are now stored in the SQLite Database.");
			}
		}
		return true;
	}

	@Override
	public boolean close() {
		try {
			sqLite.closeConnection();
			return true;
		} catch (SQLException e) {
			WirelessRedstone.getWRLogger().severe("Couldn't close connection from the database!");
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean canConvert() {
		for (File file : channelFolder.listFiles()) {
			if (file.getName().contains(".yml")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean convertFromAnotherStorage() {
		WirelessRedstone.getWRLogger().info("Backingup all the channels...");

		if (!backupData("yml")) {
			WirelessRedstone.getWRLogger().severe("Backup failed! Aborting data transfer!");
		} else {
			WirelessRedstone.getWRLogger().info("Backup done. Starting data transfer...");

			YamlStorage yaml = new YamlStorage(channelFolder, plugin);
			yaml.init(false);

			for (WirelessChannel channel : yaml.getAllChannels()) {
				createWirelessChannel(channel);
			}
			yaml.close();

			for (File f : channelFolder.listFiles()) {
				if (f.getName().contains(".yml")) {
					f.delete();
				}
			}
		}
		return true;
	}

	@Override
	public boolean isChannelEmpty(WirelessChannel channel) {
		return (channel.getReceivers().size() < 1) && (channel.getTransmitters().size() < 1) && (channel.getScreens().size() < 1);
	}

	@Override
	public WirelessChannel getWirelessChannel(String channelNameInput) {
		try {
			//Start connection
			Statement statement = connection.createStatement();

			//Start query
			ResultSet rsGetChannel = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\";");
			ArrayList<String> channels = new ArrayList<>();

			//Get resultset and get the channel name (maybe get multiple names)

			while (rsGetChannel.next()) {
				channels.add(getNormalName(rsGetChannel.getString("name")));
			}

			rsGetChannel.close();

			for (String channelName : channels) {
				if (channelName.equals(channelNameInput)) {
					//Get channel info
					PreparedStatement psChannelInfo = connection.prepareStatement("SELECT * FROM ?;");
					psChannelInfo.setString(1, getDBName(channelName));
					psChannelInfo.executeUpdate();
					ResultSet rsChannelInfo = psChannelInfo.getResultSet();

					WirelessChannel channel = new WirelessChannel(rsChannelInfo.getString(sqlChannelName));

					// Set the Id, the name, and the locked variable
					channel.setId(rsChannelInfo.getInt(sqlChannelId));
					switch (rsChannelInfo.getInt(sqlChannelLocked)) {
						case 0:
							channel.setLocked(false);
							break;
						case 1:
							channel.setLocked(true);
							break;
						default:
							channel.setLocked(false);
							break;
					}

					// Set the owners
					ArrayList<String> owners = new ArrayList<>();
					while (rsChannelInfo.next()) {
						if (rsChannelInfo.getString(sqlChannelOwners) != null) owners.add(rsChannelInfo.getString(sqlChannelOwners));
					}
					channel.setOwners(owners);
					rsChannelInfo.close();

					// Because a SQLite ResultSet is TYPE_FORWARD only, we have
					// to create a third ResultSet and close the second
					PreparedStatement psSigns = connection.prepareStatement("SELECT * FROM ?;");
					psSigns.setString(1, getDBName(channelName));
					psSigns.executeUpdate();
					ResultSet rsSigns = psSigns.getResultSet();

					ArrayList<WirelessReceiver> receivers = new ArrayList<>();
					ArrayList<WirelessTransmitter> transmitters = new ArrayList<>();
					ArrayList<WirelessScreen> screens = new ArrayList<>();
					rsSigns.next(); //First row doesn't contain any sign

					while (rsSigns.next()) {
						if (rsSigns.getString(sqlSignType).equals("receiver")) {
							WirelessReceiver receiver = new WirelessReceiver();
							receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
							receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
							receiver.setOwner(rsSigns.getString(sqlSignOwner));
							receiver.setWorld(rsSigns.getString(sqlSignWorld));
							receiver.setX(rsSigns.getInt(sqlSignX));
							receiver.setY(rsSigns.getInt(sqlSignY));
							receiver.setZ(rsSigns.getInt(sqlSignZ));
							receivers.add(receiver);
						} else if (rsSigns.getString(sqlSignType).equals("receiver_inverter")) {
							WirelessReceiverInverter receiver = new WirelessReceiverInverter();
							receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
							receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
							receiver.setOwner(rsSigns.getString(sqlSignOwner));
							receiver.setWorld(rsSigns.getString(sqlSignWorld));
							receiver.setX(rsSigns.getInt(sqlSignX));
							receiver.setY(rsSigns.getInt(sqlSignY));
							receiver.setZ(rsSigns.getInt(sqlSignZ));
							receivers.add(receiver);
						} else if (rsSigns.getString(sqlSignType).equals("receiver_delayer_")) {
							String str = rsSigns.getString(sqlSignType).split("receiver_delayer_")[1];
							int delay;
							try {
								delay = Integer.parseInt(str);
							} catch (NumberFormatException ex) {
								delay = 0;
							}
							WirelessReceiverDelayer receiver = new WirelessReceiverDelayer(delay);
							receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
							receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
							receiver.setOwner(rsSigns.getString(sqlSignOwner));
							receiver.setWorld(rsSigns.getString(sqlSignWorld));
							receiver.setX(rsSigns.getInt(sqlSignX));
							receiver.setY(rsSigns.getInt(sqlSignY));
							receiver.setZ(rsSigns.getInt(sqlSignZ));
							receivers.add(receiver);
						} else if (rsSigns.getString(sqlSignType).equals("receiver_clock_")) {
							String str = rsSigns.getString(sqlSignType).split("receiver_clock_")[1];
							int delay;
							try {
								delay = Integer.parseInt(str);
							} catch (NumberFormatException ex) {
								delay = 0;
							}
							WirelessReceiverClock receiver = new WirelessReceiverClock(delay);
							receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
							receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
							receiver.setOwner(rsSigns.getString(sqlSignOwner));
							receiver.setWorld(rsSigns.getString(sqlSignWorld));
							receiver.setX(rsSigns.getInt(sqlSignX));
							receiver.setY(rsSigns.getInt(sqlSignY));
							receiver.setZ(rsSigns.getInt(sqlSignZ));
							receivers.add(receiver);
						} else if (rsSigns.getString(sqlSignType).equals("receiver_switch_")) {
							String str = rsSigns.getString(sqlSignType).split("receiver_switch_")[1];
							boolean toggle;
							try {
								toggle = Boolean.parseBoolean(str);
							} catch (Exception ex) {
								toggle = false;
							}
							WirelessReceiverSwitch receiver = new WirelessReceiverSwitch(toggle);
							receiver.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
							receiver.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
							receiver.setOwner(rsSigns.getString(sqlSignOwner));
							receiver.setWorld(rsSigns.getString(sqlSignWorld));
							receiver.setX(rsSigns.getInt(sqlSignX));
							receiver.setY(rsSigns.getInt(sqlSignY));
							receiver.setZ(rsSigns.getInt(sqlSignZ));
							receivers.add(receiver);
						} else if (rsSigns.getString(sqlSignType).equals("transmitter")) {
							WirelessTransmitter transmitter = new WirelessTransmitter();
							transmitter.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
							transmitter.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
							transmitter.setOwner(rsSigns.getString(sqlSignOwner));
							transmitter.setWorld(rsSigns.getString(sqlSignWorld));
							transmitter.setX(rsSigns.getInt(sqlSignX));
							transmitter.setY(rsSigns.getInt(sqlSignY));
							transmitter.setZ(rsSigns.getInt(sqlSignZ));
							transmitters.add(transmitter);
						} else if (rsSigns.getString(sqlSignType).equals("screen")) {
							WirelessScreen screen = new WirelessScreen();
							screen.setDirection(WirelessRedstone.WireBox.intToBlockFaceSign(rsSigns.getInt(sqlDirection)));
							screen.setIsWallSign(rsSigns.getBoolean(sqlIsWallSign));
							screen.setOwner(rsSigns.getString(sqlSignOwner));
							screen.setWorld(rsSigns.getString(sqlSignWorld));
							screen.setX(rsSigns.getInt(sqlSignX));
							screen.setY(rsSigns.getInt(sqlSignY));
							screen.setZ(rsSigns.getInt(sqlSignZ));
							screens.add(screen);
						} else {
							WirelessRedstone.getWRLogger().warning("I don't recognize '" + rsSigns.getString(sqlSignType) + "'!");
						}

						channel.setReceivers(receivers);
						channel.setTransmitters(transmitters);
						channel.setScreens(screens);

						rsSigns.close();
						statement.close();
						return channel;
					}
				}
			}
			statement.close();
			return null;
		} catch (SQLException e) {
			WirelessRedstone.getWRLogger().severe("Couldn't get WirelessChannel from DB");
			if (WirelessRedstone.config.getDebugMode()) e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getWirelessChannelName(Location loc) {
		for (WirelessChannel channel : getAllChannels()) {
			for (WirelessReceiver receiver : channel.getReceivers()) {
				if (WirelessRedstone.sameLocation(receiver.getLocation(), loc)) return channel.getName();
			}
			for (WirelessTransmitter transmitter : channel.getTransmitters()) {
				if (WirelessRedstone.sameLocation(transmitter.getLocation(), loc)) return channel.getName();
			}
			for (WirelessScreen screen : channel.getScreens()) {
				if (WirelessRedstone.sameLocation(screen.getLocation(), loc)) return channel.getName();
			}
		}
		return null;
	}

	@Override
	public IWirelessPoint getWirelessRedstoneSign(Location loc) {
		for (WirelessChannel channel : getAllChannels()) {
			for (WirelessReceiver receiver : channel.getReceivers()) {
				if (WirelessRedstone.sameLocation(receiver.getLocation(), loc)) return receiver;
			}
			for (WirelessTransmitter transmitter : channel.getTransmitters()) {
				if (WirelessRedstone.sameLocation(transmitter.getLocation(), loc)) return transmitter;
			}
			for (WirelessScreen screen : channel.getScreens()) {
				if (WirelessRedstone.sameLocation(screen.getLocation(), loc)) return screen;
			}
		}
		return null;
	}

	@Override
	public Collection<WirelessChannel> getAllChannels() {
		try {
			Statement statement;
			statement = connection.createStatement();
			ArrayList<WirelessChannel> channels = new ArrayList<>();

			ResultSet rs = null;

			try {
				rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
			} catch (NullPointerException ex) {
				WirelessRedstone.getWRLogger().warning("SQLite: NullPointerException while requesting all the channels, "
						+ "there're probably no channels");
				return new ArrayList<>();
			}
			ArrayList<String> channelNames = new ArrayList<>();

			while (rs.next()) {
				channelNames.add(getNormalName(rs.getString(sqlChannelName)));
			}
			rs.close();
			statement.close();

			for (String channelName : channelNames) {
				channels.add(getWirelessChannel(channelName));
			}
			return channels;
		} catch (SQLException | NullPointerException e) {
			if (WirelessRedstone.config.getDebugMode()) e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean createWirelessChannel(WirelessChannel channel) {
		if (!sqlTableExists(channel.getName())) // Check if channel already
		// exists
		{
			// Get the type of the sign that has been created
			if (!channel.getReceivers().isEmpty() && !channel.getTransmitters().isEmpty() && !channel.getScreens().isEmpty()) {
				WirelessRedstone.getWRLogger().severe("Channel created with no IWirelessPoint in, stopping the creation of the channel.");
				return false;
			}

			try {
				PreparedStatement psTable = connection.prepareStatement("CREATE TABLE ? (? int, ? char(64), ? int(1), ? char(64), " +
						//Sign colums
						"? char(32), ? int, ? int, ? int, ? int, ? char(64), ? char(128), ? int(1)) " +
						//Values
						"VALUES(?, ?, ?, ?);");
				//Info
				psTable.setString(1, getDBName(channel.getName()));
				psTable.setString(2, sqlChannelId);
				psTable.setString(3, sqlChannelName);
				psTable.setString(4, sqlChannelLocked);
				psTable.setString(5, sqlChannelOwners);
				//Sign colums
				psTable.setString(6, sqlSignType);
				psTable.setString(7, sqlSignX);
				psTable.setString(8, sqlSignY);
				psTable.setString(9, sqlSignZ);
				psTable.setString(10, sqlDirection);
				psTable.setString(11, sqlSignOwner);
				psTable.setString(12, sqlIsWallSign);
				//Values
				psTable.setInt(13, channel.getId());
				psTable.setString(14, channel.getName());
				psTable.setInt(15, 0);
				psTable.setString(16, channel.getOwners().get(0));
				psTable.executeUpdate();

				psTable.close();

				// Create the wireless points
				ArrayList<IWirelessPoint> points = new ArrayList<IWirelessPoint>();
				for (IWirelessPoint ipoint : channel.getReceivers()) {
					points.add(ipoint);
				}
				for (IWirelessPoint ipoint : channel.getTransmitters()) {
					points.add(ipoint);
				}
				for (IWirelessPoint ipoint : channel.getScreens()) {
					points.add(ipoint);
				}
				for (IWirelessPoint ipoint : points) {
					createWirelessPoint(channel.getName(), ipoint);
				}

				if (WirelessRedstone.cache == null) Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(), new Runnable() {
					@Override
					public void run() {
						WirelessRedstone.cache.update();
					}
				}, 1L);
				else WirelessRedstone.cache.update();
				return true;
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		WirelessRedstone.getWRLogger().debug("Tried to create a channel that already exists in the database");
		return false;
	}

	@Override
	public void checkChannel(String channelName) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			if (isChannelEmpty(channel)) removeWirelessChannel(channelName);
		}
	}

	@Override
	public void removeWirelessChannel(String channelName) {
		try {
			WirelessRedstone.WireBox.removeSigns(getWirelessChannel(channelName));
			if (!sqlTableExists(channelName)) return;
			PreparedStatement ps = connection.prepareStatement("DROP TABLE ?");
			ps.setString(1, getDBName(channelName));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			WirelessRedstone.cache.update();
		}
	}

	@Override
	public boolean createWirelessPoint(String channelName, IWirelessPoint point) {
		if (!sqlTableExists(channelName)) {
			WirelessRedstone.getWRLogger().severe("Could not create this wireless point in the channel " + channelName + ", it does not exist!");
		}

		int isWallSign;
		String signType;

		if (point instanceof WirelessReceiver) {
			if (point instanceof WirelessReceiverInverter) {
				signType = "receiver_inverter";
			} else if (point instanceof WirelessReceiverDelayer) {
				signType = "receiver_delayer_" + ((WirelessReceiverDelayer) (point)).getDelay();
			} else if (point instanceof WirelessReceiverSwitch) {
				boolean state;
				if (WirelessRedstone.WireBox.switchState.get(((WirelessReceiverSwitch) (point)).getLocation()) != null)
					state = WirelessRedstone.WireBox.switchState.get(((WirelessReceiverSwitch) (point)).getLocation());
				else state = false;
				signType = "receiver_switch_" + state;
			} else if (point instanceof WirelessReceiverClock){
				signType = "receiver_clock_" + ((WirelessReceiverClock) (point)).getDelay();
			} else {
				signType = "receiver";
			}
		} else if (point instanceof WirelessTransmitter) {
			signType = "transmitter";
		} else if (point instanceof WirelessScreen) {
			signType = "screen";
		} else {
			return false;
		}

		if (point.getIsWallSign()) {
			isWallSign = 1;
		} else {
			isWallSign = 0;
		}

		try {
			int intDirection = WirelessRedstone.WireBox.signFaceToInt(point.getDirection());

			PreparedStatement ps = connection.prepareStatement("INSERT INTO ? (?, ?, ?, ?, ?, ?, ?, ?) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, getDBName(channelName));
			ps.setString(2, sqlSignType);
			ps.setString(3, sqlSignX);
			ps.setString(4, sqlSignY);
			ps.setString(5, sqlSignZ);
			ps.setString(6, sqlDirection);
			ps.setString(7, sqlSignOwner);
			ps.setString(8, sqlSignWorld);
			ps.setString(9, sqlIsWallSign);
			//Values
			ps.setString(10, signType);
			ps.setInt(11, point.getX());
			ps.setInt(12, point.getY());
			ps.setInt(13, point.getZ());
			ps.setInt(14, intDirection);
			ps.setString(15, point.getOwner());
			ps.setString(16, point.getWorld());
			ps.setInt(17, isWallSign);

			ps.executeUpdate();
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean removeIWirelessPoint(String channelName, Location loc) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if(channel == null)
			return false;
		for(WirelessReceiver receiver : channel.getReceivers()){
			if(WirelessRedstone.sameLocation(receiver.getLocation(), loc))
				return removeWirelessReceiver(channelName, loc);
		}
		for(WirelessTransmitter transmitter : channel.getTransmitters()){
			if(WirelessRedstone.sameLocation(transmitter.getLocation(), loc))
				return removeWirelessTransmitter(channelName, loc);
		}
		for(WirelessScreen screen : channel.getScreens()){
			if(WirelessRedstone.sameLocation(screen.getLocation(), loc))
				return removeWirelessScreen(channelName, loc);
		}
		return false;
	}

	@Override
	public boolean removeWirelessReceiver(String channelName, Location loc) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeReceiverAt(loc);
			return removeWirelessPoint(channelName, loc, loc.getWorld()
					.getName());
		} else
			return false;
	}

	@Override
	public boolean removeWirelessTransmitter(String channelName, Location loc) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeTransmitterAt(loc);
			return removeWirelessPoint(channelName, loc, loc.getWorld()
					.getName());
		} else
			return false;
	}

	@Override
	public boolean removeWirelessScreen(String channelName, Location loc) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeScreenAt(loc);
			return removeWirelessPoint(channelName, loc, loc.getWorld()
					.getName());
		} else
			return false;
	}

	@Override
	public void updateChannel(String channelName, WirelessChannel channel) {
		try {
			int locked = (channel.isLocked()) ? 1 : 0;

			PreparedStatement ps = connection.prepareStatement("UPDATE ? SET ?=?, ?=? WHERE ?=?");
			ps.setString(1, getDBName(channelName));
			ps.setString(2, sqlChannelName);
			ps.setString(3, channel.getName());
			ps.setString(4, sqlChannelLocked);
			ps.setInt(5, locked);
			ps.setString(6, sqlChannelId);
			ps.setInt(7, channel.getId());
			ps.executeUpdate();

			// Then update the owners
			/*
			 * Temporary disabled because it makes the plugin crashing.
			 * statement.executeUpdate("ALTER TABLE " + getDBName(channelName) +
			 * " DROP COLUMN " + sql_channelowners);
			 * statement.executeUpdate("ALTER TABLE " + getDBName(channelName) +
			 * " ADD COLUMN " + sql_channelowners); for(String owner :
			 * channel.getOwners()) { statement.executeUpdate("INSERT INTO " +
			 * getDBName(channelName) + " (" + sql_channelowners + ") VALUES " +
			 * owner); }
			 */
			ps.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean renameWirelessChannel(String channelName, String newChannelName) {
		WirelessChannel channel = getWirelessChannel(channelName);
		channel.setName(newChannelName);

		List<IWirelessPoint> signs = new ArrayList<IWirelessPoint>();

		signs.addAll(channel.getReceivers());
		signs.addAll(channel.getTransmitters());
		signs.addAll(channel.getScreens());

		for (IWirelessPoint sign : signs) {
			Location loc = new Location(Bukkit.getWorld(sign.getWorld()),
					sign.getX(), sign.getY(), sign.getZ());
			Sign signBlock = (Sign) loc.getBlock();
			signBlock.setLine(1, newChannelName);
			signBlock.update(true);
		}

		try {
			Statement statement = connection.createStatement();

			// Remove the old channel in the config
			PreparedStatement ps = connection.prepareStatement("DROP TABLE ?");
			ps.setString(1, getDBName(channelName));
			ps.executeUpdate();

			createWirelessChannel(channel);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public boolean wipeData() {
		// Backup before wiping
		if (channelFolder.listFiles().length > 0)
			backupData("db");

		try {
			// Get the names of all the tables
			Statement statement = connection.createStatement();
			ResultSet rs = statement
					.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");
			ArrayList<String> tables = new ArrayList<String>();
			while (rs.next()) {
				tables.add(rs.getString("name"));
			}
			rs.close();
			statement.close();

			// Erase all the tables
			for (String channelName : tables) {
				removeWirelessChannel(channelName);
			}

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean backupData(String extension) {
		try {
			String zipName = "WRBackup "
					+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-"
					+ Calendar.getInstance().get(Calendar.MONTH) + "-"
					+ Calendar.getInstance().get(Calendar.YEAR) + "_"
					+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "."
					+ Calendar.getInstance().get(Calendar.MINUTE) + "."
					+ Calendar.getInstance().get(Calendar.SECOND);
			FileOutputStream fos = new FileOutputStream((channelFolder
					.getCanonicalPath().split(channelFolder.getName())[0])
					+ zipName + ".zip");
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (File file : channelFolder.listFiles()) {
				if (!file.isDirectory()
						&& file.getName().contains("." + extension)) {
					FileInputStream fis = new FileInputStream(file);

					ZipEntry zipEntry = new ZipEntry(file.getName());
					zos.putNextEntry(zipEntry);

					byte[] bytes = new byte[1024];
					int length;

					while ((length = fis.read(bytes)) >= 0) {
						zos.write(bytes, 0, length);
					}
					zos.closeEntry();
					fis.close();
				}
			}
			zos.close();
			fos.close();

			WirelessRedstone.getWRLogger().info("Channels saved in archive: " + zipName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean purgeData() {
		try {
			// Get the names of all the tables
			Collection<WirelessChannel> channels = new ArrayList<WirelessChannel>();
			channels = getAllChannels();

			ArrayList<String> remove = new ArrayList<String>();
			ArrayList<IWirelessPoint> removeSigns = new ArrayList<IWirelessPoint>();

			// Erase channel if empty or world doesn't exist
			for (WirelessChannel channel : channels) {
				HashMap<Location, String> receivers = new HashMap<Location, String>();
				HashMap<Location, String> transmitters = new HashMap<Location, String>();
				HashMap<Location, String> screens = new HashMap<Location, String>();
				ArrayList<Location> locationCheck = new ArrayList<Location>();

				for (WirelessTransmitter transmitter : channel
						.getTransmitters()) {
					if(locationCheck.contains(transmitter.getLocation()))
						receivers.put(transmitter.getLocation(), channel.getName()
								+ "~" + transmitter.getWorld());
					else
						locationCheck.add(transmitter.getLocation());
					if (Bukkit.getWorld(transmitter.getWorld()) == null) {
						transmitters.put(
								transmitter.getLocation(),
								channel.getName() + "~"
										+ transmitter.getWorld());
					}
				}

				for (WirelessReceiver receiver : channel.getReceivers()) {
					if(locationCheck.contains(receiver.getLocation()))
						receivers.put(receiver.getLocation(), channel.getName()
								+ "~" + receiver.getWorld());
					else
						locationCheck.add(receiver.getLocation());
					if (Bukkit.getWorld(receiver.getWorld()) == null) {
						receivers.put(receiver.getLocation(), channel.getName()
								+ "~" + receiver.getWorld());
					}
				}

				for (WirelessScreen screen : channel.getScreens()) {
					if(locationCheck.contains(screen.getLocation()))
						receivers.put(screen.getLocation(), channel.getName()
								+ "~" + screen.getWorld());
					else
						locationCheck.add(screen.getLocation());
					if (Bukkit.getWorld(screen.getWorld()) == null) {
						screens.put(screen.getLocation(), channel.getName()
								+ "~" + screen.getWorld());
					}
				}

				for (Map.Entry<Location, String> receiverRemove : receivers
						.entrySet()) {
					removeWirelessReceiver(
							receiverRemove.getValue().split("~")[0],
							receiverRemove.getKey(), receiverRemove.getValue()
									.split("~")[1]);
				}
				for (Map.Entry<Location, String> transmitterRemove : transmitters
						.entrySet()) {
					removeWirelessTransmitter(transmitterRemove.getValue()
									.split("~")[0], transmitterRemove.getKey(),
							transmitterRemove.getValue().split("~")[1]);
				}
				for (Map.Entry<Location, String> screenRemove : screens.entrySet()) {
					removeWirelessScreen(screenRemove.getValue().split("~")[0],
							screenRemove.getKey(), screenRemove.getValue()
									.split("~")[1]);
				}

				if ((channel.getReceivers().size() < 1)
						&& (channel.getTransmitters().size() < 1)
						&& (channel.getScreens().size() < 1)) {
					remove.add(channel.getName());
				}
			}

			for (String channelRemove : remove) {
				WirelessRedstone.config.removeWirelessChannel(channelRemove);
			}
			return true;
		} catch (Exception e) {
			WirelessRedstone
					.getWRLogger()
					.severe("An error occured. Enable debug mode to see the stacktraces.");
			if (WirelessRedstone.config.getDebugMode()) {
				e.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public int restoreData() {
		try {
			if(getLastBackup() == null) {
				if(WirelessRedstone.config.getDebugMode())
					WirelessRedstone.getWRLogger().debug("Couldn't get last backup, aborting restore");
				return 0;
			}
			File mainFolder = new File(channelFolder
					.getCanonicalPath().split(channelFolder.getName())[0]);

			return unZip(mainFolder + File.separator + getLastBackup(), channelFolder.getAbsolutePath());
		} catch (Exception e){
			if(WirelessRedstone.config.getDebugMode())
				e.printStackTrace();
			return 0;
		}
	}

	@Override
	public void updateReceivers() {
		for(WirelessChannel channel : getAllChannels()){
			for(WirelessReceiver receiver : channel.getReceivers()){
				if(receiver instanceof WirelessReceiverSwitch){
					if(WirelessRedstone.config.getDebugMode())
						WirelessRedstone.getWRLogger().debug("Updating Switcher from channel " + channel.getName());
					updateSwitch(channel, receiver);
				}
			}
		}
	}

	/**
	 * Private method to purge data. Don't use it anywhere else
	 */
	private boolean removeWirelessReceiver(final String channelName,
	                                       final Location loc, final String world) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeReceiverAt(loc, world);
			return removeWirelessPoint(channelName, loc, world);
		} else
			return false;
	}

	/**
	 * Private method to purge data. Don't use it anywhere else
	 */
	private boolean removeWirelessTransmitter(final String channelName,
	                                          final Location loc, final String world) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeTransmitterAt(loc, world);
			return removeWirelessPoint(channelName, loc, world);
		} else
			return false;
	}

	/**
	 * Private method to purge data. Don't use it anywhere else
	 */
	private boolean removeWirelessScreen(final String channelName,
	                                     final Location loc, final String world) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeScreenAt(loc, world);
			return removeWirelessPoint(channelName, loc, world);
		} else
			return false;
	}

	private void updateSwitch(WirelessChannel channel, WirelessReceiver receiver){
		if(!(receiver instanceof WirelessReceiverSwitch))
			return;
		try {
			PreparedStatement ps = connection.prepareStatement("UPDATE ? SET ?=? WHERE ?=? AND ?=? AND ?=? AND ?=?");
			ps.setString(1, getDBName(channel.getName()));
			ps.setString(2, sqlSignType);
			ps.setString(3, "receiver_switch_" + ((WirelessReceiverSwitch) receiver).getState());
			ps.setString(4, sqlSignWorld);
			ps.setString(5, receiver.getWorld());
			ps.setString(6, sqlSignX);
			ps.setInt(7, receiver.getX());
			ps.setString(8, sqlSignY);
			ps.setInt(9, receiver.getY());
			ps.setString(10, sqlSignZ);
			ps.setInt(11, receiver.getZ());
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String getLastBackup() {
		ArrayList<String> files = new ArrayList<String>();
		try {
			File folder = new File(channelFolder
					.getCanonicalPath().split(channelFolder.getName())[0]);
			for (final File fileEntry : folder.listFiles()) {
				if (fileEntry.isDirectory()) {
					continue;
				} else if (!fileEntry.getName().startsWith("WRBackup")) {
					continue;
				} else {
					files.add(fileEntry.getName());
				}
			}
		} catch (Exception e) {
			if(WirelessRedstone.config.getDebugMode())
				e.printStackTrace();
			return null;
		}
		if(!files.isEmpty())
			return files.get(files.size() - 1);

		WirelessRedstone.getWRLogger().warning("There are no backups, aborting restore");
		return null;
	}

	private int unZip(String zipFile, String outputFolder){
		byte[] buffer = new byte[1024];
		try{
			//create output directory is not exists
			File folder = new File(outputFolder);
			if(!folder.exists()){
				folder.mkdir();
			}

			//get the zip file content
			ZipInputStream zis =
					new ZipInputStream(new FileInputStream(zipFile));
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			int returnValue = 1;
			while(ze != null){
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				if(WirelessRedstone.config.getDebugMode())
					WirelessRedstone.getWRLogger().debug("File unziped: " + newFile.getAbsoluteFile());

				if (fileName.endsWith(".db")) {
					returnValue = 2;
					if (WirelessRedstone.config.getDebugMode())
						WirelessRedstone.getWRLogger().debug("Found DB file! Changing storage type to DB after restore.");
				} else if (fileName.endsWith(".yml")) {
					returnValue = 3;
					if (WirelessRedstone.config.getDebugMode())
						WirelessRedstone.getWRLogger().debug("Found yml file! Changing storage type to yml after restore.");
				}

				//create all non exists folders
				//else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();

			if(WirelessRedstone.config.getDebugMode())
				WirelessRedstone.getWRLogger().debug("Unpacking zip done!");

			return returnValue;
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return 0;
	}

	private boolean removeWirelessPoint(final String channelName, final Location loc, final String world) {
		try {
			Statement statement = connection.createStatement();
			PreparedStatement ps = connection.prepareStatement("DELETE FROM ? WHERE ?=? AND ?=? AND ?=? AND ?=?");
			ps.setString(1, getDBName(channelName));
			ps.setString(2, sqlSignX);
			ps.setInt(3, loc.getBlockX());
			ps.setString(4, sqlSignY);
			ps.setInt(5, loc.getBlockY());
			ps.setString(6, sqlSignZ);
			ps.setInt(7, loc.getBlockZ());
			ps.setString(10, sqlSignWorld);
			ps.setString(11, world);
			ps.executeUpdate();

			statement.close();
			WirelessRedstone.cache.update();
		} catch (SQLException ex) {
			WirelessRedstone.getWRLogger().debug(ex.getMessage());
			return false;
		}
		return true;
	}

	private String getDBName(String normalName) {
		try {
			Integer.parseInt(normalName);
			normalName = "num_" + normalName;
		} catch (NumberFormatException ignored) {
		}

		for (char character : WirelessRedstone.config.badCharacters) {
			if (normalName.contains(String.valueOf(character))) {
				String ascii = "" + (int) character;
				String code = "_char_" + ascii + "_";
				normalName = normalName.replace(String.valueOf(character), code);
			}
		}
		return normalName;
	}

	private String getNormalName(String asciiName) {
		if (asciiName.contains("num_")) {
			asciiName = asciiName.replace("num_", "");
			return asciiName;
		}
		for (char character : WirelessRedstone.config.badCharacters) {
			String ascii = "" + (int) character;
			String code = "_char_" + ascii + "_";
			if (asciiName.contains(code)) {
				asciiName = asciiName.replace(code, String.valueOf(character));
			}
		}
		return asciiName;
	}

	private boolean sqlTableExists(final String name) {
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = \"table\"");

			while (rs.next()) {
				if (getNormalName(rs.getString("name")).equals(name)) {
					rs.close();
					statement.close();
					return true;
				}
			}

			rs.close();
			statement.close();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
