package net.licks92.WirelessRedstone.WorldEdit;

import net.licks92.WirelessRedstone.Main;
import org.bukkit.Bukkit;

public class WorldEditLoader {

    public WorldEditLoader(){
        try
        {
            boolean validVersion = true;
            String version = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit").getDescription().getVersion();
            if (version.contains(".")) {
                String[] version_split = version.replaceAll("[^0-9.]", "").split("\\.");
                double value = Double.parseDouble(version_split[0] + "." + version_split[1]);
                if ((value > 0.0D) && (value < 6.0D)) {
                    validVersion = false;
                }
            } else if (version.contains("-")) {
                int value = Integer.parseInt(version.split("-")[0].replaceAll("[^0-9]", ""));
                if ((value > 0) && (value < 3122)) {
                    validVersion = false;
                }
            }
            if (validVersion) {
                WorldEditHooker.register();
                Main.getWRLogger().debug("Hooked into WorldEdit");
            } else {
                Main.getWRLogger().severe("Error while hooking worldedit. Invalid WorldEdit version.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
