package com.example.trantrungduong95.truesms.Presenter.Fragment_;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.BlockAdapter;
import com.example.trantrungduong95.truesms.CustomAdapter.FragmentBlacklistAdapter;
import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Presenter.Activity_.BlacklistActivity;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;

public class Fragment_Blacklist extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView listView;
    public static BlockAdapter blockAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);
        setHasOptionsMenu(true);

        listView = (ListView) view.findViewById(R.id.list_blacklist);

        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);

        blockAdapter = new BlockAdapter(getActivity(), R.layout.custom_block, ((BlacklistActivity) getActivity()).blockList);
        listView.setAdapter(blockAdapter);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onItemLongClick(parent, view, position, id);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.title_restore));
        builder.setMessage(getActivity().getString(R.string.message_restore));
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                BlacklistActivity activity = (BlacklistActivity) getActivity();
                activity.update_frg_1(activity.blockList.get(position).getNumber());
                recover_msg(position);
            }
        });
        builder.show();
        return true;
    }

    private void recover_msg(int position) {
        ((BlacklistActivity) getActivity()).db.deleteBlock(((BlacklistActivity) getActivity()).blockList.get(position));
        ((BlacklistActivity) getActivity()).blockList.remove(((BlacklistActivity) getActivity()).blockList.get(position));
        blockAdapter.notifyDataSetChanged();
    }

    public void update_list_phone(String number) {
        BlacklistActivity myActivity = (BlacklistActivity) getActivity();
        for (int i = 0; i < myActivity.blockList.size(); i++) {
            if (myActivity.blockList.get(i).getNumber().equals(number)) {
                recover_msg(i);
                break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.blacklist, menu);
        menu.removeItem(R.id.action_search);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add_blacklist: // start settings activity
                final EditText editText = new EditText(getActivity());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                int maxLength = 11;
                editText.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(maxLength)
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getString(R.string.title_add_blacklit));
                builder.setMessage(getActivity().getString(R.string.title_add_blacklit_hint));
                builder.setView(editText);
                builder.setNegativeButton(android.R.string.no, null);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (!editText.getText().toString().equals("")) {
                            if (isDuplicated(editText.getText().toString())) {
                                Block block = new Block();
                                block.setNumber(editText.getText().toString());
                                ((BlacklistActivity) getActivity()).db.addBlock(block);
                                ((BlacklistActivity) getActivity()).blockList.add(block);
                                blockAdapter.notifyDataSetChanged();

                                if (isExits(editText.getText().toString()) != null) {
                                    Conversation a = isExits(editText.getText().toString());
                                    Fragment_Conv_Blacklist.conversationArrayList.clear();

                                    Fragment_Conv_Blacklist.conversationArrayList = getConvBlacklist();
                                    Fragment_Conv_Blacklist.fragmentBlacklistAdapter = new FragmentBlacklistAdapter
                                            (getActivity(), R.layout.conversationlist_item, Fragment_Conv_Blacklist.conversationArrayList);
                                    Fragment_Conv_Blacklist.listView.setAdapter(Fragment_Conv_Blacklist.fragmentBlacklistAdapter);
                                }
                            }
                            else Toast.makeText(getActivity(), getActivity().getString(R.string.db_isExits), Toast.LENGTH_SHORT).show();

                        } else
                            Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();

                    }
                });
                builder.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Update mailbox block when added.
    public Conversation isExits(String addr) {
        if (addr == null) {
            return null;
        }
        for (Conversation conv : getConv()) {
            if (conv.getContact().getNumber().equals(addr)) {
                return conv;
            }
        }
        return null;
    }

    public ArrayList<Conversation> getConv() {
        ArrayList<Conversation> conversationArrayList = new ArrayList<>();
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
                conversationArrayList.add(conv);
                c.moveToNext();
            }
            c.close();
        }
        return conversationArrayList;
    }

    public ArrayList<Conversation> getConvBlacklist(/*ArrayList<Conversation> convList*/) {
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

    public boolean isDuplicated( String number) {
        for (int i = 0; i <  ((BlacklistActivity) getActivity()).blockList.size(); i++) {
            if ( ((BlacklistActivity) getActivity()).blockList.get(i).getNumber().equals(number)) {
                return false;
            }
        }
        return true;
    }
}
