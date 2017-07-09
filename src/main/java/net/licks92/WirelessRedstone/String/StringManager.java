package net.licks92.WirelessRedstone.String;

import com.google.gson.Gson;
import net.licks92.WirelessRedstone.WirelessRedstone;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class StringManager {

    private final String stringFolder = "languages/";
    private Strings strings;

    public List<String> tagsTransmitter;
    public List<String> tagsReceiver;
    public List<String> tagsScreen;
    public List<String> tagsReceiverDefaultType;
    public List<String> tagsReceiverInverterType;
    public List<String> tagsReceiverDelayerType;
    public List<String> tagsReceiverClockType;
    public List<String> tagsReceiverSwitchType;

    public StringManager(String language) {
        tagsTransmitter = new ArrayList<String>();
        tagsReceiver = new ArrayList<String>();
        tagsScreen = new ArrayList<String>();
        tagsReceiverDefaultType = new ArrayList<String>();
        tagsReceiverInverterType = new ArrayList<String>();
        tagsReceiverDelayerType = new ArrayList<String>();
        tagsReceiverClockType = new ArrayList<String>();
        tagsReceiverSwitchType = new ArrayList<String>();

        WirelessRedstone.getWRLogger().debug("Loading the tags...");

        tagsTransmitter.add("[wrt]");
        tagsTransmitter.add("[transmitter]");

        tagsReceiver.add("[wrr]");
        tagsReceiver.add("[receiver]");

        tagsScreen.add("[wrs]");
        tagsScreen.add("[screen]");

        tagsReceiverDefaultType.add("[default]");
        tagsReceiverDefaultType.add("[normal]");

        tagsReceiverInverterType.add("[inverter]");
        tagsReceiverInverterType.add("[inv]");

        tagsReceiverDelayerType.add("[delayer]");
        tagsReceiverDelayerType.add("[delay]");

        tagsReceiverClockType.add("[clock]");

        tagsReceiverSwitchType.add("[switch]");
        tagsReceiverSwitchType.add("[switcher]");

        WirelessRedstone plugin = WirelessRedstone.getInstance();

        InputStream stream = plugin.getResource(stringFolder + "strings_" + language + ".json");

        if (stream == null)
            stream = plugin.getResource(stringFolder + "strings_en.json");

        Reader reader = null;
        try {
            reader = new InputStreamReader(stream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            WirelessRedstone.getWRLogger().severe("There is a problem while loading the strings.");
            e.printStackTrace();
            return;
        }

        strings = new Gson().fromJson(reader, Strings.class);
    }

    public Strings getStrings() {
        return strings;
    }

}
