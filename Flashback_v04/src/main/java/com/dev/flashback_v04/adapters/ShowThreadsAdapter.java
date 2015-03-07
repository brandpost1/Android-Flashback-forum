package com.dev.flashback_v04.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
 * Created by Viktor on 2013-06-17.
 */
public class ShowThreadsAdapter extends BaseAdapter {

	private static final int TYPE_THREAD = 0;
	private static final int TYPE_FORUM = 1;
	private float forumTitleTextSize;
	private float forumInfoTextSize;
	private float threadTitleTextSize;
	private float threadInfoTextSize;


	private LayoutInflater mInflater;

	// All threads on the current page
	private ArrayList<HashMap<String, String>> mThreads;
	// Any subforums that might be present
	private ArrayList<HashMap<String, String>> mForums;

	private Context mContext;
    private ArrayList<HashMap<String, String>> mItems;

    public ShowThreadsAdapter(Context context) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
        mForums = new ArrayList<HashMap<String, String>>();
        mThreads = new ArrayList<HashMap<String, String>>();

		// Get textsize values from preferences
		SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		forumTitleTextSize = Float.parseFloat(appPrefs.getString("forumname_fontsize", "18"));
		forumInfoTextSize = Float.parseFloat(appPrefs.getString("foruminfo_fontsize", "14"));
		threadTitleTextSize = Float.parseFloat(appPrefs.getString("threadtitle_fontsize", "18"));
		threadInfoTextSize = Float.parseFloat(appPrefs.getString("threadinfo_fontsize", "14"));
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
		ImageView lastPage = null;

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
		}
		switch (type) {
			case TYPE_FORUM:
				forumTitle = (TextView)view.findViewById(R.id.forumTitle);
				forumInfo = (TextView)view.findViewById(R.id.forumInfo);

				// Set fontsize
				forumTitle.setTextSize(forumTitleTextSize);
				forumInfo.setTextSize(forumInfoTextSize);

				// Set value
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
				lastPage = (ImageView)view.findViewById(R.id.thread_gotolastpage);

				// Set fontsize
				threadTitle.setTextSize(threadTitleTextSize);
				threadAuthor.setTextSize(threadInfoTextSize);
				threadViews.setTextSize(threadInfoTextSize);
				threadNumReplies.setTextSize(threadInfoTextSize);
				lastPost.setTextSize(threadInfoTextSize);

				// Set value
				((TextView)view.findViewById(R.id.authorText)).setTextSize(threadInfoTextSize);
				((TextView)view.findViewById(R.id.threadReplies)).setTextSize(threadInfoTextSize);
				((TextView)view.findViewById(R.id.threadViews)).setTextSize(threadInfoTextSize);


				final String url = getThreads().get(position).get("ThreadLink");
				final int numpages = Integer.parseInt(getThreads().get(position).get("ThreadNumPages"));
				final String threadname = getThreads().get(position).get("ThreadName");

				lastPage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Bundle args = new Bundle();
						args.putInt("LastPage", numpages);
						args.putString("Url", url);
						args.putString("ThreadName", threadname);

						((MainActivity)mContext).openThread(url, numpages, numpages, threadname);
					}
				});

                if(mThreads.get(position).get("ThreadSticky").equals("true")) {
                    pinned.setVisibility(View.VISIBLE);
                } else {
                    pinned.setVisibility(View.INVISIBLE);
                }

				if(mThreads.get(position).get("BoldTitle").equals("true")) {
					threadTitle.setTypeface(Typeface.DEFAULT_BOLD);
				} else {
					threadTitle.setTypeface(Typeface.DEFAULT);
				}

                if(mThreads.get(position).get("ThreadLocked").equals("true")) {
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
