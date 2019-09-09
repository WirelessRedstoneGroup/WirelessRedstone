package net.licks92.wirelessredstone.compat;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InternalBlockData {

    boolean isPowerable(@NotNull Block block);

    boolean isPowered(@NotNull Block block);

    boolean isRedstoneSwitch(@NotNull Block block);

    BlockFace getSignRotation(@NotNull Block block);

    BlockFace getDirectionalFacing(@NotNull Block block);

    BlockFace getRedstoneSwitchFacing(@NotNull Block block);

    void setRedstoneWallTorch(@NotNull Block block, @NotNull BlockFace blockFace, @Nullable BlockFace storedDirection);

    void setSignWall(@NotNull Block block, @NotNull BlockFace blockFace, @Nullable BlockFace storedDirection);

    void setSignRotation(@NotNull Block block, @NotNull BlockFace blockFace);

}
