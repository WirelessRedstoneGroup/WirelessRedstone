package net.licks92.WirelessRedstone.Configuration;

import net.licks92.WirelessRedstone.Channel.*;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class YamlStorage implements IWirelessStorageConfiguration {
    private final File channelFolder;
    private final WirelessRedstone plugin;

    public YamlStorage(final File channelFolder, final WirelessRedstone r_plugin) {
        plugin = r_plugin;
        this.channelFolder = channelFolder;
    }

    @Override
    public boolean initStorage() {
        return init(true);
    }

    public boolean init(final boolean allowConvert) {
        //Initialize the serialization
        ConfigurationSerialization.registerClass(WirelessTransmitter.class, "WirelessTransmitter");
        ConfigurationSerialization.registerClass(WirelessChannel.class, "WirelessChannel");
        ConfigurationSerialization.registerClass(WirelessScreen.class, "WirelessScreen");
        ConfigurationSerialization.registerClass(WirelessReceiver.class, "WirelessReceiver");
        ConfigurationSerialization.registerClass(WirelessReceiverInverter.class, "WirelessReceiverInverter");
        ConfigurationSerialization.registerClass(WirelessReceiverDelayer.class, "WirelessReceiverDelayer");
        ConfigurationSerialization.registerClass(WirelessReceiverClock.class, "WirelessReceiverClock");

        if (canConvert() && allowConvert) {
            WirelessRedstone.getWRLogger().info("WirelessRedstone found one or many channels in SQL Database.");
            WirelessRedstone.getWRLogger().info("Beginning data transfer... (from SQL Database to Yaml Files)");
            if (convertFromAnotherStorage()) {
                WirelessRedstone.getWRLogger().info("Done ! All the channels are now stored in the Yaml Files.");
            }
        }

        return true;
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
	public boolean canConvert() {
        for (File file : channelFolder.listFiles()) {
            if (file.getName().contains(".db")) {
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean convertFromAnotherStorage() {
        WirelessRedstone.getWRLogger().info("Backuping the channels/ folder before transfer.");
        if (!backupData("db")) {
            WirelessRedstone.getWRLogger().severe("Backup failed ! Data transfer abort...");
        } else {
            WirelessRedstone.getWRLogger().info("Backup done. Starting data transfer...");

            SQLStorage sql = new SQLStorage(channelFolder, plugin);
            sql.init(false);
            for (WirelessChannel channel : sql.getAllChannels()) {
                //Something fails here! Channels do not transfer the transmitter that's strange!
                createWirelessChannel(channel);
            }
            sql.close();
            for (File f : channelFolder.listFiles()) {
                if (f.getName().contains(".db")) {
                    f.delete();
                }
            }
        }
        return true;
    }

    @Override
    public WirelessChannel getWirelessChannel(final String channelName) {
        FileConfiguration channelConfig = new YamlConfiguration();
        try {
            File channelFile = new File(channelFolder, channelName + ".yml");
            channelConfig.load(channelFile);
        } catch (FileNotFoundException e) {
            WirelessRedstone.getWRLogger().debug("File " + channelName + ".yml wasn't found in the channels folder, returning null.");
            return null;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Object channel = channelConfig.get(channelName);
        if (channel == null)
            return null; // channel not found
        else if (!(channel instanceof WirelessChannel)) {
            WirelessRedstone.getWRLogger().warning("Channel " + channelName + " does not seem to be of type WirelessChannel.");
            return null;
        } else
            return (WirelessChannel) channel;
    }

    public void setWirelessChannel(final String channelName, final WirelessChannel channel) {
        FileConfiguration channelConfig = new YamlConfiguration();
        try {
            File channelFile = new File(channelFolder, channelName + ".yml");
            if (channel != null)
                channelFile.createNewFile();
            channelConfig.load(channelFile);
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        channelConfig.set(channelName, channel);

        try {
            channelConfig.save(new File(channelFolder, channelName + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean createWirelessChannel(final WirelessChannel channel) {
        setWirelessChannel(channel.getName(), channel);

        return true;
    }

    @Override
    public void removeWirelessChannel(final String channelName) {
        WirelessRedstone.WireBox.removeSigns(getWirelessChannel(channelName));
        setWirelessChannel(channelName, null);
        for (File f : channelFolder.listFiles()) {
            if (f.getName().equals(channelName + ".yml")) {
                f.delete();
            }
        }
        WirelessRedstone.getWRLogger().debug("Channel " + channelName + " successfully removed and file deleted.");
    }

    @Override
	public boolean renameWirelessChannel(final String channelName, final String newChannelName) {
        WirelessChannel channel = getWirelessChannel(channelName);

        List<IWirelessPoint> signs = new ArrayList<IWirelessPoint>();

        signs.addAll(channel.getReceivers());
        signs.addAll(channel.getTransmitters());
        signs.addAll(channel.getScreens());

        for (IWirelessPoint sign : signs) {
            Location loc = new Location(Bukkit.getWorld(sign.getWorld()), sign.getX(), sign.getY(), sign.getZ());
            Sign signBlock = (Sign) loc.getBlock();
            signBlock.setLine(1, newChannelName);
        }

        //Remove the old channel in the config
        setWirelessChannel(channelName, null);

        for (File f : channelFolder.listFiles()) {
            if (f.getName().equals(channelName)) {
                f.delete();
            }
        }

        //Set a new channel
        createWirelessChannel(channel);

        return true;
    }

    @Override
    public boolean createWirelessPoint(final String channelName, final IWirelessPoint point) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (point instanceof WirelessReceiver) {
            WirelessRedstone.getWRLogger().debug("Yaml config : Creating a receiver of class "
                    + point.getClass());
            if (point instanceof WirelessReceiverInverter) {
                channel.addReceiver((WirelessReceiverInverter) point);
                WirelessRedstone.getWRLogger().debug("Yaml Config : Adding an inverter");
            } else {
                channel.addReceiver((WirelessReceiver) point);
                WirelessRedstone.getWRLogger().debug("Yaml Config : Adding a default receiver");
            }
        } else if (point instanceof WirelessTransmitter)
            channel.addTransmitter((WirelessTransmitter) point);
        else if (point instanceof WirelessScreen)
            channel.addScreen((WirelessScreen) point);
        setWirelessChannel(channelName, channel);

        return true;
    }

    @Override
    public boolean removeWirelessReceiver(final String channelName, final Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeReceiverAt(loc);
            updateChannel(channelName, channel);
            return true;
        } else
            return false;
    }

    @Override
    public boolean removeWirelessTransmitter(final String channelName, final Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeTransmitterAt(loc);
            updateChannel(channelName, channel);
            return true;
        } else
            return false;
    }

    @Override
    public boolean removeWirelessScreen(final String channelName, final Location loc) {
        WirelessChannel channel = getWirelessChannel(channelName);
        if (channel != null) {
            channel.removeScreenAt(loc);
            updateChannel(channelName, channel);
            return true;
        } else
            return false;
    }

	/**
	 * Private method to purge data. Don't use it anywhere else
	 *
	 * @param channelName
	 * @param loc
	 * @param world
	 * @return succeeded
	 */
	private boolean removeWirelessReceiver(final String channelName,
			final Location loc, final String world) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeReceiverAt(loc, world);
			updateChannel(channelName, channel);
			return true;
		} else
			return false;
	}

	/**
	 * Private method to purge data. Don't use it anywhere else
	 *
	 * @param channelName
	 * @param loc
	 * @param world
	 * @return succeeded
	 */
	private boolean removeWirelessTransmitter(final String channelName,
			final Location loc, final String world) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeTransmitterAt(loc, world);
			updateChannel(channelName, channel);
			return true;
		} else
			return false;
	}

	/**
	 * Private method to purge data. Don't use it anywhere else
	 *
	 * @param channelName
	 * @param loc
	 * @param world
	 * @return succeeded
	 */
	private boolean removeWirelessScreen(final String channelName,
			final Location loc, final String world) {
		WirelessChannel channel = getWirelessChannel(channelName);
		if (channel != null) {
			channel.removeScreenAt(loc, world);
			updateChannel(channelName, channel);
			return true;
		} else
			return false;
	}

    @Override
    public boolean wipeData() {
        //Backup the channels folder first.
        if(channelFolder.listFiles().length > 0)
            backupData("yml");

        //Then remove the channels and the files.
        for (File f : channelFolder.listFiles()) {
            String name = f.getName();
            int pos = name.lastIndexOf(".");
            if (pos > 0) {
                removeWirelessChannel(name.substring(0, pos));
            }
        }
        return true;
    }

    @Override
    public boolean backupData(final String extension) {
        try {
			String zipName = "WRBackup "
					+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-"
					+ Calendar.getInstance().get(Calendar.MONTH) + "-"
					+ Calendar.getInstance().get(Calendar.YEAR) + "_"
					+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "."
					+ Calendar.getInstance().get(Calendar.MINUTE) + "."
					+ Calendar.getInstance().get(Calendar.SECOND);
            FileOutputStream fos = new FileOutputStream((channelFolder.getCanonicalPath().split(channelFolder.getName())[0]) + zipName + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : channelFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().contains("." + extension)) {
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

            WirelessRedstone.getWRLogger().info("Channels saved in archive : " + zipName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Collection<WirelessChannel> getAllChannels() {
        List<WirelessChannel> channels = new ArrayList<WirelessChannel>();


        for (File f : channelFolder.listFiles(new YamlFilter())) {
            FileConfiguration channelConfig = new YamlConfiguration();
            try {
                channelConfig.load(f);
            } catch (InvalidConfigurationException | IOException e) {
                e.printStackTrace();
            }
            String channelName;
            try {
                channelName = f.getName().split(".yml")[0];
            } catch (ArrayIndexOutOfBoundsException ex) {
                continue;
            }
            Object channel = channelConfig.get(channelName);
            if (channel instanceof WirelessChannel) {
                channels.add((WirelessChannel) channel);
                WirelessRedstone.getWRLogger().debug("Channel added in getAllChannels() list : " + ((WirelessChannel) channel).getName());
            } else if (channel == null) {
                WirelessRedstone.getWRLogger().debug("File " + f.getName() + " does not contain a Wireless Channel. Removing it.");
                f.delete();
            } else
                WirelessRedstone.getWRLogger().warning("Channel " + channel + " is not of type WirelessChannel.");
        }
        if (channels.isEmpty()) {
            return new ArrayList<WirelessChannel>();
        }
        return channels;
    }

    @Override
    public void updateChannel(final String channelName, final WirelessChannel channel) {
        setWirelessChannel(channelName, channel);
    }

	@Override
	public boolean purgeData() {
		try {
			// Get the names of all the tables
			Collection<WirelessChannel> channels = new ArrayList<WirelessChannel>();
			channels = getAllChannels();

			ArrayList<String> remove = new ArrayList<String>();

			// Erase channel if empty or world doesn't exist
			for (WirelessChannel channel : channels) {
				HashMap<Location, String> receivers = new HashMap<Location, String>();
				HashMap<Location, String> transmitters = new HashMap<Location, String>();
				HashMap<Location, String> screens = new HashMap<Location, String>();

				for (WirelessReceiver receiver : channel.getReceivers()) {
					if (Bukkit.getWorld(receiver.getWorld()) == null) {
						receivers.put(receiver.getLocation(), channel.getName()
								+ "~" + receiver.getWorld());
					}
				}
				for (WirelessTransmitter transmitter : channel
						.getTransmitters()) {
					if (Bukkit.getWorld(transmitter.getWorld()) == null) {
						transmitters.put(
								transmitter.getLocation(),
								channel.getName() + "~"
										+ transmitter.getWorld());
					}
				}
				for (WirelessScreen screen : channel.getScreens()) {
					if (Bukkit.getWorld(screen.getWorld()) == null) {
						screens.put(screen.getLocation(), channel.getName()
								+ "~" + screen.getWorld());
					}
				}

				for (Entry<Location, String> receiverRemove : receivers
						.entrySet()) {
					removeWirelessReceiver(
							receiverRemove.getValue().split("~")[0],
							receiverRemove.getKey(), receiverRemove.getValue()
									.split("~")[1]);
				}
				for (Entry<Location, String> transmitterRemove : transmitters
						.entrySet()) {
					removeWirelessTransmitter(transmitterRemove.getValue()
							.split("~")[0], transmitterRemove.getKey(),
							transmitterRemove.getValue().split("~")[1]);
				}
				for (Entry<Location, String> screenRemove : screens.entrySet()) {
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
}

class YamlFilter implements FilenameFilter {
    @Override
    public boolean accept(final File file, final String name) {
        return name.contains(".yml");
    }

}
