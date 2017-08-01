package com.example.trantrungduong95.truesms;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.ConversationSearchAdapter;
import com.example.trantrungduong95.truesms.CustomAdapter.ConversationsAdapter;
import com.example.trantrungduong95.truesms.Fab.FloatingActionButton;
import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Feedback;
import com.example.trantrungduong95.truesms.Model.Message;
import com.example.trantrungduong95.truesms.Model.Search;
import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;
import com.example.trantrungduong95.truesms.Presenter.Activity_.AboutActivity;
import com.example.trantrungduong95.truesms.Presenter.AsyncHelper;
import com.example.trantrungduong95.truesms.Presenter.Activity_.BlacklistActivity;
import com.example.trantrungduong95.truesms.Presenter.Activity_.ConversationActivity;
import com.example.trantrungduong95.truesms.Presenter.Activity_.DefaultAndPermission;
import com.example.trantrungduong95.truesms.Presenter.Activity_.FilterActivity;
import com.example.trantrungduong95.truesms.Presenter.Activity_.SettingsNewActivity;
import com.example.trantrungduong95.truesms.Presenter.Activity_.SettingsOldActivity;
import com.example.trantrungduong95.truesms.Presenter.SpamHandler;
import com.example.trantrungduong95.truesms.Presenter.Utils;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;
import com.firebase.client.Firebase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements OnItemClickListener, OnItemLongClickListener {
    //Tag
    public static String TAG = "MainActivity";
    //ORIG_URI to resolve.
    public static Uri URI = Uri.parse("content://mms-sms/conversations/");
    //Number of items.
    private static final int WHICH_N = 6;
    //Index in dialog: answer.
    private static final int WHICH_ANSWER = 0;
    //Index in dialog: answer.
    private static final int WHICH_CALL = 1;
    //Index in dialog: view/add contact.
    private static final int WHICH_VIEW_CONTACT = 2;
    //Index in dialog: view.
    private static final int WHICH_VIEW = 3;
    //Index in dialog: delete.
    private static final int WHICH_DELETE = 4;
    //Index in dialog: mark as spam.
    private static final int WHICH_MARK_SPAM = 5;
    //Minimum date.
    public static long MIN_DATE = 10000000000L;
    //Miliseconds per seconds.
    public static long MILLIS = 1000L;
    //Show contact's photo.
    public static boolean showContactPhoto = false;
    //Show emoticons in {@link }.
    public static boolean showEmoticons = false;

    //Dialog items shown if an item was long clicked.
    private String[] longItemClickDialog = null;

    //Conversations.
    private ConversationsAdapter adapter = null;

    //holding today 00:00.
    private static Calendar CAL_DAYAGO = Calendar.getInstance();

    static {
        // Get time for now - 24 hours
        CAL_DAYAGO.add(Calendar.DAY_OF_MONTH, -1);
    }

    private static final int PERMISSIONS_REQUEST_READ_SMS = 1;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2;

    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSearch;
    private android.support.v7.app.ActionBar action;
    private ConversationSearchAdapter conversationSearchAdapter;

    //listview search
    private ListView listView;
    //listview conversations
    private ListView listview_conversation;

    //db
    private SpamHandler db = new SpamHandler(this);

    public static boolean turnOffNof = false;

    private boolean updateContact = false;

    //Firebase save feedback
    private Firebase myFirebase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        //SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(SettingsOldActivity.getTheme(this));
        Utils.setLocale(this);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);
        myFirebase = new Firebase("https://democn-6f3ab.firebaseio.com/");
        listview_conversation = getListView();

        listview_conversation.setOnItemClickListener(this);
        listview_conversation.setOnItemLongClickListener(this);
        longItemClickDialog = new String[WHICH_N];
        longItemClickDialog[WHICH_ANSWER] = getString(R.string.reply);
        longItemClickDialog[WHICH_CALL] = getString(R.string.call);
        longItemClickDialog[WHICH_VIEW_CONTACT] = getString(R.string.view_contact_);
        longItemClickDialog[WHICH_VIEW] = getString(R.string.view_thread_);
        longItemClickDialog[WHICH_DELETE] = getString(R.string.delete_thread_);
        longItemClickDialog[WHICH_MARK_SPAM] = getString(R.string.filter_spam_);

        initAdapter();

        if (!DefaultAndPermission.isDefaultApp(this)) {
            Builder b = new Builder(this);
            b.setTitle(R.string.not_default_app);
            b.setMessage(R.string.not_default_app_message);
            b.setNegativeButton(android.R.string.cancel, null);
            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, BuildConfig.APPLICATION_ID);
                    startActivity(intent);
                }
            });
            b.show();
        }

        //Fab
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.button_floating_action);
        floatingActionButton.show();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_compose = getComposeIntent(MainActivity.this, null);
                try {
                    startActivity(intent_compose);
                } catch (ActivityNotFoundException e) {
                    Log.e("er launching intent: ", intent_compose.getAction() + ", " + intent_compose.getData());
                    Toast.makeText(getApplication(), getString(R.string.error_launching_messaging_app), Toast.LENGTH_LONG).show();
                }
            }
        });

        db.createDefaultFilterIfNeed();
        //getDataLastSMS(this,"12","Hot hot hot vip");

        Log.d("onCreate", "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        CAL_DAYAGO.setTimeInMillis(System.currentTimeMillis());
        CAL_DAYAGO.add(Calendar.DAY_OF_MONTH, -1);

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        showContactPhoto = p.getBoolean(SettingsOldActivity.PREFS_CONTACT_PHOTO, true);
        showEmoticons = p.getBoolean(SettingsOldActivity.PREFS_EMOTICONS, false);
        if (adapter != null) {
            adapter.startMsgListQuery();
        }
        //adapter = new ConversationsAdapter(this);
        //setListAdapter(adapter);
        Log.d("onResume", "");
    }

    @Override
    public void onStart() {
        super.onStart();
        AsyncHelper.setAdapter(adapter);
        Log.e("onStart", "");
    }

    @Override
    public void onStop() {
        super.onStop();
        AsyncHelper.setAdapter(null);
        Log.e("onStop", "");
    }

    @Override
    public void onBackPressed() {
        Log.e("onBackPressed","onBackPressed");
        if (isSearchOpened) {
            handleMenuSearch();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (SettingsOldActivity.getTheme(this) == R.style.Theme_TrueSMS) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (SettingsOldActivity.getTextcolor(this) == Color.BLACK) {
                prefs.edit().putInt(SettingsOldActivity.PREFS_TEXTCOLOR, Color.WHITE).apply();
            }
            recreateActivity();
        } else if (SettingsOldActivity.getTheme(this) == R.style.Theme_TrueSMS_Light) {
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (SettingsOldActivity.getTextcolor(this) == Color.WHITE) {
                    prefs.edit().putInt(SettingsOldActivity.PREFS_TEXTCOLOR, Color.BLACK).apply();
                }
                recreateActivity();
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversationlist, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hideDeleteAll = p.getBoolean(SettingsOldActivity.PREFS_HIDE_DELETE_ALL_THREADS, false);
        menu.findItem(R.id.item_delete_all_threads).setVisible(!hideDeleteAll);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // just try again.
                    initAdapter();
                } else {
                    // this app is useless without permission for reading sms
                    Log.e(TAG, "permission for reading sms denied, exit");
                    finish();
                }
                return;
            }
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // just try again.
                    initAdapter();
                } else {
                    // this app is useless without permission for reading sms
                    Log.e(TAG, "permission for reading contacts denied, exit");
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                handleMenuSearch();
                return true;
            case R.id.item_settings: // start settings activity
                if (Build.VERSION.SDK_INT >= 19) {
                    startActivity(new Intent(this, SettingsNewActivity.class));
                } else {
                    startActivity(new Intent(this, SettingsOldActivity.class));
                }
                return true;
            case R.id.item_delete_all_threads:
                deleteMessages(this, Uri.parse("content://sms/"), R.string.delete_threads_,
                        R.string.delete_threads_question, null);
                return true;
            case R.id.item_blacklist:
                Intent intent_blacklist = new Intent(this, BlacklistActivity.class);
                startActivity(intent_blacklist);
                return true;
            case R.id.item_filterd:
                Intent intent_item_filterd = new Intent(this, FilterActivity.class);
                startActivity(intent_item_filterd);
                return true;
            case R.id.item_about:
                return true;
            case R.id.item_about_c:
                Intent iAbout = new Intent(this, AboutActivity.class);
                startActivity(iAbout);
                return true;
            case R.id.item_feedback:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_feedback, null);
                final EditText mName = (EditText) mView.findViewById(R.id.edt_nameFeedback);
                final EditText mEmail = (EditText) mView.findViewById(R.id.edt_emailFeedback);
                final EditText mAddress = (EditText) mView.findViewById(R.id.edt_AddressFeedback);
                final EditText mDescribe = (EditText) mView.findViewById(R.id.edt_DescribeFeedBack);
                final Button mSendFeedBack = (Button) mView.findViewById(R.id.btnSend_FeedBack);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();



                mName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!mName.getText().toString().isEmpty() && !mEmail.getText().toString().isEmpty() &&
                                !mAddress.getText().toString().isEmpty() && !mDescribe.getText().toString().isEmpty()) {
                            mSendFeedBack.setText(getString(R.string.send_));
                        }
                        else  mSendFeedBack.setText(getString(R.string.menu_close));
                    }
                });
                mEmail.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!mName.getText().toString().isEmpty() && !mEmail.getText().toString().isEmpty() &&
                                !mAddress.getText().toString().isEmpty() && !mDescribe.getText().toString().isEmpty()) {
                            mSendFeedBack.setText(getString(R.string.send_));
                        }
                        else  mSendFeedBack.setText(getString(R.string.menu_close));
                    }
                });
                mAddress.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!mName.getText().toString().isEmpty() && !mEmail.getText().toString().isEmpty() &&
                                !mAddress.getText().toString().isEmpty() && !mDescribe.getText().toString().isEmpty()) {
                            mSendFeedBack.setText(getString(R.string.send_));
                        }
                        else  mSendFeedBack.setText(getString(R.string.menu_close));
                    }
                });
                mDescribe.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!mName.getText().toString().isEmpty() && !mEmail.getText().toString().isEmpty() &&
                                !mAddress.getText().toString().isEmpty() && !mDescribe.getText().toString().isEmpty()) {
                            mSendFeedBack.setText(getString(R.string.send_));
                        }
                        else  mSendFeedBack.setText(getString(R.string.menu_close));
                    }
                });
                mSendFeedBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!mSendFeedBack.getText().toString().equals(getString(R.string.menu_close)) &&
                                !mName.getText().toString().isEmpty() && !mEmail.getText().toString().isEmpty() &&
                                !mAddress.getText().toString().isEmpty() && !mDescribe.getText().toString().isEmpty()){
                            mSendFeedBack.setText(getString(R.string.send_));
                            handleFeedback(mName.getText().toString(),mEmail.getText().toString(),
                                    mAddress.getText().toString(),mDescribe.getText().toString());
                            dialog.hide();
                        }else if (mSendFeedBack.getText().toString().equals(getString(R.string.menu_close))){
                            dialog.hide();
                        }
                    }
                });
                //todo feedback
                return true;

            case R.id.item_close:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Get Listview.
    private ListView getListView() {
        return (ListView) findViewById(R.id.conversations_list);
    }

    //Set ListAdapter to ListView.
    private void setListAdapter(ListAdapter adapter) {
        ListView v = getListView();
        v.setAdapter(adapter);
    }

    protected void handleMenuSearch() {
        action = getSupportActionBar();
        if (isSearchOpened) { //test if the search is open

            if (action != null) {
                action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
                action.setDisplayShowTitleEnabled(true); //show the title in the action bar
            }


            //hides the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            //add the search icon in the action bar
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search));

            listview_conversation.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);

            isSearchOpened = false;
        } else { //open the search entry

            if (action != null) {
                action.setDisplayShowCustomEnabled(true); //enable it to display a
                // custom view in the action bar.
                action.setCustomView(R.layout.search_bar);//add the custom view
                action.setDisplayShowTitleEnabled(false); //hide the title
                edtSearch = (EditText) action.getCustomView().findViewById(R.id.edtSearch); //the text editor
            }

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            //this is a listener to do a search when the user clicks on search button
            listview_conversation.setVisibility(View.INVISIBLE);
            listView = (ListView) findViewById(R.id.listView);
            listView.setVisibility(View.VISIBLE);

            // new search list
            //final ArrayList<Search> conversationsArrayList = new ArrayList<>();
            //getAllValues(listview_conversation, conversationsArrayList);

            List<Block> blockList = db.getAllBlocks();
            ArrayList<Search> conversationsArrayList1 = new ArrayList<>();
            conversationsArrayList1 = getConvSearch(blockList);

            //set adapter search
            conversationSearchAdapter = new ConversationSearchAdapter(conversationsArrayList1, this);
            listView.setAdapter(conversationSearchAdapter);

            registerForContextMenu(listView);
            listView.setTextFilterEnabled(true);
            edtSearch.addTextChangedListener(new TextWatcher() {
                                                 @Override
                                                 public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                 }

                                                 @Override
                                                 public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                     if (count < before) {
                                                         // We're deleting char so we need to reset the adapter data
                                                         conversationSearchAdapter.resetData();
                                                     }

                                                     conversationSearchAdapter.getFilter().filter(s.toString());
                                                 }

                                                 @Override
                                                 public void afterTextChanged(Editable s) {
                                                 }
                                             }
            );

            edtSearch.requestFocus();

            // click item listview
            final ArrayList<Search> finalConversationsArrayList = conversationsArrayList1;
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String number = finalConversationsArrayList.get(position).getNum();
                    Pattern pattern = Pattern.compile("^[0-9]*$");
                    Matcher matcher = pattern.matcher(number);
                    if (!matcher.matches()) {
                        startActivity(getComposeIntent(MainActivity.this,cleanRecipient(number)));
                    } else
                        startActivity(getComposeIntent(MainActivity.this, number));

                }
            });

            //add the close icon
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_cancel));
            isSearchOpened = true;
        }
    }

