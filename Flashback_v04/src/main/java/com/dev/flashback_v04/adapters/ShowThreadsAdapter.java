package com.dev.flashback_v04.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.dev.flashback_v04.Forum;
import com.dev.flashback_v04.Thread;
import com.dev.flashback_v04.Post;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.asynctasks.ForumsParserTask;
import com.dev.flashback_v04.asynctasks.ThreadsParserTask;
import com.dev.flashback_v04.interfaces.OnTaskComplete;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Viktor on 2013-06-17.
 */
public class ShowThreadsAdapter extends BaseAdapter implements OnTaskComplete {

	private static final int TYPE_THREAD = 0;
	private static final int TYPE_FORUM = 1;

    public Boolean updatedthreads = false;
    public Boolean updatedforums = false;

	LayoutInflater mInflater;

	// Base url
	String baseUrl;
	// Forum url
	String url;
	// The page currently being displayed
	int currentPage = 1;
	// Cached pages
	HashMap<Integer, ArrayList<Thread>> mHashMap;
	// All threads on the current page
	ArrayList<Thread> mThreads;
	// Any subforums that might be present
	ArrayList<Forum> mForums;

	ForumsParserTask mForumsParserTask;
	ThreadsParserTask mThreadsParserTask;
	Context mContext;

	public ShowThreadsAdapter(Context context, String forum_url) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		url = forum_url;
		baseUrl = forum_url;
		mContext = context;
		mHashMap = new HashMap<Integer, ArrayList<Thread>>();

		mForums = new ArrayList<Forum>();
		mThreads = new ArrayList<Thread>();

		mForumsParserTask = new ForumsParserTask(this, mContext);
		mThreadsParserTask = new ThreadsParserTask(this, mContext);
		mForumsParserTask.execute(url);
		mThreadsParserTask.execute(url);

	}

	@Override
	public int getCount() {
		int forums = mForums.size();
		int threads = mThreads.size();
		int total = forums+threads;
		return total;
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
		return (int)2;
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

				forumTitle.setText(mForums.get(position).getName());
				forumInfo.setText(mForums.get(position).getInfo());
				break;
			case TYPE_THREAD:
				threadTitle = (TextView)view.findViewById(R.id.threadTitle);
				threadAuthor = (TextView)view.findViewById(R.id.threadAuthor);
				threadViews = (TextView)view.findViewById(R.id.viewCount);
				threadNumReplies = (TextView)view.findViewById(R.id.postsCount);
                lastPost = (TextView)view.findViewById(R.id.lastPost);
                pinned = (ImageView)view.findViewById(R.id.pinned);
                locked = (ImageView)view.findViewById(R.id.locked);

                if(mThreads.get(position).getSticky()) {
                    pinned.setVisibility(View.VISIBLE);
                } else {
                    pinned.setVisibility(View.INVISIBLE);
                }
                if(mThreads.get(position).getLocked()) {
                    locked.setVisibility(View.VISIBLE);
                } else {
                    locked.setVisibility(View.INVISIBLE);
                }
				threadTitle.setText(mThreads.get(position).getThreadName());
				threadAuthor.setText(mThreads.get(position).getThreadAuthor());
				threadNumReplies.setText(mThreads.get(position).getThreadReplies());
				threadViews.setText(mThreads.get(position).getThreadViews());
                lastPost.setText(mThreads.get(position).getLastPost());
				break;
		}

		return view;
	}

	public void updateCurrentPage() {
		String updateUrl = this.url;

		if(currentPage == 1) {
			mForumsParserTask = new ForumsParserTask(this, mContext);
			mThreadsParserTask = new ThreadsParserTask(this, mContext);
			mForumsParserTask.execute(baseUrl);
			mThreadsParserTask.execute(baseUrl);

		} else {
			updateUrl = baseUrl.concat("p"+currentPage);
			mThreadsParserTask = new ThreadsParserTask(this, mContext);
			mThreadsParserTask.execute(updateUrl);
		}
		if(mHashMap.containsKey(Integer.valueOf(currentPage))) {
			mHashMap.remove(Integer.valueOf(currentPage));
			mHashMap.put(Integer.valueOf(currentPage), mThreads);
		}

	}

	@Override
	public void updateForums(ArrayList<Forum> forums) {

		mForums = forums;
        ((MainActivity)mContext).supportInvalidateOptionsMenu();
		notifyDataSetChanged();
        updatedforums = true;
	}

	@Override
	public void updateThreads(ArrayList<Thread> threads) {

		mThreads = threads;
		notifyDataSetChanged();
        updatedthreads = true;
	}

	@Override
	public void updatePosts(ArrayList<Post> mPosts) {
		// Do nothing
	}

    @Override
    public void updateSize(int size) {

    }

    public ArrayList<Forum> getForums() {
		return mForums;
	}

	public ArrayList<Thread> getThreads() {
		return mThreads;
	}
}
