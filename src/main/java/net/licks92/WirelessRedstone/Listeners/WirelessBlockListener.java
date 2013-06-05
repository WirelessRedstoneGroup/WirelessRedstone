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
import net.licks92.WirelessRedstone.Channel.WirelessReceiver.Type;

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
		if (WirelessRedstone.WireBox.isReceiver(event.getLine(0))
				|| WirelessRedstone.WireBox.isTransmitter(event.getLine(0))
				|| WirelessRedstone.WireBox.isScreen(event.getLine(0)))
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

			if (!WirelessRedstone.WireBox.hasAccessToChannel(event.getPlayer(), cname))
			{
				event.getBlock().setType(Material.AIR);
				event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
				event.getPlayer().sendMessage(WirelessRedstone.strings.playerCannotCreateSign);
				return;
			}
			
			if (WirelessRedstone.WireBox.isReceiver(event.getLine(0)))
			{
				if(WirelessRedstone.WireBox.isReceiverInverter(event.getLine(2)))
				{
					if(!WirelessRedstone.WireBox.addWirelessReceiver(cname, event.getBlock(), event.getPlayer(), Type.Inverter))
					{
						event.setCancelled(true);
						event.getBlock().breakNaturally();
					}
				}
				if(WirelessRedstone.WireBox.isReceiverDelayer(event.getLine(2)))
				{
					if(!WirelessRedstone.WireBox.addWirelessReceiver(cname, event.getBlock(), event.getPlayer(), Type.Delayer))
					{
						event.setCancelled(true);
						event.getBlock().breakNaturally();
					}
				}
				else if(WirelessRedstone.WireBox.isReceiverDefault(event.getLine(2)))
				{
					if(!WirelessRedstone.WireBox.addWirelessReceiver(cname, event.getBlock(), event.getPlayer(), Type.Default))
					{
						event.setCancelled(true);
						event.getBlock().breakNaturally();
					}
				}
				else
				{
					if(!WirelessRedstone.WireBox.addWirelessReceiver(cname, event.getBlock(), event.getPlayer(), Type.Default))
					{
						event.setCancelled(true);
						event.getBlock().breakNaturally();
					}
				}
			}
			else if(WirelessRedstone.WireBox.isTransmitter(event.getLine(0)))
			{
				if(!WirelessRedstone.WireBox.addWirelessTransmitter(cname, event.getBlock(), event.getPlayer()))
				{
					event.setCancelled(true);
					event.getBlock().breakNaturally();
				}
			}
			
			else if(WirelessRedstone.WireBox.isScreen(event.getLine(0)))
			{
				if(!WirelessRedstone.WireBox.addWirelessScreen(cname, event.getBlock(), event.getPlayer()))
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
		WirelessChannel channel;
		
		// Lets check if the sign is a Transmitter and if the channel name not
		// is empty
		if (!WirelessRedstone.WireBox.isTransmitter(signObject.getLine(0)) || signObject.getLine(1) == null || signObject.getLine(1) == "")
		{
			return;
		}
		else
		{
			channel = WirelessRedstone.config.getWirelessChannel(signObject.getLine(1));
		}
		if(channel == null)
		{
			WirelessRedstone.getWRLogger().debug("The transmitter at location "
					+ signObject.getX() + ","
					+ signObject.getY() + ","
					+ signObject.getZ() + " "
					+ "in the world " + signObject.getWorld().getName()
					+ " is actually linked with a null channel.");
			return;
		}
		if (event.getBlock().isBlockPowered() || event.getBlock().isBlockIndirectlyPowered())
		{
			channel.turnOn();
		}
		else if (!event.getBlock().isBlockPowered() || !event.getBlock().isBlockIndirectlyPowered())
		{
			channel.turnOff();
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if ((event.getBlock().getState() instanceof Sign))
		{
			Sign signObject = (Sign) event.getBlock().getState();
			if (WirelessRedstone.WireBox.isReceiver(signObject.getLine(0)))
			{
				if(!WirelessRedstone.config.getSignDrop())
				{
					cancelEvent(event);
				}
				if (WirelessRedstone.WireBox.hasAccessToChannel(event.getPlayer(),signObject.getLine(1))
						&& plugin.permissions.canRemoveReceiver(event.getPlayer()))
				{
					if (WirelessRedstone.WireBox.removeWirelessReceiver(signObject.getLine(1), event.getBlock().getLocation()))
					{
						if (WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getTransmitters().size() == 0
								&& WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getReceivers().size() == 0)
						{
							WirelessRedstone.config.removeWirelessChannel(signObject.getLine(1));
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
						WirelessRedstone.getWRLogger().debug("Receiver wasn't found in the config, but the sign has been successfuly removed !");
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
			else if (WirelessRedstone.WireBox.isTransmitter(signObject.getLine(0)))
			{
				if(!WirelessRedstone.config.getSignDrop())
				{
					cancelEvent(event);
				}
				if (WirelessRedstone.WireBox.hasAccessToChannel(event.getPlayer(),signObject.getLine(1))
						&& plugin.permissions.canRemoveTransmitter(event.getPlayer()))
				{
					if (WirelessRedstone.WireBox.removeWirelessTransmitter(signObject.getLine(1), event.getBlock().getLocation()))
					{
						event.getPlayer().sendMessage(WirelessRedstone.strings.signDestroyed);
						if (WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getTransmitters().size() == 0
								&& WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getReceivers().size() == 0
								&& WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getScreens().size() == 0)
						{
							WirelessRedstone.config.removeWirelessChannel(signObject.getLine(1));
							event.getPlayer().sendMessage(WirelessRedstone.strings.channelRemovedCauseNoSign);
						}
						
						else if (WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getTransmitters().size() == 0)
						{
							event.getPlayer().sendMessage(ChatColor.GREEN + "[WirelessRedstone] No other Transmitters found, switching receivers to off.");
							for (WirelessReceiver receiver : WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getReceivers())
							{
								Location rloc = receiver.getLocation();
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
						WirelessRedstone.getWRLogger().debug("Transmitter wasn't found in the config, but the sign has been successfuly removed !");
					}
				}
				else
				{
					event.getPlayer().sendMessage(WirelessRedstone.strings.playerCannotDestroySign);
					event.setCancelled(true);
				}
				return;
			}
			else if(WirelessRedstone.WireBox.isScreen(signObject.getLine(0)))
			{
				if(!WirelessRedstone.config.getSignDrop())
				{
					cancelEvent(event);
				}
				if (WirelessRedstone.WireBox.hasAccessToChannel(event.getPlayer(),signObject.getLine(1))
						&& plugin.permissions.canRemoveScreen(event.getPlayer()))
				{
					if (WirelessRedstone.WireBox.removeWirelessScreen(signObject.getLine(1), event.getBlock().getLocation()))
					{
						event.getPlayer().sendMessage(WirelessRedstone.strings.signDestroyed);
						if (WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getTransmitters().size() == 0
								&& WirelessRedstone.config.getWirelessChannel(signObject.getLine(1)).getReceivers().size() == 0)
						{
							WirelessRedstone.config.removeWirelessChannel(signObject.getLine(1));
							event.getPlayer().sendMessage(WirelessRedstone.strings.channelRemovedCauseNoSign);
						}
					}
					else
					{
						WirelessRedstone.getWRLogger().debug("Screen wasn't found in the config, but the sign has been successfuly removed !");
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
			for (Location loc : WirelessRedstone.cache.getAllReceiverLocations())
			{
				if (loc.equals(event.getBlock().getLocation()))
				{
					event.getPlayer().sendMessage(WirelessRedstone.strings.playerCannotDestroyReceiverTorch);
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	/**
	 * From the bukkit javadoc :
	 * Represents events with a source block and a destination block, currently only applies to
	 * liquid (lava and water) and teleporting dragon eggs.
	 * If a Block From To event is cancelled, the block will not move (the liquid will not flow).
	 * 
	 * @param event
	 * @deprecated Because there's currently nothing that breaks a sign and sends that event.
	 */
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event)
	{	
		if (event.getToBlock().getType() == Material.REDSTONE_TORCH_ON)
		{
			for (Location loc : WirelessRedstone.cache.getAllReceiverLocations())
			{
				if (loc.getBlockX() == event.getToBlock().getLocation().getBlockX() || loc.getBlockY() == event.getToBlock().getLocation().getBlockY() || loc.getBlockZ() == event.getToBlock().getLocation().getBlockZ())
				{
					WirelessRedstone.WireBox.removeReceiverAt(loc, false);
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
