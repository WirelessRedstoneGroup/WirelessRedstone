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


import org.bukkit.Location;
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
	
	private List<String> owners;
	private List<WirelessTransmitter> transmitters = new LinkedList<WirelessTransmitter>();
	private List<WirelessReceiver> receivers = new LinkedList<WirelessReceiver>();
	private List<WirelessScreen> screens = new LinkedList<WirelessScreen>();

	public WirelessChannel()
	{
		
	}
	
	@SuppressWarnings("unchecked")
	public WirelessChannel(Map<String, Object> map)
	{
		id = (Integer) map.get("id");
		name = (String) map.get("name");
		owners = (List<String>) map.get("owners");
		setTransmitters((List<WirelessTransmitter>) map.get("transmitters"));
		setReceivers((List<WirelessReceiver>) map.get("receivers"));
		setScreens((List<WirelessScreen>) map.get("screens"));
	}
	
	public void removeReceiverAt(Location loc)
	{
		@SuppressWarnings("unused")
		int i = 0;
		for(WirelessReceiver receiver : receivers)
		{
			if(receiver.getX() == loc.getBlockX() && receiver.getZ() == loc.getBlockZ() && receiver.getY() == loc.getBlockY())
			{
				receivers.remove(receiver);
				return;
			}
			i++;
		}
	}
	
	public void removeTransmitterAt(Location loc)
	{
		int i = 0;
		for(WirelessTransmitter transmitter : transmitters)
		{
			if(transmitter.getX() == loc.getBlockX() && transmitter.getZ() == loc.getBlockZ() && transmitter.getY() == loc.getBlockY())
			{
				transmitters.remove(i);
				return;
			}
			i++;
		}
	}
	
	public void removeScreenAt(Location loc)
	{
		int i = 0;
		for(WirelessScreen screen : screens)
		{
			if(screen.getX() == loc.getBlockX() && screen.getZ() == loc.getBlockZ() && screen.getY() == loc.getBlockY())
			{
				screens.remove(i);
				return;
			}
			i++;
		}
	}
	
	public boolean removeOwner(String username)
	{
		return this.owners.remove(username);
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
			screens = new ArrayList<WirelessScreen>();
		
		screens.add(screen);
	}

	public void addOwner(String username) {
		if (this.owners == null) 
			this.owners = new ArrayList<String>();
		
		if(!this.owners.contains(username))
			this.owners.add(username);
	}

	public void setName(String name) {
		this.name = name;
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
		return this.owners;
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
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public void deserialize(Map<String,Object> map)
	{
		this.setId((Integer) map.get("id"));
		this.setName((String) map.get("name"));
		this.setOwners((List<String>) map.get("owners"));
		this.setReceivers((List<WirelessReceiver>) map.get("receivers"));
		this.setTransmitters((List<WirelessTransmitter>) map.get("transmitters"));
		this.setScreens((List<WirelessScreen>) map.get("screens"));
	}
}
