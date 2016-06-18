package net.licks92.WirelessRedstone.Signs;

import com.avaje.ebean.validation.NotNull;
import net.licks92.WirelessRedstone.ConfigManager;
import net.licks92.WirelessRedstone.Main;
import net.licks92.WirelessRedstone.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.scheduler.BukkitTask;

import javax.persistence.Id;
import java.util.*;

@SerializableAs("WirelessChannel")
public class WirelessChannel implements ConfigurationSerializable {

    @Id
    private int id;
    @NotNull
    private String name;
    @NotNull
    private boolean locked;

    private List<String> owners = new LinkedList<String>();
    private List<WirelessTransmitter> transmitters = new LinkedList<WirelessTransmitter>();
    private List<WirelessReceiver> receivers = new LinkedList<WirelessReceiver>();
    private List<WirelessScreen> screens = new LinkedList<WirelessScreen>();

    public WirelessChannel(String name) {
        this.setName(name);
    }

    public WirelessChannel(Map<String, Object> map) {
        this.setId((Integer) map.get("id"));
        this.setName((String) map.get("name"));
        this.setOwners((List<String>) map.get("owners"));
        this.setReceivers((List<WirelessReceiver>) map.get("receivers"));
        this.setTransmitters((List<WirelessTransmitter>) map.get("transmitters"));
        this.setScreens((List<WirelessScreen>) map.get("screens"));
        try {
            this.setLocked((Boolean) map.get("locked"));
        } catch (NullPointerException ignored) {

        }
    }

