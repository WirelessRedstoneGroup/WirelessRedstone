package net.licks92.wirelessredstone.signs;

import net.licks92.wirelessredstone.ConfigManager;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SerializableAs("WirelessChannel")
public class WirelessChannel implements ConfigurationSerializable {

    private int id;
    private String name;
    private boolean active;
    private boolean locked;

    private List<String> owners = new ArrayList<>();
    private List<WirelessTransmitter> transmitters = new ArrayList<>();
    private List<WirelessReceiver> receivers = new ArrayList<>();
    private List<WirelessScreen> screens = new ArrayList<>();

    public WirelessChannel(String name) {
        this.name = name;
        this.active = false;
        this.locked = false;
    }

    public WirelessChannel(String name, boolean locked) {
        this.name = name;
        this.active = false;
        this.locked = locked;
    }

    public WirelessChannel(String name, List<String> owners) {
        this.name = name;
        this.owners = owners;
        this.active = false;
        this.locked = false;
    }

    public WirelessChannel(String name, List<String> owners, boolean locked) {
        this.name = name;
        this.owners = owners;
        this.active = false;
        this.locked = locked;
    }

    public WirelessChannel(Map<String, Object> map) {
        this.setId((Integer) map.get("id"));
        this.setName((String) map.get("name"));
        this.active = (Boolean) map.getOrDefault("active", false);
        this.setOwners((List<String>) map.get("owners"));
        this.setReceivers((List<WirelessReceiver>) map.get("receivers"));
        this.setTransmitters((List<WirelessTransmitter>) map.get("transmitters"));
        this.setScreens((List<WirelessScreen>) map.get("screens"));
        try {
            this.setLocked((Boolean) map.get("locked"));
        } catch (NullPointerException ignored) {
            this.setLocked(false);
        }

        convertOwnersToUuid();
    }

    public void turnOn() {
        turnOn(0);
    }

    public void turnOn(int time) {
        WirelessRedstone.getWRLogger().debug("Channel#turnOn() WirelessChannel{" +
                "name='" + name + '\'' +
                ", active=" + active +
                "}");

        if (isLocked()) {
            WirelessRedstone.getWRLogger().debug("Channel " + name + " didn't turn on because locked.");
            return;
        }

        if (time > 0 && time < 50) {
            throw new IllegalArgumentException("Time must be at least 50ms.");
        }

        if (active) {
            return;
        }

        active = true;

        getReceivers().forEach(receiver -> receiver.turnOn(name));
        getScreens().forEach(WirelessScreen::turnOn);

        WirelessRedstone.getStorage().updateSwitchState(this);

        if (time >= 50) {
            Bukkit.getScheduler().runTaskLater(WirelessRedstone.getInstance(),
                    () -> turnOff(null, true),
                    time / 50);
        }
    }

    public void turnOff(Location loc) {
        turnOff(loc, false);
    }

    public void turnOff(Location loc, boolean force) {
        if (isLocked()) {
            WirelessRedstone.getWRLogger().debug("Channel " + name + " didn't turn off because locked.");
            return;
        }

        if (!active) {
            return;
        }

        boolean canTurnOff = true;
        if (ConfigManager.getConfig().useORLogic() && !force) {
            for (WirelessTransmitter transmitter : getTransmitters()) {
                if (loc != null) {
                    if (Utils.sameLocation(loc, transmitter.getLocation())) {
                        continue;
                    }
                }

                if (transmitter.isPowered()) {
                    canTurnOff = false;
                    break;
                }
            }
        }

        WirelessRedstone.getWRLogger().debug("Channel#turnOff() WirelessChannel{" +
                "name='" + name + '\'' +
                ", active=" + active +
                ", canTurnOff=" + canTurnOff +
                "}");

        if (!canTurnOff) {
            active = true;
            return;
        }

        active = false;

        getReceivers().forEach(receiver -> receiver.turnOff(name));
        getScreens().forEach(WirelessScreen::turnOff);
    }

    public void addWirelessPoint(WirelessPoint wirelessPoint) {
        if (wirelessPoint instanceof WirelessTransmitter) {
            if (!transmitters.contains(wirelessPoint)) {
                transmitters.add((WirelessTransmitter) wirelessPoint);
            }
        } else if (wirelessPoint instanceof WirelessScreen) {
            if (!screens.contains(wirelessPoint)) {
                screens.add((WirelessScreen) wirelessPoint);
            }
        } else if (wirelessPoint instanceof WirelessReceiver) {
            if (!receivers.contains(wirelessPoint)) {
                receivers.add((WirelessReceiver) wirelessPoint);
            }
        }

        //TODO: Maybe add owner from wirelesspoint to list of owners
    }

    public void removeWirelessPoint(WirelessPoint wirelessPoint) {
        if (wirelessPoint instanceof WirelessTransmitter) {
            transmitters.remove(wirelessPoint);
        } else if (wirelessPoint instanceof WirelessScreen) {
            screens.remove(wirelessPoint);
        } else if (wirelessPoint instanceof WirelessReceiver) {
            receivers.remove(wirelessPoint);
        }

        //TODO: Maybe remove owner from wirelesspoint to list of owners
    }

    public void addOwner(String uuid) {
        if (!owners.contains(uuid))
            owners.add(uuid);
    }

    public void removeOwner(String uuid) {
        owners.remove(uuid);
    }

    public void convertOwnersToUuid() {
        Iterator<String> ownersIterator = owners.iterator();
        while (ownersIterator.hasNext()) {
            String owner = ownersIterator.next();
            if (!owner.contains("-")) {
                if (Bukkit.getPlayer(owner) == null) {
                    if (Bukkit.getOfflinePlayer(owner).hasPlayedBefore()) {
                        owners.add(Bukkit.getOfflinePlayer(owner).getUniqueId().toString());
                        owners.remove(owner);
                    }
                } else {
                    owners.add(Objects.requireNonNull(Bukkit.getPlayer(owner)).getUniqueId().toString());
                    owners.remove(owner);
                }
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public List<WirelessTransmitter> getTransmitters() {
        return transmitters;
    }

    public void setTransmitters(List<WirelessTransmitter> transmitters) {
        this.transmitters = transmitters;
    }

    public List<WirelessReceiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<WirelessReceiver> receivers) {
        this.receivers = receivers;
    }

    public List<WirelessScreen> getScreens() {
        return screens;
    }

    public void setScreens(List<WirelessScreen> screens) {
        this.screens = screens;
    }

    public boolean isActive() {
        return active;
    }

    public List<WirelessPoint> getSigns() {
        List<WirelessPoint> signs = new ArrayList<>();
        signs.addAll(getTransmitters());
        signs.addAll(getReceivers());
        signs.addAll(getScreens());
        return signs;
    }

    public boolean isEmpty() {
        return getSigns().isEmpty();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", getId());
        map.put("name", getName());
        map.put("active", isActive());
        map.put("owners", getOwners());
        map.put("receivers", getReceivers());
        map.put("transmitters", getTransmitters());
        map.put("screens", getScreens());
        map.put("locked", isLocked());
        return map;
    }

    @Override
    public String toString() {
        return "WirelessChannel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", locked=" + locked +
                ", owners=" + owners +
                ", transmitters=" + transmitters +
                ", receivers=" + receivers +
                ", screens=" + screens +
                '}';
    }
}
