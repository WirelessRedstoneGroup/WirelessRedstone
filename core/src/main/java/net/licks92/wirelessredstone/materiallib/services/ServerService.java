package net.licks92.wirelessredstone.materiallib.services;

import net.licks92.wirelessredstone.materiallib.utilities.ServerVersion;
import org.bukkit.Server;

import java.util.logging.Logger;

public class ServerService {
    private final Logger logger;
    private final Server server;
    private ServerVersion serverVersion;

    public ServerService(Logger logger, Server server) {
        this.logger = logger;
        this.server = server;
    }

    public ServerVersion getVersion() {
        if (serverVersion != null) {
            return serverVersion;
        }
        String versionString = "V" + server.getBukkitVersion().split("-", 2)[0].replace(".", "_");
        try {
            serverVersion = ServerVersion.valueOf(versionString);
        } catch (IllegalArgumentException e) {
            logger.warning("Unknown server version " + versionString + ", assuming newer than " + ServerVersion.getLastKnown());
            serverVersion = ServerVersion.NEWER;
        }
        return serverVersion;
    }
}
