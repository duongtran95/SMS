package com.example.trantrungduong95.truesms.Presenter.Fragment_;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.trantrungduong95.truesms.CustomAdapter.FilterAdapter;
import com.example.trantrungduong95.truesms.CustomAdapter.FragmentFilterdAdapter;
import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Test;
import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;
import com.example.trantrungduong95.truesms.Presenter.Activity_.ConversationActivity;
import com.example.trantrungduong95.truesms.Presenter.SpamHandler;
import com.example.trantrungduong95.truesms.R;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Conv_Filter extends android.support.v4.app.Fragment  implements
        AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{
    //Number of items.
    private static final int WHICH_N = 2;
    //Index in dialog: answer.
    private static final int WHICH_VIEW = 0;
    //Index in dialog: call.
    private static final int WHICH_CALL = 1;
/*    //Index in dialog: view/add contact.
    private static final int WHICH_VIEW_CONTACT = 2;*/

    public static ListView listView;
    public static ArrayList<Conversation> conversationArrayList = new ArrayList<>();
    public static FragmentFilterdAdapter fragmentFilterdAdapter;

    String[] longItemClickDialog = new String[WHICH_N];

    public static List<Block> blockList = new ArrayList<Block>();
    public SpamHandler db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frament_conv_blacklist, container, false);
        setHasOptionsMenu(true);

        handleData(view);
        return view;
    }

    private void handleData(View view) {
        db = new SpamHandler(getActivity());
        blockList = db.getAllBlocks();

        conversationArrayList = getConvFilter();

        listView = (ListView) view.findViewById(R.id.conversations_list_blacklist);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);

        longItemClickDialog[WHICH_VIEW] = getString(R.string.view_thread_);
        longItemClickDialog[WHICH_CALL] = getString(R.string.call);
        //longItemClickDialog[WHICH_VIEW_CONTACT] = getString(R.string.view_contact_);

        fragmentFilterdAdapter = new FragmentFilterdAdapter(getActivity(),R.layout.conversationlist_item, conversationArrayList);
        listView.setAdapter(fragmentFilterdAdapter);
    }

    public ArrayList<Test> ReadFilterMailbox() {
        ArrayList<Test> messages = new ArrayList<>();
        Uri uriSms = Uri.parse("content://sms/");
        ContentResolver cr = getActivity().getContentResolver();
        Cursor c = cr.query(uriSms, null, null, null, null);

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

    public ArrayList<Conversation> getConvFilter() {
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.filterd, menu);
        menu.removeItem(R.id.item_add_filter);
        menu.removeItem(R.id.item_delete_all_filter);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.item_delete_all_filter: // start settings activity

                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation conv = conversationArrayList.get(position);
        //onItemLongClick(parent, view, position, id);
        Intent i;
        i = new Intent(getActivity(), ConversationActivity.class);
        i.setData(conv.getUri());
        i.putExtra("flag",true);
        startActivity(i);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Conversation conv = conversationArrayList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] items = longItemClickDialog;
        Contact contact = conv.getContact();
        final String number = contact.getNumber();
        final String name = contact.getName();
        if (TextUtils.isEmpty(name)) {
            builder.setTitle(number);
            items = items.clone();
            //items[WHICH_VIEW_CONTACT] = getString(R.string.add_contact_);
        } else {
            builder.setTitle(name);
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i;
                try {
                    switch (which) {
                        case WHICH_VIEW:
                            //startActivity(MainActivity.getComposeIntent(getActivity(), number));
                            i = new Intent(getActivity(), ConversationActivity.class);
                            i.setData(conv.getUri());
                            i.putExtra("flag",true);
                            startActivity(i);
                            break;

                        case WHICH_CALL:
                            i = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + number));
                            startActivity(i);
                            break;
                        /*case WHICH_VIEW_CONTACT:
                            if (name == null) {
                                i = ContactsWrapper.getInstance().getInsertPickIntent(number);
                                Conversation.flushCache();
                            } else {
                                Uri uri = conv.getContact().getUri();
                                i = new Intent(Intent.ACTION_VIEW, uri);
                            }
                            startActivity(i);
                            break;*/
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

    public boolean isBlocked(String addr) {
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
}
