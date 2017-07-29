package com.example.trantrungduong95.truesms.Model.Wrapper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.People.Extensions;
import android.provider.Contacts.PeopleColumns;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;
import android.provider.Contacts.PresenceColumns;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.trantrungduong95.truesms.Model.Contact;

@SuppressWarnings("deprecation")
public class ContactsWrapperOld extends ContactsWrapper {

    //Tag
    private String TAG = "ContactsWrapperOld";

    //Selection for getting link Contact from number.
    private String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + PhonesColumns.NUMBER + ",?)";

    //link Uri for getting link Contact from number.
    private Uri PHONES_WITH_PRESENCE_URI = Uri.parse(Phones.CONTENT_URI + "_with_presence");

    //Projection for getting link Contact from number.
    private String[] CALLER_ID_PROJECTION = new String[]{PhonesColumns.NUMBER, // 0
            PeopleColumns.NAME, // 1
            Phones.PERSON_ID, // 2
            PresenceColumns.PRESENCE_STATUS, // 3
    };

    //link Uri for getting link Contact from number.
    private Uri PHONES_WITHOUT_PRESENCE_URI = Phones.CONTENT_URI;

    //Projection for getting link Contact from number.
    private String[] CALLER_ID_PROJECTION_WITHOUT_PRESENCE = new String[]{
            PhonesColumns.NUMBER, // 0
            PeopleColumns.NAME, // 1
            Phones.PERSON_ID, // 2
    };

    //Index in CALLER_ID_PROJECTION: number.
    private int INDEX_CALLER_ID_NUMBER = 0;

    //Index in CALLER_ID_PROJECTION: name.
    private int INDEX_CALLER_ID_NAME = 1;

    //Index in CALLER_ID_PROJECTION: id.
    private int INDEX_CALLER_ID_CONTACTID = 2;

    //Index in CALLER_ID_PROJECTION: presence.
    private int INDEX_CALLER_ID_PRESENCE = 3;

    //Projection for persons query, filter.
    private  String[] PROJECTION_FILTER = new String[]{Extensions.PERSON_ID, // 0
            PeopleColumns.NAME, // 1
            PhonesColumns.NUMBER, // 2
            PhonesColumns.TYPE // 3
    };

    //Projection for persons query, content.
    private String[] PROJECTION_CONTENT = new String[]{BaseColumns._ID, // 0
            PeopleColumns.NAME, // 1
            PhonesColumns.NUMBER, // 2
            PhonesColumns.TYPE // 3
    };

    //SQL to select mobile numbers only.
    private String MOBILES_ONLY = PhonesColumns.TYPE + " = "
            + PhonesColumns.TYPE_MOBILE;

    ///Sort Order.
    private String SORT_ORDER = PeopleColumns.STARRED + " DESC, "
            + PeopleColumns.TIMES_CONTACTED + " DESC, " + PeopleColumns.NAME + " ASC, "
            + PhonesColumns.TYPE;

    @Override
    public Uri getContactUri(long id) {
        return ContentUris.withAppendedId(People.CONTENT_URI, id);
    }