    public void turnOn(Integer time) {
        if (isLocked()) {
            Main.getWRLogger().debug("Channel " + name + " didn't turn on because locked.");
            return;
        }

        int timeInTicks = time / 50; // It's the time in ticks, where the timevariable is supposed to be the time in ms.
        turnOn();

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("WirelessRedstone"),
                new Runnable() {
                    @Override
                    public void run() {
                        turnOff();
                    }

                }, timeInTicks);
    }

    public void turnOn() {
        if (isLocked()) {
            Main.getWRLogger().debug("Channel " + name + " didn't turn on because locked.");
            return;
        }
        // Turning on the receivers ONLY if the channel isn't active.
        try {
            // Change receivers
            for (WirelessReceiver receiver : receivers) {
                receiver.turnOn(getName());
            }

            // Turning on screens
            for (WirelessScreen screen : screens) {
                screen.turnOn();
            }
        } catch (RuntimeException e) {
            Main.getWRLogger().severe("Error while turning on the receivers of channel " + name
                    + ". Please turn the debug mode on to get more informations.");

            if (ConfigManager.getConfig().getDebugMode())
                e.printStackTrace();
        }
        if (!Main.getSignManager().activeChannels.contains(getName())) {
            Main.getSignManager().activeChannels.add(getName());
        }
    }

    public void turnOff() {
        try {
            // Change receivers
            for (WirelessReceiver receiver : getReceivers()) {
                receiver.turnOff(getName());

                for (BlockFace blockFace : Utils.getEveryBlockFace(true)) {
                    Bukkit.getServer().getPluginManager().callEvent(
                            new BlockRedstoneEvent(receiver.getLocation().getBlock().getRelative(blockFace),
                                    receiver.getLocation().getBlock().getRelative(blockFace).getBlockPower(), 0));
                }
            }

            // Change screens
            for (WirelessScreen screen : screens) {
                screen.turnOff();
            }
        } catch (RuntimeException e) {
            Main.getWRLogger()
                    .severe("Error while updating redstone onBlockRedstoneChange for Screens, turn on the Debug Mode to get more informations.");

            if (ConfigManager.getConfig().getDebugMode())
                e.printStackTrace();
        }
        if (Main.getSignManager().activeChannels.contains(getName())) {
            Main.getSignManager().activeChannels.remove(getName());
        }
    }

    public void startClock(BukkitTask task) {
        Main.getSignManager().clockTasks.put(task.getTaskId(),
                getName());
        Main.getWRLogger().debug(
                "Added clock task " + task.getTaskId()
                        + " to te list for circuit " + getName());
    }

    public void stopClock() {
        ArrayList<Integer> remove = new ArrayList<Integer>();
        for (Map.Entry<Integer, String> task : Main.getSignManager().clockTasks
                .entrySet()) {
            if (!task.getValue().equalsIgnoreCase(getName())) {
                continue;
            }
            Bukkit.getScheduler().cancelTask(task.getKey());
            remove.add(task.getKey());
            Main.getWRLogger().debug("Stopped clock task " + task);
        }
        for (Integer i : remove) {
            Main.getSignManager().clockTasks.remove(i);
        }
        remove.clear();
    }

    public void toggle(Integer redstoneValue, Block block) {
        if (redstoneValue > 0) {
            if (Main.getSignManager().activeChannels
                    .contains(getName())) {
                return;
            }
            turnOn();
        } else if (redstoneValue == 0) {
            if (!isOn()) {
                turnOff();
            }
        }
    }

    public boolean isActive() {
        for (WirelessTransmitter t : getTransmitters()) {
            Location loc = new Location(Bukkit.getWorld(t.getWorld()),
                    t.getX(), t.getY(), t.getZ());
            Block block = loc.getBlock();
            if (block.getState() instanceof Sign) {
                if (block.isBlockIndirectlyPowered()
                        || block.isBlockIndirectlyPowered()) {
                    return true;
                }
            }

            for (BlockFace blockFace : Utils.getEveryBlockFace(true)) {
                if (block.getRelative(blockFace).isBlockIndirectlyPowered()
                        || block.getRelative(blockFace)
                        .isBlockIndirectlyPowered()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isOn() {
        boolean on = false;
        for (WirelessTransmitter transmitter : transmitters) {
            if (transmitter.isActive()) {
                on = true;
            }
        }
        return ConfigManager.getConfig().useORLogic() ? on : false;
    }

    public void addTransmitter(WirelessTransmitter transmitter) {
        if (transmitters == null)
            transmitters = new ArrayList<WirelessTransmitter>();

        transmitters.add(transmitter);
    }

    public void addReceiver(WirelessReceiver receiver) {
        if (receivers == null)
            receivers = new ArrayList<WirelessReceiver>();

        receivers.add(receiver);
    }

    public void addScreen(WirelessScreen screen) {
        if (screens == null)
            screens = new LinkedList<WirelessScreen>();

        screens.add(screen);
    }

    public void removeReceiverAt(Location loc) {
        for (WirelessReceiver receiver : receivers) {
            if (receiver.getX() == loc.getBlockX()
                    && receiver.getZ() == loc.getBlockZ()
                    && receiver.getY() == loc.getBlockY()) {
                if (!receiver.getWorld().equalsIgnoreCase(
                        loc.getWorld().getName())) {
                    continue;
                }
                receivers.remove(receiver);
                return;
            }
        }
    }

    public void removeTransmitterAt(Location loc) {
        for (WirelessTransmitter transmitter : transmitters) {
            if (transmitter.getX() == loc.getBlockX()
                    && transmitter.getZ() == loc.getBlockZ()
                    && transmitter.getY() == loc.getBlockY()) {
                if (!transmitter.getWorld().equalsIgnoreCase(
                        loc.getWorld().getName())) {
                    continue;
                }
                transmitters.remove(transmitter);
                return;
            }
        }
    }

    public void removeScreenAt(Location loc) {
        for (WirelessScreen screen : screens) {
            if (screen.getX() == loc.getBlockX()
                    && screen.getZ() == loc.getBlockZ()
                    && screen.getY() == loc.getBlockY()) {
                if (!screen.getWorld().equalsIgnoreCase(
                        loc.getWorld().getName())) {
                    continue;
                }
                screens.remove(screen);
                return;
            }
        }
    }

    public void removeReceiverAt(Location loc, String world) { //TODO: Finish rebuild and check if this is called
        for (WirelessReceiver receiver : receivers) {
            if (receiver.getX() == loc.getBlockX()
                    && receiver.getZ() == loc.getBlockZ()
                    && receiver.getY() == loc.getBlockY()) {
                if (!receiver.getWorld().equalsIgnoreCase(world)) {
                    continue;
                }
                receivers.remove(receiver);
                return;
            }
        }
    }

    public void removeTransmitterAt(Location loc, String world) { //TODO: Finish rebuild and check if this is called
        for (WirelessTransmitter transmitter : transmitters) {
            if (transmitter.getX() == loc.getBlockX()
                    && transmitter.getZ() == loc.getBlockZ()
                    && transmitter.getY() == loc.getBlockY()) {
                if (!transmitter.getWorld().equalsIgnoreCase(world)) {
                    continue;
                }
                transmitters.remove(transmitter);
                return;
            }
        }
    }

    public void removeScreenAt(Location loc, String world) { //TODO: Finish rebuild and check if this is called
        for (WirelessScreen screen : screens) {
            if (screen.getX() == loc.getBlockX()
                    && screen.getZ() == loc.getBlockZ()
                    && screen.getY() == loc.getBlockY()) {
                if (!screen.getWorld().equalsIgnoreCase(world)) {
                    continue;
                }
                screens.remove(screen);
                return;
            }
        }
    }

    public boolean removeOwner(String username) {
        return this.owners.remove(username);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public void addOwner(String username) {
        if (this.owners == null)
            this.owners = new LinkedList<String>();

        if (!this.owners.contains(username))
            this.owners.add(username);
    }

    public void setTransmitters(List<WirelessTransmitter> transmitters) {
        if (transmitters != null)
            this.transmitters = transmitters;
        else
            this.transmitters = new LinkedList<WirelessTransmitter>();
    }

    public void setReceivers(List<WirelessReceiver> receivers) {
        if (receivers != null)
            this.receivers = receivers;
        else
            this.receivers = new LinkedList<WirelessReceiver>();
    }

    public void setScreens(List<WirelessScreen> screens) {
        if (screens != null)
            this.screens = screens;
        else
            this.screens = new LinkedList<WirelessScreen>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return locked;
    }

    public List<String> getOwners() {
        return owners;
    }

    public List<WirelessTransmitter> getTransmitters() {
        try {
            return this.transmitters;
        } catch (NullPointerException ex) {
            return new LinkedList<WirelessTransmitter>();
        }
    }

    public List<WirelessReceiver> getReceivers() {
        try {
            return this.receivers;
        } catch (NullPointerException ex) {
            return new LinkedList<WirelessReceiver>();
        }
    }

    public List<WirelessScreen> getScreens() {
        try {
            return this.screens;
        } catch (NullPointerException ex) {
            return new LinkedList<WirelessScreen>();
        }
    }

    public List<WirelessReceiver> getReceiversOfType(WirelessReceiver.Type type) {
        List<WirelessReceiver> returnList = new LinkedList<>();

        for (WirelessReceiver receiver : getReceivers()) {
            if (receiver instanceof WirelessReceiverClock && type == WirelessReceiver.Type.CLOCK) {
                returnList.add(receiver);
            } else if (receiver instanceof WirelessReceiverInverter && type == WirelessReceiver.Type.INVERTER) {
                returnList.add(receiver);
            } else if (receiver instanceof WirelessReceiverDelayer && type == WirelessReceiver.Type.DELAYER) {
                returnList.add(receiver);
            } else if (receiver instanceof WirelessReceiverSwitch && type == WirelessReceiver.Type.SWITCH) {
                returnList.add(receiver);
            } else if (type == WirelessReceiver.Type.DEFAULT) {
                returnList.add(receiver);
            }
        }

        return returnList;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", getId());
        map.put("name", getName());
        map.put("owners", getOwners());
        map.put("receivers", getReceivers());
        map.put("transmitters", getTransmitters());
        map.put("screens", getScreens());
        map.put("locked", isLocked());
        return map;
    }

}
