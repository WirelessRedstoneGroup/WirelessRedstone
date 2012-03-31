package net.licks92.WirelessRedstone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class WireBox
{
	private final WirelessRedstone plugin;
	//private Location tempLoc;
	private List<Location> receiverlistcachelocation;
	private List<IWirelessPoint> allPointsListCache;

	public WireBox(WirelessRedstone wirelessRedstone)
	{
		this.plugin = wirelessRedstone;
	}

	public boolean isTransmitter(String data)
	{
		if (data.toLowerCase().equalsIgnoreCase("[transmitter]")|| data.toLowerCase().equalsIgnoreCase("[WRt]"))
			return true;
		else
			return false;
	}

	public boolean isReceiver(String data)
	{
		if (data.toLowerCase().equalsIgnoreCase("[receiver]")|| data.toLowerCase().equalsIgnoreCase("[WRr]"))
			return true;
		else
			return false;
	}

	public WirelessChannel getChannel(String channel)
	{
		Object tempObject = WirelessRedstone.config.get("WirelessChannels." + channel);
		if (tempObject instanceof WirelessChannel)
		{
			return (WirelessChannel) tempObject;
		}

		return null;
	}

	public boolean hasAccessToChannel(Player player, String channelname)
	{
		if (getChannel(channelname) != null)
		{
			if(this.plugin.permissionsHandler.hasPermission(player, "WirelessRedstone.admin"))
			{
				return true;
			}
			else if(getChannel(channelname).getOwners().contains(player.getName()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		

		return true;
	}

	public boolean SaveChannel(WirelessChannel channel)
	{
		WirelessRedstone.config.set("WirelessChannels." + channel.getName(), channel);
		WirelessRedstone.config.save();
		return true;
	}

	public boolean AddWirelessReceiver(String cname, Block cblock, Player player)
	{
		Location loc = cblock.getLocation();
		Boolean isWallSign = false;
		if (cblock.getType() == Material.WALL_SIGN)
		{
			isWallSign = true;
		}
		if (WirelessRedstone.config.get("WirelessChannels." + cname) == null)
		{
			WirelessChannel channel = new WirelessChannel();
			channel.addOwner(player.getName());
			channel.setName(cname);
			WirelessReceiver receiver = new WirelessReceiver();
			receiver.setOwner(player.getName());
			receiver.setWorld(loc.getWorld().getName());
			receiver.setX(loc.getBlockX());
			receiver.setY(loc.getBlockY());
			receiver.setZ(loc.getBlockZ());
			receiver.setDirection(cblock.getData());
			receiver.setisWallSign(isWallSign);
			channel.addReceiver(receiver);
			WirelessRedstone.config.set("WirelessChannels." + cname,channel);
			WirelessRedstone.config.save();
			player.sendMessage("[WirelessRedstone] You just created a new channel! Place a Transmitter to complete! typ /wrhelp for more info!");
			this.UpdateCache();
			return true;
		}
		else
		{
			Object tempobject = WirelessRedstone.config.get("WirelessChannels." + cname);
			if (tempobject instanceof WirelessChannel)
			{
				WirelessChannel channel = (WirelessChannel) tempobject;
				WirelessReceiver receiver = new WirelessReceiver();
				receiver.setOwner(player.getName());
				receiver.setWorld(loc.getWorld().getName());
				receiver.setX(loc.getBlockX());
				receiver.setY(loc.getBlockY());
				receiver.setZ(loc.getBlockZ());
				receiver.setDirection(cblock.getData());
				receiver.setisWallSign(isWallSign);
				channel.addReceiver(receiver);
				WirelessRedstone.config.set("WirelessChannels." + cname, channel);
				WirelessRedstone.config.save();
				player.sendMessage("[WirelessRedstone] You just extended a channel!");
				this.UpdateCache();
				return true;
			}
		}
		return false;
	}

	public boolean isValidLocation(Location loc)
	{
		if (loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.EAST).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.NORTH).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.WEST).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.EAST).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.NORTH).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.WEST).getType() == Material.AIR
				|| loc.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.AIR)
		{

		}
		return false;
	}

	public ArrayList<Location> getReceiverLocations(WirelessChannel channel) {
		ArrayList<Location> returnlist = new ArrayList<Location>();
		for (WirelessReceiver receiver : channel.getReceivers()) {
			returnlist.add(this.getPointLocation(receiver));
		}
		return returnlist;
	}

	public ArrayList<Location> getReceiverLocations(String channelname) {
		WirelessChannel channel = this.plugin.WireBox.getChannel(channelname);
		ArrayList<Location> returnlist = new ArrayList<Location>();
		for (WirelessReceiver receiver : channel.getReceivers()) {
			returnlist.add(this.getPointLocation(receiver));
		}
		return returnlist;
	}

	public void removeReceiverAt(final Location loc, final boolean byplayer)
	{
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
		{
					public void run()
					{
						for (WirelessChannel channel : getChannels())
						{
							for (WirelessReceiver receiver : channel.getReceivers())
							{
								if (receiver.getX() == loc.getBlockX()
										&& receiver.getY() == loc.getBlockY()
										&& receiver.getZ() == loc.getBlockZ())
								{
									channel.removeReceiverAt(loc);
									SaveChannel(channel);
									if (!byplayer)
									{
										for (String owner : channel.getOwners())
										{
											try
											{
												if (plugin.getServer().getPlayer(owner).isOnline())
												{
													plugin.getServer().getPlayer(owner)
															.sendMessage("One of your signs on channel: "
														    + channel.getName()
															+ " is broken by nature.");
												}
											} catch (Exception ex) {
												// NA
											}
										}
									}
									return;
								}
							}
						}
					}
				});
	}

	public boolean RemoveWirelessReceiver(String cname, Location loc)
	{
		if (WirelessRedstone.config.get("WirelessChannels." + cname) == null)
			return false;

		Object tempObject = WirelessRedstone.config.get("WirelessChannels." + cname);
		if (tempObject instanceof WirelessChannel)
		{
			WirelessChannel channel = (WirelessChannel) tempObject;
			channel.removeReceiverAt(loc);
			WirelessRedstone.config.set("WirelessChannels." + cname,
					channel);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public boolean RemoveWirelessTransmitter(String cname, Location loc)
	{
		if (WirelessRedstone.config.get("WirelessChannels." + cname) == null)
			return false;

		Object tempObject = WirelessRedstone.config.get("WirelessChannels." + cname);
		if (tempObject instanceof WirelessChannel)
		{
			WirelessChannel channel = (WirelessChannel) tempObject;
			channel.removeTransmitterAt(loc);
			WirelessRedstone.config.set("WirelessChannels." + cname, channel);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public boolean removeChannel(String cname)
	{
		Object tempObject = WirelessRedstone.config.get("WirelessChannels." + cname);
		if (tempObject instanceof WirelessChannel)
		{
			WirelessChannel ccopy = (WirelessChannel) tempObject;
			this.removeSigns(ccopy);
			WirelessRedstone.config.set("WirelessChannels." + cname, null);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public Collection<WirelessChannel> getChannels()
	{
		Object tmpO = WirelessRedstone.config.get("WirelessChannels");
		if (tmpO instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, WirelessChannel> Channels = (Map<String, WirelessChannel>) tmpO;
			return Channels.values();
		}
		return null;
	}

	private void removeSigns(WirelessChannel channel)
	{
		try
		{
			for (IWirelessPoint point : channel.getReceivers())
			{
				this.getPointLocation(point).getBlock().setType(Material.AIR);
			}
		}
		catch(NullPointerException ex)
		{
			//When there isn't any receiver, it'll throw this exception.
		}

		try
		{
			for (IWirelessPoint point : channel.getTransmitters())
			{
				this.getPointLocation(point).getBlock().setType(Material.AIR);
			}
		}
		catch(NullPointerException ex)
		{
			//When there isn't any receiver, it'll throw this exception.
		}
	}

	public boolean addWirelessTransmitter(String cname, Block cblock, Player player)
	{
		Location loc = cblock.getLocation();
		Boolean isWallSign = false;
		if (cblock.getType() == Material.WALL_SIGN) {
			isWallSign = true;
		}
		if (WirelessRedstone.config.get("WirelessChannels." + cname) == null)
		{
			WirelessChannel channel = new WirelessChannel();
			channel.addOwner(player.getName());
			channel.setName(cname);
			WirelessTransmitter transmitter = new WirelessTransmitter();
			transmitter.setOwner(player.getName());
			transmitter.setWorld(loc.getWorld().getName());
			transmitter.setX(loc.getBlockX());
			transmitter.setY(loc.getBlockY());
			transmitter.setZ(loc.getBlockZ());
			transmitter.setDirection(cblock.getData());
			transmitter.setisWallSign(isWallSign);
			channel.addTransmitter(transmitter);
			WirelessRedstone.config.set("WirelessChannels." + cname, channel);

			WirelessRedstone.config.save();
			player.sendMessage("[WirelessRedstone] You just created a new channel! Place a Receiver to complete! typ /wrhelp for more info!");
			this.UpdateCache();
			return true;
		}
		else
		{
			Object tempobject = WirelessRedstone.config.get("WirelessChannels." + cname);
			if (tempobject instanceof WirelessChannel) {
				WirelessChannel channel = (WirelessChannel) tempobject;
				WirelessTransmitter transmitter = new WirelessTransmitter();
				transmitter.setOwner(player.getName());
				transmitter.setWorld(loc.getWorld().getName());
				transmitter.setX(loc.getBlockX());
				transmitter.setY(loc.getBlockY());
				transmitter.setZ(loc.getBlockZ());
				transmitter.setDirection(cblock.getData());
				transmitter.setisWallSign(isWallSign);
				channel.addTransmitter(transmitter);
				WirelessRedstone.config.set("WirelessChannels." + cname, channel);
				WirelessRedstone.config.save();
				player.sendMessage("[WirelessRedstone] You just extended a channel!");
				this.UpdateCache();
				return true;
			}
		}
		return false;
	}

	public boolean containsChannel(String name) {
		return (WirelessRedstone.config.get("WirelessChannels." + name) != null);
	}

	public List<IWirelessPoint> getAllSigns() {
		return allPointsListCache;
	}

	public void UpdateAllSignsList() {
		plugin.getServer().getScheduler()
				.scheduleAsyncDelayedTask(plugin, new Runnable() {

					public void run() {
						ArrayList<IWirelessPoint> returnlist = new ArrayList<IWirelessPoint>();
						Object tmpO = WirelessRedstone.config.get("WirelessChannels");
						if (tmpO instanceof Map<?, ?>) {
							@SuppressWarnings("unchecked")
							Map<String, WirelessChannel> Channels = (Map<String, WirelessChannel>) tmpO;
							for (WirelessChannel channel : Channels.values()) {
								try {
									for (IWirelessPoint point : channel
											.getReceivers()) {
										returnlist.add(point);
									}

									for (IWirelessPoint point : channel
											.getTransmitters()) {
										returnlist.add(point);
									}
								} catch (Exception e) {

								}
							}
						}
						allPointsListCache = returnlist;
					}
				}, 0L);
	}

	public List<Location> getAllReceiverLocations() {
		return receiverlistcachelocation;
	}

	public void UpdateReceiverLocations()
	{
		plugin.getServer().getScheduler()
				.scheduleAsyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						List<Location> returnlist = new ArrayList<Location>();
						Object tmpO = WirelessRedstone.config.get("WirelessChannels");
						if (tmpO instanceof Map<?, ?>) {
							@SuppressWarnings("unchecked")
							Map<String, WirelessChannel> Channels = (Map<String, WirelessChannel>) tmpO;
							for (WirelessChannel channel : Channels.values()) {
								try {
									for (WirelessReceiver point : channel
											.getReceivers()) {
										Location floc = getPointLocation(point);
										returnlist.add(floc);
									}
								} catch (Exception e) {
								}
							}
						} else {

						}
						receiverlistcachelocation = returnlist;
					}
				}, 0L);
	}

	public void UpdateCache()
	{
		UpdateReceiverLocations();
		UpdateAllSignsList();
	}

	public Location getPointLocation(IWirelessPoint point) {
		return new Location(plugin.getServer().getWorld(point.getWorld()),
				point.getX(), point.getY(), point.getZ());
	}

	public void UpdateChacheNoThread()
	{
		ArrayList<Location> returnlist = new ArrayList<Location>();
		Object tmpO = WirelessRedstone.config.get("WirelessChannels");
		if (tmpO instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, WirelessChannel> Channels = (Map<String, WirelessChannel>) tmpO;
			for (WirelessChannel channel : Channels.values())
			{
				try
				{
					for (WirelessReceiver point : channel.getReceivers())
					{
						Location floc = getPointLocation(point);
						returnlist.add(floc);
					}
				} catch (Exception e) {

				}
			}
		}

		receiverlistcachelocation = returnlist;

		ArrayList<IWirelessPoint> returnlist2 = new ArrayList<IWirelessPoint>();
		Object tmpO2 = WirelessRedstone.config.get("WirelessChannels");
		if (tmpO2 instanceof List<?>) {
			@SuppressWarnings("unchecked")
			Map<String, WirelessChannel> Channels = (Map<String, WirelessChannel>) tmpO2;
			for (WirelessChannel channel : Channels.values()) {
				for (IWirelessPoint point : channel.getReceivers()) {
					returnlist2.add(point);
				}

				for (IWirelessPoint point : channel.getTransmitters()) {
					returnlist2.add(point);
				}
			}
		}
		allPointsListCache = returnlist2;
	}

}
