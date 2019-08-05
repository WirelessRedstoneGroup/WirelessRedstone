/*
 * MIT License
 *
 * Copyright (c) Tyler Suehr 2019.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tylersuehr.sql;
import net.licks92.wirelessredstone.WirelessRedstone;

import java.io.Closeable;
import java.io.File;

/**
 * Manages the SQLite database file, allowing it to be versioned.
 *
 * This helper affords the following database techniques:
 * (1) Code-First: Create a database during runtime.
 * (2) Database-First: Use an existing SQLite database file during runtime.
 *
 * <b>Code-First Approach</b>
 * Create an object and inherit {@link SQLiteOpenHelper}. Implement the abstract methods.
 *
 * When {@link SQLiteOpenHelper} is instantiated, if the SQLite database file doesn't exist,
 * it will be created and then will call, {@link #onCreate(SQLiteDatabase)}. This can be used
 * to create all your tables, views, and insert statements if needed.
 *
 * If the SQLite database file already exists, it will compare the given version to the
 * user_version of the database and call, {@link #onUpdate(SQLiteDatabase, int, int)}, if your
 * given version was higher than the user_version. This can be used to drop all the tables and
 * re-create them if you've updated the table structure.
 *
 * <b>Database-First Approach</b>
 * Create an object and inherit {@link SQLiteOpenHelper}. Implement the abstract methods.
 *
 * You will not need to worry about using {@link #onCreate(SQLiteDatabase)} because the database
 * will already have been created because it already exists.
 *
 * When {@link SQLiteOpenHelper} is instantiated, if the SQLite database file already exists, it
 * will compare the given version to the user_version of the database and call,
 * {@link #onUpdate(SQLiteDatabase, int, int)}, if your given version was higher than the
 * user_version. This can be used to drop all the tables and re-create them if you've updated
 * the table structure.
 *
 * @author Tyler Suehr
 */
public abstract class SQLiteOpenHelper implements Closeable {
    /* Stores reference to the SQLite database instance */
    private SQLiteDatabase database;
    /* Stores version of the SQLite database */
    private final int version;
    /* Stores name of the SQLite database */
    private final String name;


    public SQLiteOpenHelper(final String dbName, final int version) {
        this.name = (dbName.contains(".db") ? dbName : dbName.concat(".db"));
        this.version = version;
    }

    @Override
    public final void close() {
        if (database != null) {
            this.database.close();
        }
    }

    /**
     * Called when a new database is created.
     * @param db the created database
     */
    protected abstract void onCreate(SQLiteDatabase db);

    /**
     * Called when a database is updated.
     *
     * @param db the updated database
     * @param oldV the old user version number
     * @param newV the new user version number
     */
    protected abstract void onUpdate(SQLiteDatabase db, int oldV, int newV);

    /**
     * Lazily loads the SQLite database, ensuring only one instance is available.
     * @return the SQLite database
     */
    public final SQLiteDatabase getWritableInstance() {
        if (database == null) {
            final File file = new File(name);
            final boolean alreadyExists = file.exists();

            // This creates our SQLite database file for us, so check if it
            // already exists before this call.
            this.database = new SQLiteDatabase(name);

            // Check if the database file already exists
            if (alreadyExists) {
                // Check if the database should be updated
                final int curVersion = database.getVersion();
                if (version > curVersion) {
                    onUpdate(database, curVersion, version);
                    this.database.setVersion(version);
                    WirelessRedstone.getWRLogger().debug("SQLite database updated!");
                }
            } else {
                // Create our database, since it doesn't exist
                onCreate(database);
                this.database.setVersion(version);
                WirelessRedstone.getWRLogger().debug("SQLite database created!");
            }
        }
        return database;
    }
}