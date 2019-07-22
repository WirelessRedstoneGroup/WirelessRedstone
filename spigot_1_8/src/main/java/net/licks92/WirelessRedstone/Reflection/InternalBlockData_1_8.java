package net.licks92.WirelessRedstone.Reflection;

import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.Redstone;
import org.bukkit.material.TripwireHook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class InternalBlockData_1_8 implements InternalBlockData {

    @Override
    public boolean isPowerable(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        return block.getState().getData() instanceof Redstone;
    }

    @Override
    public boolean isPowered(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        if (!(block.getState().getData() instanceof Redstone)) {
            return false;
        }

        return ((Redstone) block.getState().getData()).isPowered();
    }

    @Override
    public boolean isRedstoneSwitch(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        return block.getState().getData() instanceof Attachable && block.getState().getData() instanceof Redstone &&
                !(block.getState().getData() instanceof TripwireHook);
    }

    @Override
    public BlockFace getSignRotation(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        return getDirectionalFacing(block);
    }

    @Override
    public BlockFace getDirectionalFacing(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        if (!(block.getState().getData() instanceof Directional)) {
            throw new IllegalArgumentException("Block needs to be a org.bukkit.material.Directional");
        }

        return ((Directional) block.getState().getData()).getFacing();
    }

    @Override
    public BlockFace getRedstoneSwitchFacing(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        if (!(block.getState().getData() instanceof Attachable)) {
            throw new IllegalArgumentException("Block needs to be a org.bukkit.material.Attachable");
        }

        return ((Attachable) block.getState().getData()).getAttachedFace();
    }

    @Override
    public void setRedstoneWallTorch(@NotNull Block block, @NotNull BlockFace blockFace, @Nullable BlockFace storedDirection) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        block.setTypeIdAndData(76, Utils.getRawData(true, blockFace), true);
    }

    @Override
    public void setSignWall(@NotNull Block block, @NotNull BlockFace blockFace, @Nullable BlockFace storedDirection) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        block.setTypeIdAndData(68, Utils.getRawData(false, blockFace), true);
    }

    @Override
    public void setSignRotation(@NotNull Block block, @NotNull BlockFace blockFace) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        Sign sign = (Sign) block.getState();
        org.bukkit.material.Sign signData = new org.bukkit.material.Sign(Material.SIGN_POST);
        signData.setFacingDirection(blockFace);
        sign.setData(signData);
        sign.update();
    }
}
