package com.example.trantrungduong95.truesms.Presenter;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.MobilePhoneAdapter;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Message;
import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;
import com.example.trantrungduong95.truesms.R;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;

import java.net.URLDecoder;
import java.util.ArrayList;

public class ComposeActivity extends AppCompatActivity implements View.OnClickListener {
    //Tag
    private String TAG = "send";

    //link Uri for saving messages.
    private static Uri URI_SMS = Uri.parse("content://sms");

    //link Uri for saving sent messages.
    public static Uri URI_SENT = Uri.parse("content://sms/sent");

    //Projection for getting the id.
    private static String[] PROJECTION_ID = new String[]{BaseColumns._ID};

    //SMS DB: address.
    private String ADDRESS = "address";

    //SMS DB: read.
    private static String READ = "read";

    //SMS DB: type.
    public static String TYPE = "type";

    //SMS DB: body.
    private static String BODY = "body";

    //SMS DB: date.
    private static String DATE = "date";

    //Message set action.
    public static String MESSAGE_SENT_ACTION = "com.android.mms.transaction.MESSAGE_SENT";

    //Hold recipient and text.
    private String phoneNo, text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @SuppressWarnings("deprecation")
    private void handleIntent(Intent intent) {
        if (parseIntent(intent)) {
            setTheme(android.R.style.Theme_Translucent_NoTitleBar);
            send();
            finish();
        } else {
            int tid = getThreadId();
            if (tid >= 0) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(MainActivity.URI, String.valueOf(tid)), this, ConversationActivity.class);
                i.putExtra("showKeyboard", true);
                startActivity(i);
                finish();
            } else {
                setTheme(SettingsOldActivity.getTheme(this));
                setContentView(R.layout.activity_compose);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                EditText et = (EditText) findViewById(R.id.compose_reply_text);
                et.addTextChangedListener(new MyTextWatcher(this, (TextView) findViewById(R.id.content_count)));
                et.setText(text);
                MultiAutoCompleteTextView mtv = (MultiAutoCompleteTextView) this.findViewById(R.id.txtPhoneNo);
                MobilePhoneAdapter mpa = new MobilePhoneAdapter(this);
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
                MobilePhoneAdapter.setMobileNumbersOnly(p.getBoolean(
                        SettingsOldActivity.PREFS_MOBILE_ONLY, false));
                mtv.setAdapter(mpa);
                mtv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                mtv.setText(phoneNo);

                View vSend = findViewById(R.id.send_SMS);
                vSend.setOnClickListener(this);
                View vAttachment = findViewById(R.id.compose_icon);
                vAttachment.setOnClickListener(this);

                if (!TextUtils.isEmpty(phoneNo)) {
                    phoneNo = phoneNo.trim();
                    if (phoneNo.endsWith(",")) {
                        phoneNo = phoneNo.substring(0, phoneNo.length() - 1).trim();
                    }
                    if (phoneNo.indexOf('<') < 0) {
                        try {
                            // try to fetch recipient's name from phone book
                            String n = ContactsWrapper.getInstance().getNameForNumber(getContentResolver(), phoneNo);
                            if (n != null) {
                                phoneNo = n + " <" + phoneNo + ">, ";
                            }
                        } catch (NullPointerException e) {
                            //Null pointer while resolving number
                            Log.e("Null pointer: ", phoneNo);
                        }
                    }
                    mtv.setText(phoneNo);
                    et.requestFocus();
                    Log.d("phoneno",phoneNo);
                } else {
                    mtv.requestFocus();
                }
                int flags = et.getInputType();
                if (p.getBoolean(SettingsOldActivity.PREFS_EDIT_SHORT_TEXT, true)) {
                    flags |= InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE;
                } else {
                    flags &= ~InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE;
                }
                et.setInputType(flags);
            }
        }
    }

    /**
     * Parse data pushed by {@link Intent}.
     *
     * @param intent {@link Intent}
     * @return true if message is ready to send
     */
    private boolean parseIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        phoneNo = null;
        String u = intent.getDataString();
        try {
            if (!TextUtils.isEmpty(u) && u.contains(":")) {
                String t = u.split(":")[1];
                if (t.startsWith("+")) {
                    phoneNo = "+" + URLDecoder.decode(t.substring(1));
                } else {
                    phoneNo = URLDecoder.decode(t);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Log.d("could not split at :", e.getMessage());
        }

        CharSequence cstext = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        text = null;
        if (cstext != null) {
            text = cstext.toString();
        }
        if (TextUtils.isEmpty(text)) {
            Log.i(TAG, "text missing");
            return false;
        }
        if (TextUtils.isEmpty(phoneNo)) {
            Log.i(TAG, "recipient missing");
            return false;
        }
        return true;
    }

    private int getThreadId() {
        if (TextUtils.isEmpty(phoneNo)) {
            return -1;
        }
        String filter = phoneNo.replaceAll("[-()/ ]", "");
        if (filter.length() > 6) {
            filter = filter.substring(filter.length() - 6);
        }
        Cursor c = getContentResolver().query(Uri.parse("content://sms"),
                new String[]{"thread_id"}, "address like '%" + filter + "'", null, null);
        int threadId = -1;
        if (c.moveToFirst()) {
            threadId = c.getInt(0);
        }
        c.close();
        return threadId;
    }

    /**
     * Send a message to a single recipient.
     *
     * @param recipient recipient
     * @param message   message
     */
    private void send(String recipient, String message) {
        Log.d("text: ", recipient);

        // save draft
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(TYPE, Message.SMS_DRAFT);
        values.put(BODY, message);
        values.put(READ, 1);
        values.put(ADDRESS, recipient);
        Uri draft = null;
        // save sms to content://sms/sent
        Cursor cursor = cr.query(URI_SMS, PROJECTION_ID,
                TYPE + " = " + Message.SMS_DRAFT + " AND " + ADDRESS + " = '" + recipient
                        + "' AND " + BODY + " like '" + message.replace("'", "_") + "'", null, DATE + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            draft = URI_SENT.buildUpon().appendPath(cursor.getString(0)).build();
            Log.d("skip saving draft: ", draft+"");
        } else {
            try {
                draft = cr.insert(URI_SENT, values);
                Log.d("draft saved: ", draft+"");
            } catch (IllegalArgumentException | SQLiteException | NullPointerException e) {
                Log.e(TAG, "unable to save draft", e);
            }
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        Log.d("send messages to: ", recipient);

        ArrayList<PendingIntent> sentIntents = new ArrayList<>();
        try {
            SmsManager smsManager = SmsManager.getDefault();

            ArrayList<String> messages = smsManager.divideMessage(message);
            for (String m : messages) {
                Log.d("divided messages: ", m+"");

                Intent sent = new Intent(MESSAGE_SENT_ACTION, draft, this, SmsReceiver.class);
                sentIntents.add(PendingIntent.getBroadcast(this, 0, sent, 0));
            }
            smsManager.sendMultipartTextMessage(recipient, null, messages, sentIntents, null);
            Log.i(TAG, "message sent");
        } catch (Exception e) {
            Log.e(TAG, "unexpected error", e);
            for (PendingIntent pi : sentIntents) {
                if (pi != null) {
                    try {
                        pi.send();
                    } catch (PendingIntent.CanceledException e1) {
                        Log.e(TAG, "unexpected error", e1);
                    }
                }
            }
        }
    }

    /**
     * Send a message.
     *
     * @return true, if message was sent
     */
    private boolean send() {
        if (TextUtils.isEmpty(phoneNo) || TextUtils.isEmpty(text)) {
            return false;
        }
        for (String r : phoneNo.split(",")) {
            r = MobilePhoneAdapter.cleanRecipient(r);
            if (TextUtils.isEmpty(r)) {
                Log.w("skip empty recipient: ", r);
                continue;
            }
            try {
                send(r, text);
            } catch (Exception e) {
                Log.e("unable to send msg: ", phoneNo, e);
                Toast.makeText(this, R.string.error_sending_failed, Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose, menu);
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
            case R.id.item_settings_compose:
                Toast.makeText(this, "Nothing", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @SuppressWarnings("deprecation")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_SMS:
                // app icon in Action Bar clicked; go home
                EditText ad = (EditText) findViewById(R.id.compose_reply_text);
                text = ad.getText().toString();
                ad = (MultiAutoCompleteTextView) findViewById(R.id.txtPhoneNo);
                phoneNo = ad.getText().toString();
                if (send()) {
                    Toast.makeText(this, getString(R.string.sending_successfully)+"", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            case R.id.compose_icon:
                Toast.makeText(this, "123", Toast.LENGTH_SHORT).show();
                return;
            default:
                break;
        }
    }

}