package com.example.trantrungduong95.truesms.Presenter.Activity_;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.trantrungduong95.truesms.CustomAdapter.ConversationSearchAdapter;
import com.example.trantrungduong95.truesms.CustomAdapter.PagerBlacklistAdapter;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Model.Search;
import com.example.trantrungduong95.truesms.Presenter.Fragment_.Fragment_Blacklist;
import com.example.trantrungduong95.truesms.Presenter.Fragment_.Fragment_Conv_Blacklist;
import com.example.trantrungduong95.truesms.Presenter.SpamHandler;

import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlacklistActivity extends AppCompatActivity {
    public List<Block> blockList = new ArrayList<Block>();
    public SpamHandler db;
    private PagerBlacklistAdapter adapter;

    //search
    private ListView listViewSearch;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSearch;
    private android.support.v7.app.ActionBar action;
    private ConversationSearchAdapter conversationSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       /* setTheme(SettingsOldActivity.getTheme(this));
        Utils.setLocale(this);*/
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blacklist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FetchDB();
        initInstancesDrawer();
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in Action Bar clicked; go home
/*            Intent intent = new Intent(this, MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);*/
                finish();
                return true;
            case R.id.action_search:
                handleMenuSearch();
                return true;
            case R.id.item_settings_blacklist_filterd:
                if (Build.VERSION.SDK_INT >= 19) {
                    startActivity(new Intent(this, SettingsNewActivity.class));
                } else {
                    startActivity(new Intent(this, SettingsOldActivity.class));
                }
                return true;
            case R.id.item_restore_blacklist: // start settings activity
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle(this.getString(R.string.title_restore));
                builder1.setMessage(this.getString(R.string.message_restore_all));
                builder1.setNegativeButton(android.R.string.no, null);
                builder1.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        db.deleteAllBlocks();

                        blockList.clear();
                        Fragment_Blacklist.blockAdapter.notifyDataSetChanged();

                        // update conversation block mailbox
                        Fragment_Conv_Blacklist.conversationArrayList.clear();
                        Fragment_Conv_Blacklist.fragmentBlacklistAdapter.notifyDataSetChanged();
                    }
                });
                builder1.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blacklist_activity, menu);
        menu.removeItem(R.id.item_settings_blacklist_filterd);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isSearchOpened) {
            handleMenuSearch();
            return;
        }
        super.onBackPressed();
    }

    //DB
    public void FetchDB() {
        db = new SpamHandler(this);
        blockList = db.getAllBlocks();
    }

    //Tabview
    private void initInstancesDrawer() {
        TabLayout tabs = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager pager = (ViewPager) findViewById(R.id.vpPager);
        adapter = new PagerBlacklistAdapter(getSupportFragmentManager(), this);

        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[]{
                Color.GREEN,
                Color.RED,
                Color.BLACK,
                Color.BLUE
        };
        if (SettingsOldActivity.getTheme(this) == R.style.Theme_TrueSMS){
            pager.setBackgroundColor(Color.parseColor("#ff303030"));
        }
        else /*if (SettingsOldActivity.getTheme(this) == R.style.Theme_TrueSMS_Light)*/{
            {
                pager.setBackgroundColor(Color.WHITE);
            }
        }
        ColorStateList myList = new ColorStateList(states, colors);
        tabs.setTabTextColors(myList);
    }

    public void update_frg_1(String number) {
        Fragment_Conv_Blacklist item = (Fragment_Conv_Blacklist) adapter.getItem(0);
        if (item != null) {
            item.update_recovery_list(number);
        }
    }

    public void update_frg_2(String number) {
        Fragment_Blacklist item = (Fragment_Blacklist) adapter.getItem(1);
        if (item != null) {
            item.update_list_phone(number);
        }
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

            Fragment_Conv_Blacklist.listView.setVisibility(View.VISIBLE);
            listViewSearch.setVisibility(View.INVISIBLE);

            isSearchOpened = false;
        } else { //open the search entry

            if (action != null) {
                action.setDisplayShowCustomEnabled(true); //enable it to display a
                // custom view in the action bar.
                action.setCustomView(R.layout.search_bar);//add the custom view
                action.setDisplayShowTitleEnabled(false); //hide the title
                edtSearch = (EditText) action.getCustomView().findViewById(R.id.edtSearch); //the text editor
            }
            //this is a listener to do a search when the user clicks on search button
            Fragment_Conv_Blacklist.listView.setVisibility(View.INVISIBLE);
            listViewSearch = (ListView) findViewById(R.id.conversations_list_blacklist_search);
            listViewSearch.setVisibility(View.VISIBLE);

            // new search list
            final ArrayList<Search> conversationsArrayList = new ArrayList<>();
            for (int i = 0; i < Fragment_Conv_Blacklist.conversationArrayList.size(); i++) {
                Search s = new Search(Fragment_Conv_Blacklist.conversationArrayList.get(i).getContact().getNameAndNumber(),
                        Fragment_Conv_Blacklist.conversationArrayList.get(i).getBody());
                conversationsArrayList.add(s);
            }

            //set adapter search
            conversationSearchAdapter = new ConversationSearchAdapter(conversationsArrayList, this);
            listViewSearch.setAdapter(conversationSearchAdapter);

            registerForContextMenu(listViewSearch);
            listViewSearch.setTextFilterEnabled(true);
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
            //imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            // click item listViewSearch
            listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String number = conversationsArrayList.get(position).getNum();
                    Pattern pattern = Pattern.compile("^[0-9]*$");
                    Matcher matcher = pattern.matcher(number);
                    if (!matcher.matches()) {
                        startActivity(MainActivity.getComposeIntent(getApplicationContext(),cleanRecipient(number)));
                    } else
                        startActivity(MainActivity.getComposeIntent(getApplicationContext(),number));

                }
            });

            //add the close icon
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_cancel));

            isSearchOpened = true;
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

    /*private String ChecknumberContact(String number) {
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts = getAllContacts();
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getName().equals(number))
                return contacts.get(i).getNumber();
        }
        return "";
    }

    private ArrayList<Contact> getAllContacts() {
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
    }*/
}
