package net.licks92.WirelessRedstone.Reflection;

import net.licks92.WirelessRedstone.CompatMaterial;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.WallSign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class InternalBlockData_1_13 implements InternalBlockData {

    @Override
    public boolean isPowerable(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        return block.getBlockData() instanceof org.bukkit.block.data.Powerable;
    }

    @Override
    public boolean isPowered(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        if (!(block instanceof Powerable)) {
            return false;
        }

        return ((Powerable) block.getBlockData()).isPowered();
    }

    @Override
    public boolean isRedstoneSwitch(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        return block.getBlockData() instanceof Switch;
    }

    @Override
    public BlockFace getSignRotation(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        if (!(block.getBlockData() instanceof Rotatable)) {
            throw new IllegalArgumentException("Block needs to be a org.bukkit.block.data.Rotatable");
        }

        return ((Rotatable) block.getBlockData()).getRotation();
    }

    @Override
    public BlockFace getDirectionalFacing(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        if (!(block.getBlockData() instanceof Directional)) {
            throw new IllegalArgumentException("Block needs to be a org.bukkit.block.data.Directional");
        }

        return ((Directional) block.getBlockData()).getFacing();
    }

    @Override
    public BlockFace getRedstoneSwitchFacing(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        if (!(block.getBlockData() instanceof Switch)) {
            throw new IllegalArgumentException("Block needs to be a org.bukkit.block.data.type.Switch");
        }

        Switch redstoneSwitch = (Switch) block.getBlockData();

        if (redstoneSwitch.getFace() == Switch.Face.CEILING) {
            return BlockFace.UP;
        } else if (redstoneSwitch.getFace() == Switch.Face.FLOOR) {
            return BlockFace.DOWN;
        }

        return getDirectionalFacing(block).getOppositeFace();
    }

    @Override
    public void setRedstoneWallTorch(@NotNull Block block, @NotNull BlockFace blockFace, @Nullable BlockFace storedDirection) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        block.setType(CompatMaterial.REDSTONE_WALL_TORCH.getMaterial(), storedDirection == BlockFace.NORTH);
        BlockState torch = block.getState();
        RedstoneWallTorch torchData = (RedstoneWallTorch) block.getBlockData();

        torchData.setFacing(blockFace);
        torch.setBlockData(torchData);
        torch.update();
    }

    @Override
    public void setSignWall(@NotNull Block block, @NotNull BlockFace blockFace, @Nullable BlockFace storedDirection) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        block.setType(CompatMaterial.WALL_SIGN.getMaterial(), storedDirection == BlockFace.NORTH);
        BlockState sign = block.getState();

        if (!(block.getBlockData() instanceof WallSign)) {
            WirelessRedstone.getWRLogger().warning("Receiver at " + block.getLocation().toString() + " is not a wall sign while it is saved as one.");
            return;
        }

        WallSign signData = (WallSign) sign.getBlockData();
        signData.setFacing(blockFace);
        sign.setBlockData(signData);
        sign.update();
    }

    @Override
    public void setSignRotation(@NotNull Block block, @NotNull BlockFace blockFace) {
        Objects.requireNonNull(block, "Block cannot be NULL");

        if (!(block.getBlockData() instanceof Rotatable)) {
            throw new IllegalArgumentException("Block needs to be a org.bukkit.block.data.Rotatable");
        }

        Rotatable blockData = ((Rotatable) block.getBlockData());
        blockData.setRotation(blockFace);
        block.setBlockData(blockData);
    }
}
