package net.licks92.WirelessRedstone.Channel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("WirelessReceiver")
public class WirelessReceiver implements ConfigurationSerializable, IWirelessPoint, Serializable
{
	private static final long serialVersionUID = -7291500732787558150L;
	private String owner;
	private int x;
	private int y;
	private int z;
	private String world;
	private int direction = 0;
	private boolean iswallsign = false;

	public enum Type{
		Default, Inverter, Delayer;
	}
	
	public WirelessReceiver()
	{
		
	}
	
	/**
	 * IMPORTANT : You shouldn't have to create a WirelessReceiver with this method.
	 * It's used by the bukkit serialization system.
	 */
	public WirelessReceiver(Map<String, Object> map)
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
	public String getOwner()
	{
		return this.owner;
	}

	@Override
	public int getX()
	{
		return this.x;
	}

	@Override
	public int getY()
	{
		return this.y;
	}

	@Override
	public int getZ()
	{
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
		return this.direction;
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
	public void setisWallSign(boolean iswallsign)
	{
		this.iswallsign = iswallsign;
	}
	
	/**
	 * This method should be called ONLY by the bukkit serialization system!
	 */
	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("direction", getDirection());
		map.put("isWallSign", getisWallSign());
		map.put("owner", getOwner());
		map.put("world", getWorld());
		map.put("x", getX());
		map.put("y", getY());
		map.put("z", getZ());
		return map;
	}
	
	/**
	 * Don't use this method!
	 * 
	 * @param map
	 */
	public void deserialize(Map<String,Object> map)
	{
		this.setDirection((Integer) map.get("direction"));
		this.setisWallSign((Boolean) map.get("isWallSign"));
		this.setOwner((String) map.get("owner"));
		this.setWorld((String) map.get("world"));
		this.setX((Integer) map.get("x"));
		this.setY((Integer) map.get("y"));
		this.setZ((Integer) map.get("z"));
	}
	
	/**
	 * @return A Location object made with the receiver data.
	 */
	public Location getLocation()
	{
		Location loc = new Location(Bukkit.getWorld(world),x,y,z);
		return loc;
	}
	
	public void turnOn(String channelName)
	{
		Location loc = getLocation();
		
		if(loc.getWorld() == null) // If the world is not loaded or doesn't exist
			return;
		
		Block block = loc.getBlock();
		
		if (block.getType() == Material.SIGN_POST)
		{
			if (!WirelessRedstone.WireBox.isValidLocation(block))
			{
				WirelessRedstone.WireBox.signWarning(block, 1);
			}
			else
			{
				block.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), (byte) 0x5,true);
				block.getState().update();
			}
		}
		else if (block.getType() == Material.WALL_SIGN)
		{
			byte data = block.getData(); // Correspond to the direction of the wall sign
			if (data == 0x2) //South
			{
				if (!WirelessRedstone.WireBox.isValidWallLocation(block))
				{
					WirelessRedstone.WireBox.signWarning(block, 1);
				}
				else
				{
					block.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x4, true);
					block.getState().update();
				}
			}
			else if (data == 0x3) //North
			{
				if (!WirelessRedstone.WireBox.isValidWallLocation(block))
				{
					WirelessRedstone.WireBox.signWarning(block, 1);
				}
				else
				{
					block.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x3, true);
					block.getState().update();
				}
			}
			else if (data == 0x4) //East
			{
				if (!WirelessRedstone.WireBox.isValidWallLocation(block))
				{
					WirelessRedstone.WireBox.signWarning(block, 1);
				}
				else
				{
					block.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x2, true);
					block.getState().update();
				}
			}
			else if (data == 0x5) //West
			{
				if (!WirelessRedstone.WireBox.isValidWallLocation(block))
				{
					WirelessRedstone.WireBox.signWarning(block, 1);
				}
				else
				{
					block.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x1, true);
					block.getState().update();
				}
			}
			else // Not West East North South ...
			{
				WirelessRedstone.getWRLogger().info("Strange Data !");
			}
		}
	}
	
	public void turnOff(String channelName)
	{
		Location loc = getLocation();
		if(loc.getWorld() == null)
			return;
		
		Block block = loc.getBlock();

		block.setType(Material.AIR);

		if (getisWallSign())
		{
			block.setType(Material.WALL_SIGN);
			block.setTypeIdAndData(Material.WALL_SIGN.getId(),(byte) getDirection(), true);
			block.getState().update();
		}
		else
		{
			block.setType(Material.SIGN_POST);
			block.setTypeIdAndData(Material.SIGN_POST.getId(),
					(byte) getDirection(), true);
			block.getState().update();
		}

		if (block.getState() instanceof Sign) {
			Sign signtemp = (Sign) block.getState();
			signtemp.setLine(0, "[WRr]");
			signtemp.setLine(1, channelName);
			signtemp.update(true);
		}
	}
}