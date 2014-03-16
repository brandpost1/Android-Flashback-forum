package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dev.flashback_v04.R;
import com.dev.flashback_v04.interfaces.UpdateStuff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2013-11-27.
 */
public class NewPostsAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    ArrayList<HashMap<String, String>> mItems;

    public NewPostsAdapter(Context context) {
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


        if(view == null) {

            view = mInflater.inflate(R.layout.thread_current_item, null);
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
        }

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
