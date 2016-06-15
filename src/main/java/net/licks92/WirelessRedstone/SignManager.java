package net.licks92.WirelessRedstone;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;

public class SignManager {

    public HashMap<Integer, String> clockTasks = new HashMap<Integer, String>();
    public HashMap<Location, Boolean> switchState = new HashMap<Location, Boolean>();
    public ArrayList<String> activeChannels = new ArrayList<String>();



}
