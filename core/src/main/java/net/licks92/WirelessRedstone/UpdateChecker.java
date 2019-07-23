package net.licks92.WirelessRedstone;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UpdateChecker {

    private static final String USER_AGENT = "WirelessRedstone-update-checker";
    private static final String UPDATE_URL = "https://wirelessredstonegroup.github.io/WirelessRedstoneUpdate/update.json";

    private static UpdateChecker instance;

    private UpdateResult lastResult = null;

    private final JavaPlugin plugin;

    private UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static UpdateChecker init(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "Plugin object cannot be NULL");

        return instance != null ? instance : (instance = new UpdateChecker(plugin));
    }

    public CompletableFuture<UpdateResult> requestUpdateCheck() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(UPDATE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.addRequestProperty("User-Agent", USER_AGENT);

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());

                JsonElement element = new JsonParser().parse(reader);
                if (!element.isJsonObject()) {
                    return new UpdateResult(UpdateReason.INVALID_JSON);
                }

                reader.close();

                JsonObject versionObject = element.getAsJsonObject().getAsJsonObject("latest");
                JsonObject versionsObject = element.getAsJsonObject().getAsJsonObject("versions");

                if (!versionsObject.has(versionObject.get("spigotversion").getAsString())) {
                    return new UpdateResult(UpdateReason.INVALID_JSON);
                }

                JsonObject updateObject = versionsObject.getAsJsonObject(versionObject.get("spigotversion").getAsString());

                Version current = Version.valueOf(plugin.getDescription().getVersion());
                Version newest = Version.valueOf(versionObject.get("spigotversion").getAsString());

                String updateUrl = updateObject.getAsJsonObject("spigot")
                        .get("downloadUrl").getAsString();

                List<String> changelog = Arrays.stream(updateObject.get("changelog").getAsString().split("#"))
                        .filter(change -> change.trim().length() > 0)
                        .collect(Collectors.toList());

                return newest.greaterThan(current) ? new UpdateResult(UpdateReason.NEW_UPDATE, newest.toString(), updateUrl, changelog) :
                        new UpdateResult(UpdateReason.UP_TO_DATE);
            } catch (IOException e) {
                e.printStackTrace();
                return new UpdateResult(UpdateReason.COULD_NOT_CONNECT);
            } catch (JsonSyntaxException e) {
                return new UpdateResult(UpdateReason.INVALID_JSON);
            }
        });
    }

    public UpdateResult getLastResult() {
        return lastResult;
    }

    public static enum UpdateReason {
        NEW_UPDATE, UP_TO_DATE, COULD_NOT_CONNECT, INVALID_JSON;
    }

    public final class UpdateResult {
        private final UpdateReason reason;
        private final String newestVersion, url;
        private List<String> changelog;

        {
            UpdateChecker.this.lastResult = this;
        }

        private UpdateResult(UpdateReason reason, String newestVersion, String url, List<String> changelog) {
            this.reason = reason;
            this.newestVersion = newestVersion;
            this.url = url;
            this.changelog = changelog;
        }

        private UpdateResult(UpdateReason reason) {
            if (reason == UpdateReason.NEW_UPDATE) {
                throw new IllegalArgumentException("Reasons that require updates must also provide the latest version, URL and changelog");
            }

            this.reason = reason;
            this.newestVersion = plugin.getDescription().getVersion();
            this.url = null;
            this.changelog = null;
        }

        public boolean updateAvailable() {
            return this.reason == UpdateReason.NEW_UPDATE;
        }

        public String getNewestVersion() {
            return newestVersion;
        }

        public String getUrl() {
            return url;
        }

        public List<String> getChangelog() {
            return changelog;
        }

        @Override
        public String toString() {
            return "UpdateResult{" +
                    "reason=" + reason +
                    ", newestVersion='" + newestVersion + '\'' +
                    ", url='" + url + '\'' +
                    ", changelog=" + changelog +
                    '}';
        }
    }
}
