package net.licks92.wirelessredstone.sentry;

import io.sentry.DefaultSentryClientFactory;
import io.sentry.dsn.Dsn;

import java.util.Arrays;
import java.util.Collection;

public class WirelessRedstoneSentryClientFactory extends DefaultSentryClientFactory {

    @Override
    protected boolean getUncaughtHandlerEnabled(Dsn dsn) {
        return false;
    }

    @Override
    protected Collection<String> getInAppFrames(Dsn dsn) {
        return Arrays.asList("net.licks92.wirelessredstone", "org.bukkit", "org.spigotmc");
    }
}
