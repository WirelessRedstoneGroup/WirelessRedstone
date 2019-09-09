package net.licks92.wirelessredstone.signs;

import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("WirelessReceiverSwitch")
public class WirelessReceiverSwitch extends WirelessReceiver {

    private boolean isActive = false;

    public WirelessReceiverSwitch(int x, int y, int z, String world, boolean isWallSign, BlockFace direction, String owner) {
        super(x, y, z, world, isWallSign, direction, owner);
    }

    public WirelessReceiverSwitch(int x, int y, int z, String world, boolean isWallSign, BlockFace direction, String owner, boolean state) {
        super(x, y, z, world, isWallSign, direction, owner);
        isActive = state;
    }

    public WirelessReceiverSwitch(Map<String, Object> map) {
        super(map);
        isActive = (Boolean) map.get("state");
    }

    @Override
    public void turnOn(String channelName) {
        if (isActive) {
            super.turnOff(channelName);
        } else {
            super.turnOn(channelName);
        }

        isActive = !isActive;
    }

    @Override
    public void turnOff(String channelName) {
    }

    @Override
    public void changeSignContent(Block block, String channelName) {
        if (!(block.getState() instanceof Sign)) {
            WirelessRedstone.getWRLogger().warning("Receiver " + toString() + " is not a Sign but the plugin does expect it to be a Sign. " +
                    "Is the sign at a valid location?");
            return;
        }

        Sign sign = (Sign) block.getState();
        sign.setLine(0, WirelessRedstone.getStringManager().tagsReceiver.get(0));
        sign.setLine(1, channelName);
        sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverSwitchType.get(0));
        sign.update();
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("state", isActive);
        return map;
    }

    @Override
    public String toString() {
        return "WirelessReceiverSwitch{" +
                "isActive=" + isActive +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", owner='" + owner + '\'' +
                ", world='" + world + '\'' +
                ", direction=" + direction +
                ", isWallSign=" + isWallSign +
                '}';
    }
}
