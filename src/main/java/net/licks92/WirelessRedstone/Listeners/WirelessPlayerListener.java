package net.licks92.WirelessRedstone.Listeners;

import net.licks92.WirelessRedstone.WirelessRedstone;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class WirelessPlayerListener implements Listener
{
	private final WirelessRedstone plugin;

	public WirelessPlayerListener(WirelessRedstone r_plugin)
	{
		plugin = r_plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		// Will be implemented later, do not delete this class plz
		/*Player player = event.getPlayer();
		if(this.plugin.permissionsHandler.hasPermission(player, "WirelessRedstone.admin"))
		{
			player.sendMessage("[WARNING] Null Channels Exist in the config! Please remove them in the settings.yml file!");
		}*/
	}
}
