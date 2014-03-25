package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.flashback_v04.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2014-03-20.
 */
public class MySubsAdapter extends BaseAdapter {

	private final LayoutInflater mInflater;
	Context mContext;
	ArrayList<HashMap<String, String>> mItems;

	public MySubsAdapter(Context context) {

		mContext = context;
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItems = new ArrayList<HashMap<String, String>>();
	}

	public void addItem(HashMap<String, String> itemMap) {
		mItems.add(itemMap);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int i) {
		return mItems.get(i);
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	public ArrayList<HashMap<String, String>> getItems() {
		return mItems;
	}

	public void setItems(ArrayList<HashMap<String, String>> items) {
		mItems = items;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		TextView threadName;
		TextView author;
		TextView lastpost;
		TextView monitormode;

		if(view == null) {
			view = mInflater.inflate(R.layout.subs_item_thread, null);
		}
		threadName = (TextView)view.findViewById(R.id.threadTitle);
		author = (TextView)view.findViewById(R.id.threadAuthor);
		lastpost = (TextView)view.findViewById(R.id.lastPost);
		monitormode = (TextView)view.findViewById(R.id.monitoring);

		threadName.setText(mItems.get(i).get("Title"));
		author.setText(mItems.get(i).get("User"));
		lastpost.setText(mItems.get(i).get("LastPost"));
		monitormode.setText(mItems.get(i).get("Monitoring"));
		return view;
	}
}
