package com.example.trantrungduong95.truesms.Model;
;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;


import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Presenter.IOUtils;
import com.example.trantrungduong95.truesms.Presenter.SmileyParser;
import com.example.trantrungduong95.truesms.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;


//Class holding a single message.
public class Message {
    //Bitmap showing the play button.
    public static Bitmap BITMAP_PLAY = Bitmap.createBitmap(1, 1, Config.RGB_565);

    //Filename for saved attachments.
    private String ATTACHMENT_FILE = "mms.";

    //Cache size.
    private static int CAHCESIZE = 50;

    //Internal Cache.
    private static LinkedHashMap<Integer, Message> CACHE = new LinkedHashMap<>(26, 0.9f, true);

    //INDEX: id.
    public static int INDEX_ID = 0;

    //INDEX: read.
    public static int INDEX_READ = 1;

    //INDEX: date.
    public static int INDEX_DATE = 2;

    //INDEX: thread_id.
    public static int INDEX_THREADID = 3;

    //INDEX: type.
    public static int INDEX_TYPE = 4;

    //INDEX: address.
    public static int INDEX_ADDRESS = 5;

    //INDEX: body.
    public static int INDEX_BODY = 6;

    //INDEX: subject.
    private int INDEX_SUBJECT = 7;

    //INDEX: m_type.
    private int INDEX_MTYPE = 8;

    //INDEX: mid.
    private int INDEX_MID = 1;

    //INDEX: content type.
    private int INDEX_CT = 2;

    //Cursor's projection.
    public static String[] PROJECTION = { //
            "_id", // 0
            "read", // 1
            Calls.DATE, // 2
            "thread_id", // 3
            Calls.TYPE, // 4
            "address", // 5
            "body", // 6
    };

    //Cursor's projection.
    public static String[] PROJECTION_SMS = { //
            PROJECTION[INDEX_ID], // 0
            PROJECTION[INDEX_READ], // 1
            PROJECTION[INDEX_DATE], // 2
            PROJECTION[INDEX_THREADID], // 3
            PROJECTION[INDEX_TYPE], // 4
            PROJECTION[INDEX_ADDRESS], // 5
            PROJECTION[INDEX_BODY], // 6
    };

    //Cursor's projection.
    public String[] PROJECTION_MMS = { //
            PROJECTION[INDEX_ID], // 0
            PROJECTION[INDEX_READ], // 1
            PROJECTION[INDEX_DATE], // 2
            PROJECTION[INDEX_THREADID], // 3
            "m_type", // 4
            PROJECTION[INDEX_ID], // 5
            PROJECTION[INDEX_ID], // 6
            "sub", // 7
            "m_type", // 8
    };

    //Cursor's projection.
    public static String[] PROJECTION_JOIN = { //
            PROJECTION[INDEX_ID], // 0
            PROJECTION[INDEX_READ], // 1
            PROJECTION[INDEX_DATE], // 2
            PROJECTION[INDEX_THREADID], // 3
            PROJECTION[INDEX_TYPE], // 4
            PROJECTION[INDEX_ADDRESS], // 5
            PROJECTION[INDEX_BODY], // 6
            "sub", // 7
            "m_type", // 8
    };

    //Cursor's projection for set read/unread operations.
    public static String[] PROJECTION_READ = { //
            PROJECTION[INDEX_ID], // 0
            PROJECTION[INDEX_READ], // 1
            PROJECTION[INDEX_DATE], // 2
            PROJECTION[INDEX_THREADID], // 3
    };

    //link Uri for parts.
    private Uri URI_PARTS = Uri.parse("content://mms/part/");

    //Cursor's projection for parts.
    private String[] PROJECTION_PARTS = { //
            "_id", // 0
            "mid", // 1
            "ct", // 2
    };

    //SQL WHERE: read/unread messages.
    public static String SELECTION_READ_UNREAD = "read = ?";

    //SQL WHERE: unread messages.
    public static String[] SELECTION_UNREAD = new String[]{"0"};

    //SQL WHERE: read messages.
    public static String[] SELECTION_READ = new String[]{"1"};

    // Cursor's sort, upside down.
    public static String SORT_VN = Calls.DATE + " ASC";

    //Cursor's sort, normal.
    public String SORT_NORM = Calls.DATE + " DESC";

    //Type for incoming sms.
    public static final int SMS_IN = Calls.INCOMING_TYPE;

    //Type for outgoing sms.
    public static final int SMS_OUT = Calls.OUTGOING_TYPE;

    //Type for sms drafts.
    public static final int SMS_DRAFT = 3;
    // Type for pending sms.
     public int SMS_PENDING = 4;

    //Type for incoming mms.
    public static final int MMS_IN = 132;

    //Type for outgoing mms.
    public static final int MMS_OUT = 128;

    //Type for mms drafts.
    // public int MMS_DRAFT = 128;

    //Type for pending mms.
    // public int MMS_PENDING = 128;

    //Type for not yet loaded mms.
    public int MMS_TOLOAD = 130;

    private int id;

    private long threadId;

    private long date;

