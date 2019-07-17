package net.licks92.WirelessRedstone;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rotatable;

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
            if (block.getBlockData() instanceof Rotatable) {
                Rotatable blockData = ((Rotatable) block.getBlockData());
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
