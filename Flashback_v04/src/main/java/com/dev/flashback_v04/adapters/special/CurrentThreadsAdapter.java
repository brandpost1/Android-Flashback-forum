package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dev.flashback_v04.*;
import com.dev.flashback_v04.asynctasks.special.CurrentThreadsParserTask;
import com.dev.flashback_v04.interfaces.UpdateStuff;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-11-27.
 */
public class CurrentThreadsAdapter extends BaseAdapter implements UpdateStuff<ArrayList<Item_CurrentThreads>> {
    public static final int ITEM_HEADER = 0;
    public static final int ITEM_ROW = 1;
    private final LayoutInflater mInflater;
    ArrayList<Item_CurrentThreads> mItems;
    CurrentThreadsParserTask parserTask;


    public CurrentThreadsAdapter(Context context) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        parserTask = new CurrentThreadsParserTask(this, context);
        parserTask.execute("https://www.flashback.org/aktuella-amnen");
        mItems = new ArrayList<Item_CurrentThreads>();
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
        if(mItems.get(position).mType == ITEM_HEADER) {
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
        switch (mItems.get(position).mType) {
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


        int type = getItemViewType(i);

        if(view == null) {

            switch(type) {
                case ITEM_HEADER:
                    view = mInflater.inflate(R.layout.thread_current_divider, null);
                    header = (TextView)view.findViewById(R.id.divider_text);
                    header.setText(mItems.get(i).mHeadline);
                    break;
                case ITEM_ROW:
                    view = mInflater.inflate(R.layout.thread_current_item, null);
                    threadName = (TextView)view.findViewById(R.id.threadTitle);
                    views = (TextView)view.findViewById(R.id.viewCount);
                    replies = (TextView)view.findViewById(R.id.repliesCount);
                    readers = (TextView)view.findViewById(R.id.readerCount);
                    sourceForum = (TextView)view.findViewById(R.id.sourceForum);
                    threadName.setText(mItems.get(i).mHeadline);
                    views.setText(mItems.get(i).mViews);
                    replies.setText(mItems.get(i).mReplies);
                    readers.setText(mItems.get(i).mReaders);
                    sourceForum.setText(mItems.get(i).mSourceForum);
                    break;
            }
        }
        switch (type) {
            case ITEM_HEADER:
                header = (TextView)view.findViewById(R.id.divider_text);
                header.setText(mItems.get(i).mHeadline);
                break;
            case ITEM_ROW:
                threadName = (TextView)view.findViewById(R.id.threadTitle);
                views = (TextView)view.findViewById(R.id.viewCount);
                replies = (TextView)view.findViewById(R.id.repliesCount);
                readers = (TextView)view.findViewById(R.id.readerCount);
                sourceForum = (TextView)view.findViewById(R.id.sourceForum);
                threadName.setText(mItems.get(i).mHeadline);
                views.setText(mItems.get(i).mViews);
                replies.setText(mItems.get(i).mReplies);
                readers.setText(mItems.get(i).mReaders);
                sourceForum.setText(mItems.get(i).mSourceForum);
                break;
        }

        return view;
    }

    @Override
    public void updateThread(ArrayList<Item_CurrentThreads> o) {
        mItems = o;
        notifyDataSetChanged();
    }
}
