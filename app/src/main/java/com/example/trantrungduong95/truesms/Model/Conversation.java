package com.example.trantrungduong95.truesms.Model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CallLog.Calls;
import android.util.Log;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Presenter.AsyncHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
//Class holding a single conversation.

public class Conversation {
    //Cache size.
    private static int CACHESIZE = 50;

    //Internal Cache.
    private static final LinkedHashMap<Integer, Conversation> CACHE = new LinkedHashMap<>(26, 0.9f, true);

    //link Uri to all threads.
    public static Uri URI_SIMPLE = Uri.parse("content://mms-sms/conversations").buildUpon().appendQueryParameter("simple", "true").build();

    private static String ID = BaseColumns._ID;

    private static String DATE = Calls.DATE;

    public static String COUNT = "message_count";

    private static String NID = "recipient_ids";

    private static String BODY = "snippet";

    private static String READ = "read";

    private static int INDEX_SIMPLE_ID = 0;

    private int INDEX_SIMPLE_DATE = 1;

    private int INDEX_SIMPLE_COUNT = 2;

    private int INDEX_SIMPLE_NID = 3;

    private int INDEX_SIMPLE_BODY = 4;

    private int INDEX_SIMPLE_READ = 5;

    //Cursor's projection.
    public static String[] PROJECTION_SIMPLE = { //
            ID, // 0
            DATE, // 1
            COUNT, // 2
            NID, // 3
            BODY, // 4
            READ, // 5
    };

    //Time of valid cache.
    private static long validCache = 0;

    private int id;

    private int threadId;

    private Contact contact;

    private long date;

    private String body;

    private int read;

    private int count = -1;

    private long lastUpdate = 0L;

    public Conversation() {
    }

    //Default constructor. Sync fetch of information
    private Conversation(Context context, Cursor cursor, boolean sync) {
        threadId = cursor.getInt(INDEX_SIMPLE_ID);
        date = cursor.getLong(INDEX_SIMPLE_DATE);
        body = cursor.getString(INDEX_SIMPLE_BODY);
        read = cursor.getInt(INDEX_SIMPLE_READ);
        count = cursor.getInt(INDEX_SIMPLE_COUNT);
        contact = new Contact(cursor.getInt(INDEX_SIMPLE_NID));

        AsyncHelper.fillConversation(context, this, sync);
        lastUpdate = System.currentTimeMillis();
    }
    //Update data. Sync fetch of information
    private void update(Context context, Cursor cursor, boolean sync) {
        Log.d("update", threadId+ ", "+ sync);
        if (cursor == null || cursor.isClosed()) {
            //"Conversation.update() on null/closed cursor
            return;
        }
        long d = cursor.getLong(INDEX_SIMPLE_DATE);
        if (d != date) {
            id = cursor.getInt(INDEX_SIMPLE_ID);
            date = d;
            body = cursor.getString(INDEX_SIMPLE_BODY);
        }
        count = cursor.getInt(INDEX_SIMPLE_COUNT);
        read = cursor.getInt(INDEX_SIMPLE_READ);
        int nid = cursor.getInt(INDEX_SIMPLE_NID);
        if (nid != contact.getRecipientId()) {
            contact = new Contact(nid);
        }
        if (lastUpdate < validCache) {
            AsyncHelper.fillConversation(context, this, sync);
            lastUpdate = System.currentTimeMillis();
        }
    }

    //Get a link Conversation.
    public static Conversation getConversation(Context context, Cursor cursor, boolean sync) {
        Log.d("getConversation", sync+"");
        synchronized (CACHE) {
            Conversation ret = CACHE.get(cursor.getInt(INDEX_SIMPLE_ID));
            if (ret == null) {
                ret = new Conversation(context, cursor, sync);
                CACHE.put(ret.getThreadId(), ret);
                Log.d("cachesize: ", CACHE.size()+"");
                while (CACHE.size() > CACHESIZE) {
                    Integer i = CACHE.keySet().iterator().next();
                    Log.d("rm con. from cache: ", i+"");
                    Conversation cc = CACHE.remove(i);
                    if (cc == null) {
                        //CACHE might be inconsistent!
                        break;
                    }
                }
            } else {
                ret.update(context, cursor, sync);
            }
            return ret;
        }
    }

    //Get a link Conversation. ForceUpdate force an update of that link Conversation
    public static Conversation getConversation(Context context, int threadId, boolean forceUpdate) {
        Log.d("getConversation", threadId+"");
        synchronized (CACHE) {
            Conversation ret = CACHE.get(threadId);
            if (ret == null || ret.getContact().getNumber() == null || forceUpdate) {
                Cursor cursor = context.getContentResolver().query(URI_SIMPLE, PROJECTION_SIMPLE,
                        ID + " = ?", new String[]{String.valueOf(threadId)}, null);
                if (cursor.moveToFirst()) {
                    ret = getConversation(context, cursor, true);
                } else {
                    Log.e("did not found conv: ", threadId+"");
                }
                cursor.close();
            }
            return ret;
        }
    }

    public static ArrayList<Block> getAllNumberConv(Context context){
        ArrayList<Block> blockArrayList =  new ArrayList<>();
        Uri uriSms = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(uriSms, null, null, null, null);
        int totalSMS = c.getCount();
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                Block block = new Block();
                String id = c.getString(c.getColumnIndexOrThrow("_id"));
                block.setId(Integer.parseInt(id));

                String phone = c.getString(c.getColumnIndexOrThrow("address"));
                block.setNumber(phone);
                blockArrayList.add(block);
            }
            c.close();
        }
        return blockArrayList;
    }

    //Flush all cached conversations.
    public static void flushCache() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    //Invalidate Cache.
    public static void invalidate() {
        validCache = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setNumberId(long nid) {
        contact = new Contact(nid);
    }

    public int getThreadId() {
        return threadId;
    }

    public long getDate() {
        return date;
    }

    public Contact getContact() {
        return contact;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String b) {
        body = b;
    }

    public int getRead() {
        return read;
    }

    //Set link Conversation's read status. Param status read status
    public void setRead(int status) {
        read = status;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int c) {
        count = c;
    }

    public Uri getUri() {
        return Uri.withAppendedPath(MainActivity.URI, String.valueOf(threadId));
    }
}
