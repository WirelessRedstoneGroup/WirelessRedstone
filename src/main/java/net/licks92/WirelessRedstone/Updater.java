package net.licks92.WirelessRedstone;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.vdurmont.semver4j.Semver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Updater {
    private static String updateUrl = "https://bart-0110.github.io/WirelessRedstoneUpdate/update.json";
    private static int maxChangelogLines = 2;

    private static boolean debug;
    private BukkitTask task;

    public Updater() {
        Main.getWRLogger().debug("Loading updater...");
        debug = ConfigManager.getConfig().getDebugMode();
        task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                checkForUpdate();
            }
        }, 1, 20 * 60 * 15); //1 sec = 20 ticks -> minutes -> every quarter
    }

    private static void checkForUpdate() {
        Main.getWRLogger().debug("Checking for update...");

        URL url = null;
        try {
            url = new URL(updateUrl);
        } catch (MalformedURLException e) {
            showError(e, "Update URL invalid. Skipping update check.");
            return;
        }

        HttpURLConnection request;
        try {
            request = (HttpURLConnection) url.openConnection();
            request.connect();
        } catch (IOException e) {
            showError(e, "IOException while fetching the latest update.");
            return;
        }

        StringBuilder json = new StringBuilder();
        try {
            InputStreamReader input = new InputStreamReader((InputStream) request.getContent());
            BufferedReader buff = new BufferedReader(input);
            String line;
            do {
                line = buff.readLine();
                json.append(line);
            } while (line != null);
        } catch (IOException e) {
            showError(e, "IOException while fetching the latest update.");
            request.disconnect();
            return;
        }

        try {
            JsonReader reader = new JsonReader(new StringReader(json.toString()));
            reader.setLenient(true);
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();

            String latestVerion = root.getAsJsonObject("latest").get("version").getAsString();

            JsonObject version = root.getAsJsonObject("versions").getAsJsonObject(latestVerion);
            String downloadUrl = version.get("downloadUrl").getAsString();
            String changelog = version.get("changelog").getAsString();

            Main.getWRLogger().debug("Comparing " + Main.getInstance().getDescription().getVersion() + " vs " + latestVerion);

            Semver sem = new Semver(latestVerion);
            if (sem.isGreaterThan(Main.getInstance().getDescription().getVersion())) {
                Main.getWRLogger().debug("New update!");
                showUpdate(latestVerion, downloadUrl, changelog);
            } else {
                Main.getWRLogger().debug("No update availible");
            }
        } catch (Exception e) {
            showError(e, "Error while parsing JSON file. Skipping update check.");
        }

        request.disconnect();
    }

    private static void showUpdate(String latestVersion, String downloadUrl, String changelog) {
        String consoleLog = "Version " + latestVersion + " is availible! Download it here: " + downloadUrl + "\nChangelog: ";

        ArrayList<Player> adminPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Main.getPermissionsManager().isWirelessAdmin(player)) {
                adminPlayers.add(player);
                Utils.sendFeedback("Version " + latestVersion + " is availible! Download it here: " + downloadUrl, player, false);
                Utils.sendFeedback("Changelog: ", player, false);
            }
        }

        String[] splitedChangelog = changelog.split("#");
        for (String line : splitedChangelog) {
            if (line.equalsIgnoreCase(""))
                continue;

            consoleLog += "\n - " + line.replaceAll("#", "");

            for (Player player : adminPlayers) {
                player.sendMessage(" - " + line);
            }
        }

        Main.getWRLogger().info(consoleLog);
    }

    private static void showError(Exception e, String text) {
        if (debug)
            e.printStackTrace();
        Main.getWRLogger().warning(text);
    }
}