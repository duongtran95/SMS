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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.ConversationSearchAdapter;
import com.example.trantrungduong95.truesms.CustomAdapter.ConversationsAdapter;
import com.example.trantrungduong95.truesms.Fab.FloatingActionButton;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Message;
import com.example.trantrungduong95.truesms.Model.Search;
import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;
import com.example.trantrungduong95.truesms.Presenter.AsyncHelper;
import com.example.trantrungduong95.truesms.Presenter.ConversationActivity;
import com.example.trantrungduong95.truesms.Presenter.DefaultAndPermission;
import com.example.trantrungduong95.truesms.Presenter.Filterd_Blacklist_Activity;
import com.example.trantrungduong95.truesms.Presenter.SettingsNewActivity;
import com.example.trantrungduong95.truesms.Presenter.SettingsOldActivity;
import com.example.trantrungduong95.truesms.Presenter.SpamDB;
import com.example.trantrungduong95.truesms.Presenter.Utils;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.PatternSyntaxException;

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

    @Override
    public void onStart() {
        super.onStart();
        AsyncHelper.setAdapter(adapter);
        Log.d("onStart", "");
    }

    @Override
    public void onStop() {
        super.onStop();
        AsyncHelper.setAdapter(null);
        Log.d("onStop", "");
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

/*    //Show all list of a particular uri.
    @SuppressWarnings("UnusedDeclaration")
    public static void showList(Context context, Uri u) {
        Log.d("GET HEADERS", u.toString());
        Cursor c = context.getContentResolver().query(u, null, null, null, null);
        if (c != null) {
            int l = c.getColumnCount();

            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < l; i++) {
                buf.append(i).append(":");
                buf.append(c.getColumnName(i));
                buf.append(" | ");
            }
            Log.d(TAG, buf.toString());
        }

    }*/


    protected void handleMenuSearch() {
        action = getSupportActionBar();
        if (isSearchOpened) { //test if the search is open

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //hides the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

            //add the search icon in the action bar
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search));

            listview_conversation.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);

            isSearchOpened = false;
        } else { //open the search entry

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title

            edtSearch = (EditText) action.getCustomView().findViewById(R.id.edtSearch); //the text editor

            //this is a listener to do a search when the user clicks on search button
            listview_conversation.setVisibility(View.INVISIBLE);
            listView = (ListView) findViewById(R.id.listView);
            listView.setVisibility(View.VISIBLE);

            // new search list
            final ArrayList<Search> conversationsArrayList = new ArrayList<>();
            getAllValues(listview_conversation, conversationsArrayList);

            //set adapter search
            conversationSearchAdapter = new ConversationSearchAdapter(conversationsArrayList, this);
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

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);

            // click item listview
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String number = ChecknumberContact(conversationsArrayList.get(position).getNum());
                    if (!ChecknumberContact(conversationsArrayList.get(position).getNum()).equals(""))
                    {
                        startActivity(getComposeIntent(MainActivity.this, number));
                    }
                    else startActivity(getComposeIntent(MainActivity.this, conversationsArrayList.get(position).getNum()));

                }
            });

            //add the close icon
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_cancel));

            isSearchOpened = true;
        }
    }

    //Get All item listview comversations
    public void getAllValues(ListView list, ArrayList<Search> arrayList) {
        View parentView = null;
        for (int i = 0; i < list.getCount(); i++) {
            parentView = getViewByPosition(i, list);
            String addr = ((TextView) parentView
                    .findViewById(R.id.addr)).getText().toString();
            String body = ((TextView) parentView
                    .findViewById(R.id.body)).getText().toString();
          /*  String date = ((TextView) parentView
                    .findViewById(R.id.date)).getText().toString();
            String count = ((TextView) parentView
                    .findViewById(R.id.count)).getText().toString();
            ImageView photo = ((ImageView) parentView.findViewById(R.id.photo));
            BitmapDrawable bitmapDrawable = (BitmapDrawable) photo.getDrawable();
            Bitmap yourBitmap = bitmapDrawable.getBitmap();*/
            Search a;
            if (!addr.equals("false")) {
                a = new Search(addr, body);
                arrayList.add(a);
            }
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition
                + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    @Override
    public void onBackPressed() {
        if (isSearchOpened) {
            handleMenuSearch();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(SettingsOldActivity.getTheme(this));
        Utils.setLocale(this);
        setContentView(R.layout.activity_main);

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
                    Toast.makeText(getApplication(), "error launching messaging app!\nPlease contact the developer.", Toast.LENGTH_LONG).show();
                }
            }
        });
        Log.d("onCreate", "");
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

    @Override
    protected void onResume() {
        super.onResume();
        CAL_DAYAGO.setTimeInMillis(System.currentTimeMillis());
        CAL_DAYAGO.add(Calendar.DAY_OF_MONTH, -1);

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        showContactPhoto = p.getBoolean(SettingsOldActivity.PREFS_CONTACT_PHOTO, true);
        showEmoticons = p.getBoolean(SettingsOldActivity.PREFS_EMOTICONS, false);
        adapter = new ConversationsAdapter(this);
        setListAdapter(adapter);
        if (adapter != null) {
            adapter.startMsgListQuery();
        }

        Log.d("onResume", "");
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
        SmsReceiver.updateNewMessageNotification(context, null);
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

    //Add or remove an entry to/from blacklist.
    public static void addToOrRemoveFromSpamlist(Context context, String addr) {
        SpamDB.toggleBlacklist(context, addr);
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
            case R.id.item_filterd_blacklist:
                Intent intent_filterd_blacklist = new Intent(this, Filterd_Blacklist_Activity.class);
                startActivity(intent_filterd_blacklist);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if (SpamDB.isBlacklisted(this, number)) {

            items = items.clone();
            items[WHICH_MARK_SPAM] = getString(R.string.dont_filter_spam_);
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i;
                try {
                    switch (which) {
                        case WHICH_ANSWER:
                            startActivity(getComposeIntent(MainActivity.this, number));
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
                            startActivity(i);
                            break;
                        case WHICH_VIEW:
                            i = new Intent(MainActivity.this, ConversationActivity.class);
                            i.setData(target);
                            startActivity(i);
                            break;
                        case WHICH_DELETE:
                            deleteMessages(MainActivity.this, target,
                                            R.string.delete_thread_,
                                            R.string.delete_thread_question,
                                            null);
                            break;
                        case WHICH_MARK_SPAM:
                            addToOrRemoveFromSpamlist(MainActivity.this, conv.getContact().getNumber());
                            deleteMsg(target,MainActivity.this);

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

    // delete uri
    public static void deleteMsg(Uri target,Context context){
                    try {
                int ret = context.getContentResolver().delete(target, null, null);
                Log.d("deleted: ", ret + "");
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
    public String ChecknumberContact(String number){
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts = getAllContacts();
        for (int i=0;i<contacts.size();i++){
            if (contacts.get(i).getName().equals(number))
                return contacts.get(i).getNumber();
        }
        return "";
    }

    public ArrayList<Contact> getAllContacts() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Contact contact = new Contact(phoneNumber, name);
            contacts.add(contact);
        }
        phones.close();
        return contacts;
    }
}
