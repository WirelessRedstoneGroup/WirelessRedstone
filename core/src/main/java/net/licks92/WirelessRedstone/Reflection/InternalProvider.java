package net.licks92.WirelessRedstone.Reflection;

import net.licks92.WirelessRedstone.Utils;
import net.licks92.WirelessRedstone.WirelessRedstone;

public class InternalProvider {

    private static InternalBlockData compatBlockData;

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
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            WirelessRedstone.getWRLogger().severe("Couldn't find a suitable InternalBlockData");
            e.printStackTrace();
        }

        return compatBlockData;
    }

}
