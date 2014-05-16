package com.dev.flashback_v04.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dev.flashback_v04.R;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Viktor on 2013-06-17.
 */

public class ShowForumsAdapter extends BaseAdapter {

	static LayoutInflater mInflater;
    ArrayList<HashMap<String, String>> mItems;

	Context mContext;

	public ShowForumsAdapter(Context context) {
		mContext = context;
		mInflater = ((Activity)mContext).getLayoutInflater();
        mItems = new ArrayList<HashMap<String, String>>();
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int i) {
		return null;
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		TextView forumTitle;
		TextView forumInfo;
		if(view == null) {
			view = mInflater.inflate(R.layout.forum_item, null);
		}
		forumTitle = (TextView)view.findViewById(R.id.forumTitle);
		forumInfo = (TextView)view.findViewById(R.id.forumInfo);
		forumTitle.setText(mItems.get(i).get("ForumName"));
		forumInfo.setText(mItems.get(i).get("ForumInfo"));

        return view;
	}

    public List<HashMap<String, String>> getmItems() {
        return mItems;
    }

    public void addItem(HashMap<String, String> data) {
        mItems.add(data);
    }

    public void addItems(ArrayList<HashMap<String, String>> items) {
        mItems = items;
    }
}
