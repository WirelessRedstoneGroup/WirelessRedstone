package net.licks92.WirelessRedstone;

import org.bukkit.Material;

import java.util.HashMap;

public enum CompatMaterial {
    REDSTONE_TORCH("REDSTONE_TORCH", "REDSTONE_TORCH_ON"),
    REDSTONE_WALL_TORCH("REDSTONE_WALL_TORCH", "REDSTONE_TORCH_ON"),
    SIGN("SIGN", "SIGN_POST"),
    WALL_SIGN("WALL_SIGN", "WALL_SIGN"),
    REPEATER("REPEATER", "DIODE"),
    REPEATER_ON("REPEATER", "DIODE_BLOCK_ON"),
    REPEATER_OFF("REPEATER", "DIODE_BLOCK_OFF");

    private Material material;

    CompatMaterial(String newVersion, String oldVersion) {
        HashMap<String, Material> materialMap = new HashMap<>();
        for (Material material : Material.values()) {
            materialMap.put(material.toString(), material);
        }

        if (Utils.newMaterialSystem()) {
            this.material = materialMap.get(newVersion);
        } else {
            this.material = materialMap.get(oldVersion);
        }
    }

    public Material getMaterial() {
        return material;
    }

}
