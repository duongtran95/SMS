package com.example.trantrungduong95.truesms.CustomAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;
import com.example.trantrungduong95.truesms.Presenter.Converter;
import com.example.trantrungduong95.truesms.Presenter.Activity_.SettingsOldActivity;
import com.example.trantrungduong95.truesms.Presenter.SmileyParser;
import com.example.trantrungduong95.truesms.R;

import java.util.List;

public class FragmentFilterdAdapter extends ArrayAdapter<Conversation> {
    private Context context;
    private int layoutResourceId;
    private List<Conversation> conversations;
    //Used text size, color.
    private int textSize, textColor;
    //Default link Drawable for link Contacts.
    private Drawable defaultContactAvatar = null;
    //Convert NCR.
    private boolean convertNCR;
    //Show emoticons as images
    private boolean showEmoticons;

    private ContactsWrapper WRAPPER = ContactsWrapper.getInstance();

    public FragmentFilterdAdapter(Context context, int layoutResourceId, List<Conversation> conversations) {
        super(context, layoutResourceId, conversations);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.conversations = conversations;

        defaultContactAvatar = context.getResources().getDrawable(R.drawable.contact_blue);

        convertNCR = SettingsOldActivity.decodeDecimalNCR(context);
        showEmoticons = SettingsOldActivity.showEmoticons(context);
        textSize = SettingsOldActivity.getTextsize(context);
        textColor = SettingsOldActivity.getTextcolor(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ConversationsHolder holder = null;

        if (view == null) {
            LayoutInflater inflater = /*((Activity) context).getLayoutInflater()*/LayoutInflater.from(context);
            view = inflater.inflate(layoutResourceId, parent, false);

            holder = new ConversationsHolder();
            holder.tvPerson = (TextView) view.findViewById(R.id.addr);
            holder.tvCount = (TextView) view.findViewById(R.id.count);
            holder.tvBody = (TextView) view.findViewById(R.id.body);
            holder.tvDate = (TextView) view.findViewById(R.id.date);
            holder.ivPhoto = (ImageView) view.findViewById(R.id.photo);
            holder.vRead = view.findViewById(R.id.read);
            view.setTag(holder);
        } else {
            holder = (ConversationsHolder) view.getTag();
        }

        Conversation conversation = conversations.get(position);
        Contact contact = conversation.getContact();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (p.getBoolean(SettingsOldActivity.PREFS_HIDE_MESSAGE_COUNT, false)) {
            holder.tvCount.setVisibility(View.GONE);
        } else {
            int count = conversation.getCount();
            if (count < 0) {
                holder.tvCount.setText("");
            } else {
                holder.tvCount.setText("");
                //holder.tvCount.setText(context.getString(R.string.count) + ": " + conversation.getCount() + "");
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
            holder.ivPhoto.setImageDrawable(contact.getAvatar(context, defaultContactAvatar));
            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.ivPhoto.setOnClickListener(WRAPPER.getQuickContact(context, holder.ivPhoto,
                    contact.getLookUpUri(context.getContentResolver()), 2, null));

        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }
        holder.tvPerson.setText(contact.getDisplayName());
        // read status
        if (conversation.getRead() == 0) {
            holder.vRead.setVisibility(View.VISIBLE);
        } else {
            holder.vRead.setVisibility(View.INVISIBLE);
        }
        // body
        CharSequence text = conversation.getBody();
        if (text == null) {
            text = context.getString(R.string.mms_conversation);
        }
        if (convertNCR) {
            text = Converter.convertDecNCR2Char(text);
        }
        if (showEmoticons) {
            text = SmileyParser.getInstance(context).addSmileySpans(text);
        }
        //todo show sms filtered last
        holder.tvBody.setText(text);

        // date
        long time = conversation.getDate();
        holder.tvDate.setText(MainActivity.getDate(context, time));

        // presence
        ImageView ivPresence = (ImageView) view.findViewById(R.id.presence);
        if (contact.getPresenceState() > 0) {
            ivPresence.setImageResource(Contact.getPresenceRes(contact.getPresenceState()));
            ivPresence.setVisibility(View.VISIBLE);
        } else {
            ivPresence.setVisibility(View.GONE);
        }

        return view;
    }

    private static class ConversationsHolder
    {
        TextView tvBody;

        TextView tvPerson;

        TextView tvCount;

        TextView tvDate;

        ImageView ivPhoto;

        View vRead;

    }
}
