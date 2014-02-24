package com.dev.flashback_v04.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;


import com.dev.flashback_v04.GlobalHelper;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.SharedPrefs;
import com.dev.flashback_v04.interfaces.OnTaskComplete;
import com.dev.flashback_v04.Thread;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-20.
 */
public class ThreadsParserTask extends AsyncTask<String, String, ArrayList<Thread>> {

	Context mContext;
	OnTaskComplete showThreadsAdapter;
	ArrayList<Thread> mThreads;
	Parser mParser;

	public ThreadsParserTask(OnTaskComplete taskComplete, Context context) {
		mParser = new Parser(context);
		showThreadsAdapter = taskComplete;
		mContext = context;
		mThreads = new ArrayList<Thread>();
	}

	@Override
	protected ArrayList<Thread> doInBackground(String... strings) {
		try {
			mThreads = mParser.getForumContents(strings[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mThreads;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

	}

	@Override
	protected void onPostExecute(ArrayList<Thread> threads) {
		super.onPostExecute(threads);
        if(threads.isEmpty()) {
            //Toast.makeText(mContext, "Tror det blev något fel.\nInga trådar hittades...", Toast.LENGTH_SHORT).show();
        }
        GlobalHelper.getPagerAdapter().NUM_PAGES = SharedPrefs.getPreference(mContext, "forum_size", "size");
        GlobalHelper.getPagerAdapter().notifyDataSetChanged();

		showThreadsAdapter.updateThreads(mThreads);
	}
}
