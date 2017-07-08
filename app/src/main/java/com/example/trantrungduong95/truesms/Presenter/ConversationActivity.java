package com.example.trantrungduong95.truesms.Presenter;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.ConversationAdapter;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Message;
import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;
import com.example.trantrungduong95.truesms.R;

public class ConversationActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, View.OnClickListener, View.OnLongClickListener {

    private String TAG = "ConversationActivity";

    public String copyMsg = "";

    //ContactsWrapper
    private ContactsWrapper WRAPPER = ContactsWrapper.getInstance();

    //Number of items.
    private int WHICH_N = 8;

    //
    private final int WHICH_VIEW_CONTACT = 0;

    //
    private final int WHICH_CALL = 1;

    //Index in dialog: mark read/unread.
    private final int WHICH_MARK_UNREAD = 2;

    //Index in dialog: reply.
    private final int WHICH_REPLY = 3;

    //
    private final int WHICH_FORWARD = 4;

    //Index in dialog: copy text.
    private final int WHICH_COPY_TEXT = 5;

    //Index in dialog: view details.
    private final int WHICH_VIEW_DETAILS = 6;

    //Index in dialog: delete.
    private final int WHICH_DELETE = 7;

    //maximum number of lines in EditText
    private int MAX_EDITTEXT_LINES = 10;

    //Package name for System's chooser.
    private String chooserPackage = null;

    //Used Uri.
    private Uri uri;

    //Conversation shown.
    private Conversation conv = null;

    //ORIG_URI to resolve.
    public static String URI = "content://mms-sms/conversations/";

    //Dialog items shown if an item was long clicked.
    private String[] longItemClickDialog = new String[WHICH_N];

    //Marked a message unread?
    private boolean markedUnread = false;

    // Compose reply edit text.
    private EditText etText;

    //Enable autosend.
    private boolean enableAutosend = true;

    //Show Contact's photo.
    private boolean showPhoto = false;

    //Default Drawable for Contacts.
    private Drawable defaultContactAvatar = null;

    //MenuItem holding Contact's picture.
    private MenuItem contactItem = null;

    //True, to update Contact's photo.
    private boolean needContactUpdate = false;

    private boolean flag = false;

    //Get ListView.
    private ListView getListView() {
        return (ListView) findViewById(R.id.conversation_list);
    }

