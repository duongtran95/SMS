package com.example.trantrungduong95.truesms.Presenter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Database holding blacklisted numbers.
public class SpamDB {
    //Name of SQLiteDatabase.
    private static String DATABASE_NAME = "spamlist";

    ///Version of SQLiteDatabase.
    private static int DATABASE_VERSION = 1;

    //Table in  SQLiteDatabase.
    private static String DATABASE_TABLE = "numbers";

    //Key in table.
    private static String KEY_NR = "nr";

    //Projection.
    private static String[] PROJECTION = new String[]{KEY_NR};

    //SQL to create SQLiteDatabase.
    private static String DATABASE_CREATE
            = "CREATE TABLE IF NOT EXISTS numbers (nr varchar(50) )";
    //link DatabaseHelper
    private DatabaseHelper dbHelper;

    //link SQLiteDatabase
    private SQLiteDatabase db;

    // Default constructor.
    private SpamDB(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //link DatabaseHelper for opening the database.
    private static class DatabaseHelper extends SQLiteOpenHelper {

        /**
         * Default constructor.
         *
         * @param context {@link Context}
         */
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //Upgrading database from version  oldVersion  to  newVersion
            //  which will destroy all old data"
            Log.w("Upgrading db from vs: ", oldVersion+ " to "+ newVersion);
            db.execSQL("DROP TABLE IF EXISTS numbers");
            onCreate(db);
        }
    }

    // Open database.

    private SpamDB open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    //Close database.
    public void close() {
        dbHelper.close();
    }

    /**
     * Insert a number into the spam database.
     *
     * @param nr number
     * @return id in database
     */
    private long insertNr(String nr) {
        if (nr == null) {
            return -1L;
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NR, nr);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Check if number is blacklisted.
     *
     * @param nr number
     * @return true if number is blacklisted
     */
    private boolean isInDB(String nr) {
        if (nr == null) {
            return false;
        }
        Cursor cursor = db.query(DATABASE_TABLE, PROJECTION, KEY_NR + " = ?",
                new String[]{nr}, null, null, null);
        boolean ret = cursor.moveToFirst();
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return ret;
    }

    /**
     * Get all blacklisted numbers.
     *
     * @return blacklist
     */
    public int getEntrieCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(nr) FROM " + DATABASE_TABLE, null);
        Log.d("cusor", cursor.toString());
        int ret = 0;
        if (cursor.moveToFirst()) {
            ret = cursor.getInt(0);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return ret;
    }

    /**
     * Get all entries from blacklist.
     *
     * @return array of entries
     */
    private String[] getAllEntries() {
        Cursor cursor = db.query(DATABASE_TABLE, PROJECTION, null, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        String[] ret = new String[cursor.getCount()];
        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                ret[i] = cursor.getString(0);
                Log.d("spam: ", ret[i]);
                ++i;
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return ret;
    }

    /**
     * Remove number from blacklist.
     *
     * @param nr number
     */
    private void removeNr(String nr) {
        if (nr == null) {
            return;
        }
        db.delete(DATABASE_TABLE, KEY_NR + " = ?", new String[]{nr});
    }

    public static String[] getBlacklist(Context context) {
        String[] blacklist;
        try {
            SpamDB spamDB = new SpamDB(context);
            spamDB.open();
            blacklist = spamDB.getAllEntries();
            spamDB.close();
        } catch (SQLiteException e) {
            //ER opening spam db, continue with empty list
            Log.e("ER opening spam db",e+"");
            blacklist = new String[0];
        }
        return blacklist;
    }

    public static boolean isBlacklisted(Context context, String number) {
        try {
            SpamDB spamDB = new SpamDB(context);
            spamDB.open();
            boolean result = spamDB.isInDB(number);
            spamDB.close();
            return result;
        } catch (SQLiteException e) {
            //ER opening spam db, continue with empty list
            Log.e("error opening spam db",e+"");
            return false;
        }
    }

    public static void toggleBlacklist(Context context, String number) {
        try {
            SpamDB spamDB = new SpamDB(context);
            spamDB.open();
            if (!spamDB.isInDB(number)) {
                spamDB.insertNr(number);
                Log.d("Added ", number+ " to spam list");
            } else {
                spamDB.removeNr(number);
                Log.d("Removed ", number+ " from spam list");
            }
            spamDB.close();
        } catch (SQLiteException e) {
            //error opening spam db, doing nothing
            Log.e(" do nothing",e+"");
        }
    }
}
