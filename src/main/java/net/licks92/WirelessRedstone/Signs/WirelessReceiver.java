package net.licks92.WirelessRedstone.Signs;

import net.licks92.WirelessRedstone.CompatMaterial;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.material.RedstoneTorch;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("WirelessReceiver")
public class WirelessReceiver extends WirelessPoint implements ConfigurationSerializable {

    public WirelessReceiver(int x, int y, int z, String world, boolean isWallSign, BlockFace direction, String owner) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.isWallSign = isWallSign;
        this.direction = direction;
        this.owner = owner;
    }

    public WirelessReceiver(Map<String, Object> map) {
        signType = SignType.RECEIVER;
        owner = (String) map.get("owner");
        world = (String) map.get("world");
        isWallSign = (Boolean) map.get("isWallSign");
        x = (Integer) map.get("x");
        y = (Integer) map.get("y");
        z = (Integer) map.get("z");

        try {
            direction = (BlockFace) BlockFace.valueOf(map.get("direction").toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                int directionInt = Integer.parseInt(map.get("direction").toString());
                direction = Utils.getBlockFace(isWallSign, directionInt);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void turnOn(String channelName) {
        changeState(true, channelName);
    }

    public void turnOff(String channelName) {
        changeState(false, channelName);
    }

    protected void changeState(boolean newState, String channelName) {
        if (getLocation() == null)
            return;

        if (getLocation().getBlock() == null)
            return;

        Block block = getLocation().getBlock();

        if (newState) {
            if (isWallSign()) {
                block.setType(CompatMaterial.REDSTONE_WALL_TORCH.getMaterial());

                RedstoneTorch torch = new RedstoneTorch();
                torch.setFacingDirection(direction);

                block.getState().setData(torch);
            } else {
                block.setType(CompatMaterial.REDSTONE_TORCH.getMaterial());
            }
        } else {
            if (isWallSign()) {
                block.setType(CompatMaterial.WALL_SIGN.getMaterial());

                Sign sign = (Sign) block.getState();

                org.bukkit.material.Sign signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
                signData.setFacingDirection(direction);
                sign.setData(signData);
                sign.update();

                changeSignContent(block, channelName);
            } else {
                block.setType(CompatMaterial.SIGN.getMaterial());

                Sign sign = (Sign) block.getState();

                org.bukkit.material.Sign signData = new org.bukkit.material.Sign(Material.SIGN);
                signData.setFacingDirection(direction);
                sign.setData(signData);
                sign.update();

                changeSignContent(block, channelName);
            }
        }
    }

    public void changeSignContent(Block block, String channelName) {
        Sign sign = (Sign) block.getState();
        sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverDefaultType.get(0));
        sign.update(true);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("direction", getDirection().name().toUpperCase());
        map.put("isWallSign", isWallSign());
        map.put("owner", getOwner());
        map.put("world", getWorld());
        map.put("x", getX());
        map.put("y", getY());
        map.put("z", getZ());
        return map;
    }


}
