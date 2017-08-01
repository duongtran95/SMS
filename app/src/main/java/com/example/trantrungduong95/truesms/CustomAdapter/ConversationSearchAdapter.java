package com.example.trantrungduong95.truesms.CustomAdapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.Model.Search;
import com.example.trantrungduong95.truesms.Presenter.Activity_.SettingsOldActivity;
import com.example.trantrungduong95.truesms.R;

public class ConversationSearchAdapter extends ArrayAdapter<Search> implements Filterable {

	private List<Search> conversationList;
	private Context context;
	private Filter conversationFilter;
	private List<Search> origConversationList;

	private int textSize, textColor;

	public ConversationSearchAdapter(List<Search> conversationList, Context context) {
		super(context, R.layout.conversation_item, conversationList);
		this.conversationList = conversationList;
		this.context = context;
		this.origConversationList = conversationList;
		textSize = SettingsOldActivity.getTextsize(context);
		textColor = SettingsOldActivity.getTextcolor(context);
	}

	public int getCount() {
		return conversationList.size();
	}

	public Search getItem(int position) {
		return conversationList.get(position);
	}

	public long getItemId(int position) {
		return conversationList.get(position).hashCode();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		ConversationHolder holder = new ConversationHolder();

		// First let's verify the convertView is not null
		if (convertView == null) {
			// This a new view we inflate the new layout
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.custom_search, null);
			// Now we can fill the layout with the right values
			TextView nameSearch = (TextView) v.findViewById(R.id.name_search);
			TextView bodySearch = (TextView) v.findViewById(R.id.body_search);
			TextView textView3 = (TextView) v.findViewById(R.id.textView3);
			TextView textView5 = (TextView) v.findViewById(R.id.textView5);

			holder.conversationNameView = nameSearch;
			holder.bodyView = bodySearch;
			holder.textView3 = textView3;
			holder.textView5 = textView5;
			v.setTag(holder);
		}
		else
			holder = (ConversationHolder) v.getTag();

		Search p = conversationList.get(position);

		if (textSize > 0) {
			holder.conversationNameView.setTextSize(textSize);
			holder.bodyView.setTextSize(textSize);
		}
		int col = textColor;
		if (col != 0) {
			holder.conversationNameView.setTextColor(col);
			holder.textView3.setTextColor(col);
			holder.textView5.setTextColor(col);
			holder.bodyView.setTextColor(col);
		}

		if (p.getNum().matches("[-+]?\\d*\\.?\\d+"))
		{
			//True Only numbers
			holder.conversationNameView.setText(p.getNum());
			holder.textView3.setText(context.getString(R.string.nameSearch1));
		}
		else {
			holder.conversationNameView.setText(p.getNum());
			holder.textView3.setText(context.getString(R.string.nameSearch));
		}
		holder.bodyView.setText(p.getContent());
		return v;
	}

	public void resetData() {
		conversationList = origConversationList;
	}


	/* *********************************
	 * We use the holder pattern
	 * It makes the view faster and avoid finding the component
	 * **********************************/

	private static class ConversationHolder {
		TextView conversationNameView;
		TextView bodyView;
		TextView textView3;
		TextView textView5;
	}

	/*
	 * We create our filter	
	 */
	
	@Override
	public Filter getFilter() {
		if (conversationFilter == null)
			conversationFilter = new PlanetFilter();
		
		return conversationFilter;
	}


	private class PlanetFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			// We implement here the filter logic
			if (constraint == null || constraint.length() == 0) {
				// No filter implemented we return all the list
				results.values = conversationList;
				results.count = conversationList.size();
			}
			else {
				// We perform filtering operation
				List<Search> nPlanetList = new ArrayList<Search>();

				for (Search p : conversationList) {
					if (p.getContent().toLowerCase().startsWith(constraint.toString().toLowerCase()))
						nPlanetList.add(p);
				}
				results.values = nPlanetList;
				results.count = nPlanetList.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {

			// Now we have to inform the adapter about the new list filtered
			if (results.count == 0)
				notifyDataSetInvalidated();
			else {
				conversationList = (List<Search>) results.values;
				notifyDataSetChanged();
			}
		}
		
	}
}
