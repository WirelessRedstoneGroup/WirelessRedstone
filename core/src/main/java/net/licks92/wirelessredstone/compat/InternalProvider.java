package net.licks92.wirelessredstone.compat;

import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import net.licks92.wirelessredstone.worldedit.WorldEditLoader;

public class InternalProvider {

    private static InternalBlockData compatBlockData;
    private static InternalWorldEditHooker compatWorldEditHooker;

    public static InternalBlockData getCompatBlockData() {
        if (compatBlockData != null) {
            return compatBlockData;
        }

        String selfPackage = InternalProvider.class.getPackage().getName();
        String className;
        if (Utils.isNewMaterialSystem()) {
            className = "InternalBlockData_1_13";
        } else {
            className = "InternalBlockData_1_8";
        }

        try {
            compatBlockData = (InternalBlockData) Class.forName(selfPackage + "." + className).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            WirelessRedstone.getWRLogger().severe("Couldn't find a suitable InternalBlockData");
            ex.printStackTrace();
        }

        return compatBlockData;
    }

    public static InternalWorldEditHooker getCompatWorldEditHooker(WorldEditLoader.WorldEditVersion version) {
        if (compatWorldEditHooker != null) {
            return compatWorldEditHooker;
        }

        String selfPackage = InternalProvider.class.getPackage().getName();
        String className;
        if (version == WorldEditLoader.WorldEditVersion.v6) {
            className = "InternalWorldEditHooker_6";
        } else if (version == WorldEditLoader.WorldEditVersion.v7) {
            className = "InternalWorldEditHooker_7";
        } else {
            throw new IllegalArgumentException("InternalProvider isn't configured with provided WorldEdit version.");
        }

        try {
            compatWorldEditHooker = (InternalWorldEditHooker) Class.forName(selfPackage + "." + className).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            WirelessRedstone.getWRLogger().severe("Couldn't find a suitable InternalWorldEditHooker");
            ex.printStackTrace();
        }

        return compatWorldEditHooker;
    }
}
