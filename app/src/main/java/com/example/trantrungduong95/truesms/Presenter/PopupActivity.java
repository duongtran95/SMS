package com.example.trantrungduong95.truesms.Presenter;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.R;
//Tiep tuc xu ly.
public class PopupActivity extends ConversationActivity{
    private Toolbar mToolbar;
    private TextView mTitle;
    private Menu mMenu;
    private ListView listView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        reloadToolbar();
        listView = getListView();
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
            mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
            setSupportActionBar(mToolbar);
        }
    }
    private ListView getListView(){
        return (ListView) findViewById(R.id.popup_messages);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Save a reference to the menu so that we can quickly access menu icons later.
        mMenu = menu;
        return true;
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
//Todo onclick send and compose
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_SMS:
                // app icon in Action Bar clicked; go home
                EditText ad = (EditText) findViewById(R.id.compose_reply_text);
                //text = ad.getText().toString();
                ad = (MultiAutoCompleteTextView) findViewById(R.id.txtPhoneNo);
                //phoneNo = ad.getText().toString();
               /* if (send()) {
                    Toast.makeText(this, getString(R.string.sending_successfully)+"", Toast.LENGTH_SHORT).show();
                    finish();
                }*/
                return;
            case R.id.compose_icon:
                Toast.makeText(this, "123", Toast.LENGTH_SHORT).show();
                return;
            default:
                break;
        }
    }

    private boolean send() {
        return false;
    }

    private void send(boolean b) {

    }
}

