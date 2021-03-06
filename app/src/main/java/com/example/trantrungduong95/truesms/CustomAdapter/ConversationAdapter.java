package com.example.trantrungduong95.truesms.CustomAdapter;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.Presenter.Activity_.DefaultAndPermission;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Message;
import com.example.trantrungduong95.truesms.Presenter.Activity_.ConversationActivity;
import com.example.trantrungduong95.truesms.Presenter.Activity_.FilterActivity;
import com.example.trantrungduong95.truesms.Presenter.Converter;
import com.example.trantrungduong95.truesms.Presenter.MessageProvider;
import com.example.trantrungduong95.truesms.Presenter.Activity_.SettingsOldActivity;
import com.example.trantrungduong95.truesms.Presenter.SmileyParser;
import com.example.trantrungduong95.truesms.R;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;

// Adapter for the list of link Conversation.
public class ConversationAdapter extends ResourceCursorAdapter {

    //Used background drawable for messages.
    private int backgroundDrawableIn, backgroundDrawableOut;

    //General WHERE clause.
    private static String WHERE = "(" + Message.PROJECTION_JOIN[Message.INDEX_TYPE] + " != "
            + Message.SMS_DRAFT + " OR " + Message.PROJECTION_JOIN[Message.INDEX_TYPE]
            + " IS NULL)";

    //WHERE clause for drafts.
    private static String WHERE_DRAFT = "(" + Message.PROJECTION_SMS[Message.INDEX_THREADID]
            + " = ? AND " + Message.PROJECTION_SMS[Message.INDEX_TYPE] + " = " + Message.SMS_DRAFT
            + ")";
    // + " OR " + type + " = " + Message.SMS_PENDING;

    private ConversationActivity mActivity;

    //Display Name (name if !=null, else address).
    private String displayName = null;

    //Used text size/color.
    private int textSize, textColor;

    //Convert NCR.
    private boolean convertNCR;

    //Show emoticons as images
    private boolean showEmoticons;

    //View holder.
    private class ViewHolder {

        TextView tvBody;

        TextView tvPerson;

        TextView tvDate;

        ImageView ivPhoto;

        View vRead;

        View vPending;

        View vLayout;

        ImageView ivInOut;

        Button btnDownload;

        Button btnImport;

        Button btnShow;
    }

    private boolean flag1;

    public ConversationAdapter(ConversationActivity c, Uri uri,boolean flag) {
        super(c, R.layout.conversation_item, getCursor(c.getContentResolver(), uri), true);
        mActivity = c;
        flag1 = flag;
        backgroundDrawableIn = SettingsOldActivity.getBubblesIn(c);
        backgroundDrawableOut = SettingsOldActivity.getBubblesOut(c);
        textSize = SettingsOldActivity.getTextsize(c);
        textColor = SettingsOldActivity.getTextcolor(c);
        convertNCR = SettingsOldActivity.decodeDecimalNCR(c);
        showEmoticons = SettingsOldActivity.showEmoticons(c);

        // Thread id
        int threadId;
        if (uri == null || uri.getLastPathSegment() == null) {
            threadId = -1;
        } else {
            threadId = Integer.parseInt(uri.getLastPathSegment());
        }
        Conversation conv = Conversation.getConversation(c, threadId, false);

        if (conv == null) {
            displayName = null;
        } else {
            Contact contact = conv.getContact();
            displayName = contact.getDisplayName();
        }
    }

    private static Cursor getCursor(ContentResolver cr, Uri uri) {
        Cursor[] cursors = new Cursor[]{null, null};

        int tid = -1;
        try {
            tid = Integer.parseInt(uri.getLastPathSegment());
        } catch (Exception e) {
            //error parsing uri
        }

        try {
            cursors[0] = cr.query(uri, Message.PROJECTION_JOIN, WHERE, null, null);
        } catch (NullPointerException e) {
            //error query:
            cursors[0] = null;
        } catch (SQLiteException e) {
           //error getting messages
        }

        String[] sel = new String[]{String.valueOf(tid)};
        try {
            cursors[1] = cr.query(Uri.parse("content://sms/"), Message.PROJECTION_SMS, WHERE_DRAFT, sel, Message.SORT_VN);
        } catch (NullPointerException e) {
            //error query
            cursors[1] = null;
        } catch (SQLiteException e) {
            //error getting drafts
        }

        if (cursors[1] == null || cursors[1].getCount() == 0) {
            return cursors[0];
        }
        if (cursors[0] == null || cursors[0].getCount() == 0) {
            return cursors[1];
        }
        return new MergeCursor(cursors);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final Message message = Message.getMessage(context, cursor);

        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.tvPerson = (TextView) view.findViewById(R.id.addr);
            holder.tvBody = (TextView) view.findViewById(R.id.body);
            holder.tvDate = (TextView) view.findViewById(R.id.date);
            holder.ivPhoto = (ImageView) view.findViewById(R.id.picture);
            holder.vRead = view.findViewById(R.id.read);
            holder.vPending = view.findViewById(R.id.pending);
            holder.vLayout = view.findViewById(R.id.layout);
            holder.ivInOut = (ImageView) view.findViewById(R.id.inout);
            holder.btnDownload = (Button) view.findViewById(R.id.btn_download_msg);
            holder.btnImport = (Button) view.findViewById(R.id.btn_import_contact);
            holder.btnShow = (Button) view.findViewById(R.id.btn_show);
            view.setTag(holder);
        }

        if (textSize > 0) {
            holder.tvBody.setTextSize(textSize);
        }
        final int col = textColor;
        if (col != 0) {
            holder.tvPerson.setTextColor(col);
            holder.tvBody.setTextColor(col);
            holder.tvDate.setTextColor(col);
        }
        int type = message.getType();

