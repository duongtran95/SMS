package com.example.trantrungduong95.truesms.Presenter.Fragment_;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.FilterAdapter;
import com.example.trantrungduong95.truesms.CustomAdapter.FragmentFilterdAdapter;
import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Filter;
import com.example.trantrungduong95.truesms.Model.Test;
import com.example.trantrungduong95.truesms.Presenter.SpamHandler;
import com.example.trantrungduong95.truesms.R;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;
import com.firebase.client.Firebase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Filter extends Fragment implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
    private static final int MODE_PRIVATE = 0;
    private ListView listView;
    private FilterAdapter filterAdapter;
    private List<Filter> filterList = new ArrayList<Filter>();
    private SpamHandler db;
    private Button btnChar, btnWord, btnPharse;
    private int type =0;
    private SharedPreferences mPref;
    private String[] longItemClickDialog = new String[WHICH_N];

    //Number of items.
    private static final int WHICH_N = 2;
    //Index in dialog: edit.
    private static final int WHICH_EDIT= 0;
    //Index in dialog: delete.
    private static final int WHICH_DELETE = 1;

    private boolean flag_edit = false;

    // number autoFiterd update Firebase
    private int countFiter = 20;

    //Firebase Filter
    Firebase myFirebase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        setHasOptionsMenu(true);

        db = new SpamHandler(getActivity());
        filterList = db.getAllFilters();

        listView = (ListView) view.findViewById(R.id.list_filter);

        myFirebase = new Firebase("https://democn-6f3ab.firebaseio.com/");
        // create SharedPreferences
        mPref = getActivity().getSharedPreferences("MyPrefsFile", MODE_PRIVATE);

        if (filterList.size() <= 30) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putInt("numberFilterd", filterList.size());
            editor.apply();
        }

        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        onclickButton(view);

        longItemClickDialog[WHICH_EDIT] = getString(R.string.filter_edit);
        longItemClickDialog[WHICH_DELETE] = getString(R.string.filter_delete);

        filterAdapter = new FilterAdapter(getActivity(),R.layout.custom_filter,filterList,type);
        listView.setAdapter(filterAdapter);

        return view;
    }

    private void onclickButton(View view){
        btnChar = (Button) view.findViewById(R.id.btnChar);
        btnChar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = 0;
                filterAdapter = new FilterAdapter(getActivity(),R.layout.custom_filter,filterList,type);
                listView.setAdapter(filterAdapter);
            }
        });

        btnWord = (Button) view.findViewById(R.id.btnWord);
        btnWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = -1;
                filterAdapter = new FilterAdapter(getActivity(),R.layout.custom_filter,filterList,type);
                listView.setAdapter(filterAdapter);
            }
        });
        btnPharse = (Button) view.findViewById(R.id.btnPharse);
        btnPharse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = 1;
                filterAdapter = new FilterAdapter(getActivity(),R.layout.custom_filter,filterList,type);
                listView.setAdapter(filterAdapter);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onItemLongClick(parent, view, position, id);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] items = longItemClickDialog;
        builder.setTitle(getActivity().getString(R.string.option_menu_filter));
        items = items.clone();
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    switch (which) {
                        case WHICH_EDIT:
                            flag_edit = true;
                            if (type==0){
                                editcharF(position);
                            }
                            else if (type==-1){
                                editWordF(position);
                            }
                            else if (type==1){
                                editPharseF(position);
                            }
                            break;
                        case WHICH_DELETE:
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                            builder2.setTitle(getActivity().getString(R.string.delete_filter));
                            builder2.setMessage(getActivity().getString(R.string.confirm_delete_filter));
                            builder2.setNegativeButton(android.R.string.no, null);
                            builder2.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    db.deleteFilter(filterList.get(position));
                                    filterList.remove(filterList.get(position));
                                    filterAdapter.notifyDataSetChanged();

                                    updateListFilterWhenAdd();
                                }
                            });
                            builder2.show();
                            break;
                        default:
                            break;
                    }
                } catch (ActivityNotFoundException e) {
                    Log.e("unable to launch activ:", e.getMessage());
                    Toast.makeText(getActivity(), R.string.error_unknown,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.create().show();

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.filterd, menu);
        menu.removeItem(R.id.action_search);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add_filter:
                return true;
            case R.id.item_char:
                addCharF();

                return true;

            case R.id.item_word:
                addWordF();

                return true;

            case R.id.item_pharse:
                addPharseF();

                return true;

            case R.id.item_delete_all_filter: // start settings activity
                AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
                builder3.setTitle(getActivity().getString(R.string.delete_all_filter));
                builder3.setMessage(getActivity().getString(R.string.confirm_all_delete_filter));
                builder3.setNegativeButton(android.R.string.no, null);
                builder3.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        db.deleteAllFilters();
                        filterList.clear();

                        db.createDefaultFilterIfNeed();
                        filterList = db.getAllFilters();
                        filterAdapter = new FilterAdapter(getActivity(),R.layout.custom_filter,filterList,type);
                        listView.setAdapter(filterAdapter);

                        updateListFilterWhenAdd();

                    }
                });
                builder3.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void autoUpdateFilterFromSever() {
        int countttt = mPref.getInt("numberFilterd", countFiter);
        Log.d("number-F-UpdateServer",countttt+ " "+ filterList.size());
        if (filterList.size() == countttt + countFiter) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putInt("numberFilterd", filterList.size());
            editor.apply();
            for (int i = countttt; i <=filterList.size()-1; i++) {
                // Đổi dữ liệu sang dạng Json và đẩy lên Firebase bằng hàm Push
                Gson gson = new Gson();
                myFirebase.child("Filter").push().setValue(gson.toJson(filterList.get(i)));
            }
            //todo call sever update
        }
    }

    private void editPharseF(final int position) {
        final EditText editText2 = new EditText(getActivity());

        editText2.setText(filterList.get(position).getPharse_());
        int pos = editText2.getText().length();
        editText2.setSelection(pos);

        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
        builder2.setTitle(getActivity().getString(R.string.title_edit_filterd_pharse));
        builder2.setMessage(getActivity().getString(R.string.title_edit_filter_hint));
        builder2.setView(editText2);
        builder2.setNegativeButton(android.R.string.no, null);
        builder2.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (!editText2.getText().toString().equals("")) {
                    if (isDuplicated(editText2.getText().toString())) {
                        Filter filter = new Filter();
                        filter.setId_(filterList.get(position).getId_());
                        filter.setPharse_(editText2.getText().toString());
                        filter.setChar_(null);
                        filter.setWord_(null);

                        db.updateFilters(filter);

                        filterList.clear();
                        filterList = db.getAllFilters();
                        filterAdapter = new FilterAdapter(getActivity(), R.layout.custom_filter, filterList, type);
                        listView.setAdapter(filterAdapter);

                        updateListFilterWhenAdd();
                    }
                    else  Toast.makeText(getActivity(), getActivity().getString(R.string.db_isExits), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                }


            }
        });
        builder2.show();
    }

    private void editWordF(final int position) {
        final EditText editText1 = new EditText(getActivity());
        final String w = "123456789";

        //Only special character
        InputFilter filterw= new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && w.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        //off space
        InputFilter filter1 = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String filtered = "";
                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (!Character.isWhitespace(character)) {
                        filtered += character;
                    }
                }

                return filtered;
            }

        };

        //limit length editText
        int maxLength = 7;
        editText1.setFilters(new InputFilter[] {filter1,filterw,
                new InputFilter.LengthFilter(maxLength)
        });

        editText1.setText(filterList.get(position).getWord_());
        int pos = editText1.getText().length();
        editText1.setSelection(pos);

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setTitle(getActivity().getString(R.string.title_menu_add_filterd));
        builder1.setMessage(getActivity().getString(R.string.title_add_filter_hint));
        builder1.setView(editText1);
        builder1.setNegativeButton(android.R.string.no, null);
        builder1.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (!editText1.getText().toString().equals("")) {
                    if (isDuplicated(editText1.getText().toString())) {
                        Filter filter = new Filter();
                        filter.setId_(filterList.get(position).getId_());
                        filter.setWord_(editText1.getText().toString());
                        filter.setChar_(null);
                        filter.setPharse_(null);

                        db.updateFilters(filter);

                        filterList.clear();
                        filterList = db.getAllFilters();
                        filterAdapter = new FilterAdapter(getActivity(), R.layout.custom_filter, filterList, type);
                        listView.setAdapter(filterAdapter);

                        updateListFilterWhenAdd();
                    }
                    else Toast.makeText(getActivity(), getActivity().getString(R.string.db_isExits), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                }


            }
        });
        builder1.show();
    }

    private void editcharF(final int position) {
        final EditText editText = new EditText(getActivity());
        final String blockCharacterSet = "123456789qwertyuiopasdfghjklzxcvbnm";

        //Only special character
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        editText.setFilters(new InputFilter[] { filter});
        editText.setText(filterList.get(position).getChar_());
        int pos = editText.getText().length();
        editText.setSelection(pos);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.title_edit_filterd_char));
        builder.setMessage(getActivity().getString(R.string.title_edit_filter_hint));
        builder.setView(editText);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (!editText.getText().toString().equals("")) {
                    if (isDuplicated(editText.getText().toString())) {
                        Filter filter = new Filter();
                        filter.setId_(filterList.get(position).getId_());
                        filter.setChar_(editText.getText().toString());
                        filter.setWord_(null);
                        filter.setPharse_(null);

                        db.updateFilters(filter);

                        filterList.clear();
                        filterList = db.getAllFilters();
                        filterAdapter = new FilterAdapter(getActivity(), R.layout.custom_filter, filterList, type);
                        listView.setAdapter(filterAdapter);

                        updateListFilterWhenAdd();
                    }
                    else
                        Toast.makeText(getActivity(), getActivity().getString(R.string.db_isExits), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void addPharseF() {
        final EditText editText2 = new EditText(getActivity());

        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
        builder2.setTitle(getActivity().getString(R.string.title_menu_add_filterd));
        builder2.setMessage(getActivity().getString(R.string.title_add_filter_hint));
        builder2.setView(editText2);
        builder2.setNegativeButton(android.R.string.no, null);
        builder2.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (!editText2.getText().toString().equals("")) {
                    if (isDuplicated(editText2.getText().toString())) {
                        Filter filter = new Filter();
                        filter.setPharse_(editText2.getText().toString());
                        filter.setChar_(null);
                        filter.setWord_(null);

                        long id = db.addFilter(filter);
                        filter.setId_((int) id);
                        filterList.add(filter);
                        filterAdapter.notifyDataSetChanged();

                        updateListFilterWhenAdd();

                        autoUpdateFilterFromSever();
                    } else
                        Toast.makeText(getActivity(), getActivity().getString(R.string.db_isExits), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                }


            }
        });
        builder2.show();
    }

    private void addWordF() {
        final EditText editText1 = new EditText(getActivity());
        final String w = "123456789";

        //Only special character
        InputFilter filterw= new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && w.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        //off space
        InputFilter filter1 = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String filtered = "";
                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (!Character.isWhitespace(character)) {
                        filtered += character;
                    }
                }

                return filtered;
            }

        };

        //limit length editText
        int maxLength = 7;
        editText1.setFilters(new InputFilter[] {filter1,filterw,
                new InputFilter.LengthFilter(maxLength)
        });


        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setTitle(getActivity().getString(R.string.title_menu_add_filterd));
        builder1.setMessage(getActivity().getString(R.string.title_add_filter_hint));
        builder1.setView(editText1);
        builder1.setNegativeButton(android.R.string.no, null);
        builder1.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (!editText1.getText().toString().equals("")) {
                    if (isDuplicated(editText1.getText().toString())) {
                        Filter filter = new Filter();
                        filter.setWord_(editText1.getText().toString());
                        filter.setChar_(null);
                        filter.setPharse_(null);

                        long id = db.addFilter(filter);
                        filter.setId_((int) id);

                        filterList.add(filter);
                        filterAdapter.notifyDataSetChanged();

                        updateListFilterWhenAdd();
                        autoUpdateFilterFromSever();
                    }
                    else Toast.makeText(getActivity(), getActivity().getString(R.string.db_isExits), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                }


            }
        });
        builder1.show();
    }

    private void addCharF() {
        final EditText editText = new EditText(getActivity());
        final String blockCharacterSet = "123456789qwertyuiopasdfghjklzxcvbnm";

        //Only special character
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        editText.setFilters(new InputFilter[] { filter});

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.title_menu_add_filterd));
        builder.setMessage(getActivity().getString(R.string.title_add_filter_hint));
        builder.setView(editText);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (!editText.getText().toString().equals("")) {
                    if (isDuplicated(editText.getText().toString())) {
                        Filter filter = new Filter();
                        filter.setChar_(editText.getText().toString());
                        filter.setWord_(null);
                        filter.setPharse_(null);

                        long id = db.addFilter(filter);
                        filter.setId_((int) id);
                        filterList.add(filter);
                        filterAdapter.notifyDataSetChanged();

                        updateListFilterWhenAdd();
                        autoUpdateFilterFromSever();
                    }
                    else Toast.makeText(getActivity(), getActivity().getString(R.string.db_isExits), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private ArrayList<Test> ReadFilterMailbox() {
        ArrayList<Test> messages = new ArrayList<>();
        Uri uriSms = Uri.parse("content://sms/");
        ContentResolver cr = getActivity().getContentResolver();
        //Cursor's projection.
        String[] PROJECTION = { //
                "_id", // 0
                "address", // 1
                "body", // 2
                "date", // 3
        };
        //Cursor c = cr.query(uriSms, null, null, null, null);
        Cursor c = cr.query(uriSms,PROJECTION,null, null, null);
        int totalSMS = c.getCount();
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                Test message = new Test();
                String id = c.getString(c.getColumnIndexOrThrow("_id"));
                message.setId(Integer.parseInt(id));
                String phone = c.getString(c.getColumnIndexOrThrow("address"));
                message.setNumber(phone);

                String body = c.getString(c.getColumnIndexOrThrow("body"));
                message.setBody_(body);
                long date = c.getLong(c.getColumnIndexOrThrow("date"));
                message.setDate_(date);

                if (SmsReceiver.filter(getActivity(),body,phone) && !isBlocked(phone)) {
                    messages.add(message);
                }
                c.moveToNext();
            }
            c.close();
        }
        return messages;
    }

    private ArrayList<Conversation> getConvFilter() {
        ArrayList<Test> messages = ReadFilterMailbox();
        ArrayList<Conversation> conversationFilter = new ArrayList<>();
        Cursor c = null;
        try {
            c = getActivity().getContentResolver().query(Conversation.URI_SIMPLE, Conversation.PROJECTION_SIMPLE, Conversation.COUNT + ">0", null, null);
        } catch (Exception e) {
            Log.e("error getting conv", e + "");
        }

        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
        }
        if (c != null && c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                Conversation conv = Conversation.getConversation(getActivity(), c, true);
                for (int j = 0; j < messages.size(); j++) {
                    if (conv.getContact().getNumber().equals(messages.get(j).getNumber())) {
                        conv.setBody(messages.get(j).getBody_());
                        conversationFilter.add(conv);
                        break;
                    }
                }
                c.moveToNext();
            }
            c.close();
        }
        return conversationFilter;
    }

    private boolean isBlocked(String addr) {
        //SpamHandler db = new SpamHandler(getActivity());
        //List<Block> blockList = db.getAllBlocks();
        if (addr == null) {
            return false;
        }
        for (Block aBlacklist : Fragment_Conv_Filter.blockList) {
            if (addr.equals(aBlacklist.getNumber())) {
                return true;
            }
        }
        return false;
    }

    private void updateListFilterWhenAdd(){
        Fragment_Conv_Filter.conversationArrayList.clear();
        Fragment_Conv_Filter.conversationArrayList = getConvFilter();
        Fragment_Conv_Filter.fragmentFilterdAdapter = new FragmentFilterdAdapter
                (getActivity(),R.layout.conversationlist_item, Fragment_Conv_Filter.conversationArrayList);
        Fragment_Conv_Filter.listView.setAdapter(Fragment_Conv_Filter.fragmentFilterdAdapter);
    }

    private boolean isDuplicated( String content) {
        for (int i = 0; i < filterList.size(); i++) {
            if (filterList.get(i).getChar_() != null && filterList.get(i).getChar_().equals(content)) {
                return false;
            }
            else if (filterList.get(i).getWord_() != null && filterList.get(i).getWord_().equals(content)){
                return false;
            }
            else if (filterList.get(i).getPharse_() != null && filterList.get(i).getPharse_().equals(content)){
                return false;
            }
        }
        return true;
    }
}
