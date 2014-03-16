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
import java.util.List;
import java.util.Map;

/**
 * Created by Viktor on 2014-03-11.
 */
public class MyQuotesAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    Context mContext;
    ArrayList<HashMap<String, String>> mItems;
    View.OnClickListener openForumListener;

    public MyQuotesAdapter(Context context, View.OnClickListener openForum) {
        openForumListener = openForum;

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
        TextView userName;
        TextView forumName;
        TextView date;
        TextView message;
        ImageView openForum;

        if(view == null) {
            view = mInflater.inflate(R.layout.my_posts_item, null);
        }
        threadName = (TextView)view.findViewById(R.id.thread_name);
        userName = (TextView)view.findViewById(R.id.user_name);
        forumName = (TextView)view.findViewById(R.id.forum_name);
        date = (TextView)view.findViewById(R.id.post_date);
        message = (TextView)view.findViewById(R.id.post_text);
        openForum = (ImageView)view.findViewById(R.id.open_forum);

        openForum.setOnClickListener(openForumListener);

        threadName.setText(mItems.get(i).get("ThreadTitle"));
        userName.setText(mItems.get(i).get("PostedBy"));
        forumName.setText(mItems.get(i).get("ForumName"));
        date.setText(mItems.get(i).get("Date"));
        message.setText(mItems.get(i).get("MessageText"));

        openForum.setTag(R.id.OPEN_FORUM, mItems.get(i).get("ForumLink"));
        openForum.setTag(R.id.OPEN_FORUM_NAME, mItems.get(i).get("ForumName"));

		view.setTag(R.id.OPEN_THREAD, mItems.get(i).get("ThreadLink"));
		view.setTag(R.id.OPEN_THREAD_NAME, mItems.get(i).get("ThreadTitle"));

        return view;
    }
}
