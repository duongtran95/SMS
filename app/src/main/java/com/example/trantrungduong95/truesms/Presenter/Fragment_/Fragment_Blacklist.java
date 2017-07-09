package com.example.trantrungduong95.truesms.Presenter.Fragment_;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
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
import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Presenter.SpamHandler;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ngomi_000 on 6/1/2017.
 */

public class Fragment_Blacklist extends Fragment implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{
    private ListView listView;
    private BlockAdapter blockAdapter;
    List<Block> blockList = new ArrayList<Block>();
    SpamHandler db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);
        setHasOptionsMenu(true);

        db = new SpamHandler(getActivity());
        //db.createDefaultBlocksIfNeed();

        blockList = db.getAllBlocks();

        listView = (ListView) view.findViewById(R.id.conversations_list_blacklist);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);

        blockAdapter = new BlockAdapter(getActivity(),R.layout.custom_block, blockList);
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
                db.deleteBlock(blockList.get(position));
                blockList = db.getAllBlocks();
                blockAdapter = new BlockAdapter(getActivity(),R.layout.custom_block, blockList);
                listView.setAdapter(blockAdapter);
            }
        });
        builder.show();
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.blacklist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add_blacklist: // start settings activity
                final EditText editText = new EditText(getActivity());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                int maxLength = 11;
                editText.setFilters(new InputFilter[] {
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

                            Block block = new Block();
                            block.setNumber(editText.getText().toString());
                            db.addBlock(block);

                            blockList = db.getAllBlocks();
                            blockAdapter = new BlockAdapter(getActivity(),R.layout.custom_block, blockList);
                            listView.setAdapter(blockAdapter);
                        }
                        else Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();

                    }
                });
                builder.show();
                return true;
            case R.id.item_restore_blacklist: // start settings activity
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setTitle(getActivity().getString(R.string.title_restore));
                builder1.setMessage(getActivity().getString(R.string.message_restore));
                builder1.setNegativeButton(android.R.string.no, null);
                builder1.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        db.deleteAllBlocks();

                        blockList = db.getAllBlocks();
                        blockAdapter = new BlockAdapter(getActivity(),R.layout.custom_block, blockList);
                        listView.setAdapter(blockAdapter);
                    }
                });
                builder1.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
