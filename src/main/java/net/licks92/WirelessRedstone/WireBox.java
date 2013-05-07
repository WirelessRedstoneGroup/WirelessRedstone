package net.licks92.WirelessRedstone;

import java.util.ArrayList;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver.Type;
import net.licks92.WirelessRedstone.Channel.WirelessReceiverInverter;
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

	public WireBox(WirelessRedstone wirelessRedstone)
	{
		this.plugin = wirelessRedstone;
	}

	/**
	 * @param data - The line of the sign
	 * 
	 * @return true if string corresponds to the tag of the transmitter.
	 */
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

	/**
	 * @param data - The line of the sign
	 * 
	 * @return true if string corresponds to the tag of the receiver.
	 */
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
	
	/**
	 * @param data - The line of the sign
	 * 
	 * @return true if string corresponds to the tag of the screen.
	 */
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
	/**
	 * @param data - The line of the sign
	 * 
	 * @return true if the string corresponds to the tag of the inverter receiver type
	 */
	public boolean isReceiverInverter(String data)
	{
		for(String tag : WirelessRedstone.strings.tagsReceiverInverterType)
		{
			if(data.toLowerCase().equals(tag.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param data - The line of the sign
	 * 
	 * @return true if the string corresponds to the tag of the default receiver type
	 */
	public boolean isReceiverDefault(String data)
	{
		for(String tag : WirelessRedstone.strings.tagsReceiverDefaultType)
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

	public boolean addWirelessReceiver(String cname, Block cblock, Player player, Type type)
	{
		WirelessRedstone.getWRLogger().debug("Adding a receiver at location " 
				+ cblock.getLocation().getBlockX() + ","
				+ cblock.getLocation().getBlockY() + ","
				+ cblock.getLocation().getBlockZ() + " in the world "
				+ cblock.getLocation().getWorld().getName() + " with the channel name "
				+ cname + " and with the type " + type
				+ " by the player " + player.getName());
		
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
			channel = new WirelessChannel(cname);
			channel.addOwner(player.getName());
			WirelessReceiver receiver;
			switch(type)
			{
			case Default:
				receiver = new WirelessReceiver();
				
			case Inverter:
				receiver = new WirelessReceiverInverter();
				
			default:
				receiver = new WirelessReceiver();
			}
			receiver.setOwner(player.getName());
			receiver.setWorld(loc.getWorld().getName());
			receiver.setX(loc.getBlockX());
			receiver.setY(loc.getBlockY());
			receiver.setZ(loc.getBlockZ());
			receiver.setDirection(cblock.getData());
			receiver.setisWallSign(isWallSign);
			channel.addReceiver(receiver);
			if(!WirelessRedstone.config.createWirelessChannel(channel))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			WirelessRedstone.cache.update();
			return true;
		}
		else
		{
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
			WirelessReceiver receiver;
			switch(type)
			{
			case Default:
				receiver = new WirelessReceiver();
				
			case Inverter:
				receiver = new WirelessReceiverInverter();
				
			default:
				receiver = new WirelessReceiver();
			}
			receiver.setOwner(player.getName());
			receiver.setWorld(loc.getWorld().getName());
			receiver.setX(loc.getBlockX());
			receiver.setY(loc.getBlockY());
			receiver.setZ(loc.getBlockZ());
			receiver.setDirection(cblock.getData());
			receiver.setisWallSign(isWallSign);
			WirelessRedstone.config.createWirelessPoint(cname, receiver);
			player.sendMessage(WirelessRedstone.strings.playerExtendedChannel);
			WirelessRedstone.cache.update();
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
			channel = new WirelessChannel(cname);
			channel.addOwner(player.getName());
			WirelessTransmitter transmitter = new WirelessTransmitter();
			transmitter.setOwner(player.getName());
			transmitter.setWorld(loc.getWorld().getName());
			transmitter.setX(loc.getBlockX());
			transmitter.setY(loc.getBlockY());
			transmitter.setZ(loc.getBlockZ());
			transmitter.setDirection(cblock.getData());
			transmitter.setisWallSign(isWallSign);
			channel.addTransmitter(transmitter);
			if(!WirelessRedstone.config.createWirelessChannel(channel))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			WirelessRedstone.cache.update();
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
			WirelessRedstone.cache.update();
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
			channel = new WirelessChannel(cname);
			channel.addOwner(player.getName());
			WirelessScreen screen = new WirelessScreen();
			screen.setOwner(player.getName());
			screen.setWorld(loc.getWorld().getName());
			screen.setX(loc.getBlockX());
			screen.setY(loc.getBlockY());
			screen.setZ(loc.getBlockZ());
			screen.setDirection(cblock.getData());
			screen.setisWallSign(isWallSign);
			channel.addScreen(screen);
			if(!WirelessRedstone.config.createWirelessChannel(channel))
			{
				player.sendMessage(WirelessRedstone.strings.channelNameContainsInvalidCaracters);
				return false;
			}
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			WirelessRedstone.cache.update();
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
				WirelessRedstone.cache.update();
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
		// Remember that here is the face where the text can be seen, not the face of the block on where the sign is.
		
		case 0x2: //North
			face = BlockFace.SOUTH;
			break;
			
		case 0x3: //South
			face = BlockFace.NORTH;
			break;
			
		case 0x4: //West
			face = BlockFace.EAST;
			break;
			
		case 0x5: //East
			face = BlockFace.WEST;
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
				|| tempBlock.getType() == Material.REDSTONE_LAMP_OFF
				|| tempBlock.getType() == Material.LEAVES)
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
				|| tempBlock.getType() == Material.REDSTONE_LAMP_OFF
				|| tempBlock.getType() == Material.LEAVES)
		{
			return false;
		}
		else
			return true;
	}
	
	public boolean isValidName(String channelName)
	{
		return true;
	}

	public ArrayList<Location> getReceiverLocations(WirelessChannel channel)
	{
		ArrayList<Location> returnlist = new ArrayList<Location>();
		for (WirelessReceiver receiver : channel.getReceivers())
		{
			returnlist.add(receiver.getLocation());
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
			returnlist.add(screen.getLocation());
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

	public void removeReceiverAt(final Location loc, final boolean byplayer)
	{
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
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
							WirelessRedstone.config.removeWirelessReceiver(channel.getName(), loc);
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
		if(WirelessRedstone.config.removeWirelessReceiver(cname, loc))
		{
			WirelessRedstone.cache.update();
			return true;
		}
		else
			return false;
	}

	public boolean removeWirelessTransmitter(String cname, Location loc)
	{
		if(WirelessRedstone.config.removeWirelessTransmitter(cname, loc))
		{
			WirelessRedstone.cache.update();
			return true;
		}
		else
			return false;
	}
	
	public boolean removeWirelessScreen(String cname, Location loc)
	{
		if(WirelessRedstone.config.removeWirelessScreen(cname, loc))
		{
			WirelessRedstone.cache.update();
			return true;
		}
		else
			return false;
	}

	public void removeSigns(WirelessChannel channel)
	{
		try
		{
			for (IWirelessPoint point : channel.getReceivers())
			{
				point.getLocation().getBlock().setType(Material.AIR);
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
				point.getLocation().getBlock().setType(Material.AIR);
			}
		}
		catch(NullPointerException ex)
		{
			//When there isn't any transmitter, it'll throw this exception.
		}
		
		try
		{
			for(IWirelessPoint point : channel.getScreens())
			{
				point.getLocation().getBlock().setType(Material.AIR);
			}
		}
		catch(NullPointerException ex)
		{
			//When there isn't any screen, it'll throw this exception.
		}
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