package com.example.trantrungduong95.truesms.Presenter;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.text.TextUtils;

//Provide search results.
public class SearchProvider extends ContentProvider {

    private static String AUTHORITY = "com.example.trantrungduong95.truesms.Presenter.SearchProvider";

    //Uri to messages.

    private static Uri SMS_URI = Uri.parse("content://sms/");


    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        if (uri == null) {
            return null;
        }
        String query = uri.getLastPathSegment();
        if (TextUtils.isEmpty(query) || query.equals(SearchManager.SUGGEST_URI_PATH_QUERY)) {
            return null;
        }
        int limit = Utils.parseInt(uri.getQueryParameter("limit"), -1);
        //limit
        String[] proj = new String[]{"_id",
                "address as " + SearchManager.SUGGEST_COLUMN_TEXT_1,
                "body as " + SearchManager.SUGGEST_COLUMN_TEXT_2};
        String where = "body like '%" + query + "%'";
        return new MergeCursor(new Cursor[]{getContext().getContentResolver()
                .query(SMS_URI, proj, where, null, null)});
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("not implemented");
    }
}
