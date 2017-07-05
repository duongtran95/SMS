package com.example.trantrungduong95.truesms.CustomAdapter;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;
import com.example.trantrungduong95.truesms.Presenter.Converter;
import com.example.trantrungduong95.truesms.Presenter.SettingsOldActivity;
import com.example.trantrungduong95.truesms.Presenter.SmileyParser;
import com.example.trantrungduong95.truesms.Presenter.SpamDB;
import com.example.trantrungduong95.truesms.R;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;

// Adapter for the list of link Conversations.

public class ConversationsAdapter extends ResourceCursorAdapter {

    //Tag
    static String TAG = "coa";

    //Cursor's sort.
    private String SORT = Calls.DATE + " DESC";

    //Used text size, color.
    private int textSize, textColor;

    private BackgroundQueryHandler queryHandler;

    //Token for link BackgroundQueryHandler: message list query.
    private final int MESSAGE_LIST_QUERY_TOKEN = 0;

    //Reference to link MainActivity.
    private Activity mActivity;

    //List of blocked numbers.
    private String[] blacklist;

    private ContactsWrapper WRAPPER = ContactsWrapper.getInstance();

    //Default link Drawable for link Contacts.
    private Drawable defaultContactAvatar = null;

    //Convert NCR.
    private boolean convertNCR;

    //Show emoticons as images
    private boolean showEmoticons;

    private class ViewHolder {

        TextView tvBody;

        TextView tvPerson;

        TextView tvCount;

        TextView tvDate;

        ImageView ivPhoto;

        View vRead;
    }

    //Handle queries in background.
    private class BackgroundQueryHandler extends AsyncQueryHandler {

        //A helper class to help make handling asynchronous link ContentResolver queries easier.
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case MESSAGE_LIST_QUERY_TOKEN:
                    ConversationsAdapter.this.changeCursor(cursor);
                    ConversationsAdapter.this.mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    return;
                default:
            }
        }
    }

    // Default Constructor.
    public ConversationsAdapter(Activity activity) {
        super(activity, R.layout.conversationlist_item, null, true);//true
        mActivity = activity;
        ContentResolver cr = activity.getContentResolver();
        queryHandler = new BackgroundQueryHandler(cr);
        blacklist = SpamDB.getBlacklist(activity);

        defaultContactAvatar = activity.getResources().getDrawable(R.drawable.contact_blue);

        convertNCR = SettingsOldActivity.decodeDecimalNCR(activity);
        showEmoticons = SettingsOldActivity.showEmoticons(activity);
        textSize = SettingsOldActivity.getTextsize(activity);
        textColor = SettingsOldActivity.getTextcolor(activity);

        Cursor cursor = null;
        try {
            cursor = cr.query(Conversation.URI_SIMPLE, Conversation.PROJECTION_SIMPLE, Conversation.COUNT + ">0", null, null);
        } catch (Exception e) {
            Log.e("error getting conv", e+"");
        }

        if (cursor != null) {
            cursor.registerContentObserver(new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    if (!selfChange) {
                        Log.d(TAG, "call startMsgListQuery();");
                        ConversationsAdapter.this.startMsgListQuery();
                        Log.d(TAG, "invalidate cache");
                        Conversation.invalidate();
                    }
                }
            });
        }
         startMsgListQuery();
    }

    //Start ConversationList query.
    public void startMsgListQuery() {
        // Cancel any pending queries
        queryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);
        try {
            // Kick off the new query
            mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
            queryHandler.startQuery(MESSAGE_LIST_QUERY_TOKEN, null, Conversation.URI_SIMPLE, Conversation.PROJECTION_SIMPLE, Conversation.COUNT + ">0", null, SORT);
        } catch (SQLiteException e) {
            Log.e(TAG, "error starting query", e);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Conversation conv = Conversation.getConversation(context, cursor, true); // false
        Contact contact = conv.getContact();

        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.tvPerson = (TextView) view.findViewById(R.id.addr);
            holder.tvCount = (TextView) view.findViewById(R.id.count);
            holder.tvBody = (TextView) view.findViewById(R.id.body);
            holder.tvDate = (TextView) view.findViewById(R.id.date);
            holder.ivPhoto = (ImageView) view.findViewById(R.id.photo);
            holder.vRead = view.findViewById(R.id.read);
            view.setTag(holder);
        }

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (p.getBoolean(SettingsOldActivity.PREFS_HIDE_MESSAGE_COUNT, false)) {
            holder.tvCount.setVisibility(View.GONE);
        } else {
            int count = conv.getCount();
            if (count < 0) {
                holder.tvCount.setText("");
            } else {
                holder.tvCount.setText(context.getString(R.string.count) + ": " + conv.getCount() + "");
            }
        }
        if (textSize > 0) {
            holder.tvBody.setTextSize(textSize);
        }

        int col = textColor;
        if (col != 0) {
            holder.tvPerson.setTextColor(col);
            holder.tvBody.setTextColor(col);
            holder.tvCount.setTextColor(col);
            holder.tvDate.setTextColor(col);
        }

        if (MainActivity.showContactPhoto) {
            holder.ivPhoto.setImageDrawable(contact.getAvatar(mActivity, defaultContactAvatar));
            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.ivPhoto.setOnClickListener(WRAPPER.getQuickContact(context, holder.ivPhoto,
                    contact.getLookUpUri(context.getContentResolver()), 2, null));

        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }
        // body
        CharSequence text = conv.getBody();

        if (isBlocked(contact.getNumber())) {
            //view.setLayoutParams(new LinearLayout.LayoutParams(10, 10));
            //view.setVisibility(View.GONE);
            MainActivity.deleteMsg(conv.getUri(),context);
            holder.tvPerson.setText(contact.getDisplayName()+" "+ context.getString(R.string.blacklist));
        }
        else if (!isBlocked(contact.getNumber()) && SmsReceiver.filter(context,text.toString(),contact.getNumber())){
            holder.tvPerson.setText("");
            holder.tvPerson.setText(contact.getDisplayName()+" "+ context.getString(R.string.filter));
        }
        else {
            holder.tvPerson.setText(contact.getDisplayName());
        }

        // read status
        if (conv.getRead() == 0) {
            holder.vRead.setVisibility(View.VISIBLE);
        } else {
            holder.vRead.setVisibility(View.INVISIBLE);
        }


        if (text == null) {
            text = context.getString(R.string.mms_conversation);
        }
        if (convertNCR) {
            text = Converter.convertDecNCR2Char(text);
        }
        if (showEmoticons) {
            text = SmileyParser.getInstance(context).addSmileySpans(text);
        }
        holder.tvBody.setText(text);

        // date
        long time = conv.getDate();
        holder.tvDate.setText(MainActivity.getDate(context, time));

        // presence
        ImageView ivPresence = (ImageView) view.findViewById(R.id.presence);
        if (contact.getPresenceState() > 0) {
            ivPresence.setImageResource(Contact.getPresenceRes(contact.getPresenceState()));
            ivPresence.setVisibility(View.VISIBLE);
        } else {
            ivPresence.setVisibility(View.GONE);
        }
    }

    /**
     * Check if address is blacklisted.
     *
     * @param addr address
     * @return true if address is blocked
     */
    public boolean isBlocked(String addr) {
        if (addr == null) {
            return false;
        }
        for (String aBlacklist : blacklist) {
            if (addr.equals(aBlacklist)) {
                return true;
            }
        }
        return false;
    }
}