        String subject = message.getSubject();
        if (subject == null) {
            subject = "";
        } else {
            subject = ": " + subject;
        }
        // incoming / outgoing / pending
        int pendingvisability = View.GONE;
        switch (type) {
            case Message.SMS_DRAFT:
                // TODO case Message.SMS_PENDING:
                // case Message.MMS_DRAFT:
                pendingvisability = View.VISIBLE;
            case Message.SMS_OUT: // handle drafts/pending here too
            case Message.MMS_OUT:
                holder.tvPerson.setText(context.getString(R.string.me) + subject);
                try {
                    holder.vLayout.setBackgroundResource(backgroundDrawableOut);
                } catch (OutOfMemoryError e) {
                    //OOM while setting bg
                }
                holder.ivInOut.setImageResource(R.drawable.ic_call_log_list_outgoing_call);
                break;
            case Message.SMS_IN:
            case Message.MMS_IN:
            default:
                holder.tvPerson.setText(displayName +" "+ subject);
                try {
                    holder.vLayout.setBackgroundResource(backgroundDrawableIn);
                } catch (OutOfMemoryError e) {
                    //OOM while setting bg
                }
                holder.ivInOut.setImageResource(R.drawable.ic_call_log_list_incoming_call);
                holder.vPending.setVisibility(View.GONE);
                break;
        }
        holder.vPending.setVisibility(pendingvisability);

        // unread / read
        if (message.getRead() == 0) {
            holder.vRead.setVisibility(View.VISIBLE);
        } else {
            holder.vRead.setVisibility(View.INVISIBLE);
        }

        long time = message.getDate();
        holder.tvDate.setText(MainActivity.getDate(context, time));

        Bitmap pic = message.getPicture();
        if (pic != null) {
            if (pic == Message.BITMAP_PLAY) {
                holder.ivPhoto.setImageResource(R.drawable.mms_play_btn);
            } else {
                holder.ivPhoto.setImageBitmap(pic);
            }
            holder.ivPhoto.setVisibility(View.VISIBLE);
            Intent i = message.getContentIntent();
            holder.ivPhoto.setOnClickListener(DefaultAndPermission.getOnClickStartActivity(context, i));
            holder.ivPhoto.setOnLongClickListener(message.getSaveAttachmentListener(mActivity));
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
            holder.ivPhoto.setOnClickListener(null);
        }

        CharSequence text = message.getBody();
        if (text == null && pic == null) {
            final Button btn = holder.btnDownload;
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent i = new Intent();
                        i.setClassName("com.android.mms", "com.android.mms.transaction.TransactionService");
                        i.putExtra("uri", message.getUri().toString());
                        i.putExtra("type", 1);
                        ComponentName cn = context.startService(i);
                        if (cn != null) {
                            btn.setEnabled(false);
                            btn.setText(R.string.downloading_);
                        } else {
                            i = new Intent(Intent.ACTION_VIEW, Uri.parse(ConversationActivity.URI + message.getThreadId()));
                            context.startActivity(Intent.createChooser(i, context.getString(R.string.view_mms)));
                        }
                    } catch (SecurityException e) {
                       //unable to start mms download
                        Toast.makeText(context, R.string.error_start_mms_download, Toast.LENGTH_LONG).show();
                    }
                }
            });
            holder.btnDownload.setVisibility(View.VISIBLE);
            holder.btnDownload.setEnabled(true);
        } else {
            holder.btnDownload.setVisibility(View.GONE);
        }

        if (text == null) {
            holder.tvBody.setVisibility(View.INVISIBLE);
            holder.btnImport.setVisibility(View.GONE);
        }
        else if (flag1 &&!SmsReceiver.filter(context, text.toString(), message.getAddress(context))){
            view.setVisibility(View.GONE);
            view.getLayoutParams().height = 1;

        }
        else if (SmsReceiver.filter(context, text.toString(), message.getAddress(context))) {
            holder.tvBody.setText(context.getString(R.string.filter_content));
            holder.btnShow.setVisibility(View.VISIBLE);
            final String t = text.toString();
            final ViewHolder finalHolder = holder;
            holder.btnShow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalHolder.btnShow.setText(context.getString(R.string.goFilterd));
                    finalHolder.tvBody.setText(t);
                    finalHolder.btnShow.setVisibility(View.GONE);
                    finalHolder.btnShow.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, FilterActivity.class);
                            context.startActivity(intent);
                        }
                    });
                }
            });
            //final String t = text.toString();
            if (flag1) {
                view.setVisibility(View.VISIBLE);
                view.getLayoutParams().height = 0;
                holder.tvBody.setText(t);
                holder.btnShow.setVisibility(View.GONE);
            }
        }
        else {
            holder.btnShow.setVisibility(View.GONE);
            if (convertNCR) {
                text = Converter.convertDecNCR2Char(text);
            }
            if (showEmoticons) {
                text = SmileyParser.getInstance(context).addSmileySpans(text);
            }
            holder.tvBody.setText(text);
            holder.tvBody.setVisibility(View.VISIBLE);
            String stext = text.toString();
            if (stext.contains("BEGIN:VCARD") && stext.contains("END:VCARD")) {
                Button btn = holder.btnImport;
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        Uri uri = ContentUris
                                .withAppendedId(MessageProvider.CONTENT_URI, message.getId());
                        i.setDataAndType(uri, "text/x-vcard");
                        try {
                            context.startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            //activity not found (text/x-vcard)
                            Toast.makeText(context, "Activity not found: text/x-vcard", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                holder.btnImport.setVisibility(View.GONE);
            }
        }
    }
}
