package net.licks92.WirelessRedstone.Channel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="wirelesschannels")
@SerializableAs("WirelessChannel")
public class WirelessChannel implements ConfigurationSerializable, Serializable
{
	private static final long serialVersionUID = -3322590857684087871L;
	@Id
	private int id;
	@NotNull
	private String name;
	@NotNull
	private boolean locked;
	
	private List<String> owners = new LinkedList<String>();
	private List<WirelessTransmitter> transmitters = new LinkedList<WirelessTransmitter>();
	private List<WirelessReceiver> receivers = new LinkedList<WirelessReceiver>();
	private List<WirelessScreen> screens = new LinkedList<WirelessScreen>();
	
	public WirelessChannel(String name)
	{
		this.setName(name);
	}
	
	@SuppressWarnings("unchecked")
	public WirelessChannel(Map<String, Object> map)
	{
		this.setId((Integer) map.get("id"));
		this.setName((String) map.get("name"));
		this.setOwners((List<String>) map.get("owners"));
		this.setReceivers((List<WirelessReceiver>) map.get("receivers"));
		this.setTransmitters((List<WirelessTransmitter>) map.get("transmitters"));
		this.setScreens((List<WirelessScreen>) map.get("screens"));
		try
		{
			this.setLocked((Boolean) map.get("locked"));
		}
		catch (NullPointerException ex)
		{
			
		}
	}
	
	/**
	 * This method is almost the same as turnOn(), except that the channel will be on for a temporary time only!
	 * 
	 * @param time - Time spent until the channel turns off.
	 */
	public void turnOn(int time)
	{
		turnOn();
		Bukkit.getScheduler().runTaskLaterAsynchronously(Bukkit.getPluginManager().getPlugin("WirelessRedstone"), new Runnable()
		{
			@Override
			public void run()
			{
				turnOff();
			}
			
		}, time);
	}
	
