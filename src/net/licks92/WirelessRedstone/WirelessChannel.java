package net.licks92.WirelessRedstone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.Location;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="wirelesschannels")
public class WirelessChannel implements Serializable
{
	private static final long serialVersionUID = -3322590857684087871L;
	@Id
	private int id;
	@NotNull
	private String name;
	
	private List<String> owners;
	private List<WirelessTransmitter> transmitters;
	private List<WirelessReceiver> receivers;

	public WirelessChannel()
	{
		
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
	
	public boolean removeOwner(String username)
	{
		return this.owners.remove(username);
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

	public void addTransmitter(WirelessTransmitter transmitter) {
		if (transmitters == null)
			transmitters = new ArrayList<WirelessTransmitter>();

		transmitters.add(transmitter);
	}

	public void addReceiver(WirelessReceiver receiver) {
		if (receivers == null)
			receivers = new ArrayList<WirelessReceiver>();

		receivers.add(receiver);
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
		this.transmitters = transmitters;
	}

	public void setReceivers(List<WirelessReceiver> receivers)
	{
		this.receivers = receivers;
	}

	public String getName()
	{
		return this.name;
	}

	public List<WirelessTransmitter> getTransmitters()
	{
		return this.transmitters;
	}

	public List<WirelessReceiver> getReceivers()
	{
		return this.receivers;
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
}
