package com.example.trantrungduong95.truesms.Presenter.Fragment_;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.trantrungduong95.truesms.Presenter.SpamDB;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by ngomi_000 on 6/24/2017.
 */

public class Fragment_Blacklist extends Fragment implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private String[] BL;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);
        listView = (ListView) view.findViewById(R.id.conversations_list_blacklist);

        BL = SpamDB.getBlacklist(getActivity());

        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);

        adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, BL);
        listView.setAdapter(adapter);
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
                Object item = parent.getItemAtPosition(position);
                String value = item.toString();
                SpamDB.toggleBlacklist(getActivity(),value);
                //Todo remove String[]
                BL[position] ="";
                adapter.notifyDataSetChanged();
            }
        });
        builder.show();
        return true;
    }
}
