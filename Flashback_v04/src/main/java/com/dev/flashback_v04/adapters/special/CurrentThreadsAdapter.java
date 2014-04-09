package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.flashback_v04.*;
import com.dev.flashback_v04.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2013-11-27.
 */
public class CurrentThreadsAdapter extends BaseAdapter {
    public static final int ITEM_HEADER = 0;
    public static final int ITEM_ROW = 1;
    private final LayoutInflater mInflater;
    ArrayList<HashMap<String, String>> mItems;
	private Context mContext;

    public CurrentThreadsAdapter(Context context) {
		mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItems = new ArrayList<HashMap<String, String>>();
		System.out.println("Currentthreadsadapter: ");
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
    public boolean isEnabled(int position) {
        if(mItems.get(position).get("Type").equals(Integer.toString(ITEM_HEADER))) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        switch (Integer.parseInt(mItems.get(position).get("Type"))) {
            case ITEM_HEADER:
                return ITEM_HEADER;
            case ITEM_ROW:
                return ITEM_ROW;
        }
        return -1;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView header = null;
        TextView threadName = null;
        TextView views = null;
        TextView readers = null;
        TextView replies = null;
        TextView sourceForum = null;
		ImageView openLastPage = null;


        int type = getItemViewType(i);

        if(view == null) {

            switch(type) {
                case ITEM_HEADER:
                    view = mInflater.inflate(R.layout.thread_current_divider, null);
                    break;
                case ITEM_ROW:
                    view = mInflater.inflate(R.layout.thread_current_item, null);
                    break;
            }
        }
        switch (type) {
            case ITEM_HEADER:
                header = (TextView)view.findViewById(R.id.divider_text);
                header.setText(mItems.get(i).get("Headline"));
                break;
            case ITEM_ROW:
                threadName = (TextView)view.findViewById(R.id.threadTitle);
                views = (TextView)view.findViewById(R.id.viewCount);
                replies = (TextView)view.findViewById(R.id.repliesCount);
                readers = (TextView)view.findViewById(R.id.readerCount);
                sourceForum = (TextView)view.findViewById(R.id.sourceForum);
				openLastPage = (ImageView)view.findViewById(R.id.current_gotolastpage);

				final String threadname = ((HashMap<String, String>)getItem(i)).get("Headline");
				final String url = ((HashMap<String, String>)getItem(i)).get("Link");

				openLastPage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Bundle args = new Bundle();
						args.putInt("LastPage", -2);
						args.putString("Url", url);
						args.putString("ThreadName", threadname);
						((MainActivity)mContext).onOptionSelected(R.id.gotolastpage, args);
					}
				});

                threadName.setText(mItems.get(i).get("Headline"));
                views.setText(mItems.get(i).get("Views"));
                replies.setText(mItems.get(i).get("Replies"));
                readers.setText(mItems.get(i).get("Readers"));
                sourceForum.setText(mItems.get(i).get("SourceForum"));
                break;
        }

        return view;
    }

    public ArrayList<HashMap<String, String>> getItems() {
        return mItems;
    }

    public void putItems(ArrayList<HashMap<String, String>> items) {
        mItems = items;
    }

    public void putItem(HashMap<String, String> data) {
        mItems.add(data);
    }
}
