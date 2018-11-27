package net.licks92.WirelessRedstone.Signs;

import net.licks92.WirelessRedstone.CompatMaterial;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.material.RedstoneTorch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
                direction = Utils.getBlockFace(false, directionInt); // In the past normal signs and wall signs where saved under one direction
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

        getLocation().getWorld().loadChunk(getLocation().getChunk());

        if (getLocation().getBlock() == null) {
            return;
        }

        Block block = getLocation().getBlock();

        if (newState) {
            if (isWallSign()) {
                if (Utils.isNewMaterialSystem()) {
                    block.setType(CompatMaterial.REDSTONE_WALL_TORCH.getMaterial());
                    BlockState torch = block.getState();

                    RedstoneTorch torchData = new RedstoneTorch();
                    torchData.setFacingDirection(direction);
                    torch.setData(torchData);
                    torch.update();
                } else {
                    boolean reflectionWorked = false;
                    try {
                        Class<?> blockClass = Class.forName("org.bukkit.block.Block");
                        Method setTypeIdAndData = blockClass.getMethod("setTypeIdAndData", Integer.TYPE, Byte.TYPE, Boolean.TYPE);
                        setTypeIdAndData.invoke(block, 76, Utils.getRawData(true, direction), true);
                        reflectionWorked = true;
                    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        WirelessRedstone.getWRLogger().debug("Couldn't pass setTypeIdAndData");

                        if (ConfigManager.getConfig().getDebugMode()) {
                            e.printStackTrace();
                        }
                    }

                    if (!reflectionWorked) {
                        block.setType(CompatMaterial.REDSTONE_WALL_TORCH.getMaterial(), false);
                        BlockState sign = block.getState();

                        sign.setRawData(Utils.getRawData(true, direction));
                        sign.update();
                    }
                }
            } else {
                block.setType(CompatMaterial.REDSTONE_TORCH.getMaterial());
            }
        } else {
            if (isWallSign()) {
                if (Utils.isNewMaterialSystem()) {
                    block.setType(CompatMaterial.WALL_SIGN.getMaterial(), false);
                    BlockState sign = block.getState();

                    org.bukkit.material.Sign signData = new org.bukkit.material.Sign();
                    signData.setFacingDirection(direction);
                    sign.setData(signData);
                    sign.update();
                } else {
                    boolean reflectionWorked = false;
                    try {
                        Class<?> blockClass = Class.forName("org.bukkit.block.Block");
                        Method setTypeIdAndData = blockClass.getMethod("setTypeIdAndData", Integer.TYPE, Byte.TYPE, Boolean.TYPE);
                        setTypeIdAndData.invoke(block, 68, Utils.getRawData(false, direction), true);
                        reflectionWorked = true;
                    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        WirelessRedstone.getWRLogger().debug("Couldn't pass setTypeIdAndData");

                        if (ConfigManager.getConfig().getDebugMode()) {
                            e.printStackTrace();
                        }
                    }

                    if (!reflectionWorked) {
                        block.setType(CompatMaterial.WALL_SIGN.getMaterial(), false);
                        BlockState sign = block.getState();

                        sign.setRawData(Utils.getRawData(false, direction));
                        sign.update();
                    }
                }

                changeSignContent(block, channelName);
            } else {
                block.setType(CompatMaterial.SIGN.getMaterial());

                BlockState sign = block.getState();

                org.bukkit.material.Sign signData = new org.bukkit.material.Sign();
                signData.setFacingDirection(direction);
                sign.setData(signData);
                sign.update();

                changeSignContent(block, channelName);
            }
        }
    }

    public void changeSignContent(Block block, String channelName) {
        if (!(block.getState() instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) block.getState();
        sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverDefaultType.get(0));
        sign.update();
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
