package net.licks92.wirelessredstone.storage;

import net.licks92.wirelessredstone.WirelessRedstone;
import net.licks92.wirelessredstone.signs.WirelessChannel;
import net.licks92.wirelessredstone.signs.WirelessPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class StorageConfiguration {

    public abstract boolean initStorage();

    public abstract boolean close();

    protected abstract Collection<WirelessChannel> getAllChannels();

    public abstract void updateSwitchState(WirelessChannel channel);

    protected abstract StorageType canConvertFromType();

    public boolean createChannel(WirelessChannel channel) {
        WirelessRedstone.getStorageManager().updateList(channel.getName(), channel);
        return true;
    }

    public boolean createWirelessPoint(String channelName, WirelessPoint wirelessPoint) {
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        //TODO: Investigate if this duplicates the wirelesspoint into the channel
        channel.addWirelessPoint(wirelessPoint);
        WirelessRedstone.getStorageManager().updateList(channelName, channel);
        return true;
    }

    public boolean removeWirelessPoint(String channelName, WirelessPoint wirelessPoint) {
        WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);
        //TODO: Investigate if this duplicates the wirelesspoint into the channel
        channel.removeWirelessPoint(wirelessPoint);

        if (channel.isEmpty()) {
            WirelessRedstone.getStorage().removeChannel(channelName, false);
        } else {
            WirelessRedstone.getStorageManager().updateList(channelName, channel);
        }
        return true;
    }

    public boolean updateChannel(String channelName, WirelessChannel channel) {
        WirelessRedstone.getStorageManager().updateList(channel.getName(), channel);
        return true;
    }

    public boolean removeChannel(String channelName, boolean removeSigns) {
        if (removeSigns) {
            WirelessChannel channel = WirelessRedstone.getStorageManager().getChannel(channelName);

            for (WirelessPoint point : channel.getSigns()) {
                World world = Bukkit.getWorld(point.getWorld());

                if (world == null)
                    continue;

                Location loc = new Location(world, point.getX(), point.getY(), point.getZ());
                loc.getBlock().setType(Material.AIR);
            }
        }
        WirelessRedstone.getStorageManager().updateList(channelName, null);

        return true;
    }

    public int purgeData() {
        int response = 0;

        for (Map.Entry<WirelessChannel, Collection<WirelessPoint>> entry : WirelessRedstone.getSignManager().getAllInvalidPoints().entrySet()) {
            for (WirelessPoint point : entry.getValue()) {
                if (!WirelessRedstone.getStorage().removeWirelessPoint(entry.getKey().getName(), point)) {
                    response = -1;
                    break;
                }

                WirelessRedstone.getWRLogger().debug("Purged WirelessPoint " + point.getLocation().toString() + " because the location is invalid.");

                response++;
            }
        }

        List<WirelessChannel> emptyChannels = new ArrayList<>();
        for (WirelessChannel channel : WirelessRedstone.getStorageManager().getChannels()) {
            if (channel.isEmpty()) {
                emptyChannels.add(channel);
            }
        }

        for (WirelessChannel channel : emptyChannels) {
            WirelessRedstone.getStorage().removeChannel(channel.getName(), false);
            response++;
        }

        return response;
    }

    public boolean backupData() {
        byte[] buffer = new byte[1024];

        if (!(new File(WirelessRedstone.getInstance().getDataFolder(), WirelessRedstone.CHANNEL_FOLDER).exists())) {
            return false;
        }

        try {
            String zipName = "WRBackup "
                    + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-"
                    + Calendar.getInstance().get(Calendar.MONTH) + "-"
                    + Calendar.getInstance().get(Calendar.YEAR) + "_"
                    + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "."
                    + Calendar.getInstance().get(Calendar.MINUTE) + "."
                    + Calendar.getInstance().get(Calendar.SECOND);
            FileOutputStream fos = new FileOutputStream(WirelessRedstone.getInstance().getDataFolder() + File.separator + zipName + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : (new File(WirelessRedstone.getInstance().getDataFolder(), WirelessRedstone.CHANNEL_FOLDER)).listFiles()) {

                ZipEntry ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            zos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean wipeData() {
        WirelessRedstone.getStorageManager().wipeList();
        return true;
    }

}
