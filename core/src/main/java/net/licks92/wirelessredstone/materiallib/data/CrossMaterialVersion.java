package net.licks92.wirelessredstone.materiallib.data;

import net.licks92.wirelessredstone.materiallib.utilities.ServerVersion;
import net.licks92.wirelessredstone.materiallib.utilities.ServerVersionInterval;

public class CrossMaterialVersion {
    private final String name;
    private final ServerVersionInterval validVersions;

    private CrossMaterialVersion(String name, ServerVersionInterval validVersions) {
        this.name = name;
        this.validVersions = validVersions;
    }

    public String getName() {
        return name;
    }

    public ServerVersionInterval getValidVersions() {
        return validVersions;
    }

    public static CrossMaterialVersion sinceRelease(String name) {
        return since(name, ServerVersion.OLDER);
    }

    public static CrossMaterialVersion since(String name, ServerVersion since) {
        return new CrossMaterialVersion(name, ServerVersionInterval.since(since));
    }

    public static CrossMaterialVersion until(String name, ServerVersion until) {
        return new CrossMaterialVersion(name, ServerVersionInterval.until(until));
    }

    public static CrossMaterialVersion between(String name, ServerVersion since, ServerVersion until) {
        return new CrossMaterialVersion(name, ServerVersionInterval.between(since, until));
    }
}