    //Set ListAdapter to ListView.
    private void setListAdapter(ListAdapter la) {
        getListView().setAdapter(la);
       // getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        enableAutosend = p.getBoolean(SettingsOldActivity.PREFS_ENABLE_AUTOSEND, true);
        showPhoto = p.getBoolean(SettingsOldActivity.PREFS_CONTACT_PHOTO, true);
        setTheme(SettingsOldActivity.getTheme(this));
        Utils.setLocale(this);

        setContentView(R.layout.activity_conversation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (showPhoto) {
            defaultContactAvatar = getResources().getDrawable(R.drawable.contact_blue);
        }

        etText = (EditText) findViewById(R.id.compose_reply_text);

        parseIntent(getIntent());

        ListView list = getListView();
        list.setOnItemLongClickListener(this);
        list.setOnItemClickListener(this);

        View vSend = findViewById(R.id.send_SMS);
        vSend.setOnClickListener(this);
        vSend.setOnLongClickListener(this);

        View vAttachment = findViewById(R.id.compose_icon);
        vAttachment.setOnClickListener(this);
        vAttachment.setOnLongClickListener(this);

        // TextWatcher updating char count on writing.
        final TextView count_reply = (TextView) findViewById(R.id.content_count);
        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                count_reply.setText(String.valueOf(etText.getText().toString().length()));
            }
        });
        etText.setMaxLines(MAX_EDITTEXT_LINES);

        longItemClickDialog[WHICH_MARK_UNREAD] = getString(R.string.mark_unread_);
        longItemClickDialog[WHICH_REPLY] = getString(R.string.reply);
        longItemClickDialog[WHICH_FORWARD] = getString(R.string.forward_);
        longItemClickDialog[WHICH_COPY_TEXT] = getString(R.string.copy_text_);
        longItemClickDialog[WHICH_VIEW_DETAILS] = getString(R.string.view_details_);
        longItemClickDialog[WHICH_DELETE] = getString(R.string.delete_message_);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        parseIntent(intent);
    }

    //Parse data pushed by Intent.
    private void parseIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        needContactUpdate = true;

        uri = intent.getData();
        if (uri != null) {
            if (!uri.toString().startsWith(URI)) {
                uri = Uri.parse(URI + uri.getLastPathSegment());
            }
        } else {
            long tid = intent.getLongExtra("thread_id", -1);
            uri = Uri.parse(URI + tid);
            if (tid < 0L) {
                try {
                    startActivity(MainActivity.getComposeIntent(this, null));
                } catch (ActivityNotFoundException e) {
                    //activity not found
                    Toast.makeText(this, R.string.error_conv_null, Toast.LENGTH_LONG).show();
                }
                finish();
                return;
            }
        }

        conv = getConversation();
        if (conv == null) {
            // failed fetching converstion
            finish();
            return;
        }

        Contact contact = conv.getContact();
        try {
            contact.update(this, false, true);
        } catch (NullPointerException e) {
            Log.e(TAG, "updating contact failed", e);
        }
        boolean showKeyboard = intent.getBooleanExtra("showKeyboard", false);

        ListView lv = getListView();
        lv.setStackFromBottom(true);

        ConversationAdapter adapter = new ConversationAdapter(this, uri);
        setListAdapter(adapter);

        updateHeader(contact);

        String body = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (!TextUtils.isEmpty(body)) {
            etText.setText(body);
            showKeyboard = true;
        }

        if (showKeyboard) {
            etText.requestFocus();
        }

        setRead();
    }

    private void updateHeader(Contact contact) {
        String displayName = contact.getDisplayName();
        setTitle(displayName);
        String number = contact.getNumber();
        if (displayName.equals(number)) {
            getSupportActionBar().setSubtitle(null);
        } else {
            getSupportActionBar().setSubtitle(number);
        }

        setContactIcon(contact);
    }

    @Nullable
    private Conversation getConversation() {
        int threadId;
        try {
            threadId = Integer.parseInt(uri.getLastPathSegment());
        } catch (NumberFormatException e) {
            //unable to parse thread id from uri
            Toast.makeText(this, R.string.error_conv_null, Toast.LENGTH_LONG).show();
            return null;
        }
        if (threadId < 0) {
            //negative thread id from uri
            Toast.makeText(this, R.string.error_conv_null, Toast.LENGTH_LONG).show();
            return null;
        }
        try {
            Conversation c = Conversation.getConversation(this, threadId, true);
            threadId = c.getThreadId(); // force a NPE :x
            return c;
        } catch (NullPointerException e) {
           //Fetched null conversation for thread
            Toast.makeText(this, R.string.error_conv_null, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private ImageView findMenuItemView(int viewId) {
        ImageView view = (ImageView) findViewById(viewId);
        if (view != null) {
            return view;
        }

        if (contactItem != null) {
            return (ImageView) contactItem.getActionView().findViewById(viewId);
        }
        return null;
    }

    //Show MenuItem holding Contact's picture.
    private void setContactIcon(Contact contact) {
        if (contact == null) {
            Log.w(TAG, "setContactIcon(null)");
            return;
        }

        if (contactItem == null) {
            Log.w(TAG, "setContactIcon: contactItem == null");
            return;
        }

        if (!needContactUpdate) {
            Log.i(TAG, "skip setContactIcon()");
            return;
        }

        String name = contact.getName();
        boolean showContactItem = showPhoto && name != null;

        if (showContactItem) {
            // photo
            ImageView ivPhoto = findMenuItemView(R.id.photo);
            if (ivPhoto == null) {
                Log.w(TAG, "ivPhoto == null");
            } else {
                ivPhoto.setImageDrawable(contact.getAvatar(this, defaultContactAvatar));
                ivPhoto.setOnClickListener(WRAPPER.getQuickContact(this, ivPhoto,
                        contact.getLookUpUri(getContentResolver()), 2, null));
            }

            // presence
            ImageView ivPresence = findMenuItemView(R.id.presence);
            if (ivPresence == null) {
                Log.w(TAG, "ivPresence == null");
            } else {
                if (contact.getPresenceState() > 0) {
                    ivPresence.setImageResource(Contact.getPresenceRes(contact.getPresenceState()));
                    ivPresence.setVisibility(View.VISIBLE);
                } else {
                    ivPresence.setVisibility(View.INVISIBLE);
                }
            }
        }

        contactItem.setVisible(showContactItem);
        needContactUpdate = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        ListView lv = getListView();
        lv.setAdapter(new ConversationAdapter(this, uri));
        markedUnread = false;

        Intent i;
        ActivityInfo ai = null;
        PackageManager pm = getPackageManager();
        try {
            i = buildIntent(enableAutosend);
            if (pm != null && PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsOldActivity.PREFS_SHOWTARGETAPP, true)) {
                ai = i.resolveActivityInfo(pm, 0);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "unable to build Intent", e);
        }
        etText.setMaxLines(MAX_EDITTEXT_LINES);

        if (ai == null) {
            etText.setMinLines(1);
        } else {
            if (chooserPackage == null) {
                try {
                    ActivityInfo cai = buildIntent(enableAutosend).resolveActivityInfo(pm, 0);
                    if (cai != null) {
                        chooserPackage = cai.packageName;
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "unable to build Intent", e);
                }
            }
            if (ai.packageName.equals(chooserPackage)) {
            } else {
                Log.d("ai.pn: ", ai.packageName);
            }
        }

    }

    @Override
    protected void onPause() {
        if (!markedUnread) {
            setRead();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Set all messages in a given thread as read.
    private void setRead() {
        if (conv != null) {
            MainActivity.markRead(this, conv.getUri(), 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation, menu);
        contactItem = menu.findItem(R.id.item_contact);
        if (conv != null) {
            setContactIcon(conv.getContact());
        }
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        if (p.getBoolean(SettingsOldActivity.PREFS_HIDE_RESTORE, false)) {
            menu.removeItem(R.id.item_restore);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in Action Bar clicked; go home
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.item_delete_thread:
                MainActivity.deleteMessages(this, uri, R.string.delete_thread_,
                        R.string.delete_thread_question, this);
                return true;
            case R.id.item_settings_conversation:
                if (Build.VERSION.SDK_INT >= 19) {
                    startActivity(new Intent(this, SettingsNewActivity.class));
                } else {
                    startActivity(new Intent(this, SettingsOldActivity.class));
                }
                return true;
            case R.id.item_call:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"
                            + conv.getContact().getNumber())));
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "unable to open dailer", e);
                    Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.item_restore:
                etText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(
                        SettingsOldActivity.PREFS_BACKUPLASTTEXT, null));
                return true;
            case R.id.item_contact:
                if (conv != null && contactItem != null) {
                    WRAPPER.showQuickContactFallBack(this, contactItem.getActionView(), conv
                            .getContact().getLookUpUri(getContentResolver()), 2, null);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onItemClick( AdapterView<?> parent, View view, int position, long id) {
        onItemLongClick(parent, view, position, id);
    }

    public boolean onItemLongClick( AdapterView<?> parent, View view, int position, long id) {
        final Context context = this;
        final Message m = Message.getMessage(this, (Cursor) parent.getItemAtPosition(position));
        final Uri target = m.getUri();
        final int read = m.getRead();
        final int type = m.getType();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.message_options_);

        Contact contact = conv.getContact();
        final String a = contact.getNumber();
        final String n = contact.getName();

        String[] items = longItemClickDialog;
        if (TextUtils.isEmpty(n)) {
            items[WHICH_VIEW_CONTACT] = getString(R.string.add_contact_);
        } else {
            items[WHICH_VIEW_CONTACT] = getString(R.string.view_contact_);
        }
        items[WHICH_CALL] = getString(R.string.call) + " " + contact.getDisplayName();
        if (read == 0) {
            items = items.clone();
            items[WHICH_MARK_UNREAD] = context.getString(R.string.mark_read_);
        }
        if (type == Message.SMS_DRAFT) {
            items = items.clone();
            items[WHICH_FORWARD] = context.getString(R.string.send_draft_);
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i;
                switch (which) {
                    case WHICH_VIEW_CONTACT:
                        if (n == null) {
                            i = ContactsWrapper.getInstance().getInsertPickIntent(a);
                            Conversation.flushCache();
                        } else {
                            Uri u = ConversationActivity.this.conv.getContact().getUri();
                            i = new Intent(Intent.ACTION_VIEW, u);
                        }
                        try {
                            ConversationActivity.this.startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            //unable to launch dailer
                            Toast.makeText(ConversationActivity.this, R.string.error_unknown, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case WHICH_CALL:
                        ConversationActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + a)));
                        break;
                    case WHICH_MARK_UNREAD:
                        MainActivity.markRead(context, target, 1 - read);
                        ConversationActivity.this.markedUnread = true;
                        break;
                    case WHICH_REPLY:
                        ConversationActivity.this.startActivity(MainActivity.getComposeIntent(ConversationActivity.this, a));
                        break;
                    case WHICH_FORWARD:
                        int resId;
                        if (type == Message.SMS_DRAFT) {
                            resId = R.string.send_draft_;
                            i = MainActivity.getComposeIntent(ConversationActivity.this, ConversationActivity.this.conv.getContact().getNumber());
                        } else {
                            resId = R.string.forward_;
                            i = MainActivity.getComposeIntent(ConversationActivity.this, null);
                        }
                        CharSequence text;
                        if (SettingsOldActivity.decodeDecimalNCR(context)) {
                            text = Converter.convertDecNCR2Char(m.getBody());
                        } else {
                            text = m.getBody();
                        }
                        i.putExtra(Intent.EXTRA_TEXT, text);
                        i.putExtra("sms_body", text);
                        startActivity(Intent.createChooser(i, context.getString(resId)));
                        break;
                    case WHICH_COPY_TEXT:
                        copyMsg = m.getBody().toString();
                        break;
                    case WHICH_VIEW_DETAILS:
                        int t = m.getType();
                        AlertDialog.Builder b = new AlertDialog.Builder(context);
                        b.setTitle(R.string.view_details_);
                        b.setCancelable(true);
                        StringBuilder sb = new StringBuilder();
                        String a = m.getAddress(context);
                        long d = m.getDate();
                        String ds = DateFormat.format(
                                context.getString(R.string.DATEFORMAT_details), d).toString();
                        String sentReceived;
                        String fromTo;
                        if (t == CallLog.Calls.INCOMING_TYPE) {
                            sentReceived = context.getString(R.string.received_);
                            fromTo = context.getString(R.string.from_);
                        } else if (t == CallLog.Calls.OUTGOING_TYPE) {
                            sentReceived = context.getString(R.string.sent_);
                            fromTo = context.getString(R.string.to_);
                        } else {
                            sentReceived = "ukwn:";
                            fromTo = "ukwn:";
                        }
                        sb.append(sentReceived).append(" ");
                        sb.append(ds);
                        sb.append("\n");
                        sb.append(fromTo).append(" ");
                        sb.append(a);
                        sb.append("\n");
                        sb.append(context.getString(R.string.type_));
                        if (m.isMMS()) {
                            sb.append(" MMS");
                        } else {
                            sb.append(" SMS");
                        }
                        b.setMessage(sb.toString());
                        b.setPositiveButton(android.R.string.ok, null);
                        b.show();
                        break;
                    case WHICH_DELETE:
                        MainActivity.deleteMessages(context, target,
                                R.string.delete_message_, R.string.delete_message_question, null);
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
        return true;
    }
////todo attachment
    @SuppressWarnings("deprecation")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_SMS:
                send(true);
                return;
            case R.id.compose_icon:
                LinearLayout attachment_panel = (LinearLayout) findViewById(R.id.attachment_panel);
                if (!flag) {
                    attachment_panel.setVisibility(View.VISIBLE);
                    flag = true;
                } else {
                    attachment_panel.setVisibility(View.GONE);
                    flag = false;
                }
                return;
            default:
                // should never happen
        }
    }

    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.send_SMS:
                send(false);
                return true;
            case R.id.compose_icon:
                LinearLayout attachment_panel = (LinearLayout) findViewById(R.id.attachment_panel);
                if (!flag) {
                    attachment_panel.setVisibility(View.VISIBLE);
                    flag = true;
                } else {
                    attachment_panel.setVisibility(View.GONE);
                    flag = false;
                }
                return true;
            default:
                return true;
        }
    }

    //Build an Intent for sending it.
    private Intent buildIntent( boolean autosend) {
        //noinspection ConstantConditions
        if (conv == null || conv.getContact() == null) {
            //"buildIntent() without contact
            throw new NullPointerException("conv and conv.getContact() must be not null");
        }
        String text = etText.getText().toString().trim();
        Intent i = MainActivity.getComposeIntent(this, conv.getContact().getNumber());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Intent.EXTRA_TEXT, text);
        i.putExtra("sms_body", text);
        if (autosend && enableAutosend && text.length() > 0) {
            i.putExtra("AUTOSEND", "1");
        }
        return i;

    }

    ///Answer/send message.
    private void send(boolean autosend) {
        try {
            Intent i = buildIntent(autosend);
            startActivity(i);
            //noinspection ConstantConditions
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(SettingsOldActivity.PREFS_BACKUPLASTTEXT, etText.getText().toString()).commit();
            etText.setText("");

        } catch (ActivityNotFoundException | NullPointerException e) {
            //unable to launch sender app
            Log.e("unable to launch sender", e.getMessage());
            Toast.makeText(this, R.string.error_sending_failed, Toast.LENGTH_LONG).show();
        }
    }
}
