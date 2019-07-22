package net.licks92.WirelessRedstone.Compat;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class CompatSignData {

    private Block block;

    public CompatSignData(Block block) {
        this.block = block;
    }

    public void setLine(int line, String text) {
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            sign.setLine(line, text);
            sign.update();
        }
    }

    public void setRotation(BlockFace face) {
        InternalProvider.getCompatBlockData().setSignRotation(block, face);
    }

}
