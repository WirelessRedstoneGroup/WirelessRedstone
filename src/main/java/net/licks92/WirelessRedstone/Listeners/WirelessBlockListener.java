package net.licks92.WirelessRedstone.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;

public class WirelessBlockListener implements Listener
{
	private final WirelessRedstone plugin;

	public WirelessBlockListener(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) //Called when a sign is created, and the text edited
	{
		if (plugin.WireBox.isReceiver(event.getLine(0))
				|| plugin.WireBox.isTransmitter(event.getLine(0))
				|| plugin.WireBox.isScreen(event.getLine(0)))
		{
			
			if (!plugin.permissions.canCreateReceiver(event.getPlayer())
					||!plugin.permissions.canCreateTransmitter(event.getPlayer())
					||!plugin.permissions.canCreateScreen(event.getPlayer()))
			{
				event.getBlock().setType(Material.AIR);
				event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
				event.getPlayer().sendMessage(WirelessRedstone.strings.playerCannotCreateSign);
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
				event.getPlayer().sendMessage(WirelessRedstone.strings.playerCannotCreateSign);
				return;
			}
			
			if (plugin.WireBox.isReceiver(event.getLine(0)))
			{
				if(!plugin.WireBox.addWirelessReceiver(cname, event.getBlock(), event.getPlayer()))
				{
					event.setCancelled(true);
					event.getBlock().breakNaturally();
				}
			}
			else if(plugin.WireBox.isTransmitter(event.getLine(0)))
			{
				if(!plugin.WireBox.addWirelessTransmitter(cname, event.getBlock(), event.getPlayer()))
				{
					event.setCancelled(true);
					event.getBlock().breakNaturally();
				}
			}
			
			else if(plugin.WireBox.isScreen(event.getLine(0)))
			{
				if(!plugin.WireBox.addWirelessScreen(cname, event.getBlock(), event.getPlayer()))
				{
					event.setCancelled(true);
					event.getBlock().breakNaturally();
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if (event.getChangedType() == Material.REDSTONE_TORCH_ON)
		{
			// Why is this event here? :p
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
		try
		{
			if(WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).isLocked())
			{
				return;
			}
		}
		catch (NullPointerException ex)
		{
			
		}
		if (event.getBlock().isBlockPowered() || event.getBlock().isBlockIndirectlyPowered())
		{
			//Turning on the receivers
			try
			{
				//Change receivers
				for (Location receiver : plugin.WireBox.getReceiverLocations(signObject.getLine(1)))
				{
					if(receiver.getWorld() == null)
						continue; // World currently not loaded
					
					if (receiver.getBlock().getType() == Material.SIGN_POST)
					{
						if (!plugin.WireBox.isValidLocation(receiver.getBlock()))
						{
							plugin.WireBox.signWarning(receiver.getBlock(), 1);
						}
						else
						{
							receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), (byte) 0x5,true);
						}
					}
					else if (receiver.getBlock().getType() == Material.WALL_SIGN)
					{
						byte data = receiver.getBlock().getData(); // Correspond to the direction of the wall sign
						if (data == 0x2) //South
						{
							if (!plugin.WireBox.isValidWallLocation(receiver.getBlock()))
							{
								plugin.WireBox.signWarning(receiver.getBlock(), 1);
							}
							else
							{
								receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x4, true);
							}
						}
						else if (data == 0x3) //North
						{
							if (!plugin.WireBox.isValidWallLocation(receiver.getBlock()))
							{
								plugin.WireBox.signWarning(receiver.getBlock(), 1);
							}
							else
							{
								receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x3, true);
							}
						}
						else if (data == 0x4) //East
						{
							if (!plugin.WireBox.isValidWallLocation(receiver.getBlock()))
							{
								plugin.WireBox.signWarning(receiver.getBlock(), 1);
							}
							else
							{
								receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x2, true);
							}
						}
						else if (data == 0x5) //West
						{
							if (!plugin.WireBox.isValidWallLocation(receiver.getBlock()))
							{
								plugin.WireBox.signWarning(receiver.getBlock(), 1);
							}
							else
							{
								receiver.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x1, true);
							}
						}
						else // Not West East North South ...
						{
							WirelessRedstone.getStackableLogger().info("Strange Data !");
						}
					}
				}
				
