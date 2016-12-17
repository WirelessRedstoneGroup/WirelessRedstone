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
import java.util.Collection;

public class Updater {
    private String updateUrl = "https://wirelessredstonegroup.github.io/WirelessRedstoneUpdate/update.json";
    private int maxChangelogLines = 2;

    private boolean debug;
    private BukkitTask task;

    private String latestVersion = null, downloadUrl = null, changelog = null;

    public Updater() {
        Main.getWRLogger().debug("Loading updater...");
        debug = ConfigManager.getConfig().getDebugMode();
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                checkForUpdate();
            }
        }, 1, 20 * 60 * 30); //1 sec = 20 ticks -> minutes -> every 30 min
    }

    private void checkForUpdate() {
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

            String latestVersion;
            if (Utils.isSpigot())
                latestVersion = root.getAsJsonObject("latest").get("spigotversion").getAsString();
            else
                latestVersion = root.getAsJsonObject("latest").get("bukkitversion").getAsString();

            JsonObject version = root.getAsJsonObject("versions").getAsJsonObject(latestVersion);
            String downloadUrl = version.getAsJsonObject(Utils.isSpigot() ? "spigot" : "bukkit").get("downloadUrl").getAsString();
            String changelog = version.get("changelog").getAsString();

            Main.getWRLogger().debug("Comparing " + Main.getInstance().getDescription().getVersion() + " vs " + latestVersion + (Utils.isSpigot() ? "-spigot" : "-bukkit"));

            Semver sem = new Semver(latestVersion);
            if (sem.isGreaterThan(Main.getInstance().getDescription().getVersion())) {
                Main.getWRLogger().debug("New update!");
                this.latestVersion = latestVersion;
                this.downloadUrl = downloadUrl;
                this.changelog = changelog;

                showUpdate(latestVersion, downloadUrl, changelog, Bukkit.getOnlinePlayers());
            } else {
                this.latestVersion = null;
                this.downloadUrl = null;
                this.changelog = null;
                Main.getWRLogger().debug("No update availible");
            }
        } catch (Exception e) {
            showError(e, "Error while parsing JSON file. Skipping update check.");
        }

        request.disconnect();
    }

    public void showUpdate(Collection<Player> players) {
        if (latestVersion != null && downloadUrl != null && changelog != null)
            showUpdate(latestVersion, downloadUrl, changelog, players);
    }

    private void showUpdate(final String latestVersion, final String downloadUrl, final String changelog, final Collection<? extends Player> checkPlayers) {
        Bukkit.getScheduler().runTask(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                Main.getWRLogger().info("Version " + latestVersion + " is availible! Download it here: " + downloadUrl);
                Main.getWRLogger().info("Changelog: ");

                ArrayList<Player> adminPlayers = new ArrayList<>();
                for (Player player : checkPlayers) {
                    if (Main.getPermissionsManager().isWirelessAdmin(player)) {
                        adminPlayers.add(player);
                        Utils.sendFeedback("Version " + latestVersion + " is availible! Download it here: ", player, false);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.getDownloadUrl(player.getName())
                                .replaceAll("%%TEXT", (Utils.isSpigot() ? "Spigot" : "Bukkit") + " download page")
                                .replaceAll("%%HOVERTEXT", "Click to go to website")
                                .replaceAll("%%LINK", downloadUrl));
                        Utils.sendFeedback("Changelog: ", player, false);
                    }
                }

                String[] splitedChangelog = changelog.split("#");
                int currentLine = 0;
                for (String line : splitedChangelog) {
                    if (line.equalsIgnoreCase(""))
                        continue;

                    if (currentLine > maxChangelogLines)
                        continue;

                    Main.getWRLogger().info(" - " + line.replaceAll("#", ""));

                    for (Player player : adminPlayers) {
                        player.sendMessage(" - " + line);
                    }
                    currentLine++;
                }
            }
        });
    }

    private void showError(Exception e, String text) {
        if (debug)
            e.printStackTrace();
        Main.getWRLogger().warning(text);
    }
}