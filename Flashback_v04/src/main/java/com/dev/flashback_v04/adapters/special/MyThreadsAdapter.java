package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2014-03-14.
 */
public class MyThreadsAdapter extends BaseAdapter {

	private ArrayList<HashMap<String, String>> mItems;
	private Context mContext;
	private LayoutInflater mInflater;
	private int POSTS_PER_PAGE;

	public MyThreadsAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItems = new ArrayList<HashMap<String, String>>();
    }

    public ArrayList<HashMap<String, String>> getItems() {
        return mItems;
    }

	public void addItem(HashMap<String, String> item) {
		mItems.add(item);
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

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
		TextView threadTitle = null;
		TextView threadAuthor = null;
		TextView threadViews = null;
		TextView threadNumReplies = null;
		TextView lastPost = null;
		ImageView img = null;
		ImageView pinned = null;
		ImageView locked = null;
		ImageView lastPage = null;


		if(view == null) {
			view = mInflater.inflate(R.layout.my_threads_item, null);
		}
		threadTitle = (TextView)view.findViewById(R.id.threadTitle);
		threadAuthor = (TextView)view.findViewById(R.id.threadAuthor);
		threadViews = (TextView)view.findViewById(R.id.viewCount);
		threadNumReplies = (TextView)view.findViewById(R.id.postsCount);
		lastPost = (TextView)view.findViewById(R.id.lastPost);
		pinned = (ImageView)view.findViewById(R.id.pinned);
		locked = (ImageView)view.findViewById(R.id.locked);
		lastPage = (ImageView)view.findViewById(R.id.search_gotolastpage);

		final String url = mItems.get(position).get("ThreadLink");
		final String name = mItems.get(position).get("ThreadName");
		String postCount = mItems.get(position).get("ThreadNumReplies");

		SharedPreferences preferences = mContext.getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);
		POSTS_PER_PAGE = preferences.getInt("Thread_Max_Posts_Page", 12);

		final int pageCount = (int)(Math.ceil((Integer.parseInt(postCount.replaceAll("\\s+","")) + 1) / (float)POSTS_PER_PAGE));

		lastPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MainActivity)mContext).openThread(url, pageCount, pageCount, name);
			}
		});

		if(mItems.get(position).get("ThreadSticky").equals("True")) {
			pinned.setVisibility(View.VISIBLE);
		} else {
			pinned.setVisibility(View.INVISIBLE);
		}
		if(mItems.get(position).get("ThreadLocked").equals("True")) {
			locked.setVisibility(View.VISIBLE);
		} else {
			locked.setVisibility(View.INVISIBLE);
		}
		threadTitle.setText(mItems.get(position).get("ThreadName"));
		threadAuthor.setText(mItems.get(position).get("ThreadAuthor"));
		threadNumReplies.setText(mItems.get(position).get("ThreadNumReplies"));
		threadViews.setText(mItems.get(position).get("ThreadNumViews"));
		lastPost.setText(mItems.get(position).get("LastPost"));
        return view;
    }

    public void setItems(ArrayList<HashMap<String,String>> items) {
        mItems = items;
    }
}
