package com.dev.flashback_v04.adapters;

import android.content.Context;
import android.graphics.Color;
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
 * Created by Viktor on 2013-06-17.
 */
public class ShowThreadsAdapter extends BaseAdapter {

	private static final int TYPE_THREAD = 0;
	private static final int TYPE_FORUM = 1;

	LayoutInflater mInflater;

	// All threads on the current page
	ArrayList<HashMap<String, String>> mThreads;
	// Any subforums that might be present
	ArrayList<HashMap<String, String>> mForums;

	Context mContext;
    private ArrayList<HashMap<String, String>> mItems;

    public ShowThreadsAdapter(Context context) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
        mForums = new ArrayList<HashMap<String, String>>();
        mThreads = new ArrayList<HashMap<String, String>>();
	}

	@Override
	public int getCount() {
		return mForums.size() + mThreads.size();
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
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if(mForums.size() > position) {
			return TYPE_FORUM;
		} else {
			return TYPE_THREAD;
		}
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		TextView forumTitle = null;
		TextView forumInfo = null;
		TextView threadTitle = null;
		TextView threadAuthor = null;
		TextView threadViews = null;
		TextView threadNumReplies = null;
        TextView lastPost = null;
        ImageView img = null;
        ImageView pinned = null;
        ImageView locked = null;

		int type = getItemViewType(position);

		// Compensate for having two datasets. After it has counted to the number of forums, reset the counter to zero and count from there.
		if(position >= mForums.size())
			position = position-mForums.size();

		if(view == null) {
			switch (type) {
				case TYPE_FORUM:
					view = mInflater.inflate(R.layout.forum_item, null);
					break;
				case TYPE_THREAD:
					view = mInflater.inflate(R.layout.thread_item, null);
					break;
			}
            //TODO: This can change the sidebar-color for threads depending on if it's locked/pinned/etc
            img = (ImageView)view.findViewById(R.id.imageView);
            img.setBackgroundColor(Color.TRANSPARENT);
		}
		switch (type) {
			case TYPE_FORUM:
				forumTitle = (TextView)view.findViewById(R.id.forumTitle);
				forumInfo = (TextView)view.findViewById(R.id.forumInfo);

				forumTitle.setText(mForums.get(position).get("ForumName"));
				forumInfo.setText(mForums.get(position).get("ForumInfo"));
				break;
			case TYPE_THREAD:
				threadTitle = (TextView)view.findViewById(R.id.threadTitle);
				threadAuthor = (TextView)view.findViewById(R.id.threadAuthor);
				threadViews = (TextView)view.findViewById(R.id.viewCount);
				threadNumReplies = (TextView)view.findViewById(R.id.postsCount);
                lastPost = (TextView)view.findViewById(R.id.lastPost);
                pinned = (ImageView)view.findViewById(R.id.pinned);
                locked = (ImageView)view.findViewById(R.id.locked);

                if(mThreads.get(position).get("ThreadSticky").equals("True")) {
                    pinned.setVisibility(View.VISIBLE);
                } else {
                    pinned.setVisibility(View.INVISIBLE);
                }
                if(mThreads.get(position).get("ThreadLocked").equals("True")) {
                    locked.setVisibility(View.VISIBLE);
                } else {
                    locked.setVisibility(View.INVISIBLE);
                }
				threadTitle.setText(mThreads.get(position).get("ThreadName"));
				threadAuthor.setText(mThreads.get(position).get("ThreadAuthor"));
				threadNumReplies.setText(mThreads.get(position).get("ThreadNumReplies"));
				threadViews.setText(mThreads.get(position).get("ThreadNumViews"));
                lastPost.setText(mThreads.get(position).get("LastPost"));
				break;
		}

		return view;
	}

    public ArrayList<HashMap<String, String>> getForums() {
		return mForums;
	}

	public ArrayList<HashMap<String, String>> getThreads() {
		return mThreads;
	}

    public void addForumItems(ArrayList<HashMap<String, String>> items) {
        mForums = items;
    }

    public void addThreadItems(ArrayList<HashMap<String, String>> items) {
        mThreads = items;
    }

    public void addThreadItem(HashMap<String, String> data) {
        mThreads.add(data);
    }

    public void addForumItem(HashMap<String, String> data) {
        mForums.add(data);
    }
}