				//Change screens
				for(Location screen : plugin.WireBox.getScreenLocations(signObject.getLine(1)))
				{
					String str = ChatColor.GREEN + "ACTIVE";
					Sign sign = (Sign) screen.getBlock().getState();
					sign.setLine(2, str);
					sign.update();
				}
			}
			catch (RuntimeException e) 
			{
				WirelessRedstone.getStackableLogger().severe("Error while updating redstone event onBlockRedstoneChange 1 :"+e.getClass()+":"+e.getStackTrace());
				return;
			}
		}
		else if (!event.getBlock().isBlockPowered())
		{
			try
			{
				WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(signObject.getLine(1));
				if(channel != null)
				{
					//Change receivers
					for (WirelessReceiver receiver : channel.getReceivers())
					{
						if(receiver.getWorld() == null)
							continue; // World currently not loaded
						
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
					
					//Change screens
					for(Location screen : plugin.WireBox.getScreenLocations(signObject.getLine(1)))
					{
						String str = ChatColor.RED + "INACTIVE";
						Sign sign = (Sign) screen.getBlock().getState();
						sign.setLine(2, str);
						sign.update();
					}
				}
			}
			catch (RuntimeException e)
			{
				WirelessRedstone.getStackableLogger().severe("Error while updating redstone onBlockRedstoneChange 2 :"+e.getClass()+":"+e.getStackTrace());
				return;
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if ((event.getBlock().getState() instanceof Sign))
		{
			Sign signObject = (Sign) event.getBlock().getState();
			if (plugin.WireBox.isReceiver(signObject.getLine(0)))
			{
				if(!WirelessRedstone.config.getSignDrop())
				{
					cancelEvent(event);
				}
				if (plugin.WireBox.hasAccessToChannel(event.getPlayer(),signObject.getLine(1))
						&& plugin.permissions.canRemoveReceiver(event.getPlayer()))
				{
					if (plugin.WireBox.removeWirelessReceiver(signObject.getLine(1), event.getBlock().getLocation()))
					{
						if (WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getTransmitters().size() == 0
								&& WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getReceivers().size() == 0)
						{
							plugin.WireBox.removeChannel(signObject.getLine(1));
							event.getPlayer().sendMessage(WirelessRedstone.strings.signDestroyed);
							event.getPlayer().sendMessage(WirelessRedstone.strings.channelRemovedCauseNoSign);
						}
						else
						{
							event.getPlayer().sendMessage(WirelessRedstone.strings.signDestroyed);
						}
					}
					else
					{
						WirelessRedstone.getStackableLogger().debug("Receiver wasn't found in the config, but the sign has been successfuly removed !");
					}
				}
				else
				{
					event.getPlayer().sendMessage(WirelessRedstone.strings.playerCannotDestroySign);
					event.setCancelled(true);
					signObject.update();
				}
				return;
			}
			else if (plugin.WireBox.isTransmitter(signObject.getLine(0)))
			{
				if(!WirelessRedstone.config.getSignDrop())
				{
					cancelEvent(event);
				}
				if (plugin.WireBox.hasAccessToChannel(event.getPlayer(),signObject.getLine(1))
						&& plugin.permissions.canRemoveTransmitter(event.getPlayer()))
				{
					if (plugin.WireBox.removeWirelessTransmitter(signObject.getLine(1), event.getBlock().getLocation()))
					{
						event.getPlayer().sendMessage(WirelessRedstone.strings.signDestroyed);
						if (WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getTransmitters().size() == 0
								&& WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getReceivers().size() == 0
								&& WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getScreens().size() == 0)
						{
							plugin.WireBox.removeChannel(signObject.getLine(1));
							event.getPlayer().sendMessage(WirelessRedstone.strings.channelRemovedCauseNoSign);
						}
						
						else if (WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getTransmitters().size() == 0)
						{
							event.getPlayer().sendMessage("[WirelessRedstone] No other Transmitters found, Resettings Power data on receivers to sign.");
							for (WirelessReceiver receiver : WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getReceivers())
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
						WirelessRedstone.getStackableLogger().debug("Transmitter wasn't found in the config, but the sign has been successfuly removed !");
					}
				}
				else
				{
					event.getPlayer().sendMessage(WirelessRedstone.strings.playerCannotDestroySign);
					event.setCancelled(true);
				}
				return;
			}
			else if(plugin.WireBox.isScreen(signObject.getLine(0)))
			{
				if(!WirelessRedstone.config.getSignDrop())
				{
					cancelEvent(event);
				}
				if (plugin.WireBox.hasAccessToChannel(event.getPlayer(),signObject.getLine(1))
						&& plugin.permissions.canRemoveScreen(event.getPlayer()))
				{
					if (plugin.WireBox.removeWirelessScreen(signObject.getLine(1), event.getBlock().getLocation()))
					{
						event.getPlayer().sendMessage("[WirelessRedstone] Succesfully removed this sign!");
					}
					
					if (WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getTransmitters().size() == 0
							&& WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getReceivers().size() == 0)
					{
						plugin.WireBox.removeChannel(signObject.getLine(1));
						event.getPlayer().sendMessage("[WirelessRedstone] Succesfully removed this sign! Channel removed, no more signs in the worlds.");
					}
				}
				else
				{
					event.getPlayer().sendMessage(WirelessRedstone.strings.playerCannotDestroySign);
					event.setCancelled(true);
				}
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
	
	private void cancelEvent(BlockBreakEvent event)
	{
		/*
		 * Methods cancelEvent and sendBlockBreakParticles, taken from http://www.bukkit.fr/index.php?threads/enlever-le-drop-dun-block.850/page-2#post-11582
		 * All credits to richie3366.
		 */
		
	    event.setCancelled(true);
	    
	    ItemStack is = event.getPlayer().getItemInHand();

	    if(is.getType().getMaxDurability() > 0){
	        is.setDurability((short) (is.getDurability() + 1));

	        if(is.getDurability() >= is.getType().getMaxDurability()){
	            event.getPlayer().setItemInHand(null);
	        }
	    }

	    Block b = event.getBlock();

	    int lastType = b.getTypeId();

	    b.setType(Material.AIR);

	    sendBlockBreakParticles(b, lastType, event.getPlayer());
	}

	private void sendBlockBreakParticles(Block b, int lastType, Player author)
	{
	    int radius = 64;
	    radius *= radius;
	 
	    for (Player player : b.getWorld().getPlayers()) {
	        int distance = (int)player.getLocation().distanceSquared(b.getLocation());
	        if (distance <= radius && !player.equals(author)){
	          player.playEffect(b.getLocation(), Effect.STEP_SOUND, lastType);
	        }
	    }
	}
}
