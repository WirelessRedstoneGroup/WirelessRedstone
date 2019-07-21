package net.licks92.WirelessRedstone.WorldEdit;

import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Bukkit;

public class WorldEditLoader {

    public WorldEditLoader(){
        try
        {
            boolean validVersion = false;
            String version = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit").getDescription().getVersion();
            if (version.contains(".")) {
                String[] version_split = version.replaceAll("[^0-9.]", "").split("\\.");
                double value = Double.parseDouble(version_split[0] + "." + version_split[1]);
                if ((value > 6.0D) && (value < 7.0D)) {
                    validVersion = true;
                }
            }

            if (validVersion) {
                WorldEditHooker.register();
                WirelessRedstone.getWRLogger().debug("Hooked into WorldEdit");
            } else {
                WirelessRedstone.getWRLogger().warning("Error while hooking worldedit. Invalid WorldEdit version.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
