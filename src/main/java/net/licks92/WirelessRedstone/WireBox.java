package net.licks92.WirelessRedstone;

import java.util.ArrayList;
import java.util.List;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class WireBox
{
	private final WirelessRedstone plugin;
	private List<Location> receiverlistcachelocation;
	private List<IWirelessPoint> allPointsListCache;

	public WireBox(WirelessRedstone wirelessRedstone)
	{
		this.plugin = wirelessRedstone;
	}

	public boolean isTransmitter(String data)
	{
		for (String tag : WirelessRedstone.strings.tagsTransmitter)
		{
			if(data.toLowerCase().equals(tag.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isReceiver(String data)
	{
		for (String tag : WirelessRedstone.strings.tagsReceiver)
		{
			if(data.toLowerCase().equals(tag.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isScreen(String data)
	{
		for (String tag : WirelessRedstone.strings.tagsScreen)
		{
			if(data.toLowerCase().equals(tag.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasAccessToChannel(Player player, String channelname)
	{
		if (WirelessRedstone.config.getWirelessChannel(channelname) != null)
		{
			if(this.plugin.permissions.isWirelessAdmin(player))
			{
				return true;
			}
			else if(WirelessRedstone.config.getWirelessChannel(channelname).getOwners().contains(player.getName()))
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

	public boolean addWirelessReceiver(String cname, Block cblock, Player player)
	{
		Location loc = cblock.getLocation();
		Boolean isWallSign = (cblock.getType() == Material.WALL_SIGN) ? true : false;
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (isWallSign)
		{
			isWallSign = true;
			if(!isValidWallLocation(cblock))
			{
				player.sendMessage(WirelessRedstone.strings.playerCannotCreateReceiverOnBlock);
				return false;
			}
		}
		else
		{
			if(!isValidLocation(cblock))
			{
				player.sendMessage(WirelessRedstone.strings.playerCannotCreateReceiverOnBlock);
				return false;
			}
		}
		if (channel == null)
		{
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
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
			WirelessRedstone.config.createWirelessChannel(cname, channel);
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			this.UpdateCache();
			return true;
		}
		else
		{
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
				WirelessReceiver receiver = new WirelessReceiver();
				receiver.setOwner(player.getName());
				receiver.setWorld(loc.getWorld().getName());
				receiver.setX(loc.getBlockX());
				receiver.setY(loc.getBlockY());
				receiver.setZ(loc.getBlockZ());
				receiver.setDirection(cblock.getData());
				receiver.setisWallSign(isWallSign);
				WirelessRedstone.config.createWirelessPoint(cname, receiver);
				player.sendMessage(WirelessRedstone.strings.playerExtendedChannel);
				this.UpdateCache();
				return true;
		}
	}
	
	public boolean addWirelessTransmitter(String cname, Block cblock, Player player)
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
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
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
			WirelessRedstone.config.createWirelessChannel(cname, channel);
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			this.UpdateCache();
			return true;
		}
		else
		{
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
			WirelessTransmitter transmitter = new WirelessTransmitter();
			transmitter.setOwner(player.getName());
			transmitter.setWorld(loc.getWorld().getName());
			transmitter.setX(loc.getBlockX());
			transmitter.setY(loc.getBlockY());
			transmitter.setZ(loc.getBlockZ());
			transmitter.setDirection(cblock.getData());
			transmitter.setisWallSign(isWallSign);
			WirelessRedstone.config.createWirelessPoint(cname, transmitter);
			player.sendMessage(WirelessRedstone.strings.playerExtendedChannel);
			this.UpdateCache();
			return true;
		}
	}
	
	public boolean addWirelessScreen(String cname, Block cblock, Player player)
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
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
			channel = new WirelessChannel();
			channel.addOwner(player.getName());
			channel.setName(cname);
			WirelessScreen screen = new WirelessScreen();
			screen.setOwner(player.getName());
			screen.setWorld(loc.getWorld().getName());
			screen.setX(loc.getBlockX());
			screen.setY(loc.getBlockY());
			screen.setZ(loc.getBlockZ());
			screen.setDirection(cblock.getData());
			screen.setisWallSign(isWallSign);
			channel.addScreen(screen);
			WirelessRedstone.config.createWirelessPoint(cname, screen);
			WirelessRedstone.config.save();
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			this.UpdateCache();
			return true;
		}
		else
		{
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
			if (channel instanceof WirelessChannel)
			{
				WirelessScreen screen = new WirelessScreen();
				screen.setOwner(player.getName());
				screen.setWorld(loc.getWorld().getName());
				screen.setX(loc.getBlockX());
				screen.setY(loc.getBlockY());
				screen.setZ(loc.getBlockZ());
				screen.setDirection(cblock.getData());
				screen.setisWallSign(isWallSign);
				channel.addScreen(screen);
				WirelessRedstone.config.createWirelessPoint(cname, screen);
				player.sendMessage(WirelessRedstone.strings.playerExtendedChannel);
				this.UpdateCache();
				return true;
			}
		}
		return false;
	}
	
	public boolean isValidWallLocation(Block block)
	{
		BlockFace face = BlockFace.DOWN;
		switch(block.getData())
		{
		case 0x2: //South
			face = BlockFace.WEST;
			break;
			
		case 0x3: //North
			face = BlockFace.EAST;
			break;
			
		case 0x4: //east
			face = BlockFace.SOUTH;
			break;
			
		case 0x5: //West
			face = BlockFace.NORTH;
			break;
		}
		Block tempBlock = block.getRelative(face);
		
		if (tempBlock.getType() == Material.AIR
				|| tempBlock.getType() == Material.PISTON_BASE
				|| tempBlock.getType() == Material.PISTON_EXTENSION
				|| tempBlock.getType() == Material.PISTON_MOVING_PIECE
				|| tempBlock.getType() == Material.PISTON_STICKY_BASE
				|| tempBlock.getType() == Material.GLOWSTONE
				|| tempBlock.getType() == Material.REDSTONE_LAMP_ON
				|| tempBlock.getType() == Material.REDSTONE_LAMP_OFF)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean isValidLocation(Block block)
	{
		if(block == null)
			return false;
		
		Block tempBlock = block.getRelative(BlockFace.DOWN);
		
		if (tempBlock.getType() == Material.AIR
				|| tempBlock.getType() == Material.PISTON_BASE
				|| tempBlock.getType() == Material.PISTON_EXTENSION
				|| tempBlock.getType() == Material.PISTON_MOVING_PIECE
				|| tempBlock.getType() == Material.PISTON_STICKY_BASE
				|| tempBlock.getType() == Material.GLOWSTONE
				|| tempBlock.getType() == Material.REDSTONE_LAMP_ON
				|| tempBlock.getType() == Material.REDSTONE_LAMP_OFF)
		{
			return false;
		}
		else
			return true;
	}

	public ArrayList<Location> getReceiverLocations(WirelessChannel channel)
	{
		ArrayList<Location> returnlist = new ArrayList<Location>();
		for (WirelessReceiver receiver : channel.getReceivers())
		{
			returnlist.add(this.getPointLocation(receiver));
		}
		return returnlist;
	}
	
	public ArrayList<Location> getReceiverLocations(String channelname)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(channelname);
		if(channel == null)
			return new ArrayList<Location>();
		
		return getReceiverLocations(channel);
	}
	
	public ArrayList<Location> getScreenLocations(WirelessChannel channel)
	{
		ArrayList<Location> returnlist = new ArrayList<Location>();
		for(WirelessScreen screen : channel.getScreens())
		{
			returnlist.add(this.getPointLocation(screen));
		}
		return returnlist;
	}
	
	public ArrayList<Location> getScreenLocations(String channelname)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(channelname);
		if(channel == null)
			return new ArrayList<Location>();
		
		return getScreenLocations(channel);
	}
	
	public boolean isActive(WirelessChannel channel)
	{
		for(WirelessTransmitter transmitter : channel.getTransmitters())
		{
			Location tempLoc = new Location(plugin.getServer().getWorld(transmitter.getWorld()),
					transmitter.getX(),
					transmitter.getY(),
					transmitter.getZ());
			if(tempLoc.getBlock().isBlockIndirectlyPowered() || tempLoc.getBlock().isBlockPowered())
			{
				return true;
			}
		}
		return false;
	}

	public void removeReceiverAt(final Location loc, final boolean byplayer)
	{
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				for (WirelessChannel channel : WirelessRedstone.config.getAllChannels())
				{
					for (WirelessReceiver receiver : channel.getReceivers())
					{
						if (receiver.getX() == loc.getBlockX()
								&& receiver.getY() == loc.getBlockY()
								&& receiver.getZ() == loc.getBlockZ())
						{
							WirelessRedstone.config.removeWirelessPoint(channel.getName(), loc);
							if (!byplayer)
							{
								for (String owner : channel.getOwners())
								{
									try
									{
										if (plugin.getServer().getPlayer(owner).isOnline())
										{
											plugin.getServer().getPlayer(owner).sendMessage("One of your signs on channel: "
												    + channel.getName()
													+ " is broken by nature.");
										}
									}
									catch (Exception ex)
									{
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

	public boolean removeWirelessReceiver(String cname, Location loc)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			WirelessRedstone.config.removeWirelessPoint(cname, loc);
			WirelessRedstone.config.updateChannel(cname, channel);
			this.UpdateCache();
			return true;
		}
		else
			return false;
	}

	public boolean removeWirelessTransmitter(String cname, Location loc)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			WirelessRedstone.config.removeWirelessPoint(cname, loc);
			WirelessRedstone.config.updateChannel(cname, channel);
			this.UpdateCache();
			return true;
		}
		else
			return false;
	}
	
	public boolean removeWirelessScreen(String cname, Location loc)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			WirelessRedstone.config.removeWirelessPoint(cname, loc);
			WirelessRedstone.config.updateChannel(cname, channel);
			this.UpdateCache();
			return true;
		}
		else
			return false;
	}

	public boolean removeChannel(String cname)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			this.removeSigns(channel);
			WirelessRedstone.config.removeChannel(cname);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
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
			//When there isn't any transmitter, it'll throw this exception.
		}
	}

	public boolean containsChannel(String name)
	{
		return (WirelessRedstone.config.getWirelessChannel(name) != null);
	}

	public List<IWirelessPoint> getAllSigns()
	{
		return allPointsListCache;
	}

	public void UpdateAllSignsList()
	{
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				ArrayList<IWirelessPoint> returnlist = new ArrayList<IWirelessPoint>();
				for (WirelessChannel channel : WirelessRedstone.config.getAllChannels())
				{
					try
					{
						for (IWirelessPoint point : channel.getReceivers())
						{
							returnlist.add(point);
						}

						for (IWirelessPoint point : channel.getTransmitters())
						{
							returnlist.add(point);
						}
						
						for (IWirelessPoint point : channel.getScreens())
						{
							returnlist.add(point);
						}
					}
					catch (Exception e)
					{

					}
				}
				allPointsListCache = returnlist;
			}
		}, 0L);
	}

	public List<Location> getAllReceiverLocations()
	{
		return receiverlistcachelocation;
	}

	public void UpdateReceiverLocations()
	{
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				List<Location> returnlist = new ArrayList<Location>();
				for (WirelessChannel channel : WirelessRedstone.config.getAllChannels())
				{
					try
					{
						for (WirelessReceiver point : channel.getReceivers())
						{
							Location floc = getPointLocation(point);
							returnlist.add(floc);
						}
					}
					catch (Exception e)
					{
						
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

	public Location getPointLocation(IWirelessPoint point)
	{
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
			}
			catch (Exception e)
			{
				
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

	public void signWarning(Block block, int code)
	{
		Sign sign = (Sign) block.getState();
		switch(code)
		{
		case 1:
			sign.setLine(2, "Bad block");
			sign.setLine(3, "Behind sign");
			sign.update();
			break;
			
		default:
			break;
		}
	}
}