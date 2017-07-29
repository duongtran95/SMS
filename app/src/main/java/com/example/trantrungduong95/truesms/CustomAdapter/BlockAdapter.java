package com.example.trantrungduong95.truesms.CustomAdapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Presenter.Activity_.SettingsOldActivity;
import com.example.trantrungduong95.truesms.R;

import java.util.List;

/**
 * Created by ngomi_000 on 7/9/2017.
 */

public class BlockAdapter extends ArrayAdapter<Block> {
    private Context context;
    private int layoutResourceId;
    private List<Block> blocks;
    //Used text size, color.
    private int textSize, textColor;

    public BlockAdapter(Context context, int layoutResourceId, List<Block> blocks) {
        super(context, layoutResourceId, blocks);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.blocks = blocks;
        textSize = SettingsOldActivity.getTextsize(context);
        textColor = SettingsOldActivity.getTextcolor(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        BlockHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new BlockHolder();
            holder.Number = (TextView)row.findViewById(R.id.custom_content_block);
            row.setTag(holder);
        }
        else
        {
            holder = (BlockHolder)row.getTag();
        }

        Block block = blocks.get(position);

        if (textSize > 0) {
            holder.Number.setTextSize(textSize);
        }
        int col = textColor;
        if (col != 0) {
            holder.Number.setTextColor(col);
        }
        holder.Number.setTextSize(24);
        holder.Number.setText(block.getNumber());
        return row;
    }

    private static class BlockHolder
    {
        TextView Number;
    }
}
