package com.example.trantrungduong95.truesms.Presenter;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.trantrungduong95.truesms.CustomAdapter.MobilePhoneAdapter;
import com.example.trantrungduong95.truesms.Model.Message;
import com.example.trantrungduong95.truesms.R;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;

import java.util.ArrayList;

//Tiep tuc xu ly.
public class PopupActivity extends AppCompatActivity implements View.OnClickListener{
    private Toolbar mToolbar;
    private TextView content;
    private TextView count_reply;
    private EditText reply_compose;
    private String phoneNo, text;
    private boolean flag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        reloadToolbar();
        Bundle extras = getIntent().getExtras();
        String body = extras.getString("body");
        phoneNo = extras.getString("addr");
        // Content popup
        content = (TextView) findViewById(R.id.content_popup);
        content.setText(body);
        //count rely
        count_reply = (TextView) findViewById(R.id.content_count);
        reply_compose = (EditText) findViewById(R.id.compose_reply_text);

        reply_compose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                count_reply.setText(String.valueOf(reply_compose.getText().toString().length()));
            }
        });
        //title toolbar
        mToolbar.setSubtitle(phoneNo);
        // click send and attachment
        View vSend = findViewById(R.id.send_SMS);
        vSend.setOnClickListener(this);
        View vAttachment = findViewById(R.id.compose_icon);
        vAttachment.setOnClickListener(this);
    }
    private void reloadToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar == null) {
            throw new RuntimeException("Toolbar not found in BaseActivity layout.");
        } else {
            mToolbar.setPopupTheme(R.style.PopupTheme);
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup, menu);
        return true;
    }
//Todo On click options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_thread:
                return true;
            case R.id.menu_call:
                return true;
            case R.id.menu_delete:
                return true;
            case R.id.menu_copy:
                return true;
            case R.id.menu_forward:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
//todo attachment
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_SMS:
                // app icon in Action Bar clicked; go home
                text = reply_compose.getText().toString();
                if (send()) {
                    Toast.makeText(this, getString(R.string.sending_successfully)+"", Toast.LENGTH_SHORT).show();
                    finish();
                }
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
                break;
        }
    }

    /**
     * Send a message.
     *
     * @return true, if message was sent
     */
    public boolean send() {
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
    private void send(String recipient, String message) {
        Log.d("text: ", recipient);

        // save draft
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ComposeActivity.TYPE, Message.SMS_DRAFT);
        values.put(ComposeActivity.BODY, message);
        values.put(ComposeActivity.READ, 1);
        values.put(ComposeActivity.ADDRESS, recipient);
        Uri draft = null;
        // save sms to content://sms/sent
        Cursor cursor = cr.query(ComposeActivity.URI_SMS, ComposeActivity.PROJECTION_ID,
                ComposeActivity.TYPE + " = " + Message.SMS_DRAFT + " AND " + ComposeActivity.ADDRESS + " = '" + recipient
                        + "' AND " + ComposeActivity.BODY + " like '" + message.replace("'", "_") + "'", null, ComposeActivity.DATE + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            draft = ComposeActivity.URI_SENT.buildUpon().appendPath(cursor.getString(0)).build();
            Log.d("skip saving draft: ", draft+"");
        } else {
            try {
                draft = cr.insert(ComposeActivity.URI_SENT, values);
                Log.d("draft saved: ", draft+"");
            } catch (IllegalArgumentException | SQLiteException | NullPointerException e) {
                //unable to save draft
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

                Intent sent = new Intent(ComposeActivity.MESSAGE_SENT_ACTION, draft, this, SmsReceiver.class);
                sentIntents.add(PendingIntent.getBroadcast(this, 0, sent, 0));
            }
            smsManager.sendMultipartTextMessage(recipient, null, messages, sentIntents, null);
        } catch (Exception e) {
            for (PendingIntent pi : sentIntents) {
                if (pi != null) {
                    try {
                        pi.send();
                    } catch (PendingIntent.CanceledException e1) {
                        //unexpected error
                    }
                }
            }
        }
    }
}

