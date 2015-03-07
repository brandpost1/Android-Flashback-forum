package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.preference.PreferenceManager;
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
 * Created by Viktor on 2014-03-19.
 */
public class PrivateMessagesAdapter extends BaseAdapter {

	private final int ITEM_DIVIDER = 0;
	private final int ITEM = 1;

	boolean darkTheme;

	private final LayoutInflater mInflater;
	Context mContext;
	ArrayList<HashMap<String, String>> mItems;

	public PrivateMessagesAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItems = new ArrayList<HashMap<String, String>>();
		darkTheme = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("theme_preference", false);
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

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if(mItems.get(position).get("ItemType").equals("Divider")) {
			return ITEM_DIVIDER;
		} else {
			return ITEM;
		}
	}

	public void setItems(ArrayList<HashMap<String, String>> items) {
		mItems = items;
	}

	@Override
	public boolean isEnabled(int position) {
		if(mItems.get(position).get("ItemType").equals("Divider")) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		int type = getItemViewType(i);

		if(view == null) {
			switch (type) {
				case ITEM_DIVIDER:
					view = mInflater.inflate(R.layout.privatemessages_divider, null);
					break;
				case ITEM:
					view = mInflater.inflate(R.layout.privatemessages_item, null);
					break;
			}
		}

		switch (type) {
			case ITEM_DIVIDER:
				TextView categoryName = (TextView)view.findViewById(R.id.divider_text);
				categoryName.setText(mItems.get(i).get("CategoryName"));
				break;
			case ITEM:
				TextView itemname = (TextView)view.findViewById(R.id.header_text);
				TextView from = (TextView)view.findViewById(R.id.from_text);
				TextView date = (TextView)view.findViewById(R.id.date);
				TextView time = (TextView)view.findViewById(R.id.time);
				ImageView icon = (ImageView)view.findViewById(R.id.status_image);

				itemname.setText(mItems.get(i).get("Headline"));
				from.setText(mItems.get(i).get("From"));
				date.setText(mItems.get(i).get("Date"));
				time.setText(mItems.get(i).get("Time"));


				String iconString = mItems.get(i).get("Icon");
				if(iconString.contains("icon-pm-replied")) {
					if(darkTheme) {
						icon.setBackgroundResource(R.drawable.ic_action_reply_dark);
					} else {
						icon.setBackgroundResource(R.drawable.ic_action_reply);
					}
				} else if(iconString.contains("icon-pm-old")) {
					if(darkTheme) {
						icon.setBackgroundResource(R.drawable.ic_action_read_dark);
					} else {
						icon.setBackgroundResource(R.drawable.ic_action_read);
					}
				} else if(iconString.contains("icon-pm-new")) {
					if(darkTheme) {
						icon.setBackgroundResource(R.drawable.ic_action_unread_dark);
					} else {
						icon.setBackgroundResource(R.drawable.ic_action_unread);
					}
				} else if(iconString.contains("icon-pm-forwarded")) {
					if(darkTheme) {
						icon.setBackgroundResource(R.drawable.ic_action_forward_dark);
					} else {
						icon.setBackgroundResource(R.drawable.ic_action_forward);
					}
				}

				break;
		}

		return view;
	}

	public void clearListData() {
		mItems.clear();
	}
}
