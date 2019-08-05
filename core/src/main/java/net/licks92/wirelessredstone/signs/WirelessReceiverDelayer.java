package net.licks92.wirelessredstone.signs;

import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("WirelessReceiverDelayer")
public class WirelessReceiverDelayer extends WirelessReceiver {

    private final int delay;

    public WirelessReceiverDelayer(int x, int y, int z, String world, boolean isWallSign, BlockFace direction, String owner, int delay) {
        super(x, y, z, world, isWallSign, direction, owner);
        this.delay = delay;
    }

    public WirelessReceiverDelayer(Map<String, Object> map) {
        super(map);
        delay = (Integer) map.get("delay");
    }

    @Override
    public void turnOn(String channelName) {
        int delayInTicks = delay / 50;

        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(),
                () -> changeState(true, channelName),
                delayInTicks);
    }

    @Override
    public void turnOff(String channelName) {
        int delayInTicks = delay / 50;

        Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(),
                () -> changeState(false, channelName),
                delayInTicks);
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
        sign.setLine(2, WirelessRedstone.getStringManager().tagsReceiverDelayerType.get(0));
        sign.setLine(3, Integer.toString(delay));
        sign.update();
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("delay", getDelay());
        return map;
    }

    @Override
    public String toString() {
        return "WirelessReceiverDelayer{" +
                "delay=" + delay +
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
