package com.example.trantrungduong95.truesms.Presenter;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.Model.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

//Provide Messages as stream.
public class MessageProvider extends ContentProvider {

    //Tag
    static String TAG = "mp";

    //Content link Uri for messages.
    public static Uri CONTENT_URI = Uri.parse("content://com.example.trantrungduong95.truesms.Presenter/msg");

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
             String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        long mid = ContentUris.parseId(uri);
        String body = null;
        Cursor cursor = getContext().getContentResolver().query(Uri.parse("content://sms/" + mid), Message.PROJECTION_SMS, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            body = cursor.getString(Message.INDEX_BODY);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if (body != null) {
            try {
                File f = File.createTempFile("message." + mid, "txt");
                f.createNewFile();
                FileWriter fw = new FileWriter(f);
                fw.append(body);
                fw.close();
                return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
            } catch (IOException e) {
                //IO ERROR
                Toast.makeText(getContext(), "IO ERROR", Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }
}
