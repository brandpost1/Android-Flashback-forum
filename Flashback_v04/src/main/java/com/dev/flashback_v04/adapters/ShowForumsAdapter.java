package com.dev.flashback_v04.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dev.flashback_v04.Forum;
import com.dev.flashback_v04.Thread;
import com.dev.flashback_v04.Post;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.asynctasks.ForumsParserTask;
import com.dev.flashback_v04.interfaces.OnTaskComplete;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-17.
 */

public class ShowForumsAdapter extends BaseAdapter implements OnTaskComplete {

	static LayoutInflater mInflater;
	ArrayList<Forum> mForums;
	String category_url;
	ForumsParserTask mParser;

	Context mContext;

	public ShowForumsAdapter(Context context, String category_url) {
		this.category_url = category_url;
		this.mContext = context;
		this.mForums = new ArrayList<Forum>();
		if(this.mInflater == null) {

			//this.mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mInflater = ((Activity)mContext).getLayoutInflater();
		}
		mParser = new ForumsParserTask(this, mContext);
		mParser.execute(category_url);

	}

	@Override
	public int getCount() {
		return mForums.size();
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
		forumTitle.setText(mForums.get(i).getName());
		forumInfo.setText(mForums.get(i).getInfo());

        // Hackish solution. Textviews did not get their proper color from the theme after the theme had been changed
            SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean darkTheme = appPrefs.getBoolean("theme_preference", false);

            if(darkTheme) {
                //forumTitle.setTextColor(mContext.getResources().getColor(R.color.WhiteSmoke));
                //forumInfo.setTextColor(mContext.getResources().getColor(R.color.WhiteSmoke));
                forumTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
                forumInfo.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            } else {
                forumTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
                forumInfo.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
                //forumTitle.setTextColor(mContext.getResources().getColor(R.color.Black));
                //forumInfo.setTextColor(mContext.getResources().getColor(R.color.Black));
            }
        // End of hackish

        return view;
	}

	public void reload() {
		mParser = new ForumsParserTask(this, mContext);
		mParser.execute(category_url);
	}

	public ArrayList<Forum> getForums() {
		return mForums;

	}

	@Override
	public void updateForums(ArrayList<Forum> forums) {
		mForums = forums;
		notifyDataSetChanged();

	}

	@Override
	public void updateThreads(ArrayList<Thread> mThreads) {
		// Do nothing
	}

	@Override
	public void updatePosts(ArrayList<Post> mPosts) {
		// Do nothing
	}

    @Override
    public void updateSize(int size) {

    }
}
