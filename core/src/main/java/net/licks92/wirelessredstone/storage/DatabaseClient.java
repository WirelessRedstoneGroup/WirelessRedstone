package net.licks92.wirelessredstone.storage;

import com.tylersuehr.sql.ContentValues;
import com.tylersuehr.sql.SQLiteDatabase;
import com.tylersuehr.sql.SQLiteOpenHelper;
import net.licks92.wirelessredstone.ConfigManager;
import net.licks92.wirelessredstone.Utils;
import net.licks92.wirelessredstone.WirelessRedstone;
import net.licks92.wirelessredstone.signs.SignType;
import net.licks92.wirelessredstone.signs.WirelessChannel;
import net.licks92.wirelessredstone.signs.WirelessPoint;
import net.licks92.wirelessredstone.signs.WirelessReceiver;
import net.licks92.wirelessredstone.signs.WirelessReceiverClock;
import net.licks92.wirelessredstone.signs.WirelessReceiverDelayer;
import net.licks92.wirelessredstone.signs.WirelessReceiverInverter;
import net.licks92.wirelessredstone.signs.WirelessReceiverSwitch;
import net.licks92.wirelessredstone.signs.WirelessScreen;
import net.licks92.wirelessredstone.signs.WirelessTransmitter;
import org.bukkit.block.BlockFace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;

public class DatabaseClient extends SQLiteOpenHelper {
    private static final String DB_NAME = "WirelessRedstoneDatabase";
    private static final int DB_VERSION = 1;

    private static final String TB_CHANNELS = "channel";
    private static final String TB_OWNERS = "owner";
    private static final String TB_TRANSMITTERS = "transmitter";
    private static final String TB_RECEIVERS = "receiver";
    private static final String TB_SCREENS = "screen";
    private static final String TB_INVERTERS = "inverter";
    private static final String TB_DELAYERS = "delayer";
    private static final String TB_SWITCH = "switch";
    private static final String TB_CLOCKS = "clock";

    private static volatile DatabaseClient instance;
    private final SQLiteDatabase db;


    private DatabaseClient(String channelFolder) {
        super(channelFolder + File.separator + DB_NAME, DB_VERSION);
        this.db = getWritableInstance();
    }

