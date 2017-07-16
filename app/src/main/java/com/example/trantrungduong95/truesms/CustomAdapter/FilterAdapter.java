package com.example.trantrungduong95.truesms.CustomAdapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Model.Filter;
import com.example.trantrungduong95.truesms.Presenter.SettingsOldActivity;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngomi_000 on 7/9/2017.
 */

public class FilterAdapter extends ArrayAdapter<Filter> {
    private Context context;
    private int layoutResourceId;
    private List<Filter> filters;
    private int type;
    //Used text size, color.
    private int textSize, textColor;

    public FilterAdapter(Context context, int layoutResourceId, List<Filter> filters ,int type) {
        super(context, layoutResourceId, filters);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.filters = filters;
        this.type = type;
        textSize = SettingsOldActivity.getTextsize(context);
        textColor = SettingsOldActivity.getTextcolor(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FilterHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FilterHolder();
            holder.content = (TextView)row.findViewById(R.id.custom_content_filterd);
            holder.stt = (TextView)row.findViewById(R.id.textView4);
            row.setTag(holder);
        }
        else
        {
            holder = (FilterHolder)row.getTag();
        }

        Filter filter = filters.get(position);

        if (textSize > 0) {
            holder.content.setTextSize(textSize);
        }
        int col = textColor;
        if (col != 0) {
            holder.content.setTextColor(col);
        }
        holder.stt.setText(String.valueOf(filter.getId_()));
        holder.content.setTextSize(24);
        if (type==0) {
            if (filter.getChar_()==null)
               row.setVisibility(View.GONE);
            else
                row.setVisibility(View.VISIBLE);
            holder.content.setText(filter.getChar_());

        }
        else if (type== -1) {
            if (filter.getWord_()==null)
               row.setVisibility(View.GONE);
            else
                row.setVisibility(View.VISIBLE);

            holder.content.setText(filter.getWord_());
        }
        else if (type == 1) {
            if (filter.getPharse_()==null)
               row.setVisibility(View.GONE);
            else
                row.setVisibility(View.VISIBLE);

            holder.content.setText(filter.getPharse_());
        }

        if (row.getVisibility() == View.VISIBLE){
            row.getLayoutParams().height = 0;
        }else{
            row.getLayoutParams().height = 1;
        }
        return row;
    }

    private static class FilterHolder
    {
        TextView content;
        TextView stt;
    }
}
