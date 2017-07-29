package com.example.trantrungduong95.truesms.CustomAdapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.example.trantrungduong95.truesms.R;

//CursorAdapter getting Name, Phone from DB

public class PhoneAdapter extends ResourceCursorAdapter {
    //Preferences: show mobile numbers only.
    private static boolean prefsMobilesOnly = false;

    //Global ContentResolver.
    private ContentResolver mContentResolver;

    //Projection for content.
    private String[] PROJECTION = new String[]{Phone._ID, // 0
            Phone.DISPLAY_NAME, // 1
            Phone.NUMBER, // 2
            Phone.TYPE // 3
    };

    //Index of id/lookup key.
    public int INDEX_ID = 0;

    public int INDEX_NAME = 1;

    public int INDEX_NUMBER = 2;

    public int INDEX_TYPE = 3;

    private String[] types;

    public PhoneAdapter(Context context) {
        super(context, R.layout.recipient_dropdown_item, null, true);
        mContentResolver = context.getContentResolver();
        types = context.getResources().getStringArray(android.R.array.phoneTypes);
    }

    private class ViewHolder {
        TextView tv1, tv2, tv3;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.tv1 = (TextView) view.findViewById(R.id.text1);
            holder.tv2 = (TextView) view.findViewById(R.id.text2);
            holder.tv3 = (TextView) view.findViewById(R.id.text3);
            view.setTag(holder);
        }
        holder.tv1.setText(cursor.getString(INDEX_NAME));
        holder.tv2.setText(cursor.getString(INDEX_NUMBER));
        int i = cursor.getInt(INDEX_TYPE) - 1;
        if (i >= 0 && i < types.length) {
            holder.tv3.setText(types[i]);
        } else {
            holder.tv3.setText("");
        }
    }

    @Override
    public String convertToString(Cursor cursor) {
        String name = cursor.getString(INDEX_NAME);
        String number = cursor.getString(INDEX_NUMBER);
        if (TextUtils.isEmpty(name)) {
            return cleanRecipient(number);
        }
        return name + " <" + cleanRecipient(number) + '>';
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String s = constraint == null ? null : constraint.toString();
        String where = prefsMobilesOnly ? Phone.TYPE + " = " + Phone.TYPE_MOBILE + " OR " + Phone.TYPE + " = " + Phone.TYPE_WORK_MOBILE : null;
        Uri u = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(s));
        Cursor cursor = mContentResolver.query(u, PROJECTION, where, null, Phone.DISPLAY_NAME);
        return cursor;
    }

    //param b set to true, if only mobile numbers should be displayed.
    public static void setMobileNumbersOnly(boolean b) {
        prefsMobilesOnly = b;
    }

    // Clean recipient's phone number from [ -.()<>]. Return clean number

    public static String cleanRecipient(String recipient) {
        if (TextUtils.isEmpty(recipient)) {
            return "";
        }
        String n;
        int i = recipient.indexOf("<");
        int j = recipient.indexOf(">");
        if (i != -1 && i < j) {
            n = recipient.substring(recipient.indexOf("<"), recipient.indexOf(">"));
        } else {
            n = recipient;
        }
        return n.replaceAll("[^*#+0-9]", "").replaceAll("^[*#][0-9]*#", "");
    }
}
