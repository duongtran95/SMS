package com.example.trantrungduong95.truesms.Model.Wrapper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


import com.example.trantrungduong95.truesms.Model.Contact;

import java.io.IOException;
import java.io.InputStream;

//Helper class to set/unset background for api5 systems.

public class ContactsWrapper5 extends ContactsWrapper {

    //Tag
    private String TAG = "cw5";

    //Uri for getting link Contact from number.
    private Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;

    //Selection for getting link Contact from number.
    private String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER + ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'";

    //Projection for getting {@link Contact} from number.
    private String[] CALLER_ID_PROJECTION = new String[]{Phone.NUMBER, // 0
            Phone.DISPLAY_NAME, // 1
            Phone.CONTACT_ID, // 2
            Phone.CONTACT_PRESENCE, // 3
            Phone.CONTACT_STATUS, // 4
            Contacts.LOOKUP_KEY, // 5
    };

    //Index in CALLER_ID_PROJECTION: number.
    private int INDEX_CALLER_ID_NUMBER = 0;

    //Index in CALLER_ID_PROJECTION: name.
    private int INDEX_CALLER_ID_NAME = 1;

    //Index in CALLER_ID_PROJECTION: id.
    private int INDEX_CALLER_ID_CONTACTID = 2;

    //Index in CALLER_ID_PROJECTION: presence.
    private int INDEX_CALLER_ID_PRESENCE = 3;

    //Index in CALLER_ID_PROJECTION: status.
    private int INDEX_CALLER_ID_STATUS = 4;

    //Index in CALLER_ID_PROJECTION: lookup key.
    private int INDEX_CALLER_ID_LOOKUP_KEY = 5;

    //Projection for persons query, filter.
    private String[] PROJECTION_FILTER = new String[]{Phone.LOOKUP_KEY, // 0
            Data.DISPLAY_NAME, // 1
            Phone.NUMBER, // 2
            Phone.TYPE // 3
    };

    //Projection for persons query, show.
    private String[] PROJECTION_CONTENT = new String[]{BaseColumns._ID, // 0
            Data.DISPLAY_NAME, // 1
            Phone.NUMBER, // 2
            Phone.TYPE // 3
    };

    //SQL to select mobile numbers only.
    private String MOBILES_ONLY = Phone.TYPE + " = " + Phone.TYPE_MOBILE;

    //Sort Order.
    private String SORT_ORDER = Phone.STARRED + " DESC, " + Phone.TIMES_CONTACTED + " DESC, " + Phone.DISPLAY_NAME + " ASC, " + Phone.TYPE;


    @Override
    public Bitmap loadContactPhoto(Context context, Uri contactUri) {
        if (contactUri == null) {
            return null;
        }
        try {
            ContentResolver cr = context.getContentResolver();
            InputStream is = Contacts.openContactPhotoInputStream(cr, contactUri);
            if (is == null) {
                return null;
            }
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            Log.e(TAG, "error getting photo: " + contactUri, e);
            return null;
        }
    }

    @Override
    public Uri getContactUri(long id) {
        return ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
    }

