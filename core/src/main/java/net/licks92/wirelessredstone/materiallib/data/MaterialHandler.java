package net.licks92.wirelessredstone.materiallib.data;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;

public class MaterialHandler {
    private final Material type;
    private final Byte data;

    public MaterialHandler(Material type, Byte data) {
        this.type = Objects.requireNonNull(type, "Type can't be null!");
        this.data = data;
    }

    public Material getType() {
        return type;
    }

    public Optional<Byte> getData() {
        return Optional.ofNullable(data);
    }

    Block setMaterial(Block block, boolean applyPhysics) {
        boolean legacy = data != null;
        block.setType(type, !legacy && applyPhysics);
        if (legacy) {
            try {
                block.getClass().getDeclaredMethod("setData", byte.class, boolean.class)
                        .invoke(block, data, applyPhysics);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return block;
    }
}
