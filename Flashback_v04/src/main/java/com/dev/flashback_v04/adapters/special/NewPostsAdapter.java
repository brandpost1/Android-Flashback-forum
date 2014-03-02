package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dev.flashback_v04.Item_CurrentThreads;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.asynctasks.special.NewThreadsParserTask;
import com.dev.flashback_v04.interfaces.UpdateStuff;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-11-27.
 */
public class NewPostsAdapter extends BaseAdapter implements UpdateStuff<ArrayList<Item_CurrentThreads>> {

    private final LayoutInflater mInflater;
    ArrayList<Item_CurrentThreads> mItems;
    NewThreadsParserTask parserTask;


    public NewPostsAdapter(Context context) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        parserTask = new NewThreadsParserTask(this, context);
        parserTask.execute("https://www.flashback.org/nya-inlagg");
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
            threadName.setText(mItems.get(i).mHeadline);
            views.setText(mItems.get(i).mViews);
            replies.setText(mItems.get(i).mReplies);
            readers.setText(mItems.get(i).mReaders);
            sourceForum.setText(mItems.get(i).mSourceForum);

        }

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

        return view;
    }

    @Override
    public void updateThread(ArrayList<Item_CurrentThreads> o) {
        mItems = o;
        notifyDataSetChanged();
    }
}
