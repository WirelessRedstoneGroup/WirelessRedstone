package net.licks92.WirelessRedstone.Compat;

import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum CompatMaterial {
    REDSTONE_TORCH("REDSTONE_TORCH", "REDSTONE_TORCH_ON"),
    REDSTONE_WALL_TORCH("REDSTONE_WALL_TORCH", "REDSTONE_TORCH_ON"),
    SIGN(new String[]{"OAK_SIGN", "SIGN"}, "SIGN_POST"),
    WALL_SIGN(new String[]{"OAK_WALL_SIGN", "WALL_SIGN"}),
    COMPARATOR("COMPARATOR", "REDSTONE_COMPARATOR"),
    COMPARATOR_ON("COMPARATOR", "REDSTONE_COMPARATOR_ON"),
    COMPARATOR_OFF("COMPARATOR", "REDSTONE_COMPARATOR_OFF"),
    REPEATER("REPEATER", "DIODE"),
    REPEATER_ON("REPEATER", "DIODE_BLOCK_ON"),
    REPEATER_OFF("REPEATER", "DIODE_BLOCK_OFF"),
    IS_PREASURE_PLATE("_PLATE");

    private Material material;
    private List<Material> materials;

    CompatMaterial(String[] versions) {
        this(versions, null);
    }

    CompatMaterial(String[] versions, String oldVersion) {
        HashMap<String, Material> materialMap = Arrays.stream(Material.values())
                .collect(Collectors.toMap(Enum::toString, material -> material, (a, b) -> b, HashMap::new));

        if (Utils.isNewMaterialSystem() || oldVersion == null) {
            for (String newVersion : versions) {
                this.material = materialMap.entrySet()
                        .stream()
                        .filter(e -> e.getKey().equalsIgnoreCase(newVersion))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);

                if (this.material != null) {
                    break;
                }
            }
        } else {
            this.material = materialMap.get(oldVersion);
        }
    }

    CompatMaterial(String newVersion, String oldVersion) {
        HashMap<String, Material> materialMap = Arrays.stream(Material.values())
                .collect(Collectors.toMap(Enum::toString, material -> material, (a, b) -> b, HashMap::new));

        if (Utils.isNewMaterialSystem()) {
            this.material = materialMap.get(newVersion);
        } else {
            this.material = materialMap.get(oldVersion);
        }
    }

    CompatMaterial(String regex) {
        this.materials = new ArrayList<>();
        Arrays.stream(Material.values())
                .filter(material -> material.toString().contains(regex))
                .forEach(material -> this.materials.add(material));
    }

    /**
     * Get a Material instance which is backwards compatible with MC <1.13
     *
     * @return Material
     */
    public Material getMaterial() {
        if (material != null) {
            return material;
        } else if (materials != null) {
            return materials.get(0);
        } else {
            throw new IllegalStateException("Material " + this.name() + " couldn't be find. Is this the latest WirelessRedstone version?");
        }
    }

    /**
     * Check if a Material exists in a Material list.
     *
     * @param material Material to check if it exists in the list
     * @return Boolean; exists or doesn't exists
     */
    public boolean isMaterial(Material material) {
        if (materials == null) {
            return this.material == material;
        } else {
            return materials.contains(material);
        }
    }

}
