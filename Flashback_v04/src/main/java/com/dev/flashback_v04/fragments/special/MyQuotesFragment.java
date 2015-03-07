package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.MyQuotesAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2014-03-09.
 */
public class MyQuotesFragment extends ListFragment {

    public class GetQuotesTask extends AsyncTask<String, HashMap<String, String>, Boolean> {

        Callback mProgressUpdate;
        Callback mCallback;
        Parser mParser;
        Context mContext;

        public GetQuotesTask(Context context, Callback callback) {
            mCallback = callback;
            mContext = context;
            mParser = new Parser(mContext);

            mProgressUpdate = new Callback<HashMap<String, String>>() {
                @Override
                public void onTaskComplete(HashMap<String, String> data) {
                    publishProgress(data);
                }
            };
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            mParser.getMyQuotes(strings[0], mProgressUpdate);
            return null;
        }

        @Override
        protected void onProgressUpdate(HashMap<String, String>... values) {
            // Hand out data to Fragment
            mCallback.onTaskComplete(values[0]);
        }
    }

    private Activity mActivity;
    private MyQuotesAdapter myQuotesAdapter;
    private GetQuotesTask mGetQuotesTask;
    private Callback quoteFetched;
    private String mUserName;
    private int pageNumber;
    private int numPages;

    View.OnClickListener openForumListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("AdapterValues", myQuotesAdapter.getItems());
        outState.putInt("PageNumber", pageNumber);
        outState.putInt("NumPages", numPages);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        openForumListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String forumUrl = (String)view.getTag(R.id.OPEN_FORUM);
                String forumName = (String)view.getTag(R.id.OPEN_FORUM_NAME);
                ((MainActivity)mActivity).openForum(forumUrl, -1, forumName);
            }
        };

        myQuotesAdapter = new MyQuotesAdapter(mActivity, openForumListener);

        if(savedInstanceState != null) {
            // Restore adapter
            ArrayList<HashMap<String, String>> savedValues = (ArrayList<HashMap<String, String>>) savedInstanceState.get("AdapterValues");
            myQuotesAdapter.setItems(savedValues);
            myQuotesAdapter.notifyDataSetChanged();

        }
        setListAdapter(myQuotesAdapter);
        quoteFetched = new Callback<HashMap<String, String>>() {
            @Override
            public void onTaskComplete(HashMap<String, String> data) {
                myQuotesAdapter.addItem(data);
                myQuotesAdapter.notifyDataSetChanged();
            }
        };

		// Get username from preferences
		mUserName = PreferenceManager.getDefaultSharedPreferences(mActivity).getString("UserName", "");

		if(savedInstanceState == null) {
			pageNumber = getArguments().getInt("PageNumber");
			numPages = getArguments().getInt("NumPages");
			mGetQuotesTask = new GetQuotesTask(mActivity, quoteFetched);
			mGetQuotesTask.execute("https://www.flashback.org/sok/quote="+ mUserName +"+?sp=1&so=d&page=" + pageNumber);
		} else {
			pageNumber = savedInstanceState.getInt("PageNumber");
			numPages = savedInstanceState.getInt("NumPages");
		}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
        TextView header;
        TextView headerRight;
        String numPagesText;

        numPagesText = "Sida " + pageNumber + " av " + numPages;

        header = (TextView)view.findViewById(R.id.headerleft);
        headerRight = (TextView)view.findViewById(R.id.headerright);
        header.setText("Mina citerade inlägg");
        headerRight.setText(numPagesText);
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
		String threadUrl = (String)v.getTag(R.id.OPEN_THREAD);
		String threadName = (String)v.getTag(R.id.OPEN_THREAD_NAME);

		// -1 because the page is unknown from this type of url
		((MainActivity)mActivity).openThread(threadUrl, 0, 0, threadName);
    }
}
