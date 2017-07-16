package com.example.trantrungduong95.truesms.Presenter.Fragment_;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
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
import com.example.trantrungduong95.truesms.Model.Filter;
import com.example.trantrungduong95.truesms.Presenter.SpamHandler;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngomi_000 on 7/10/2017.
 */

public class Fragment_Filter extends Fragment implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
    private ListView listView;
    private FilterAdapter filterAdapter;
    List<Filter> filterList = new ArrayList<Filter>();
    SpamHandler db;
    Button btnChar, btnWord, btnPharse;
    int type =0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        setHasOptionsMenu(true);

        db = new SpamHandler(getActivity());
        db.createDefaultFilterIfNeed();
        filterList = db.getAllFilters();

        listView = (ListView) view.findViewById(R.id.list_filter);

        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        onclickButton(view);
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
        builder.setTitle(getActivity().getString(R.string.delete_filter));
        builder.setMessage(getActivity().getString(R.string.confirm_delete_filter));
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                db.deleteFilter(filterList.get(position));
                filterList.remove(filterList.get(position));
                filterAdapter.notifyDataSetChanged();
            }
        });
        builder.show();
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.filterd, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add_filter: // start settings activity
                return true;
            case R.id.item_char:
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
                            Filter filter = new Filter();
                            filter.setChar_(editText.getText().toString());
                            filter.setWord_(null);
                            filter.setPharse_(null);
                            long id = db.addFilter(filter);
                            filter.setId_((int) id);

                            filterList.add(filter);
                            filterAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                builder.show();
                return true;

            case R.id.item_word:
                final EditText editText1 = new EditText(getActivity());

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
                editText1.setFilters(new InputFilter[] {filter1,
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
                            Filter filter = new Filter();
                            filter.setWord_(editText1.getText().toString());
                            filter.setChar_(null);
                            filter.setPharse_(null);
                            long id = db.addFilter(filter);
                            filter.setId_((int) id);

                            filterList.add(filter);
                            filterAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                builder1.show();
                return true;

            case R.id.item_pharse:
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
                            Filter filter = new Filter();
                            filter.setPharse_(editText2.getText().toString());
                            filter.setChar_(null);
                            filter.setWord_(null);
                            long id = db.addFilter(filter);
                            filter.setId_((int) id);

                            filterList.add(filter);
                            filterAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.non_empty), Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                builder2.show();
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
                        filterAdapter.notifyDataSetChanged();

                    }
                });
                builder3.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
