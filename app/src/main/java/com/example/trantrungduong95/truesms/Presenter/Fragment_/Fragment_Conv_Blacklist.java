package com.example.trantrungduong95.truesms.Presenter.Fragment_;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
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

import com.example.trantrungduong95.truesms.CustomAdapter.FragmentBlacklistAdapter;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;
import com.example.trantrungduong95.truesms.Presenter.Activity_.BlacklistActivity;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;

public class Fragment_Conv_Blacklist extends android.support.v4.app.Fragment implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    //Number of items.
    private static final int WHICH_N = 4;
    //Index in dialog: answer.
    private static final int WHICH_VIEW = 0;
    //Index in dialog: call.
    private static final int WHICH_CALL = 1;
    /*    //Index in dialog: view/add contact.
        private static final int WHICH_VIEW_CONTACT = 2;*/
    //Index in dialog: restore.
    private static final int WHICH_RESTORE = 2;
    //Index in dialog: delete.
    private static final int WHICH_DELETE = 3;
    //Dialog items shown if an item was long clicked.
    private String[] longItemClickDialog = null;

    public static ListView listView;
    public static ArrayList<Conversation> conversationArrayList = new ArrayList<>();
    public static FragmentBlacklistAdapter fragmentBlacklistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frament_conv_blacklist, container, false);
        setHasOptionsMenu(true);
        handleData(view);
        return view;
    }

    private void handleData(View view) {
        conversationArrayList = getConvBlacklist();
        listView = (ListView) view.findViewById(R.id.conversations_list_blacklist);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);

        longItemClickDialog = new String[WHICH_N];
        longItemClickDialog[WHICH_VIEW] = getString(R.string.view_thread_);
        longItemClickDialog[WHICH_CALL] = getString(R.string.call);
        //longItemClickDialog[WHICH_VIEW_CONTACT] = getString(R.string.view_contact_);
        longItemClickDialog[WHICH_RESTORE] = getString(R.string.title_restore);
        longItemClickDialog[WHICH_DELETE] = getString(R.string.delete_thread_);
        fragmentBlacklistAdapter = new FragmentBlacklistAdapter(getActivity(), R.layout.conversationlist_item, conversationArrayList);
        listView.setAdapter(fragmentBlacklistAdapter);
    }

    public boolean isBlocked(String addr) {
        if (addr == null) {
            return false;
        }
        for (Block aBlacklist : ((BlacklistActivity) getActivity()).blockList) {
            if (addr.equals(aBlacklist.getNumber())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Conversation> getConvBlacklist() {
        ArrayList<Conversation> convList = new ArrayList<>();
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
                if (isBlocked(conv.getContact().getNumber())) {
                    convList.add(conv);
                }
                c.moveToNext();
            }
            c.close();
        }
        return convList;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.blacklist, menu);
        menu.removeItem(R.id.item_add_blacklist);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_restore_blacklist: // start settings activity
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setTitle(getActivity().getString(R.string.title_restore));
                builder1.setMessage(getActivity().getString(R.string.message_restore));
                builder1.setNegativeButton(android.R.string.no, null);
                builder1.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        ((BlacklistActivity) getActivity()).db.deleteAllBlocks();
                        ((BlacklistActivity) getActivity()).blockList = ((BlacklistActivity) getActivity()).db.getAllBlocks();
                        conversationArrayList.clear();
                        fragmentBlacklistAdapter.notifyDataSetChanged();
                    }
                });
                builder1.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation conv = conversationArrayList.get(position);
        //onItemLongClick(parent, view, position, id);
        Intent i;
        i = MainActivity.getComposeIntent(getActivity(), conv.getContact().getNumber());
        MainActivity.turnOffNof = true;
        startActivity(i);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final Conversation conv = conversationArrayList.get(position);
        final Uri target = conv.getUri();
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
                            i = MainActivity.getComposeIntent(getActivity(), number);
                            MainActivity.turnOffNof = true;
                            startActivity(i);
                            break;
                        case WHICH_CALL:
                            i = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + number));
                            startActivity(i);
                            break;
                       /* case WHICH_VIEW_CONTACT:
                            if (name == null) {
                                i = ContactsWrapper.getInstance().getInsertPickIntent(number);
                                Conversation.flushCache();
                            } else {
                                Uri uri = conv.getContact().getUri();
                                i = new Intent(Intent.ACTION_VIEW, uri);
                            }
                            startActivity(i);
                            break;*/
                        case WHICH_RESTORE:
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(getActivity().getString(R.string.title_restore));
                            builder.setMessage(getActivity().getString(R.string.message_restore));
                            builder.setNegativeButton(android.R.string.no, null);
                            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    BlacklistActivity activity = (BlacklistActivity) getActivity();
                                    activity.update_frg_2(number);
                                    recover_msg(position);
                                }
                            });
                            builder.show();
                            break;
                        case WHICH_DELETE:
                            MainActivity.deleteMessages(getActivity(), target,
                                    R.string.delete_thread_,
                                    R.string.delete_thread_question,
                                    null);
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

    private void recover_msg(int position) {
        String number = conversationArrayList.get(position).getContact().getNumber();
        Block block = new Block();
        block.setNumber(number);
        ((BlacklistActivity) getActivity()).db.deleteBlock(block);
        conversationArrayList.remove(conversationArrayList.get(position));
        fragmentBlacklistAdapter.notifyDataSetChanged();


    }

    public void update_recovery_list(String number) {
        for (int i = 0; i < conversationArrayList.size(); i++) {
            if (conversationArrayList.get(i).getContact().getNumber().equals(number)) {
                recover_msg(i);
                break;
            }
        }
    }
}