    @Override
    public Uri getContactUri(ContentResolver cr, String id) {
        try {
            return Uri.withAppendedPath(People.CONTENT_URI, id);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "unable to get uri for id: " + id, e);
            return null;
        }
    }

    @Override
    public Uri getLookupUri(ContentResolver cr, String id) {
        return this.getContactUri(null, id);
    }

    @Override
    public Bitmap loadContactPhoto(Context context, Uri contactUri) {
        if (contactUri == null) {
            return null;
        }
        try {
            return People.loadContactPhoto(context, contactUri, -1, null);
        } catch (Exception e) {
            Log.e(TAG, "error getting photo: " + contactUri, e);
            return null;
        }
    }

    @Override
    public Cursor getContact(ContentResolver cr, String number) {
        String n = this.cleanNumber(number);
        if (n == null || n.length() == 0) {
            return null;
        }
        Uri uri = Uri.withAppendedPath(Phones.CONTENT_FILTER_URL, n);
        Cursor c = cr.query(uri, PROJECTION_FILTER, null, null, null);
        if (c != null && c.moveToFirst()) {
            return c;
        }
        return null;
    }

    @Override
    protected Cursor getContact(ContentResolver cr, Uri uri) {
        try {
            String[] p = PROJECTION_FILTER;
            Cursor c = cr.query(uri, p, null, null, null);
            if (c.moveToFirst()) {
                return c;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error fetching contact: " + uri, e);
        }
        return null;
    }

    @Override
    public Uri getContentUri() {
        return Phones.CONTENT_URI;
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
        s.append("(" + PeopleColumns.NAME + " LIKE ");
        s.append(f);
        s.append(") OR (" + PhonesColumns.NUMBER + " LIKE ");
        s.append(f);
        s.append(")");
        return s.toString();
    }

    @Override
    public Intent getPickPhoneIntent() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType(Phones.CONTENT_ITEM_TYPE);
        return i;
    }

    @Override
    public Intent getInsertPickIntent(String address) {
        Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        i.setType(People.CONTENT_ITEM_TYPE);
        i.putExtra(Contacts.Intents.Insert.PHONE, address);
        i.putExtra(Contacts.Intents.Insert.PHONE_TYPE, PhonesColumns.TYPE_MOBILE);
        return i;
    }

    @Override
    public void showQuickContact(Context context, View target, Uri lookupUri,
                                 int mode, String[] excludeMimes) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, lookupUri));
    }

    @Override
    public boolean updateContactDetails(Context context, boolean loadOnly,
                                        boolean loadAvatar, Contact contact) {
        boolean changed = false;
        long rid = contact.mRecipientId;
        ContentResolver cr = context.getContentResolver();

        // mNumber
        String number = contact.mNumber;
        boolean changedNameAndNumber = false;
        if (rid > 0 && (!loadOnly || number == null)) {
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

        // mName + mPersonId + mLookupKey + mPresenceState
        if (number != null && (!loadOnly || contact.mName == null || contact.mPersonId < 0)) {
            String n = PhoneNumberUtils.stripSeparators(number);
            if (!TextUtils.isEmpty(n)) {
                boolean withpresence = true;
                Cursor cursor;
                try {
                    cursor = cr.query(PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION,
                            CALLER_ID_SELECTION, new String[]{n}, null);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "could not query: " + PHONES_WITH_PRESENCE_URI, e);
                    Log.i(TAG, "try without presence: " + PHONES_WITHOUT_PRESENCE_URI);
                    cursor = cr.query(PHONES_WITHOUT_PRESENCE_URI,
                            CALLER_ID_PROJECTION_WITHOUT_PRESENCE, CALLER_ID_SELECTION,
                            new String[]{n}, null);
                    withpresence = false;
                }
                if (cursor.moveToFirst()) {
                    long pid = cursor.getLong(INDEX_CALLER_ID_CONTACTID);
                    String na = cursor.getString(INDEX_CALLER_ID_NAME);
                    int prs = PRESENCE_STATE_UNKNOWN;
                    if (withpresence) {
                        cursor.getInt(INDEX_CALLER_ID_PRESENCE);
                    }
                    if (pid != contact.mPersonId) {
                        contact.mPersonId = pid;
                        contact.mLookupKey = String.valueOf(pid);
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
                }
                cursor.close();
            }
        }

        // mNameAndNumber
        if (changedNameAndNumber) {
            contact.updateNameAndNumer();
        }
        // mPresenceText;
        contact.mPresenceText = null;

        if (contact.mLookupKey == null && contact.mPersonId >= 0) {
            contact.mLookupKey = String.valueOf(contact.mPersonId);
            changed = true;
        }

        if (loadAvatar && contact.mPersonId >= 0) {
            // mAvatar[Data];
            Bitmap b = this.loadContactPhoto(context, this.getContactUri(contact.mPersonId));
            if (b == null) {
                contact.mAvatar = null;
                contact.mAvatarData = null;
            } else {
                contact.mAvatar = new BitmapDrawable(b);
            }
        }
        return changed;
    }
}
