package net.licks92.wirelessredstone.string;

import com.google.gson.Gson;
import net.licks92.wirelessredstone.WirelessRedstone;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StringManager {

    private final String stringFolder = "languages/";
    private final Strings strings;

    public final List<String> tagsTransmitter;
    public final List<String> tagsReceiver;
    public final List<String> tagsScreen;
    public final List<String> tagsReceiverDefaultType;
    public final List<String> tagsReceiverInverterType;
    public final List<String> tagsReceiverDelayerType;
    public final List<String> tagsReceiverClockType;
    public final List<String> tagsReceiverSwitchType;

    public StringManager(String language) {
        tagsTransmitter = new ArrayList<>();
        tagsReceiver = new ArrayList<>();
        tagsScreen = new ArrayList<>();
        tagsReceiverDefaultType = new ArrayList<>();
        tagsReceiverInverterType = new ArrayList<>();
        tagsReceiverDelayerType = new ArrayList<>();
        tagsReceiverClockType = new ArrayList<>();
        tagsReceiverSwitchType = new ArrayList<>();

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

        if (stream == null) {
            stream = plugin.getResource(stringFolder + "strings_en.json");
        }

        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

        strings = new Gson().fromJson(reader, Strings.class);
    }

    public Strings getStrings() {
        return strings;
    }

}
