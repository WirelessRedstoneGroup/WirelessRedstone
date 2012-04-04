package net.licks92.WirelessRedstone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
		return WirelessRedstone.config.getWirelessChannel(channel);
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
		WirelessRedstone.config.setWirelessChannel(channel.getName(), channel);
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
		
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		
		if (channel == null)
		{
			channel = new WirelessChannel();
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
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			player.sendMessage("[WirelessRedstone] You just created a new channel! Place a Transmitter to complete! typ /wrhelp for more info!");
			this.UpdateCache();
			return true;
		}
		else
		{
			WirelessReceiver receiver = new WirelessReceiver();
			receiver.setOwner(player.getName());
			receiver.setWorld(loc.getWorld().getName());
			receiver.setX(loc.getBlockX());
			receiver.setY(loc.getBlockY());
			receiver.setZ(loc.getBlockZ());
			receiver.setDirection(cblock.getData());
			receiver.setisWallSign(isWallSign);
			channel.addReceiver(receiver);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			player.sendMessage("[WirelessRedstone] You just extended a channel!");
			this.UpdateCache();
			return true;
		}
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
		if(channel == null)
			return new ArrayList<Location>();
		
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
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if(channel != null)
		{
			channel.removeReceiverAt(loc);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public boolean RemoveWirelessTransmitter(String cname, Location loc)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			channel.removeTransmitterAt(loc);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public boolean removeChannel(String cname)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			this.removeSigns(channel);
			WirelessRedstone.config.setWirelessChannel(cname, null);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public Collection<WirelessChannel> getChannels()
	{
		return WirelessRedstone.config.getAllChannels();
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
		
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel == null)
		{
			channel = new WirelessChannel();
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
			WirelessRedstone.config.setWirelessChannel(cname, channel);

			WirelessRedstone.config.save();
			player.sendMessage("[WirelessRedstone] You just created a new channel! Place a Receiver to complete! typ /wrhelp for more info!");
			this.UpdateCache();
			return true;
		}
		else
		{
			WirelessTransmitter transmitter = new WirelessTransmitter();
			transmitter.setOwner(player.getName());
			transmitter.setWorld(loc.getWorld().getName());
			transmitter.setX(loc.getBlockX());
			transmitter.setY(loc.getBlockY());
			transmitter.setZ(loc.getBlockZ());
			transmitter.setDirection(cblock.getData());
			transmitter.setisWallSign(isWallSign);
			channel.addTransmitter(transmitter);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			player.sendMessage("[WirelessRedstone] You just extended a channel!");
			this.UpdateCache();
			return true;
		}
	}

	public boolean containsChannel(String name) {
		return (WirelessRedstone.config.getWirelessChannel(name) != null);
	}

	public List<IWirelessPoint> getAllSigns() {
		return allPointsListCache;
	}

	public void UpdateAllSignsList() {
		plugin.getServer().getScheduler()
				.scheduleAsyncDelayedTask(plugin, new Runnable() {

					public void run() {
						ArrayList<IWirelessPoint> returnlist = new ArrayList<IWirelessPoint>();
							for (WirelessChannel channel : WirelessRedstone.config.getAllChannels()) {
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
							for (WirelessChannel channel : WirelessRedstone.config.getAllChannels()) {
								try {
									for (WirelessReceiver point : channel
											.getReceivers()) {
										Location floc = getPointLocation(point);
										returnlist.add(floc);
									}
								} catch (Exception e) {
								}
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
		for (WirelessChannel channel : WirelessRedstone.config.getAllChannels())
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

		receiverlistcachelocation = returnlist;

		ArrayList<IWirelessPoint> returnlist2 = new ArrayList<IWirelessPoint>();
		for (WirelessChannel channel : WirelessRedstone.config.getAllChannels()) {
			for (IWirelessPoint point : channel.getReceivers()) {
				returnlist2.add(point);
			}

			for (IWirelessPoint point : channel.getTransmitters()) {
				returnlist2.add(point);
			}
		}
		allPointsListCache = returnlist2;
	}

}
