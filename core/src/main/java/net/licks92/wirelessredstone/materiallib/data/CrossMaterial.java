package net.licks92.wirelessredstone.materiallib.data;

import com.google.common.collect.Lists;
import net.licks92.wirelessredstone.materiallib.utilities.ServerVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.licks92.wirelessredstone.materiallib.data.CrossMaterialVersion.*;

public enum CrossMaterial {

    AIR(
            sinceRelease("AIR")
    ),

    COMPARATOR(
            until("REDSTONE_COMPARATOR", ServerVersion.V1_12_2),
            since("COMPARATOR", ServerVersion.V1_13)
    ),

    COMPARATOR_OFF(
            until("REDSTONE_COMPARATOR_OFF", ServerVersion.V1_12_2)
    ),

    COMPARATOR_ON(
            until("REDSTONE_COMPARATOR_ON", ServerVersion.V1_12_2)
    ),

    REDSTONE_TORCH(
            until("REDSTONE_TORCH_ON", ServerVersion.V1_12_2),
            since("REDSTONE_TORCH", ServerVersion.V1_13)
    ),

    REDSTONE_WALL_TORCH(
            until("REDSTONE_TORCH_ON", ServerVersion.V1_12_2),
            since("REDSTONE_WALL_TORCH", ServerVersion.V1_13)
    ),

    REPEATER(
            until("DIODE", ServerVersion.V1_12_2),
            since("REPEATER", ServerVersion.V1_13)
    ),

    REPEATER_OFF(
            until("DIODE_BLOCK_OFF", ServerVersion.V1_12_2)
    ),

    REPEATER_ON(
            until("DIODE_BLOCK_ON", ServerVersion.V1_12_2)
    ),

    SIGN(
            until("SIGN_POST", ServerVersion.V1_12_2),
            between("SIGN", ServerVersion.V1_13, ServerVersion.V1_13_2),
            since("OAK_SIGN", ServerVersion.V1_14)
    ),

    WALL_SIGN(
            until("WALL_SIGN", ServerVersion.V1_13_2),
            since("OAK_WALL_SIGN", ServerVersion.V1_14)
    );

    private static boolean initialized;

    private final List<CrossMaterialVersion> versions;
    private MaterialHandler handle;

    CrossMaterial(CrossMaterialVersion... versions) {
        if (versions.length == 0) {
            throw new IllegalArgumentException("No versions for material " + name());
        }
        this.versions = Arrays.asList(versions);
    }

    public List<CrossMaterialVersion> getVersions() {
        return Collections.unmodifiableList(versions);
    }

    public Optional<CrossMaterialVersion> getMostSuitableVersion(ServerVersion version) {
        for (CrossMaterialVersion candidate : Lists.reverse(versions)) {
            if (candidate.getValidVersions().isBetweenBounds(version)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    public Optional<MaterialHandler> getHandle() {
        return Optional.ofNullable(handle);
    }

    public boolean equals(Material material) {
        return getHandle().map(handle -> handle.getType() == material).orElse(false);
    }

    public Block setMaterial(Block block) {
        return getHandle().map(handle -> handle.setMaterial(block, true)).orElseThrow(IllegalStateException::new);
    }

    public Block setMaterial(Block block, boolean applyPhysics) {
        return getHandle().map(handle -> handle.setMaterial(block, applyPhysics)).orElseThrow(IllegalStateException::new);
    }

    public static Collection<CrossMaterial> getMaterials() {
        return Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static void initialize(ServerVersion serverVersion) {
        if (initialized) {
            throw new IllegalStateException("Already initialized!");
        }
        initialized = true;
        for (CrossMaterial material : values()) {
            material.getMostSuitableVersion(serverVersion)
                    .ifPresent(version -> {
                        String[] query = version.getName().split(":", 2);
                        Material type = Material.getMaterial(query[0].toUpperCase());
                        if (type == null) {
                            throw new IllegalStateException("Unable to find expected material " + material.name());
                        }
                        Byte data = null;
                        if (query.length == 2) {
                            if (serverVersion.isNewerOrSame(ServerVersion.V1_13)) {
                                throw new IllegalStateException("Can't use material data in >= 1.13");
                            }
                            data = (byte) Integer.parseInt(query[1]);
                        }
                        material.handle = new MaterialHandler(type, data);
                    });
        }
    }
}
