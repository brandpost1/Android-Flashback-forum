package com.dev.flashback_v04.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


import com.dev.flashback_v04.NewParser;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;

import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowForumsAdapter;

import com.dev.flashback_v04.interfaces.Callback;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowForumsFragment extends ListFragment {

    // Fetches category content
    public class ForumsParserTask extends AsyncTask<String, HashMap<String, String>, Boolean> {

        Context mContext;
        Callback mCallback;
        Callback<HashMap<String, String>> mProgressUpdate;
        Parser mParser;

        public ForumsParserTask(Context context, Callback callback) {
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
        protected void onProgressUpdate(HashMap<String, String>... data) {
            super.onProgressUpdate(data[0]);
            // Give back to fragment
            mCallback.onTaskComplete(data[0]);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                // Url, Callback
                //mParser.getCategoryContent(strings[0], mProgressUpdate);
				NewParser parser = new NewParser(mContext);
				parser.getForums(strings[0], mProgressUpdate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private Callback forumFetched;
	ShowForumsAdapter mAdapter;
	int selected_category;
	ArrayList<String> categories;
    ArrayList<String> categorynames;
	String category_url = null;
    Activity mActivity;
    ForumsParserTask forumsParserTask;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        mActivity = activity;
	}

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String url = mAdapter.getmItems().get(position).get("ForumLink");
        String forumname = mAdapter.getmItems().get(position).get("ForumName");

		((MainActivity)getParentFragment().getActivity()).openForum(url, forumname);
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("SavedCategories", categories);
        outState.putString("SavedUrl", category_url);
        outState.putInt("SavedSelected", selected_category);
        outState.putStringArrayList("CategoryNames", categorynames);
        outState.putSerializable("AdapterValues", (Serializable) mAdapter.getmItems());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ShowForumsAdapter(getActivity());

        if(savedInstanceState == null) {
            categories = getArguments().getStringArrayList("Categories");
            selected_category = getArguments().getInt("index");
            categorynames = getArguments().getStringArrayList("CategoryNames");
            category_url = categories.get(selected_category);

            forumFetched = new Callback<HashMap<String, String>>() {
                @Override
                public void onTaskComplete(HashMap<String, String> data) {
                    mAdapter.addItem(data);
                    mAdapter.notifyDataSetChanged();
                }
            };

            forumsParserTask = new ForumsParserTask(mActivity, forumFetched);
            forumsParserTask.execute(categories.get(selected_category));
        } else {
            // Restore adapter
            ArrayList<HashMap<String, String>> savedValues = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("AdapterValues");
            mAdapter.addItems(savedValues);
            mAdapter.notifyDataSetChanged();
            // Restore other
            categories = savedInstanceState.getStringArrayList("SavedCategories");
            category_url = savedInstanceState.getString("SavedUrl");
            selected_category = savedInstanceState.getInt("SavedSelected");
            categorynames = savedInstanceState.getStringArrayList("CategoryNames");
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);

        setListAdapter(mAdapter);

        String showthisname;
        try {
            showthisname = categorynames.get(selected_category);
        } catch (NullPointerException e) {
            showthisname = "Error-name";
            e.printStackTrace();
        }


        TextView header = (TextView)view.findViewById(R.id.headerleft);
        TextView headerright = (TextView)view.findViewById(R.id.headerright);
        header.setText(showthisname);
        headerright.setText("");
		return view;

	}
}
