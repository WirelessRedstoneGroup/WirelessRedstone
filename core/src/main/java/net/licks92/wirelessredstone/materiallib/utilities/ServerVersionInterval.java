package net.licks92.wirelessredstone.materiallib.utilities;

public class ServerVersionInterval {
    private final ServerVersion lowBound;
    private final ServerVersion highBound;

    private ServerVersionInterval(ServerVersion lowBound, ServerVersion highBound) {
        if (lowBound == null && highBound == null) {
            throw new IllegalArgumentException("Both lowBound and highBound can't be null at the same time!");
        }
        if (lowBound == null) {
            lowBound = ServerVersion.OLDER;
        }
        if (highBound == null) {
            highBound = ServerVersion.NEWER;
        }
        if (lowBound.getOrder() > highBound.getOrder()) {
            throw new IllegalArgumentException("The lowBound can't be higher than the highBound!");
        }
        this.lowBound = lowBound;
        this.highBound = highBound;
    }

    public ServerVersion getLowBound() {
        return lowBound;
    }

    public ServerVersion getHighBound() {
        return highBound;
    }

    public boolean isBetweenBounds(ServerVersion version) {
        return version.isBetween(lowBound, highBound);
    }

    public static ServerVersionInterval since(ServerVersion version) {
        return new ServerVersionInterval(version, null);
    }

    public static ServerVersionInterval until(ServerVersion version) {
        return new ServerVersionInterval(null, version);
    }

    public static ServerVersionInterval between(ServerVersion lowBound, ServerVersion highBound) {
        return new ServerVersionInterval(lowBound, highBound);
    }
}
