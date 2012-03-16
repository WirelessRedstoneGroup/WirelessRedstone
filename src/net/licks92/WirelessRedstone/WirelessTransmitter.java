package net.licks92.WirelessRedstone;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.world.WorldEvent;

public class WirelessTransmitter implements Serializable, IWirelessPoint
{
	/**
	 * Wireless Transmitter
	 * Variables are integrated in the config file
	 */
	private static final long serialVersionUID = 7054486521728647260L;
	private String owner;
	private int x;
	private int y;
	private int z;
	private String world;
	private int direction = 0;
	private boolean iswallsign = false;

	public WirelessTransmitter()
	{
		
	}

	@Override
	public String getOwner()
	{
		return this.owner;
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getZ() {
		return this.z;
	}

	@Override
	public String getWorld() {
		return this.world;
	}

	@Override
	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void setZ(int z) {
		this.z = z;
	}

	@Override
	public void setWorld(String world) {
		this.world = world;
	}

	@Override
	public int getDirection() {
		return direction;
	}

	@Override
	public void setDirection(int direction) {
		this.direction = direction;
	}

	@Override
	public boolean getisWallSign() {
		return iswallsign;
	}

	@Override
	public void setisWallSign(boolean iswallsign) {
		this.iswallsign = iswallsign;
	}
}