    private String address;

    private CharSequence body;

    private int type;

    private int read;

    private String subject = null;

    private Bitmap picture = null;

    //link Integer to for viewing the content.
    private Intent contentIntent = null;

    //Is this message a MMS?
    private boolean isMms;

    public Message() {
    }

    //context link Context to spawn the link SmileyParser.
     //link Cursor to read the data
    public Message(Context context, Cursor cursor) {
        id = cursor.getInt(INDEX_ID);  // GEtLong
        threadId = cursor.getLong(INDEX_THREADID);
        date = cursor.getLong(INDEX_DATE);
        if (date < MainActivity.MIN_DATE) {
            date *= MainActivity.MILLIS;
        }
        if (cursor.getColumnIndex(PROJECTION_JOIN[INDEX_TYPE]) >= 0) {
            address = cursor.getString(INDEX_ADDRESS);
            body = cursor.getString(INDEX_BODY);
        } else {
            body = null;
            address = null;
        }
        type = cursor.getInt(INDEX_TYPE);
        read = cursor.getInt(INDEX_READ);
        if (body == null) {
            isMms = true;
            try {
                fetchMmsParts(context);
            } catch (OutOfMemoryError e) {
                Log.e("error loading parts", e.getMessage());
                try {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (Exception e1) {
                    Log.e("error creating Toast", e1.getMessage());
                }
            }
        } else {
            isMms = false;
        }
        try {
            subject = cursor.getString(INDEX_SUBJECT);
        } catch (IllegalStateException e) {
            subject = null;
        }
        try {
            if (cursor.getColumnCount() > INDEX_MTYPE) {
                int t = cursor.getInt(INDEX_MTYPE);
                if (t != 0) {
                    type = t;
                }
            }
        } catch (IllegalStateException e) {
            subject = null;
        }
    }

    //Update link Message.
    //cursor link Cursor to read from
    public void update(Cursor cursor) {
        read = cursor.getInt(INDEX_READ);
        type = cursor.getInt(INDEX_TYPE);
        try {
            if (cursor.getColumnCount() > INDEX_MTYPE) {
                int t = cursor.getInt(INDEX_MTYPE);
                if (t != 0) {
                    type = t;
                }
            }
        } catch (IllegalStateException e) {
            Log.e("wrong projection?", e.getMessage());
        }
    }

    //Fetch a part.
    private CharSequence fetchPart(InputStream is) {
        String ret = null;
        // get part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[256];
            int len = is.read(buffer);
            while (len >= 0) {
                baos.write(buffer, 0, len);
                len = is.read(buffer);
            }
            ret = new String(baos.toByteArray());
        } catch (IOException e) {
            Log.e("Failed toload part data", e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("Failed to close stream", e.getMessage());
                } // Ignore
            }
        }
        return ret;
    }

    //Fetch MMS parts.
    private void fetchMmsParts(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try{
            cursor = cr.query(URI_PARTS, null, PROJECTION_PARTS[INDEX_MID] + " = ?",
                    new String[]{String.valueOf(id)}, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }
            int iID = cursor.getColumnIndex(PROJECTION_PARTS[INDEX_ID]);
            int iCT = cursor.getColumnIndex(PROJECTION_PARTS[INDEX_CT]);
            int iText = cursor.getColumnIndex("text");
            do {
                int pid = cursor.getInt(iID);
                String ct = cursor.getString(iCT);
                Log.d("part: ", pid+ " "+ ct);

                // get part
                InputStream is = null;

                Uri uri = ContentUris.withAppendedId(URI_PARTS, pid);
                if (uri == null) {
                    Log.w("parts URI=null for pid=", pid+"");
                    continue;
                }
                try {
                    is = cr.openInputStream(uri);
                } catch (IOException | NullPointerException e) {
                    Log.e("Failed toload part data", e.getMessage());
                }
                if (is == null) {
                    Log.i("InputStream for part ", pid+ " is null");
                    if (iText >= 0 && ct != null && ct.startsWith("text/")) {
                        body = cursor.getString(iText);
                    }
                    continue;
                }
                if (ct == null) {
                    continue;
                }
                if (ct.startsWith("image/")) {
                    picture = BitmapFactory.decodeStream(is);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(uri, ct);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    contentIntent = i;
                    continue; // skip the rest
                } else if (ct.startsWith("video/") || ct.startsWith("audio/")) {
                    picture = BITMAP_PLAY;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(uri, ct);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    contentIntent = i;
                    continue; // skip the rest
                } else if (ct.startsWith("text/")) {
                    body = fetchPart(is);
                }

                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("Failed to close stream", e.getMessage());
                } // Ignore
            } while (cursor.moveToNext());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

     //Get a link Message from cache or link Cursor.
    public static Message getMessage(Context context, Cursor cursor) {
        synchronized (CACHE) {
            String body = cursor.getString(INDEX_BODY);
            int id = cursor.getInt(INDEX_ID);
            if (body == null) { // MMS
                id *= -1;
            }
            Message ret = CACHE.get(id);
            if (ret == null) {
                ret = new Message(context, cursor);
                CACHE.put(id, ret);
                Log.d("cachesize: ", CACHE.size()+"");
                while (CACHE.size() > CAHCESIZE) {
                    Integer i = CACHE.keySet().iterator().next();
                    Log.d("rm msg. from cache: ", i+"");
                    Message cc = CACHE.remove(i);
                    if (cc == null) {
                        //CACHE might be inconsistent!
                        break;
                    }
                }
            } else {
                ret.update(cursor);
            }
            return ret;
        }
    }

    //Flush all cached messages.
    public static void flushCache() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    public long getId() {
        return id;
    }

    public long getThreadId() {
        return threadId;
    }

    public long getDate() {
        return date;
    }

     //Context link Context to query SMS DB for an address.
    public String getAddress(Context context) {
        if (address == null && context != null) {
            String select = Message.PROJECTION[Message.INDEX_THREADID] + " = '"
                    + getThreadId() + "' and " + Message.PROJECTION[Message.INDEX_ADDRESS]
                    + " != ''";
            Log.d("select: ", select);
            Cursor cur = context.getContentResolver().query(Uri.parse("content://sms/"),
                    Message.PROJECTION, select, null, null);
            if (cur != null && cur.moveToFirst()) {
                address = cur.getString(Message.INDEX_ADDRESS);
                Log.d("found address: ", address);
            }
            if (cur != null) {
                cur.close();
            }
        }
        return address;
    }

    public CharSequence getBody() {
        return body;
    }

    public int getType() {
        return type;
    }

    public int getRead() {
        return read;
    }

    public boolean isMMS() {
        return isMms;
    }

    public String getSubject() {
        return subject;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public Intent getContentIntent() {
        return contentIntent;
    }

    // Get a link OnLongClickListener to save the attachment.
    public OnLongClickListener getSaveAttachmentListener(final Activity context) {
        if (contentIntent == null) {
            return null;
        }
        return new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    Log.d("save attachment: ", id+"");
                    String fn = ATTACHMENT_FILE;
                    Intent ci = contentIntent;
                    String ct = ci.getType();
                    Log.d("content type: ", ct);
                    if (ct == null) {
                        fn += "null";
                    } else if (ct.startsWith("image/")) {
                        switch (ct) {
                            case "image/jpeg":
                                fn += "jpg";
                                break;
                            case "image/gif":
                                fn += "gif";
                                break;
                            default:
                                fn += "png";
                                break;
                        }
                    } else if (ct.startsWith("audio/")) {
                        switch (ct) {
                            case "audio/3gpp":
                                fn += "3gpp";
                                break;
                            case "audio/mpeg":
                                fn += "mp3";
                                break;
                            case "audio/mid":
                                fn += "mid";
                                break;
                            default:
                                fn += "wav";
                                break;
                        }
                    } else if (ct.startsWith("video/")) {
                        if (ct.equals("video/3gpp")) {
                            fn += "3gpp";
                        } else {
                            fn += "avi";
                        }
                    } else {
                        fn += "ukn";
                    }
                    File file = createUniqueFile(Environment.getExternalStorageDirectory(), fn);
                    //noinspection ConstantConditions
                    InputStream in = context.getContentResolver().openInputStream(ci.getData());
                    OutputStream out = new FileOutputStream(file);
                    IOUtils.copy(in, out);
                    out.flush();
                    out.close();
                    //noinspection ConstantConditions
                    in.close();
                    Log.i("attachment saved: ", file.getPath());
                    Toast.makeText(context,
                            context.getString(R.string.attachment_saved) + " " + fn,
                            Toast.LENGTH_LONG).show();
                    return true;
                } catch (IOException e) {
                    Log.e("IO ERROR", e.getMessage());
                    Toast.makeText(context, R.string.attachment_not_saved, Toast.LENGTH_LONG)
                            .show();
                } catch (NullPointerException e) {
                    Log.e("NULL ERROR", e.getMessage());
                    Toast.makeText(context, R.string.attachment_not_saved, Toast.LENGTH_LONG)
                            .show();
                }
                return true;
            }
        };
    }

    //return link Uri of this link Message.
    public Uri getUri() {
        if (isMms) {
            return Uri.parse("content://mms/" + id);
        } else {
            return Uri.parse("content://sms/" + id);
        }
    }

    //Creates a unique file in the given directory by appending a hyphen and a number to the given filename.
    //Directory directory name
    //Return path to file
    private File createUniqueFile(File directory, String filename) {
        File file = new File(directory, filename);
        if (!file.exists()) {
            return file;
        }
        // Get the extension of the file, if any.
        int index = filename.lastIndexOf('.');
        String format;
        if (index != -1) {
            String name = filename.substring(0, index);
            String extension = filename.substring(index);
            format = name + "-%d" + extension;
        } else {
            format = filename + "-%d";
        }
        for (int i = 2; i < Integer.MAX_VALUE; i++) {
            file = new File(directory, String.format(format, i));
            if (!file.exists()) {
                return file;
            }
        }
        return null;
    }
}
