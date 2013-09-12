package net.licks92.WirelessRedstone.Channel;

import java.util.HashMap;
import java.util.Map;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("WirelessScreen")
public class WirelessScreen implements IWirelessPoint, ConfigurationSerializable
{
	private String owner;
	private int x;
	private int y;
	private int z;
	private String world;
	private int direction = 0;
	private boolean iswallsign = false;
	
	public WirelessScreen()
	{
		
	}
	
	public WirelessScreen(Map<String, Object> map)
	{
		owner = (String) map.get("owner");
		world = (String) map.get("world");
		direction = (Integer) map.get("direction");
		iswallsign = (Boolean) map.get("isWallSign");
		x = (Integer) map.get("x");
		y = (Integer) map.get("y");
		z = (Integer) map.get("z");
	}
	
	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("direction", this.direction);
		map.put("isWallSign", getisWallSign());
		map.put("owner", getOwner());
		map.put("world", getWorld());
		map.put("x", getX());
		map.put("y", getY());
		map.put("z", getZ());
		return map;
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
	public BlockFace getDirection() {
		return WirelessRedstone.WireBox.intDirectionToBlockFace(direction);
	}

	@Override
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	@Override
	public void setDirection(BlockFace face) {
		setDirection(WirelessRedstone.WireBox.blockFace2IntDirection(face));
	}

	@Override
	public boolean getisWallSign() {
		return iswallsign;
	}

	@Override
	public void setisWallSign(boolean iswallsign) {
		this.iswallsign = iswallsign;
	}

	@Override
	public Location getLocation()
	{
		Location loc = new Location(Bukkit.getWorld(world),x,y,z);
		return loc;
	}
	
	public void turnOn()
	{
		String str = ChatColor.GREEN + "ACTIVE";
		Sign sign = (Sign) getLocation().getBlock().getState();
		sign.setLine(2, str);
		sign.update();
	}
	
	public void turnOff()
	{
		String str = ChatColor.RED + "INACTIVE";
		Sign sign = (Sign) getLocation().getBlock().getState();
		sign.setLine(2, str);
		sign.update();
	}
}
