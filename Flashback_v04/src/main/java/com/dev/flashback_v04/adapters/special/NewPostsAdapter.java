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
 * Created by Viktor on 2013-11-27.
 */
public class NewPostsAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    ArrayList<HashMap<String, String>> mItems;
	private Context mContext;
	private int POSTS_PER_PAGE;

	public NewPostsAdapter(Context context) {
		mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItems = new ArrayList<HashMap<String, String>>();
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
    public int getViewTypeCount() {
        return 1;
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
		ImageView lastPage = null;


        if(view == null) {
            view = mInflater.inflate(R.layout.thread_current_item, null);
        }

		lastPage = (ImageView)view.findViewById(R.id.current_gotolastpage);
		final String name = mItems.get(i).get("Headline");
		final String url = mItems.get(i).get("Link");
		String postCount = mItems.get(i).get("Replies");

		SharedPreferences preferences = mContext.getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);
		POSTS_PER_PAGE = preferences.getInt("Thread_Max_Posts_Page", 12);

		final int pageCount = (int)(Math.ceil((Integer.parseInt(postCount.replaceAll("\\s+","")) + 1) / (float)POSTS_PER_PAGE));
		lastPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MainActivity)mContext).openThread(url, pageCount, pageCount, name);
			}
		});

        threadName = (TextView)view.findViewById(R.id.threadTitle);
        views = (TextView)view.findViewById(R.id.viewCount);
        replies = (TextView)view.findViewById(R.id.repliesCount);
        readers = (TextView)view.findViewById(R.id.readerCount);
        sourceForum = (TextView)view.findViewById(R.id.sourceForum);
        threadName.setText(mItems.get(i).get("Headline"));
        views.setText(mItems.get(i).get("Views"));
        replies.setText(mItems.get(i).get("Replies"));
        readers.setText(mItems.get(i).get("Readers"));
        sourceForum.setText(mItems.get(i).get("SourceForum"));

        return view;
    }

    public void putItem(HashMap<String, String> data) {
        mItems.add(data);
    }

    public void putItems(ArrayList<HashMap<String, String>> items) {
        mItems = items;
    }

    public ArrayList<HashMap<String, String>> getItems() {
        return mItems;
    }
}
