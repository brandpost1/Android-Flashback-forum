package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.NewThreadsAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Viktor on 2013-11-14.
 */
public class NewThreadsFragment extends ListFragment {

    public class NewThreadsParserTask extends AsyncTask<String, HashMap<String, String>, Boolean> {

        Context mContext;
        Parser mParser;
        Callback mCallback;
        Callback mProgressUpdate;

        public NewThreadsParserTask(Context context, Callback callback) {
            mParser = new Parser(context);
            mContext = context;
            mCallback = callback;

            mProgressUpdate = new Callback<HashMap<String, String>>() {
                @Override
                public void onTaskComplete(HashMap<String, String> data) {
                    publishProgress(data);
                }
            };
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                mParser.getNewThreads(strings[0], mProgressUpdate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(HashMap<String, String>... values) {
            super.onProgressUpdate(values);
            mCallback.onTaskComplete(values[0]);
        }
    }


    private NewThreadsAdapter mAdapter;
    private NewThreadsParserTask threadsParserTask;
    private Callback threadFetched;
    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
        TextView header;
        TextView headerright;
        header = (TextView)view.findViewById(R.id.headerleft);
        headerright = (TextView)view.findViewById(R.id.headerright);
        header.setText("Nya ämnen");
        headerright.setText("");
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("AdapterValues", mAdapter.getItems());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new NewThreadsAdapter(getActivity());

        if(savedInstanceState == null) {
            threadFetched = new Callback<HashMap<String, String>>() {
                @Override
                public void onTaskComplete(HashMap<String, String> data) {
                    mAdapter.putItem(data);
                    mAdapter.notifyDataSetChanged();
                }
            };
            threadsParserTask = new NewThreadsParserTask(mActivity, threadFetched);
            threadsParserTask.execute("https://www.flashback.org/nya-amnen");
        } else {
            // Restore adapter
            ArrayList<HashMap<String, String>> items = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("AdapterValues");
            mAdapter.putItems(items);
            mAdapter.notifyDataSetChanged();
        }
        setListAdapter(mAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        SharedPreferences sessionPrefs;
        int posts_per_page;
        String total_thread_posts;
        int replies;
        int numpages;
        String threadname;
        String url;

        try {
            sessionPrefs = (getActivity()).getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);
            posts_per_page = sessionPrefs.getInt("Thread_Max_Posts_Page", 12);
            total_thread_posts = ((HashMap<String, String>)mAdapter.getItem(position)).get("Replies");

            total_thread_posts = total_thread_posts.replace("\u00A0","");

            replies = Integer.parseInt(total_thread_posts.replace(" ", "").toString());
            threadname = ((HashMap<String, String>)mAdapter.getItem(position)).get("Headline");

            numpages = (int) Math.ceil(replies / posts_per_page) + 1;
            url = ((HashMap<String, String>)mAdapter.getItem(position)).get("Link");

            ((MainActivity)getActivity()).openThread(url, 0, threadname);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
