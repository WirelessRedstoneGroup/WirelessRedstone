package net.licks92.WirelessRedstone.Channel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.material.RedstoneTorch;

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
	public BlockFace getDirection() {
		return WirelessRedstone.WireBox.intDirectionToBlockFace(direction);
	}

	@Override
	/**
	 * You should ALWAYS use the setDirection(BlockFace) method.
	 * 
	 * @param direction
	 */
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
		map.put("direction", this.direction);
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
				block.setType(Material.REDSTONE_TORCH_ON);
				block.getState().update();
			}
		}
		else if (block.getType() == Material.WALL_SIGN)
		{
			org.bukkit.material.Sign data = (org.bukkit.material.Sign) block.getState().getData();
			
			if (!WirelessRedstone.WireBox.isValidWallLocation(block))
			{
				WirelessRedstone.WireBox.signWarning(block, 1);
			}
			else
			{
				/**
				 * Here we have a problem with bukkit. The wall sign is not facing the same direction in game as the torch,
				 * but the torch should be facing the same direction. It's currently a bug of bukkit (1.6.2-R1.0)
				 */
				block.setType(Material.REDSTONE_TORCH_ON);
				block.getState().setType(Material.REDSTONE_TORCH_ON);
				RedstoneTorch torch = new RedstoneTorch();
				torch.setFacingDirection(data.getFacing());
				block.getState().setData(torch);
				block.getState().update();
				WirelessRedstone.getWRLogger().debug("Wall_sign facing to " + data.getFacing() + " and attached face " + data.getAttachedFace());
				WirelessRedstone.getWRLogger().debug("Torch on the wall facing to " + torch.getFacing() + " and attached face " + torch.getAttachedFace());
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
		
		org.bukkit.material.Sign sign;
		
		if (getisWallSign())
		{
			sign = new org.bukkit.material.Sign(Material.WALL_SIGN);
			block.setType(Material.WALL_SIGN);
		}
		else
		{
			block.setType(Material.SIGN_POST);
			sign = new org.bukkit.material.Sign(Material.SIGN_POST);
		}
		sign.setFacingDirection(getDirection());
		block.getState().setData(sign);
		block.getState().update();

		if (block.getState() instanceof Sign) {
			Sign signtemp = (Sign) block.getState();
			signtemp.setLine(0, WirelessRedstone.strings.tagsReceiver.get(0));
			signtemp.setLine(1, channelName);
			signtemp.setLine(2, WirelessRedstone.strings.tagsReceiverDefaultType.get(0));
			signtemp.update(true);
		}
	}
}