/*    //Get All item listview comversations
    public void getAllValues(ListView list, ArrayList<Search> arrayList) {
        View parentView = null;
        for (int i = 0; i < list.getCount(); i++) {
            parentView = getViewByPosition(i, list);
            String addr = ((TextView) parentView
                    .findViewById(R.id.addr)).getText().toString();
            String body = ((TextView) parentView
                    .findViewById(R.id.body)).getText().toString();
            String date = ((TextView) parentView
                    .findViewById(R.id.date)).getText().toString();
            String count = ((TextView) parentView
                    .findViewById(R.id.count)).getText().toString();
            ImageView photo = ((ImageView) parentView.findViewById(R.id.photo));
            BitmapDrawable bitmapDrawable = (BitmapDrawable) photo.getDrawable();
            Bitmap yourBitmap = bitmapDrawable.getBitmap();
            Search a;
            if (!addr.equals("false")) {
                a = new Search(addr, body);
                arrayList.add(a);
            }
        }
    }*/

/*    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition
                + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }*/


    private ArrayList<Search> getConvSearch(List<Block> blockList) {
        ArrayList<Search> convListSearch = new ArrayList<>();
        Cursor c = null;
        try {
            c = getContentResolver().query(Conversation.URI_SIMPLE, Conversation.PROJECTION_SIMPLE, Conversation.COUNT + ">0", null, null);
        } catch (Exception e) {
            Log.e("error getting conv", e + "");
        }

        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
        }
        if (c != null && c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                Conversation conv = Conversation.getConversation(this, c, true);
                if (!isBlocked(conv.getContact().getNumber(), blockList)) {
                    /*if (messageLastSMS(conv) !=null) {
                        Search search = new Search(conv.getContact().getNameAndNumber(),conv.getBody());
                        *//*search.setNum(conv.getContact().getNameAndNumber());
                        search.setContent(conv.getBody());*//*

                    }*/
                    Search search = null;//conv.getContact().getNameAndNumber(),conv.getBody());
                    if (SmsReceiver.filter(this,conv.getBody(),conv.getContact().getNumber())){
                        /*if (messageLastSMS(conv) != null){
                            Message a = messageLastSMS(conv);*/
                            //Log.e("1234",a.getBody().toString());
                            search = new Search(conv.getContact().getNameAndNumber(),conv.getBody());
                            //conv.setBody(a.getBody().toString());
                        //}
                    }else {
                        search = new Search(conv.getContact().getNameAndNumber(),conv.getBody());
                    }
                    convListSearch.add(search);
                }
                c.moveToNext();
            }
            c.close();
        }
        return convListSearch;
}

    private boolean isBlocked(String addr, List<Block> blockList) {
        if (addr == null) {
            return false;
        }
        for (Block aBlacklist : blockList) {
            if (addr.equals(aBlacklist.getNumber())) {
                return true;
            }
        }
        return false;
    }

    private void recreateActivity() {
        //Delaying activity recreate by 1 millisecond. If the recreate is not delayed and is done
        // immediately in onResume() you will get RuntimeException: Performing pause of activity that is not resumed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        }, 1);
    }

    private void initAdapter() {
        if (!DefaultAndPermission.requestPermission(this, Manifest.permission.READ_SMS,
                PERMISSIONS_REQUEST_READ_SMS, R.string.permissions_read_sms,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })) {
            return;
        }

        if (!DefaultAndPermission.requestPermission(this, Manifest.permission.READ_CONTACTS,
                PERMISSIONS_REQUEST_READ_CONTACTS, R.string.permissions_read_contacts,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })) {
            return;
        }

        adapter = new ConversationsAdapter(this);
        setListAdapter(adapter);

        adapter.startMsgListQuery();

    }

    //Mark all messages with a given uri as read.
    public static void markRead(Context context, Uri uri, int read) {
        Log.d("markRead(", uri + "," + read + ")");
        if (uri == null) {
            return;
        }
        String[] sel = Message.SELECTION_UNREAD;
        if (read == 0) {
            sel = Message.SELECTION_READ;
        }
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(Message.PROJECTION[Message.INDEX_READ], read);
        try {
            cr.update(uri, cv, Message.SELECTION_READ_UNREAD, sel);
        } catch (IllegalArgumentException | SQLiteException e) {
            Log.e(TAG, "failed update", e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //Turn off Nof
        if (!turnOffNof) {
            SmsReceiver.updateNewMessageNotification(context, null);
        }
    }

    //Delete messages with a given link Uri. Activity to finish when deleting.
    public static void deleteMessages(final Context context, final Uri uri, int title, int message, final Activity activity) {
        Log.i("deleteMessages(..,", uri + " ,..)");
        Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                try {
                    int ret = context.getContentResolver().delete(uri, null, null);
                    Log.d("deleted: ", ret + "");
                    if (activity != null && !activity.isFinishing()) {
                        activity.finish();
                    }
                    if (ret > 0) {
                        Conversation.flushCache();
                        Message.flushCache();
                        SmsReceiver.updateNewMessageNotification(context, null);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Argument Error", e);
                    Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_LONG).show();
                } catch (SQLiteException e) {
                    Log.e(TAG, "SQL Error", e);
                    Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_LONG).show();
                }

            }
        });
        builder.show();
    }

    // handle feedback
    private void handleFeedback(String name, String email,String sdt,String describe ) {
        Feedback fb = new Feedback();
        fb.setName(name);
        fb.setEmail(email);
        fb.setAddress(sdt);
        fb.setDescribe(describe);
        // Đổi dữ liệu sang dạng Json và đẩy lên Firebase bằng hàm Push
        Gson gson = new Gson();
        myFirebase.child("FeedBack").push().setValue(gson.toJson(fb));
    }

    // Get a Intent for sending a new message.
    public static Intent getComposeIntent(Context context, String address) {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (address == null) {
            i.setData(Uri.parse("sms:"));
        } else {

            i.setData(Uri.parse("smsto:" + address));
        }

        return i;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation c = Conversation.getConversation(this, (Cursor) parent.getItemAtPosition(position), false);
        Uri target = c.getUri();

        Intent i = new Intent(this, ConversationActivity.class);
        i.setData(target);
        i.putExtra("turnOffCompose", true);
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Log.e("error launching intent ", i.getAction() + ", " + i.getData());
            Toast.makeText(this, "error launching messaging app!\n" + "Please contact the developer.", Toast.LENGTH_LONG).show();
        }
    }

    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {
        final Conversation conv = Conversation.getConversation(this, (Cursor) parent.getItemAtPosition(position), true);
        final Uri target = conv.getUri();
        if (ContentUris.parseId(target) < 0) {
            // do not show anything for broken threadIds
            return true;
        }
        Builder builder = new Builder(this);
        String[] items = longItemClickDialog;
        Contact contact = conv.getContact();
        final String number = contact.getNumber();
        final String name = contact.getName();
        if (TextUtils.isEmpty(name)) {
            builder.setTitle(number);
            items = items.clone();
            items[WHICH_VIEW_CONTACT] = getString(R.string.add_contact_);
        } else {
            builder.setTitle(name);
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i;
                try {
                    switch (which) {
                        case WHICH_ANSWER:
                            i = getComposeIntent(MainActivity.this, number);
                            i.putExtra("turnOffCompose", true);
                            startActivity(i);
                            break;
                        case WHICH_CALL:
                            i = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + number));
                            startActivity(i);
                            break;
                        case WHICH_VIEW_CONTACT:
                            if (name == null) {
                                i = ContactsWrapper.getInstance().getInsertPickIntent(number);
                                Conversation.flushCache();
                            } else {
                                Uri uri = conv.getContact().getUri();
                                i = new Intent(Intent.ACTION_VIEW, uri);
                            }
                            try {
                                startActivity(i);
                            } catch (ActivityNotFoundException e) {
                                //unable to launch dailer
                                Toast.makeText(MainActivity.this, R.string.error_unknown, Toast.LENGTH_LONG).show();
                            }
                            //startActivity(i);
                            break;
                        case WHICH_VIEW:
                            i = new Intent(MainActivity.this, ConversationActivity.class);
                            i.setData(target);
                            i.putExtra("turnOffCompose", true);
                            startActivity(i);
                            break;
                        case WHICH_DELETE:
                            deleteMessages(MainActivity.this, target,
                                    R.string.delete_thread_,
                                    R.string.delete_thread_question,
                                    null);
                            break;
                        case WHICH_MARK_SPAM:
                            Block block = new Block();
                            block.setNumber(conv.getContact().getNumber());
                            db.addBlock(block);
                            markRead(MainActivity.this,conv.getUri(),1);

                            adapter.getBlacklist().add(block);
                            adapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "unable to launch activity:", e);
                    Toast.makeText(MainActivity.this, R.string.error_unknown,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.create().show();
        return true;
    }

    // Convert time into formated date.
    public static String getDate(Context context, long time) {
        long t = time;
        if (t < MIN_DATE) {
            t *= MILLIS;
        }
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsOldActivity.PREFS_FULL_DATE, false)) {
            return DateFormat.getTimeFormat(context).format(t) + " "
                    + DateFormat.getDateFormat(context).format(t);
        } else if (t < CAL_DAYAGO.getTimeInMillis()) {
            return DateFormat.getDateFormat(context).format(t);
        } else {
            return DateFormat.getTimeFormat(context).format(t);
        }
    }

    //Clean phone number from [ -.()<>]. Return clean number
    private String cleanRecipient(String recipient) {
        if (TextUtils.isEmpty(recipient)) {
            return "";
        }
        String n;
        int i = recipient.indexOf("<");
        int j = recipient.indexOf(">");
        if (i != -1 && i < j) {
            n = recipient.substring(recipient.indexOf("<"), recipient.indexOf(">"));
        } else {
            n = recipient;
        }
        return n.replaceAll("[^*#+0-9]", "").replaceAll("^[*#][0-9]*#", "");
    }

    /*private Message messageLastSMS(Conversation conv){
        Message message = new Message();
        Uri URI_SMS = Uri.parse("content://sms/");
        String SORT = CallLog.Calls.DATE + " DESC";
        Cursor c = null;
        try {
            c = getContentResolver().query
                    (URI_SMS, Message.PROJECTION, "address =?", new String[]{conv.getContact().getNumber()}, SORT);
        } catch (Exception e) {
            Log.e("error getting conv", e + "");
        }
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
        }
            if (c != null && c.moveToFirst()) {
                for (int i = 0; i < totalSMS; i++) {
                    message = Message.getMessage(this, c);
                    if (!message.getBody().toString().equals(conv.getBody())) {
                        Log.e("123", message.getBody().toString());
                        return message;
                    }
                    c.moveToNext();
                }
                c.close();
            }
        return message;
    }*/
}