    protected static synchronized DatabaseClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseClient hasn't been initialized");
        }

        return instance;
    }

    protected static synchronized DatabaseClient init(String channelFolder) {
        if (instance == null) {
            Objects.requireNonNull(channelFolder, "Channel folder can't be null");

            instance = new DatabaseClient(channelFolder);
        }
        return instance;
    }

    @Override
    protected void onCreate(SQLiteDatabase db) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(WirelessRedstone.getInstance().getResource("database/Database_1.sql")),
                StandardCharsets.UTF_8))) {
            String sql = br.lines().collect(Collectors.joining(System.lineSeparator()));;
            db.execSql(sql);
        } catch (IOException ex) {
            WirelessRedstone.getWRLogger().info("There was an error while initializing the database.");

            ex.printStackTrace();
        }
    }

    @Override
    protected void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion) {
        WirelessRedstone.getWRLogger().info("Updating SQLite database. This could take a while. As a precaution, a backup will be created.");
        if (WirelessRedstone.getStorage().backupData()) {
            if (oldVersion == 0) {
                try {
                    performUpdate1(db);
                } catch (SQLException | IOException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException("There was an error while performing database update 1.");
                }
            }
        } else {
            throw new RuntimeException("There was an error while backing up the database. The channels folder couldn't be accessed.");
        }
        WirelessRedstone.getWRLogger().info("Updating SQLite database done.");
    }

    /**
     * Expose the SQLiteDatabase to anything that wants to use it.
     */
    protected SQLiteDatabase getDatabase() {
        return db;
    }

    protected Collection<WirelessChannel> getAllChannels() {
        Collection<WirelessChannel> channels = new ArrayList<>();

        try {
            ResultSet resultSet = getDatabase().query(TB_CHANNELS, null, null, null);
            while (resultSet.next()) {
                channels.add(new WirelessChannel(resultSet.getString("name"), resultSet.getBoolean("locked")));
            }

            resultSet.close();

            Iterator<WirelessChannel> iterator = channels.iterator();
            while (iterator.hasNext()) {
                WirelessChannel channel = iterator.next();

                resultSet = getDatabase().query(TB_OWNERS, new String[]{"user"}, "[channel_name]='" + channel.getName() + "'", null, null);
                while (resultSet.next()) {
                    channel.addOwner(resultSet.getString("user"));
                }

                resultSet.close();

                resultSet = getDatabase().query(TB_TRANSMITTERS, "[channel_name]='" + channel.getName() + "'", null, null);
                while (resultSet.next()) {
                    WirelessPoint point = new WirelessTransmitter(
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getInt("z"),
                            resultSet.getString("world"),
                            resultSet.getInt("is_wallsign") != 0,
                            BlockFace.valueOf(resultSet.getString("direction")),
                            resultSet.getString("owner")
                    );
                    channel.addWirelessPoint(point);
                    WirelessRedstone.getWRLogger().debug("Transmitter found: " + point);
                }

                resultSet.close();

                resultSet = getDatabase().query(TB_RECEIVERS, "[channel_name]='" + channel.getName() + "'", null, null);
                while (resultSet.next()) {
                    WirelessPoint point = new WirelessReceiver(
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getInt("z"),
                            resultSet.getString("world"),
                            resultSet.getInt("is_wallsign") != 0,
                            BlockFace.valueOf(resultSet.getString("direction")),
                            resultSet.getString("owner")
                    );
                    channel.addWirelessPoint(point);
                    WirelessRedstone.getWRLogger().debug("Receiver found: " + point);
                }

                resultSet.close();

                resultSet = getDatabase().query(TB_SCREENS, "[channel_name]='" + channel.getName() + "'", null, null);
                while (resultSet.next()) {
                    WirelessPoint point = new WirelessScreen(
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getInt("z"),
                            resultSet.getString("world"),
                            resultSet.getInt("is_wallsign") != 0,
                            BlockFace.valueOf(resultSet.getString("direction")),
                            resultSet.getString("owner")
                    );
                    channel.addWirelessPoint(point);
                    WirelessRedstone.getWRLogger().debug("Screen found: " + point);
                }

                resultSet.close();

                resultSet = getDatabase().query(TB_INVERTERS, "[channel_name]='" + channel.getName() + "'", null, null);
                while (resultSet.next()) {
                    WirelessPoint point = new WirelessReceiverInverter(
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getInt("z"),
                            resultSet.getString("world"),
                            resultSet.getInt("is_wallsign") != 0,
                            BlockFace.valueOf(resultSet.getString("direction")),
                            resultSet.getString("owner")
                    );
                    channel.addWirelessPoint(point);
                    WirelessRedstone.getWRLogger().debug("Inverter found: " + point);
                }

                resultSet.close();

                resultSet = getDatabase().query(TB_DELAYERS, "[channel_name]='" + channel.getName() + "'", null, null);
                while (resultSet.next()) {
                    WirelessPoint point = new WirelessReceiverDelayer(
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getInt("z"),
                            resultSet.getString("world"),
                            resultSet.getInt("is_wallsign") != 0,
                            BlockFace.valueOf(resultSet.getString("direction")),
                            resultSet.getString("owner"),
                            resultSet.getInt("delay")
                    );
                    channel.addWirelessPoint(point);
                    WirelessRedstone.getWRLogger().debug("Delayer found: " + point);
                }

                resultSet.close();

                resultSet = getDatabase().query(TB_SWITCH, "[channel_name]='" + channel.getName() + "'", null, null);
                while (resultSet.next()) {
                    WirelessPoint point = new WirelessReceiverSwitch(
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getInt("z"),
                            resultSet.getString("world"),
                            resultSet.getInt("is_wallsign") != 0,
                            BlockFace.valueOf(resultSet.getString("direction")),
                            resultSet.getString("owner"),
                            resultSet.getBoolean("powered")
                    );
                    channel.addWirelessPoint(point);
                    WirelessRedstone.getWRLogger().debug("Switch found: " + point);
                }

                resultSet.close();

                resultSet = getDatabase().query(TB_CLOCKS, "[channel_name]='" + channel.getName() + "'", null, null);
                while (resultSet.next()) {
                    WirelessPoint point = new WirelessReceiverClock(
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getInt("z"),
                            resultSet.getString("world"),
                            resultSet.getInt("is_wallsign") != 0,
                            BlockFace.valueOf(resultSet.getString("direction")),
                            resultSet.getString("owner"),
                            resultSet.getInt("delay")
                    );
                    channel.addWirelessPoint(point);
                    WirelessRedstone.getWRLogger().debug("Clock found: " + point);
                }

                resultSet.close();
            }
        } catch (SQLException ex) {
            WirelessRedstone.getWRLogger().severe("Couldn't retrieve channels from the database!");

            ex.printStackTrace();
        }
        return channels;
    }

    protected void recreateDatabase() {
        onCreate(getDatabase());
    }

    protected boolean insertWirelessPoint(WirelessChannel channel, WirelessPoint point) {
        if (point == null) {
            throw new IllegalArgumentException("WirelessPoint can not be null.");
        }

        try {
            if (isWirelessPointInDb(point)) {
                WirelessRedstone.getWRLogger().warning("WirelesPoint " + point + " is a duplicate in the storage. Skipping saving the WirelessPoint");
                return false;
            }
        } catch (SQLException ex) {
            WirelessRedstone.getWRLogger().warning("Database exception, enable debug mode to see the full stacktrace.");

            if (ConfigManager.getConfig().getDebugMode()) {
                ex.printStackTrace();
            }
        }

        ContentValues values = new ContentValues();
        try {
            if (!isChannelInDb(channel.getName())) {
                values.put("name", escape(channel.getName()));
                values.put("locked", channel.isLocked());
                getDatabase().insert(TB_CHANNELS, values);
                WirelessRedstone.getWRLogger().debug("Channel created in database. " + channel.getName());
            }
        } catch (SQLException ex) {
            WirelessRedstone.getWRLogger().warning("Database exception, enable debug mode to see the full stacktrace.");

            if (ConfigManager.getConfig().getDebugMode()) {
                ex.printStackTrace();
            }
        }

        String table;
        values = new ContentValues();
        values.put("x", point.getX());
        values.put("y", point.getY());
        values.put("z", point.getZ());
        values.put("world", point.getWorld());
        values.put("channel_name", channel.getName());
        values.put("direction", point.getDirection().toString());
        values.put("owner", point.getOwner());
        values.put("is_wallsign", point.isWallSign());

        if (point instanceof WirelessTransmitter) {
            table = TB_TRANSMITTERS;
        } else if (point instanceof WirelessScreen) {
            table = TB_SCREENS;
        } else if (point instanceof WirelessReceiver) {
            if (point instanceof WirelessReceiverInverter) {
                table = TB_INVERTERS;
            } else if (point instanceof WirelessReceiverDelayer) {
                table = TB_DELAYERS;
                values.put("delay", ((WirelessReceiverDelayer) point).getDelay());
            } else if (point instanceof WirelessReceiverSwitch) {
                table = TB_SWITCH;
                values.put("powered", ((WirelessReceiverSwitch) point).isActive());
            } else if (point instanceof WirelessReceiverClock) {
                table = TB_CLOCKS;
                values.put("delay", ((WirelessReceiverClock) point).getDelay());
            } else {
                table = TB_RECEIVERS;
            }
        } else {
            WirelessRedstone.getWRLogger().debug("Can't add wirelesspoint to database. Couldn't find what type the wirelesspoint is.");
            WirelessRedstone.getWRLogger().debug(point.toString());
            return false;
        }

        getDatabase().insert(table, values);
        WirelessRedstone.getWRLogger().debug("Placed new WirelessPoint in the database");

        return true;
    }

    protected void updateSwitch(WirelessReceiverSwitch receiver) {
        ContentValues values = new ContentValues();
        values.put("powered", receiver.isActive());
        getDatabase().update(TB_SWITCH, values,
                "[x]=" + receiver.getX() + " AND [y]=" + receiver.getY() + " AND [z]=" + receiver.getZ() + " AND [world]='" + receiver.getWorld() + "'");
    }

    protected boolean isChannelInDb(String channelName) throws SQLException {
        boolean exists = false;

        ResultSet resultSet = getDatabase().query(TB_CHANNELS, "[name]='" + escape(channelName) + "'", null, null);
        while (resultSet.next() && !exists) {
            exists = true;
        }

        resultSet.close();

        return exists;
    }

    protected boolean isWirelessPointInDb(WirelessPoint point) throws SQLException {
        boolean exists = false;
        String table;

        if (point instanceof WirelessTransmitter) {
            table = TB_TRANSMITTERS;
        } else if (point instanceof WirelessScreen) {
            table = TB_SCREENS;
        } else if (point instanceof WirelessReceiver) {
            if (point instanceof WirelessReceiverInverter) {
                table = TB_INVERTERS;
            } else if (point instanceof WirelessReceiverDelayer) {
                table = TB_DELAYERS;
            } else if (point instanceof WirelessReceiverSwitch) {
                table = TB_SWITCH;
            } else if (point instanceof WirelessReceiverClock) {
                table = TB_CLOCKS;
            } else {
                table = TB_RECEIVERS;
            }
        } else {
            WirelessRedstone.getWRLogger().debug("Can't find wirelesspoint in database. Couldn't find what type the wirelesspoint is.");
            WirelessRedstone.getWRLogger().debug(point.toString());
            return false;
        }

        ResultSet resultSet = getDatabase().query(table,
                "[x]=" + point.getX() + " AND [y]=" + point.getY() + " AND [z]=" + point.getZ() + " AND [world]='" + point.getWorld() + "'",
                null, null);
        while (resultSet.next() && !exists) {
            exists = true;
        }

        resultSet.close();

        return exists;
    }

    private void performUpdate1(SQLiteDatabase db) throws SQLException, IOException {
        Collection<WirelessChannel> channels = new ArrayList<>();
        Collection<String> channelNames = new ArrayList<>();
        int channelIteration = 0;
        int progress = 0;

        ResultSet resultSet = db.rawQuery("SELECT [name] FROM [sqlite_master] WHERE [type] = 'table'");

        while (resultSet.next()) {
            WirelessRedstone.getWRLogger().debug("Found channel: " + resultSet.getString(1));
            channelNames.add(resultSet.getString(1));
        }

        resultSet.close();

        for (String channelName : channelNames) {
            if ((int) Math.floor((float) channelIteration / (float) channelNames.size() * 100) % 5 == 0
                    && (int) Math.floor((float) channelIteration / (float) channelNames.size() * 100) != progress) {
                progress = (int) Math.floor((float) channelIteration / (float) channelNames.size() * 100);
                WirelessRedstone.getWRLogger().info("Database upgrade stage 1/2; Progress: " + progress + "%");
            }

            WirelessChannel channel = null;
            int channelInfoIteration = 0;
            resultSet = db.query(channelName, null, null, null);

            while (resultSet.next()) {
                if (channelInfoIteration == 0) {
                    if (resultSet.getString("name") != null) {
                        WirelessRedstone.getWRLogger().debug("---------------");
                        WirelessRedstone.getWRLogger().debug("Created channel: " + resultSet.getString("name") + " | " +
                                Collections.singletonList(resultSet.getString("owners")) + " | " +
                                resultSet.getBoolean("locked")
                        );

                        channel = new WirelessChannel(
                                resultSet.getString("name"),
                                Collections.singletonList(resultSet.getString("owners")),
                                resultSet.getBoolean("locked")
                        );
                    }
                }

                if (channelInfoIteration > 0 && channel == null) {
                    continue;
                }

                WirelessRedstone.getWRLogger().debug("---------------");

                if (channelInfoIteration > 0) {
                    if (resultSet.getString("signType") != null) {
                        String signTypeSerialized = resultSet.getString("signType");
                        SignType signType = getSignType(signTypeSerialized);
                        WirelessRedstone.getWRLogger().debug("SignType " + signType);

                        if (signType == null) {
                            continue;
                        }

                        switch (signType) {
                            case TRANSMITTER:
                                WirelessPoint point = new WirelessTransmitter(
                                        resultSet.getInt("x"),
                                        resultSet.getInt("y"),
                                        resultSet.getInt("z"),
                                        resultSet.getString("world"),
                                        resultSet.getInt("isWallSign") != 0,
                                        getBlockFaceOldDatabase(resultSet),
                                        resultSet.getString("signOwner")
                                );
                                channel.addWirelessPoint(point);

                                WirelessRedstone.getWRLogger().debug(point.toString());
                                break;
                            case RECEIVER:
                                point = new WirelessReceiver(
                                        resultSet.getInt("x"),
                                        resultSet.getInt("y"),
                                        resultSet.getInt("z"),
                                        resultSet.getString("world"),
                                        resultSet.getInt("isWallSign") != 0,
                                        getBlockFaceOldDatabase(resultSet),
                                        resultSet.getString("signOwner")
                                );
                                channel.addWirelessPoint(point);

                                WirelessRedstone.getWRLogger().debug(point.toString());
                                break;
                            case SCREEN:
                                point = new WirelessScreen(
                                        resultSet.getInt("x"),
                                        resultSet.getInt("y"),
                                        resultSet.getInt("z"),
                                        resultSet.getString("world"),
                                        resultSet.getInt("isWallSign") != 0,
                                        getBlockFaceOldDatabase(resultSet),
                                        resultSet.getString("signOwner")
                                );
                                channel.addWirelessPoint(point);

                                WirelessRedstone.getWRLogger().debug(point.toString());
                                break;
                            case RECEIVER_INVERTER:
                                point = new WirelessReceiverInverter(
                                        resultSet.getInt("x"),
                                        resultSet.getInt("y"),
                                        resultSet.getInt("z"),
                                        resultSet.getString("world"),
                                        resultSet.getInt("isWallSign") != 0,
                                        getBlockFaceOldDatabase(resultSet),
                                        resultSet.getString("signOwner")
                                );
                                channel.addWirelessPoint(point);

                                WirelessRedstone.getWRLogger().debug(point.toString());
                                break;
                            case RECEIVER_DELAYER:
                                int delay;

                                try {
                                    delay = Integer.parseInt(signTypeSerialized.split("_")[2]);
                                } catch (NumberFormatException e) {
                                    continue;
                                }

                                point = new WirelessReceiverDelayer(
                                        resultSet.getInt("x"),
                                        resultSet.getInt("y"),
                                        resultSet.getInt("z"),
                                        resultSet.getString("world"),
                                        resultSet.getInt("isWallSign") != 0,
                                        getBlockFaceOldDatabase(resultSet),
                                        resultSet.getString("signOwner"),
                                        delay
                                );
                                channel.addWirelessPoint(point);

                                WirelessRedstone.getWRLogger().debug(point.toString());
                                break;
                            case RECEIVER_SWITCH:
                                boolean state;

                                state = Boolean.parseBoolean(signTypeSerialized.split("_")[2]);

                                point = new WirelessReceiverSwitch(
                                        resultSet.getInt("x"),
                                        resultSet.getInt("y"),
                                        resultSet.getInt("z"),
                                        resultSet.getString("world"),
                                        resultSet.getInt("isWallSign") != 0,
                                        getBlockFaceOldDatabase(resultSet),
                                        resultSet.getString("signOwner"),
                                        state
                                );
                                channel.addWirelessPoint(point);

                                WirelessRedstone.getWRLogger().debug(point.toString());
                                break;
                            case RECEIVER_CLOCK:
                                try {
                                    delay = Integer.parseInt(signTypeSerialized.split("_")[2]);
                                } catch (NumberFormatException e) {
                                    continue;
                                }

                                point = new WirelessReceiverClock(
                                        resultSet.getInt("x"),
                                        resultSet.getInt("y"),
                                        resultSet.getInt("z"),
                                        resultSet.getString("world"),
                                        resultSet.getInt("isWallSign") != 0,
                                        getBlockFaceOldDatabase(resultSet),
                                        resultSet.getString("signOwner"),
                                        delay
                                );
                                channel.addWirelessPoint(point);

                                WirelessRedstone.getWRLogger().debug(point.toString());
                                break;
                        }
                    }
                }
                channelInfoIteration++;
            }
            resultSet.close();
            channels.add(channel);
            channelIteration++;

            db.execSql("DROP TABLE IF EXISTS [" + channelName + "];");
        }

        WirelessRedstone.getWRLogger().debug("---------------");

        onCreate(db);

        progress = 0;
        channelIteration = 0;
        for (WirelessChannel channel : channels) {
            if ((int) Math.floor((float) channelIteration / (float) channels.size() * 100) % 5 == 0
                    && (int) Math.floor((float) channelIteration / (float) channels.size() * 100) != progress) {
                progress = (int) Math.floor((float) channelIteration / (float) channels.size() * 100);
                WirelessRedstone.getWRLogger().info("Database upgrade stage 2/2; Progress: " + progress + "%");
            }

            ContentValues values = new ContentValues();
            values.put("name", channel.getName());
            values.put("locked", channel.isLocked());
            db.insert(TB_CHANNELS, values);
            WirelessRedstone.getWRLogger().debug("Inserted channel " + channel.getName());

            for (String owner : channel.getOwners()) {
                values = new ContentValues();
                values.put("channel_name", channel.getName());
                values.put("user", owner);
                db.insert(TB_OWNERS, values);
                WirelessRedstone.getWRLogger().debug("Inserted owner " + owner + "|" + channel.getName());
            }

            for (WirelessTransmitter point : channel.getTransmitters()) {
                values = new ContentValues();
                values.put("x", point.getX());
                values.put("y", point.getY());
                values.put("z", point.getZ());
                values.put("world", point.getWorld());
                values.put("channel_name", channel.getName());
                values.put("direction", point.getDirection().toString());
                values.put("owner", point.getOwner());
                values.put("is_wallsign", point.isWallSign());
                db.insert(TB_TRANSMITTERS, values);
                WirelessRedstone.getWRLogger().debug("Inserted transmitter " + point.toString() + "|" + channel.getName());
            }

            for (WirelessScreen point : channel.getScreens()) {
                values = new ContentValues();
                values.put("x", point.getX());
                values.put("y", point.getY());
                values.put("z", point.getZ());
                values.put("world", point.getWorld());
                values.put("channel_name", channel.getName());
                values.put("direction", point.getDirection().toString());
                values.put("owner", point.getOwner());
                values.put("is_wallsign", point.isWallSign());
                db.insert(TB_SCREENS, values);
                WirelessRedstone.getWRLogger().debug("Inserted screen " + point.toString() + "|" + channel.getName());
            }

            for (WirelessReceiver point : channel.getReceivers()) {
                if (point instanceof WirelessReceiverInverter) {
                    values = new ContentValues();
                    values.put("x", point.getX());
                    values.put("y", point.getY());
                    values.put("z", point.getZ());
                    values.put("world", point.getWorld());
                    values.put("channel_name", channel.getName());
                    values.put("direction", point.getDirection().toString());
                    values.put("owner", point.getOwner());
                    values.put("is_wallsign", point.isWallSign());
                    db.insert(TB_INVERTERS, values);
                    WirelessRedstone.getWRLogger().debug("Inserted inverter " + point.toString() + "|" + channel.getName());
                } else if (point instanceof WirelessReceiverDelayer) {
                    values = new ContentValues();
                    values.put("x", point.getX());
                    values.put("y", point.getY());
                    values.put("z", point.getZ());
                    values.put("world", point.getWorld());
                    values.put("channel_name", channel.getName());
                    values.put("direction", point.getDirection().toString());
                    values.put("owner", point.getOwner());
                    values.put("is_wallsign", point.isWallSign());
                    values.put("delay", ((WirelessReceiverDelayer) point).getDelay());
                    db.insert(TB_DELAYERS, values);
                    WirelessRedstone.getWRLogger().debug("Inserted delayer " + point.toString() + "|" + channel.getName());
                } else if (point instanceof WirelessReceiverSwitch) {
                    values = new ContentValues();
                    values.put("x", point.getX());
                    values.put("y", point.getY());
                    values.put("z", point.getZ());
                    values.put("world", point.getWorld());
                    values.put("channel_name", channel.getName());
                    values.put("direction", point.getDirection().toString());
                    values.put("owner", point.getOwner());
                    values.put("is_wallsign", point.isWallSign());
                    values.put("powered", ((WirelessReceiverSwitch) point).isActive());
                    db.insert(TB_SWITCH, values);
                    WirelessRedstone.getWRLogger().debug("Inserted switch " + point.toString() + "|" + channel.getName());
                } else if (point instanceof WirelessReceiverClock) {
                    values = new ContentValues();
                    values.put("x", point.getX());
                    values.put("y", point.getY());
                    values.put("z", point.getZ());
                    values.put("world", point.getWorld());
                    values.put("channel_name", channel.getName());
                    values.put("direction", point.getDirection().toString());
                    values.put("owner", point.getOwner());
                    values.put("is_wallsign", point.isWallSign());
                    values.put("delay", ((WirelessReceiverClock) point).getDelay());
                    db.insert(TB_CLOCKS, values);
                    WirelessRedstone.getWRLogger().debug("Inserted clock " + point.toString() + "|" + channel.getName());
                } else {
                    values = new ContentValues();
                    values.put("x", point.getX());
                    values.put("y", point.getY());
                    values.put("z", point.getZ());
                    values.put("world", point.getWorld());
                    values.put("channel_name", channel.getName());
                    values.put("direction", point.getDirection().toString());
                    values.put("owner", point.getOwner());
                    values.put("is_wallsign", point.isWallSign());
                    db.insert(TB_RECEIVERS, values);
                    WirelessRedstone.getWRLogger().debug("Inserted receiver " + point.toString() + "|" + channel.getName());
                }
            }
            channelIteration++;
        }
    }

    private SignType getSignType(String signTypeSerialized) {
        if (signTypeSerialized.equalsIgnoreCase("transmitter")) {
            return SignType.TRANSMITTER;
        } else if (signTypeSerialized.equalsIgnoreCase("receiver")) {
            return SignType.RECEIVER;
        } else if (signTypeSerialized.equalsIgnoreCase("screen")) {
            return SignType.SCREEN;
        } else if (signTypeSerialized.contains("receiver")) {
            String[] receiver = signTypeSerialized.split("_");

            if (receiver[1].equalsIgnoreCase("inverter")) {
                return SignType.RECEIVER_INVERTER;
            } else if (receiver[1].equalsIgnoreCase("delayer")) {
                return SignType.RECEIVER_DELAYER;
            } else if (receiver[1].equalsIgnoreCase("switch")) {
                return SignType.RECEIVER_SWITCH;
            } else if (receiver[1].equalsIgnoreCase("clock")) {
                return SignType.RECEIVER_CLOCK;
            }
        }

        return null;
    }

    private BlockFace getBlockFaceOldDatabase(ResultSet resultSet) throws SQLException {
        Object directionObject = resultSet.getObject("direction");

        if (directionObject instanceof Integer) {
            return Utils.getBlockFace(false, (int) directionObject);
        } else if (directionObject instanceof String) {
            return BlockFace.valueOf(directionObject.toString().toUpperCase());
        } else {
            throw new IllegalArgumentException("Direction (" + directionObject + ") row inside database isn't parsable.");
        }
    }

    private String escape(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException();
        }

        str = str.replace("\\", "\\\\");
        str = str.replace("'", "\\'");
        str = str.replace("\0", "\\0");
        str = str.replace("\n", "\\n");
        str = str.replace("\r", "\\r");
        str = str.replace("\"", "\\\"");
        str = str.replace("\\x1a", "\\Z");
        return str;
    }
}