    @Override
    public Uri getContactUri(ContentResolver cr, String id) {
        if (id == null) {
            return null;
        }
        try {
            return Contacts.lookupContact(cr,
                    Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, id));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "unable to get uri for id: " + id, e);
            return null;
        }
    }

    @Override
    public Uri getLookupUri(ContentResolver cr, String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        Uri ret = null;
        Cursor c = cr.query(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, id),
                new String[]{Contacts.LOOKUP_KEY, BaseColumns._ID}, null, null, BaseColumns._ID
                        + " ASC");
        if (c != null) {
            if (c.moveToFirst()) {
                ret = Contacts.getLookupUri(c.getLong(1), id);
            }
            c.close();
        }
        if (ret == null) {
            ret = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, id);
        }
        return ret;
    }

    @Override
    public Cursor getContact(ContentResolver cr, String number) {
        String n = this.cleanNumber(number);
        if (n == null || n.length() == 0) {
            return null;
        }
        Uri uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, n);
        // FIXME: this is broken in android os; issue #8255
        Cursor c = cr.query(uri, PROJECTION_FILTER, null, null, null);
        if (c != null && c.moveToFirst()) {
            return c;
        }
        // Fallback to API3
        c = new ContactsWrapper3().getContact(cr, n);
        if (c != null && c.moveToFirst()) {
            // get orig API5 cursor for the real number
            String where = PROJECTION_FILTER[FILTER_INDEX_NUMBER] + " = '"
                    + c.getString(FILTER_INDEX_NUMBER) + "'";
            Cursor c0 = cr.query(Phone.CONTENT_URI, PROJECTION_FILTER, where, null, null);
            if (c0 != null && c0.moveToFirst()) {
                return c0;
            }
        }
        return null;
    }

    @Override
    protected Cursor getContact(ContentResolver cr, Uri uri) {
        // FIXME: this is broken in android os; issue #8255
        try {
            Cursor c = cr.query(uri, PROJECTION_FILTER, null, null, null);
            if (c != null && c.moveToFirst()) {
                return c;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error fetching contact: " + uri, e);
        }
        return null;
    }

    @Override
    public Uri getContentUri() {
        return Phone.CONTENT_URI;
    }

    @Override
    public String[] getContentProjection() {
        return PROJECTION_CONTENT;
    }

    @Override
    public String getMobilesOnlyString() {
        return MOBILES_ONLY;
    }

    @Override
    public String getContentSort() {
        return SORT_ORDER;
    }

    @Override
    public String getContentWhere(String filter) {
        String f = DatabaseUtils.sqlEscapeString('%' + filter + '%');
        StringBuilder s = new StringBuilder();
        s.append("(" + Data.DISPLAY_NAME + " LIKE ");
        s.append(f);
        s.append(") OR (" + Phone.DATA1 + " LIKE ");
        s.append(f);
        s.append(")");
        return s.toString();
    }

    @Override
    public Intent getPickPhoneIntent() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType(Phone.CONTENT_ITEM_TYPE);
        return i;
    }

    @Override
    public Intent getInsertPickIntent(String address) {
        Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        i.setType(Contacts.CONTENT_ITEM_TYPE);
        i.putExtra(ContactsContract.Intents.Insert.PHONE, address);
        i.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, Phone.TYPE_MOBILE);
        return i;
    }

    @Override
    public void showQuickContact(Context context, View target, Uri lookupUri,
                                 int mode, String[] excludeMimes) {
        ContactsContract.QuickContact.showQuickContact(context, target, lookupUri, mode,
                excludeMimes);
    }

    @Override
    public boolean updateContactDetails(Context context, boolean loadOnly,
                                        boolean loadAvatar, Contact contact) {
        if (contact == null) {
            Log.w(TAG, "updateContactDetails(null)");
            return false;
        }
        boolean changed = false;
        long rid = contact.mRecipientId;
        ContentResolver cr = context.getContentResolver();

        // mNumber
        String number = contact.mNumber;
        boolean changedNameAndNumber = false;
        if (rid > 0L && (!loadOnly || number == null)) {
            Cursor cursor = cr.query(ContentUris.withAppendedId(CANONICAL_ADDRESS, rid),
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                number = cursor.getString(0);
                if (number != null && !number.startsWith("000") && number.startsWith("00")) {
                    number = number.replaceFirst("^00", "+");
                }
                contact.mNumber = number;
                changedNameAndNumber = true;
                changed = true;
            }
            cursor.close();
        }

        // mName + mPersonId + mLookupKey + mPresenceState + mPresenceText
        if (number != null && (!loadOnly || contact.mName == null || contact.mPersonId < 0L)) {
            String n = PhoneNumberUtils.toCallerIDMinMatch(number);
            if (!TextUtils.isEmpty(n)) {
                String selection = CALLER_ID_SELECTION.replace("+", n);
                Cursor cursor = cr.query(PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION,
                        selection, new String[]{number}, null);

                if (cursor.moveToFirst()) {
                    long pid = cursor.getLong(INDEX_CALLER_ID_CONTACTID);
                    String lookup = cursor.getString(INDEX_CALLER_ID_LOOKUP_KEY);
                    String na = cursor.getString(INDEX_CALLER_ID_NAME);
                    int prs = cursor.getInt(INDEX_CALLER_ID_PRESENCE);
                    String prt = cursor.getString(INDEX_CALLER_ID_STATUS);
                    if (pid != contact.mPersonId) {
                        contact.mPersonId = pid;
                        changed = true;
                    }
                    if (lookup != null && !lookup.equals(contact.mLookupKey)) {
                        contact.mLookupKey = lookup;
                        changed = true;
                    }
                    if (na != null && !na.equals(contact.mName)) {
                        contact.mName = na;
                        changedNameAndNumber = true;
                        changed = true;
                    }
                    if (prs != contact.mPresenceState) {
                        contact.mPresenceState = prs;
                        changed = true;
                    }
                    if (prt != null && !prt.equals(contact.mPresenceText)) {
                        contact.mPresenceText = prt;
                        changed = true;
                    }
                }
                cursor.close();
            }
        }

        // mNameAndNumber
        if (changedNameAndNumber) {
            contact.updateNameAndNumer();
        }

        if (loadAvatar && contact.mAvatarData == null && contact.mAvatar == null) {
            byte[] data = this.loadAvatarData(context, contact);
            synchronized (contact) {
                if (data != null) {
                    contact.mAvatarData = data;
                    changed = true;
                }
            }
        }
        return changed;
    }

    ///Load the avatar data from the cursor into memory. Don't decode the data until someone callsfor it (see getAvatar).
    private byte[] loadAvatarData(Context context, Contact contact) {
        byte[] data = null;

        if (contact.mPersonId <= 0L || contact.mAvatar != null) {
            return null;
        }

        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contact.mPersonId);

        InputStream avatarDataStream = Contacts.openContactPhotoInputStream(
                context.getContentResolver(), contactUri);
        try {
            if (avatarDataStream != null) {
                data = new byte[avatarDataStream.available()];
                avatarDataStream.read(data, 0, data.length);
            }
        } catch (IOException e) {
            Log.e(TAG, "error recoding stream", e);
        } finally {
            try {
                if (avatarDataStream != null) {
                    avatarDataStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "error closing stream", e);
            }
        }

        return data;
    }
}