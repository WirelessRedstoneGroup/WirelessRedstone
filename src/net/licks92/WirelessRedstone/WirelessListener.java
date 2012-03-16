package net.licks92.WirelessRedstone;

import java.util.List;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class WirelessListener implements Listener
{
	private final WirelessRedstone plugin;

	public WirelessListener(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event)
	{
		if (plugin.WireBox.isReceiver(event.getLine(0)) || plugin.WireBox.isTransmitter(event.getLine(0)))
		{
			if (!plugin.permissionsHandler.hasPermission(event.getPlayer(),"WirelessRedstone.createsign"))
			{
				event.getBlock().setType(Material.AIR);
				event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
				event.getPlayer().sendMessage("[WirelessRedstone] You don't have the permission to create this sign!");
				return;
			}

			if (event.getLine(1) == null)
			{
				event.getBlock().setType(Material.AIR);
				event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
				event.getPlayer().sendMessage("[WirelessRedstone] No Channelname given!");
				return;
			}

			String cname = event.getLine(1);

			if (!plugin.WireBox.hasAccessToChannel(event.getPlayer(), cname))
			{
				event.getBlock().setType(Material.AIR);
				event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
				event.getPlayer().sendMessage("[WirelessRedstone] You don't have the permission to create this sign!");
				return;
			}

			if (plugin.WireBox.isReceiver(event.getLine(0)))
			{
				plugin.WireBox.AddWirelessReceiver(cname, event.getBlock(), event.getPlayer());
			} else {
				plugin.WireBox.addWirelessTransmitter(cname, event.getBlock(),
						event.getPlayer());
			}
		}
	}

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if (event.getChangedType() == Material.REDSTONE_TORCH_ON)
		{
			
		}
	}

	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event)
	{
		if (!(event.getBlock().getState() instanceof Sign))
		{
			return;
		}
		
		Sign signObject = (Sign) event.getBlock().getState();
		
		// Lets check if the sign is a Transmitter and if the channel name not
		// is empty
		if (!plugin.WireBox.isTransmitter(signObject.getLine(0)) || signObject.getLine(1) == null || signObject.getLine(1) == "")
		{
			return;
		}
		if (event.getBlock().isBlockPowered() || event.getBlock().isBlockIndirectlyPowered())
		{
			try
			{
				for (Location receiver : plugin.WireBox.getReceiverLocations(signObject.getLine(1)))
				{
					if (receiver.getBlock().getType() == Material.SIGN_POST)
					{
						if (receiver.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
						{
							this.plugin.WireBox.removeReceiverAt(receiver,false);
						}
						receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), (byte) 0x5,true);
					}
					else if (receiver.getBlock().getType() == Material.WALL_SIGN)
					{
						if (receiver.getBlock().getData() == 0x2)
						{
							if (receiver.getBlock().getRelative(BlockFace.WEST).getType() == Material.AIR)
							{
								this.plugin.WireBox.removeReceiverAt(receiver,false);
							}
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x4, true);
						}
						else if
						(receiver.getBlock().getData() == 0x3)
						{
							if (receiver.getBlock().getRelative(BlockFace.EAST).getType() == Material.AIR)
							{
								this.plugin.WireBox.removeReceiverAt(receiver,false);
							}
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x3, true);
						}
						else if (receiver.getBlock().getData() == 0x4)
						{
							if (receiver.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.AIR)
							{
								this.plugin.WireBox.removeReceiverAt(receiver,false);
							}
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x2, true);
						}
						else if (receiver.getBlock().getData() == 0x5)
						{
							if (receiver.getBlock().getRelative(BlockFace.NORTH).getType() == Material.AIR)
							{
								this.plugin.WireBox.removeReceiverAt(receiver,false);
							}
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x1, true);
						}
						else
						{
							WirelessRedstone.getStackableLogger().info("Weirdest sign Efar!");
						}
					}

				}
			}
			catch (Exception e) 
			{
				WirelessRedstone.getStackableLogger().severe(e.getMessage());
				return;
			}
		}
		else if (!event.getBlock().isBlockPowered())
		{
			try
			{
				for (WirelessReceiver receiver : plugin.WireBox.getChannel(signObject.getLine(1)).getReceivers())
				{
					Location rloc = plugin.WireBox.getPointLocation(receiver);
					Block othersign = rloc.getBlock();

					othersign.setType(Material.AIR);

					if (receiver.getisWallSign())
					{
						othersign.setType(Material.WALL_SIGN);
						othersign.setTypeIdAndData(Material.WALL_SIGN.getId(),(byte) receiver.getDirection(), true);
					}
					else
					{
						othersign.setType(Material.SIGN_POST);
						othersign.setTypeIdAndData(Material.SIGN_POST.getId(),
								(byte) receiver.getDirection(), true);
					}

					if (othersign.getState() instanceof Sign) {
						Sign signtemp = (Sign) othersign.getState();
						signtemp.setLine(0, "[WRr]");
						signtemp.setLine(1, signObject.getLine(1));
						signtemp.update(true);
					}
				}
			}
			catch (Exception e)
			{
				WirelessRedstone.getStackableLogger().severe(e.getMessage());
				return;
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if ((event.getBlock().getState() instanceof Sign))
		{
			Sign signObject = (Sign) event.getBlock();
			if (plugin.WireBox.isReceiver(signObject.getLine(0)))
			{
				if (plugin.WireBox.hasAccessToChannel(event.getPlayer(),signObject.getLine(1)))
				{
					if (plugin.WireBox.RemoveWirelessReceiver(signObject.getLine(1), event.getBlock().getLocation()))
					{
						if (plugin.WireBox.getChannel(signObject.getLine(1)).getTransmitters().size() == 0
								&& plugin.WireBox.getChannel(signObject.getLine(1)).getReceivers().size() == 0)
						{
							plugin.WireBox.removeChannel(signObject.getLine(1));
							event.getPlayer().sendMessage("[WirelessRedstone] Succesfully removed this sign! Channel removed, no more signs in the worlds.");
						}
						else
						{
							event.getPlayer().sendMessage("[WirelessRedstone] Succesfully removed this sign!");
						}
					}
					else
					{
						event.getPlayer().sendMessage("[WirelessRedstone] Something went wrong!");
					}
				}
				else
				{
					event.getPlayer().sendMessage("[WirelessRedstone] You are not allowed to remove this sign!");
					event.setCancelled(true);
				}
				return;
			}
			else if (plugin.WireBox.isTransmitter(signObject.getLine(0)))
			{
				if (plugin.WireBox.hasAccessToChannel(event.getPlayer(),signObject.getLine(1)))
				{
					if (plugin.WireBox.RemoveWirelessTransmitter(signObject.getLine(1), event.getBlock().getLocation()))
					{
						event.getPlayer().sendMessage("[WirelessRedstone] Succesfully removed this sign!");
						if (plugin.WireBox.getChannel(signObject.getLine(1)).getTransmitters().size() == 0)
						{
							event.getPlayer().sendMessage("[WirelessRedstone] No other Transmitters found, Resettings Power data on receivers to sign.");
							for (WirelessReceiver receiver : plugin.WireBox.getChannel(signObject.getLine(1)).getReceivers())
							{
								Location rloc = plugin.WireBox.getPointLocation(receiver);
								Block othersign = rloc.getBlock();
								if (receiver.getisWallSign())
								{
									othersign.getWorld().getBlockAt(rloc).setTypeIdAndData(Material.WALL_SIGN.getId(),(byte) receiver.getDirection(),true);
								}
								else
								{
									othersign.getWorld().getBlockAt(rloc).setTypeIdAndData(Material.SIGN_POST.getId(),(byte) receiver.getDirection(),true);
								}
								if (othersign.getState() instanceof Sign)
								{
									Sign signtemp = (Sign) othersign.getState();
									signtemp.setLine(0, "[WRr]");
									signtemp.setLine(1, signObject.getLine(1));
									
									if (receiver.getisWallSign())
									{
										signtemp.setData(new MaterialData(Material.WALL_SIGN,(byte) receiver.getDirection()));
									}
									else
									{
										signtemp.setData(new MaterialData(Material.SIGN_POST,(byte) receiver.getDirection()));
									}
									signtemp.update(true);
								}
							}
						}
					}
					else 
					{
						event.getPlayer().sendMessage("[WirelessRedstone] Something went wrong!");
					}
				}
				else
				{
					event.getPlayer().sendMessage("[WirelessRedstone] You are not allowed to remove this sign!");
					event.setCancelled(true);
				}
				return;
			}
		}
		else if(event.getBlock().getType().equals(Material.REDSTONE_TORCH_ON)) 
		{
			for (Location loc : plugin.WireBox.getAllReceiverLocations())
			{
				if (loc.equals(event.getBlock().getLocation()))
				{
					event.getPlayer().sendMessage("[WirelessRedstone] You cannot break my magic torches my friend!");
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event)
	{
		if (event.getToBlock().getType() == Material.REDSTONE_TORCH_ON)
		{
			for (Location loc : plugin.WireBox.getAllReceiverLocations())
			{
				if (loc.getBlockX() == event.getToBlock().getLocation().getBlockX() || loc.getBlockY() == event.getToBlock().getLocation().getBlockY() || loc.getBlockZ() == event.getToBlock().getLocation().getBlockZ())
				{
					plugin.WireBox.removeReceiverAt(loc, false);
					return;
				}
			}
		}
	}

	// Method borrowed from MinecraftMania! Credits to Afforess!
	// https://github.com/Afforess/MinecartMania/blob/master/src/com/afforess/minecartmaniacore/api/MinecartManiaCoreWorldListener.java
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		if (!event.isCancelled())
		{
			if (WirelessRedstone.config.isCancelChunkUnloads())
			{
				try
				{
					List<IWirelessPoint> points = plugin.WireBox.getAllSigns();
					for (IWirelessPoint point : points)
					{
						if (Math.abs(event.getChunk().getX() - plugin.WireBox.getPointLocation(point).getBlock().getChunk().getX()) > WirelessRedstone.config.getChunkUnloadRange())
						{
							continue;
						}
						if (Math.abs(event.getChunk().getZ() - plugin.WireBox.getPointLocation(point).getBlock().getChunk().getZ()) > WirelessRedstone.config.getChunkUnloadRange())
						{
							continue;
						}

						WirelessRedstone.getStackableLogger().finer("Chunk prevented from unloading!");
						event.setCancelled(true);
						return;
					}
				} catch (Exception e) {
					return;
				}
			}
		}
	}
}