	/**
	 * Simply turns on the wireless channel, means that all the receivers and screens will turn on.
	 */
	public void turnOn()
	{
		//Turning on the receivers ONLY if the channel isn't active.
		try
		{
			//Change receivers
			for (Location receiver : WirelessRedstone.WireBox.getReceiverLocations(getName()))
			{
				if(receiver.getWorld() == null)
					continue; // World currently not loaded
				
				if (receiver.getBlock().getType() == Material.SIGN_POST)
				{
					if (!WirelessRedstone.WireBox.isValidLocation(receiver.getBlock()))
					{
						WirelessRedstone.WireBox.signWarning(receiver.getBlock(), 1);
					}
					else
					{
						receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), (byte) 0x5,true);
						receiver.getBlock().getState().update();
					}
				}
				else if (receiver.getBlock().getType() == Material.WALL_SIGN)
				{
					byte data = receiver.getBlock().getData(); // Correspond to the direction of the wall sign
					if (data == 0x2) //South
					{
						if (!WirelessRedstone.WireBox.isValidWallLocation(receiver.getBlock()))
						{
							WirelessRedstone.WireBox.signWarning(receiver.getBlock(), 1);
						}
						else
						{
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x4, true);
							receiver.getBlock().getState().update();
						}
					}
					else if (data == 0x3) //North
					{
						if (!WirelessRedstone.WireBox.isValidWallLocation(receiver.getBlock()))
						{
							WirelessRedstone.WireBox.signWarning(receiver.getBlock(), 1);
						}
						else
						{
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x3, true);
							receiver.getBlock().getState().update();
						}
					}
					else if (data == 0x4) //East
					{
						if (!WirelessRedstone.WireBox.isValidWallLocation(receiver.getBlock()))
						{
							WirelessRedstone.WireBox.signWarning(receiver.getBlock(), 1);
						}
						else
						{
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x2, true);
							receiver.getBlock().getState().update();
						}
					}
					else if (data == 0x5) //West
					{
						if (!WirelessRedstone.WireBox.isValidWallLocation(receiver.getBlock()))
						{
							WirelessRedstone.WireBox.signWarning(receiver.getBlock(), 1);
						}
						else
						{
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x1, true);
							receiver.getBlock().getState().update();
						}
					}
					else // Not West East North South ...
					{
						WirelessRedstone.getWRLogger().info("Strange Data !");
					}
				}
			}
			
			//Turning on screens
			for(Location screen : WirelessRedstone.WireBox.getScreenLocations(getName()))
			{
				String str = ChatColor.GREEN + "ACTIVE";
				Sign sign = (Sign) screen.getBlock().getState();
				sign.setLine(2, str);
				sign.update();
			}
		}
		catch (RuntimeException e) 
		{
			WirelessRedstone.getWRLogger().severe("Error while updating redstone event onBlockRedstoneChange for Receivers. Turn on the Debug Mode to get more informations.");
			if(WirelessRedstone.config.getDebugMode())
				e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Simply turns off the channel : all the receivers and screens turn off.
	 */
	public void turnOff()
	{
		try
		{
				//Change receivers
				for (WirelessReceiver receiver : getReceivers())
				{
					if(receiver.getWorld() == null)
						continue; // World currently not loaded
					
					Location rloc = WirelessRedstone.WireBox.getPointLocation(receiver);
					Block othersign = rloc.getBlock();

					othersign.setType(Material.AIR);

					if (receiver.getisWallSign())
					{
						othersign.setType(Material.WALL_SIGN);
						othersign.setTypeIdAndData(Material.WALL_SIGN.getId(),(byte) receiver.getDirection(), true);
						othersign.getState().update();
					}
					else
					{
						othersign.setType(Material.SIGN_POST);
						othersign.setTypeIdAndData(Material.SIGN_POST.getId(),
								(byte) receiver.getDirection(), true);
						othersign.getState().update();
					}

					if (othersign.getState() instanceof Sign) {
						Sign signtemp = (Sign) othersign.getState();
						signtemp.setLine(0, "[WRr]");
						signtemp.setLine(1, getName());
						signtemp.update(true);
					}
				}
				
				//Change screens
				for(Location screen : WirelessRedstone.WireBox.getScreenLocations(getName()))
				{
					String str = ChatColor.RED + "INACTIVE";
					Sign sign = (Sign) screen.getBlock().getState();
					sign.setLine(2, str);
					sign.update();
				}
		}
		catch (RuntimeException e)
		{
			WirelessRedstone.getWRLogger().severe("Error while updating redstone onBlockRedstoneChange for Screens , turn on the Debug Mode to get more informations.");
			if(WirelessRedstone.config.getDebugMode())
				e.printStackTrace();
			return;
		}
	}
	
	/**
	 * @return true if one of the transmitters is active, false if they are all off.
	 */
	public boolean isActive()
	{
		for(WirelessTransmitter t : getTransmitters())
		{
			Location loc = new Location(Bukkit.getWorld(t.getWorld()), t.getX(), t.getY(), t.getZ());
			Block block = loc.getBlock();
			if(block.getState() instanceof Sign)
			{
				if(block.isBlockIndirectlyPowered() || block.isBlockIndirectlyPowered())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public void removeReceiverAt(Location loc)
	{
		for(WirelessReceiver receiver : receivers)
		{
			if(receiver.getX() == loc.getBlockX() && receiver.getZ() == loc.getBlockZ() && receiver.getY() == loc.getBlockY())
			{
				receivers.remove(receiver);
				return;
			}
		}
	}
	
	public void removeTransmitterAt(Location loc)
	{
		for(WirelessTransmitter transmitter : transmitters)
		{
			if(transmitter.getX() == loc.getBlockX() && transmitter.getZ() == loc.getBlockZ() && transmitter.getY() == loc.getBlockY())
			{
				transmitters.remove(transmitter);
				return;
			}
		}
	}
	
	public void removeScreenAt(Location loc)
	{
		for(WirelessScreen screen : screens)
		{
			if(screen.getX() == loc.getBlockX() && screen.getZ() == loc.getBlockZ() && screen.getY() == loc.getBlockY())
			{
				screens.remove(screen);
				return;
			}
		}
	}
	
	public boolean removeOwner(String username)
	{
		boolean ret = this.owners.remove(username);
		
		return ret;
	}

	public void addTransmitter(WirelessTransmitter transmitter)
	{
		if (transmitters == null)
			transmitters = new ArrayList<WirelessTransmitter>();

		transmitters.add(transmitter);
	}

	public void addReceiver(WirelessReceiver receiver)
	{
		if (receivers == null)
			receivers = new ArrayList<WirelessReceiver>();

		receivers.add(receiver);
	}
	
	public void addScreen(WirelessScreen screen)
	{
		if (screens == null)
			screens = new LinkedList<WirelessScreen>();
		
		screens.add(screen);
	}

	public void addOwner(String username)
	{
		if (this.owners == null) 
			this.owners = new LinkedList<String>();
		
		if(!this.owners.contains(username))
			this.owners.add(username);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setLocked(boolean value) {
		this.locked = value;
	}

	public void setOwners(List<String> owners) {
		this.owners = owners;
	}

	public void setTransmitters(List<WirelessTransmitter> transmitters)
	{
		if(transmitters != null)
			this.transmitters = transmitters;
		else
			this.transmitters = new LinkedList<WirelessTransmitter>();
	}

	public void setReceivers(List<WirelessReceiver> receivers)
	{
		if(receivers != null)
			this.receivers = receivers;
		else
			this.receivers = new LinkedList<WirelessReceiver>();
	}
	
	public void setScreens(List<WirelessScreen> screens)
	{
		if(screens != null)
			this.screens = screens;
		else
			this.screens = new LinkedList<WirelessScreen>();
	}

	public String getName()
	{
		return this.name;
	}
	
	public boolean isLocked()
	{
		return this.locked;
	}

	public List<WirelessTransmitter> getTransmitters()
	{
		try
		{
			return this.transmitters;
		}
		catch (NullPointerException ex)
		{
			return new LinkedList<WirelessTransmitter>();
		}
	}

	public List<WirelessReceiver> getReceivers()
	{
		try
		{
			return this.receivers;
		}
		catch (NullPointerException ex)
		{
			return new LinkedList<WirelessReceiver>();
		}
	}
	
	public List<WirelessScreen> getScreens()
	{
		try
		{
			return this.screens;
		}
		catch (NullPointerException ex)
		{
			return new LinkedList<WirelessScreen>();
		}
	}

	public List<String> getOwners()
	{
		try
		{
			return this.owners;
		}
		catch(NullPointerException ex)
		{
			return new LinkedList<String>();
		}
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", getId());
		map.put("name", getName());
		map.put("owners", getOwners());
		map.put("receivers", getReceivers());
		map.put("transmitters", getTransmitters());
		map.put("screens", getScreens());
		map.put("locked", isLocked());
		return map;
	}
}
