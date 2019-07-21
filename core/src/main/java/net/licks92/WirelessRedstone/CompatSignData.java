package net.licks92.WirelessRedstone;

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
        if (Utils.isNewMaterialSystem()) {
            if (block.getBlockData() instanceof org.bukkit.block.data.Rotatable) {
                org.bukkit.block.data.Rotatable blockData = ((org.bukkit.block.data.Rotatable) block.getBlockData());
                blockData.setRotation(face);
                block.setBlockData(blockData);
            }
        } else if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            org.bukkit.material.Sign signData = new org.bukkit.material.Sign(CompatMaterial.SIGN.getMaterial());
            signData.setFacingDirection(face);
            sign.setData(signData);
            sign.update();
        }
    }

}
