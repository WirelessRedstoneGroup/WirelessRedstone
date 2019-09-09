package net.licks92.wirelessredstone.worldedit;

import net.licks92.wirelessredstone.compat.InternalProvider;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.Bukkit;

import java.util.Objects;

public class WorldEditLoader {

    public WorldEditLoader() {
        try {
            WorldEditVersion version = null;

            String detectedVersion = Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit")).getDescription().getVersion();
            if (detectedVersion.contains(".")) {
                String[] version_split = detectedVersion.replaceAll("[^0-9.]", "").split("\\.");
                double value = Double.parseDouble(version_split[0] + "." + version_split[1]);
                if ((value >= 6.0D) && (value < 7.0D)) {
                    version = WorldEditVersion.v6;
                } else if ((value >= 7.0D) && (value < 8.0D)) {
                    version = WorldEditVersion.v7;
                }
            }

            if (version != null) {
                InternalProvider.getCompatWorldEditHooker(version).register();
                WirelessRedstone.getWRLogger().debug("Hooked into WorldEdit");
            } else {
                WirelessRedstone.getWRLogger().warning("Error while hooking worldedit. Invalid WorldEdit version.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public enum WorldEditVersion {
        v6, v7
    }

}