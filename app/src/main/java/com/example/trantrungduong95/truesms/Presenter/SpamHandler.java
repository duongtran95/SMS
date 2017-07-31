package com.example.trantrungduong95.truesms.Presenter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Model.Filter;

import java.util.ArrayList;
import java.util.List;

public class SpamHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Spam";

    // Blocks table name
    private static final String TABLE_BLOCK = "Block";
    // Blocks Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NUMBER = "number";

    // Blocks table name
    private static final String TABLE_FILTER = "Filter";
    // Blocks Table Columns names
    private static final String KEY_IDF = "id";
    private static final String KEY_CHAR = "char";
    private static final String KEY_WORD = "word";
    private static final String KEY_PHARSE = "pharse";

    /*    // Blocks table name
    private static final String TABLE_FILTER = "Filter";
    // Blocks Table Columns names
    private static final String KEY_IDF = "id";
    private static final String KEY_TYPEF = "type";

    private static final String TABLE_CHAR = "Char";
    // Blocks Table Columns names
    private static final String KEY_IDC = "id";
    private static final String KEY_CHAR = "char";

    private static final String TABLE_WORD = "Word";
    // Blocks Table Columns names
    private static final String KEY_IDW = "id";
    private static final String KEY_WORD = "word";

    private static final String TABLE_PHARSE = "Pharse";
    // Blocks Table Columns names
    private static final String KEY_IDP = "id";
    private static final String KEY_PHARSE = "pharse";*/
    public SpamHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BLOCK_TABLE = "CREATE TABLE " + TABLE_BLOCK + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NUMBER + " TEXT" + ")";
        String CREATE_FILTER_TABLE = "CREATE TABLE " + TABLE_FILTER + "("
                + KEY_IDF + " INTEGER PRIMARY KEY," + KEY_CHAR + " TEXT,"
                + KEY_WORD + " TEXT," + KEY_PHARSE + " TEXT" + ")";
        db.execSQL(CREATE_BLOCK_TABLE);
        db.execSQL(CREATE_FILTER_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTER);
        // Create tables again
        onCreate(db);
    }

    public void createDefaultBlocksIfNeed() {
        int count = this.getBlocksCount();
        if (count == 0) {
            Block block = new Block(0, "123");
            Block block1 = new Block(1, "456");
            this.addBlock(block);
            this.addBlock(block1);
        }
    }

    public void addBlock(Block block) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NUMBER, block.getNumber());

        // Trèn một dòng dữ liệu vào bảng.
        db.insert(TABLE_BLOCK, null, values);

        // Đóng kết nối database.
        db.close();
    }


    public Block getBlock(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BLOCK, new String[]{KEY_ID, KEY_NUMBER}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Block block = new Block(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
        // return note
        return block;
    }


    public List<Block> getAllBlocks() {
        List<Block> blockList = new ArrayList<Block>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_BLOCK;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // Duyệt trên con trỏ, và thêm vào danh sách.
        if (cursor.moveToFirst()) {
            do {
                Block block = new Block();
                block.setId(Integer.parseInt(cursor.getString(0)));
                block.setNumber(cursor.getString(1));

                // Thêm vào danh sách.
                blockList.add(block);
            } while (cursor.moveToNext());
        }

        // return note list
        return blockList;
    }

    public int getBlocksCount() {

        String countQuery = "SELECT  * FROM " + TABLE_BLOCK;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        // return count
        return count;
    }


    public int updateBlock(Block block) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NUMBER, block.getNumber());

        // updating row
        return db.update(TABLE_BLOCK, values, KEY_ID + " = ?",
                new String[]{String.valueOf(block.getId())});
    }

    public void deleteBlock(Block block) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BLOCK, KEY_NUMBER + " = ?",
                new String[]{block.getNumber()});
        db.close();
    }

    public void deleteAllBlocks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_BLOCK); //delete all rows in a table
        db.close();
    }

    private boolean isInDB(String number) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (number == null) {
            return false;
        }
        Cursor cursor = db.query(TABLE_BLOCK, new String[]{KEY_ID, KEY_NUMBER}, KEY_NUMBER + " = ?",
                new String[]{number}, null, null, null);
        boolean c = cursor.moveToFirst();
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return c;
    }

    public boolean isBlacklisted(String number) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            boolean result = isInDB(number);
            db.close();
            return result;
        } catch (SQLiteException e) {
            //ER opening spam db, continue with empty list
            Log.e("error opening spam db", e + "");
            return false;
        }
    }

    //////////////////////////////////////////////Filter///////////////////////////////////////////////

    public void createDefaultFilterIfNeed() {
        int count = this.getFiltersCount();
        if (count == 0) {
            Filter filter = new Filter(0, null, "vip", null);
            Filter filter1 = new Filter(1, null, "[Hot]", null);
            Filter filter2 = new Filter(2, null, "[Vip]", null);
            Filter filter3 = new Filter(3, null, "qc", null);
            Filter filter4 = new Filter(4, null, "[QC]", null);
            Filter filter5 = new Filter(5, null, "lh", null);
            Filter filter6 = new Filter(6, null, "km", null);
            Filter filter7 = new Filter(7, null, "hot", null);
            Filter filter8 = new Filter(8, null, "kq", null);
            Filter filter9 = new Filter(9, null, "sim", null);
            Filter filter10 = new Filter(10, null, "l/h", null);
            Filter filter11 = new Filter(11, null, "loto", null);
            Filter filter12 = new Filter(12, null, "Tbao", null);

            Filter filter13 = new Filter(13, null, null, "cho vay");
            Filter filter14 = new Filter(14, null, null, "mua bán");
            Filter filter15 = new Filter(15, null, null, "bán sim");
            Filter filter16 = new Filter(16, null, null, "căn hộ");
            Filter filter17 = new Filter(17, null, null, "chung cư");
            Filter filter18 = new Filter(18, null, null, "trúng thưởng");
            Filter filter19 = new Filter(19, null, null, "thông báo");
            Filter filter20 = new Filter(20, null, null, "chúc mừng");
            Filter filter21 = new Filter(21, null, null, "khuyến mại");
            Filter filter22 = new Filter(22, null, null, "bán đất");
            Filter filter23 = new Filter(23, null, null, "bán nhà");
            Filter filter24 = new Filter(24, null, null, "miễn phí");
            Filter filter25 = new Filter(25, null, null, "quảng cáo");
            Filter filter26 = new Filter(26, null, null, "siêu hot");
            Filter filter27 = new Filter(27, null, null, "siêu rẻ");
            Filter filter28 = new Filter(28, null, null, "may mắn");
            Filter filter29 = new Filter(29, null, null, "(miễn phí)");

            this.addFilter(filter);
            this.addFilter(filter1);
            this.addFilter(filter2);
            this.addFilter(filter3);
            this.addFilter(filter4);
            this.addFilter(filter5);
            this.addFilter(filter6);
            this.addFilter(filter7);
            this.addFilter(filter8);
            this.addFilter(filter9);
            this.addFilter(filter10);
            this.addFilter(filter11);
            this.addFilter(filter12);
            this.addFilter(filter13);
            this.addFilter(filter14);
            this.addFilter(filter15);
            this.addFilter(filter16);
            this.addFilter(filter17);
            this.addFilter(filter18);
            this.addFilter(filter19);
            this.addFilter(filter20);
            this.addFilter(filter21);
            this.addFilter(filter22);
            this.addFilter(filter23);
            this.addFilter(filter24);
            this.addFilter(filter25);
            this.addFilter(filter26);
            this.addFilter(filter27);
            this.addFilter(filter28);
            this.addFilter(filter29);
        }
    }


    public long addFilter(Filter filter) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHAR, filter.getChar_());
        values.put(KEY_WORD, filter.getWord_());
        values.put(KEY_PHARSE, filter.getPharse_());

        // Trèn một dòng dữ liệu vào bảng.
        long id = db.insert(TABLE_FILTER, null, values);

        // Đóng kết nối database.
        db.close();

        return id;
    }


    public Filter getFilter(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FILTER, new String[]{KEY_IDF,
                        KEY_CHAR, KEY_WORD, KEY_PHARSE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Filter filter = new Filter(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3));
        // return note
        return filter;
    }




    public List<Filter> getAllFilters() {
        List<Filter> filterList = new ArrayList<Filter>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_FILTER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // Duyệt trên con trỏ, và thêm vào danh sách.
        if (cursor.moveToFirst()) {
            do {
                Filter filter = new Filter();
                filter.setId_(Integer.parseInt(cursor.getString(0)));
                filter.setChar_(cursor.getString(1));
                filter.setWord_(cursor.getString(2));
                filter.setPharse_(cursor.getString(3));
                // Thêm vào danh sách.
                filterList.add(filter);
            } while (cursor.moveToNext());
        }

        // return note list
        return filterList;
    }

    public int getFiltersCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FILTER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        // return count
        return count;
    }


    public int updateFilters(Filter filter) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHAR, filter.getChar_());
        values.put(KEY_WORD, filter.getWord_());
        values.put(KEY_PHARSE, filter.getPharse_());
        // updating row
        int editid = db.update(TABLE_FILTER, values, KEY_IDF + " = ?",
                new String[]{String.valueOf(filter.getId_())});
        return editid;
    }

    public void deleteFilter(Filter filter) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FILTER, KEY_IDF + " = ?",
                new String[] { String.valueOf(filter.getId_()) });
        db.close();
    }
    public void deleteAllFilters(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_FILTER); //delete all rows in a table
        db.close();
    }
}
