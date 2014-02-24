package com.dev.flashback_v04.asynctasks.special;

import android.content.Context;
import android.os.AsyncTask;


import com.dev.flashback_v04.Item_CurrentThreads;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.interfaces.UpdateStuff;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-20.
 */
public class CurrentThreadsParserTask extends AsyncTask<String, String, ArrayList<Item_CurrentThreads>> {

    Context mContext;

    ArrayList<Item_CurrentThreads> mThreads;
    UpdateStuff fetchComplete;
    Parser mParser;

    public CurrentThreadsParserTask(UpdateStuff fetchComplete, Context context) {
        mParser = new Parser(context);
        this.fetchComplete = fetchComplete;
        mContext = context;
        mThreads = new ArrayList<Item_CurrentThreads>();
    }

    @Override
    protected ArrayList<Item_CurrentThreads> doInBackground(String... strings) {
        try {
            mThreads = mParser.getCurrent(strings[0]);
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
    protected void onPostExecute(ArrayList<Item_CurrentThreads> threads) {
        super.onPostExecute(threads);
        fetchComplete.updateThread(threads);
    }
}