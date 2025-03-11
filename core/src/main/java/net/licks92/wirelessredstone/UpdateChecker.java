package net.licks92.wirelessredstone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class UpdateChecker {

    private static final String USER_AGENT = "WirelessRedstone-update-checker";
    private static final String UPDATE_URL = "https://wirelessredstonegroup.github.io/WirelessRedstoneUpdate/update2.json";

    private static volatile UpdateChecker instance; // Ensures thread-safety for singleton
    private final JavaPlugin plugin;
    private volatile UpdateResult lastResult; // Thread-safe storage
    private final HttpClient httpClient; // Modern HTTP client for async requests

    // Constructor
    private UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Initializes the UpdateChecker for the given plugin.
     */
    public static UpdateChecker init(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "Plugin object cannot be NULL");
        if (instance == null) { // Double-checked locking for thread safety
            synchronized (UpdateChecker.class) {
                if (instance == null) {
                    instance = new UpdateChecker(plugin);
                }
            }
        }
        return instance;
    }

    /**
     * Returns the current instance of UpdateChecker.
     *
     * @throws IllegalStateException if not initialized
     */
    public static UpdateChecker getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UpdateChecker is not initialized!");
        }
        return instance;
    }

    /**
     * Performs an asynchronous update check.
     *
     * @return a CompletableFuture that completes with the update result.
     */
    public CompletableFuture<UpdateResult> requestUpdateCheck() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(UPDATE_URL))
                        .header("User-Agent", USER_AGENT)
                        .build();

                // Send request and get response
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    plugin.getLogger().warning("Failed to fetch update info. HTTP Status: " + response.statusCode());
                    return new UpdateResult(UpdateReason.COULD_NOT_CONNECT, null, null, null);
                }

                // Parse the response body
                return parseResponse(response.body());
            } catch (IOException | InterruptedException e) {
                plugin.getLogger().severe("Failed to check for updates: " + e.getMessage());
                return new UpdateResult(UpdateReason.COULD_NOT_CONNECT, null, null, null);
            } catch (JsonSyntaxException e) {
                plugin.getLogger().warning("Invalid JSON received from update server.");
                return new UpdateResult(UpdateReason.INVALID_JSON, null, null, null);
            }
        });
    }

    /**
     * Parses the JSON response to determine update results.
     */
    private UpdateResult parseResponse(String responseBody) {
        JsonElement rootElement = JsonParser.parseString(responseBody);
        if (!rootElement.isJsonObject()) {
            return new UpdateResult(UpdateReason.INVALID_JSON, null, null, null);
        }

        JsonObject root = rootElement.getAsJsonObject();
        JsonObject latest = root.getAsJsonObject("latest");
        JsonObject versions = root.getAsJsonObject("versions");

        String spigotVersion = latest.get("spigotversion").getAsString();
        String currentVersion = plugin.getDescription().getVersion();

        if (!versions.has(spigotVersion)) {
            return new UpdateResult(UpdateReason.INVALID_JSON, null, null, null);
        }

        JsonObject updateData = versions.getAsJsonObject(spigotVersion);

        boolean isUpdateAvailable = versionGreaterThan(spigotVersion, currentVersion);
        if (isUpdateAvailable) {
            String downloadUrl = updateData.get("downloadUrl").getAsString();
            List<String> changelog = IntStream.range(0, updateData.getAsJsonArray("changelog").size())
                    .mapToObj(i -> updateData.getAsJsonArray("changelog").get(i).getAsString())
                    .toList();

            return new UpdateResult(UpdateReason.NEW_UPDATE, spigotVersion, downloadUrl, changelog);
        }

        return new UpdateResult(UpdateReason.UP_TO_DATE, currentVersion, null, null);
    }

    /**
     * Compares two version strings and determines if the first version is greater than the second.
     * The versions are expected to be in a semantic versioning format.
     *
     * @param newVersion the new version string to compare, must not be null
     * @param currentVersion the current version string to compare against, must not be null
     * @return true if the new version is greater than the current version; false if not, or if an exception occurs
     */
    private boolean versionGreaterThan(@NotNull String newVersion, @NotNull String currentVersion) {
        try {
            var newVer = com.github.zafarkhaja.semver.Version.valueOf(newVersion);
            var currentVer = com.github.zafarkhaja.semver.Version.valueOf(currentVersion);

            return newVer.greaterThan(currentVer);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid version string provided: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns the result of the last update check performed by the UpdateChecker.
     *
     * @return the {@link UpdateResult} object containing information about the update
     *         check, such as the update reason, newest version, download URL, and changelog.
     */
    public UpdateResult getLastResult() {
        return lastResult;
    }

    // Enum for update reason explanations
    public enum UpdateReason {
        NEW_UPDATE, UP_TO_DATE, COULD_NOT_CONNECT, INVALID_JSON
    }

    /**
     * Record to represent the result of an update check.
     */
    public record UpdateResult(UpdateReason reason, String newestVersion,
                               String downloadUrl, List<String> changelog) {

        public boolean updateAvailable() {
            return reason == UpdateChecker.UpdateReason.NEW_UPDATE;
        }

        @Override
        public String toString() {
            return "UpdateResult{" +
                    "reason=" + reason +
                    ", newestVersion='" + newestVersion + '\'' +
                    ", downloadUrl='" + downloadUrl + '\'' +
                    ", changelog=" + changelog +
                    '}';
        }

        public String getNewestVersion() {
            return newestVersion;
        }

        public String getUrl() {
            return downloadUrl;
        }
    }
}