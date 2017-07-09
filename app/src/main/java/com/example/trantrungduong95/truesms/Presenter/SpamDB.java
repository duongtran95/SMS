/*
package com.example.trantrungduong95.truesms.Presenter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Database holding blacklisted Block and Filtered list.
public class SpamDB {
    //Name of SQLiteDatabase.
    private String DATABASE_NAME = "Spam";

    ///Version of SQLiteDatabase.
    private int DATABASE_VERSION = 1;

    //Table in  SQLiteDatabase.
    private String DATABASE_TABLE = "Block";

    //Key in table.
    private String KEY_NUMBER = "number";

    //Projection.
    private String[] PROJECTION = new String[]{KEY_NUMBER};

    //SQL to create SQLiteDatabase.
    private String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS Block (number varchar(50) )";
    //link DatabaseHelper
    private DatabaseHelper dbHelper;

    //link SQLiteDatabase
    private SQLiteDatabase db;

    // Default constructor.
    private SpamDB(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //link DatabaseHelper for opening the database.
    private class DatabaseHelper extends SQLiteOpenHelper {
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
            db.execSQL("DROP TABLE IF EXISTS Block");
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

    //Insert a number into the spam database.
    private long insertNumber(String number) {
        if (number == null) {
            return -1;
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NUMBER, number);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Check if number is blacklisted.
    private boolean isInDB(String number) {
        if (number == null) {
            return false;
        }
        Cursor cursor = db.query(DATABASE_TABLE, PROJECTION, KEY_NUMBER + " = ?",
                new String[]{number}, null, null, null);
        boolean ret = cursor.moveToFirst();
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return ret;
    }

    // Get all blacklisted Block

    public int getEntrieCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(number) FROM " + DATABASE_TABLE, null);
        int ret = 0;
        if (cursor.moveToFirst()) {
            ret = cursor.getInt(0);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return ret;
    }

    // Get all entries from blacklist.
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

    //Remove number from blacklist.
    private void removeNumber(String number) {
        if (number == null) {
            return;
        }
        db.delete(DATABASE_TABLE, KEY_NUMBER + " = ?", new String[]{number});
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
            Log.e("ERR opening spam db",e+"");
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
                spamDB.insertNumber(number);
                Log.d("Added ", number+ " to spam list");
            } else {
                spamDB.removeNumber(number);
                Log.d("Removed ", number+ " from spam list");
            }
            spamDB.close();
        } catch (SQLiteException e) {
            //error opening spam db, doing nothing
            Log.e("Do nothing",e+"");
        }
    }

}
*/
