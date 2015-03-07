package com.dev.flashback_v04.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;

import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowForumsAdapter;

import com.dev.flashback_v04.interfaces.Callback;
import com.dev.flashback_v04.interfaces.ErrorHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowForumsFragment extends ListFragment {

    // Fetches category content
    private class ForumsParserTask extends AsyncTask<String, HashMap<String, String>, String> {

        Context mContext;
        Callback mCallback;
        Callback<HashMap<String, String>> mProgressUpdate;
		ErrorHandler mErrorHandler;

        Parser mParser;

        public ForumsParserTask(Context context, Callback callback, ErrorHandler errorHandler) {
            mParser = new Parser(context);
            mContext = context;
            mCallback = callback;

			mErrorHandler = errorHandler;

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
        protected String doInBackground(String... strings) {
			String response = null;
            try {
                // Url, Callback
                response = mParser.getCategoryContent(strings[0], mProgressUpdate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

		@Override
		protected void onPostExecute(String response) {
			super.onPostExecute(response);
			if(response != null) {
				mErrorHandler.handleError(response);
			} else {
				hasErrors = false;
			}
		}
	}

    private Callback forumFetched;
	private ErrorHandler mErrorHandler;
	private RelativeLayout mErrorLayout;

	private boolean hasErrors;
	private String errorMessage;

	private ShowForumsAdapter mAdapter;
	private int selected_category;
	private ArrayList<String> categories;
	private ArrayList<String> categorynames;
	private String category_url = null;
	private MainActivity mActivity;
    private ForumsParserTask forumsParserTask;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        mActivity = (MainActivity)activity;
	}

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String url = mAdapter.getmItems().get(position).get("ForumLink");
        String forumname = mAdapter.getmItems().get(position).get("ForumName");
		int numThreads = Integer.parseInt(mAdapter.getmItems().get(position).get("NumberOfThreads"));
		int numPages = (int)Math.ceil(numThreads / 50.0);

		mActivity.openForum(url, -1, forumname);
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
		outState.putBoolean("HasErrors", hasErrors);
		outState.putString("ErrorMessage", errorMessage);
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

		forumFetched = new Callback<HashMap<String, String>>() {
			@Override
			public void onTaskComplete(HashMap<String, String> data) {
				mAdapter.addItem(data);
				mAdapter.notifyDataSetChanged();
			}
		};

		mErrorHandler = new ErrorHandler() {
			@Override
			public void handleError(String errormessage) {
				hasErrors = true;
				errorMessage = errormessage;
				showErrorBox();
			}
		};

        if(savedInstanceState == null) {
            categories = getArguments().getStringArrayList("Categories");
            selected_category = getArguments().getInt("index");
            categorynames = getArguments().getStringArrayList("CategoryNames");
            category_url = categories.get(selected_category);

			getContent();
        } else {
            // Restore adapter
            ArrayList<HashMap<String, String>> savedValues = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("AdapterValues");
            mAdapter.addItems(savedValues);
            mAdapter.notifyDataSetChanged();
            // Restore other
			hasErrors = savedInstanceState.getBoolean("HasErrors");
			errorMessage = savedInstanceState.getString("ErrorMessage");
            categories = savedInstanceState.getStringArrayList("SavedCategories");
            category_url = savedInstanceState.getString("SavedUrl");
            selected_category = savedInstanceState.getInt("SavedSelected");
            categorynames = savedInstanceState.getStringArrayList("CategoryNames");
        }
    }

	private void getContent() {
		forumsParserTask = new ForumsParserTask(mActivity, forumFetched, mErrorHandler);
		forumsParserTask.execute(categories.get(selected_category));
	}

	private void showErrorBox() {
		if(mErrorLayout != null) {
			final Button retrybutton = (Button) mErrorLayout.findViewById(R.id.retrybutton);
			retrybutton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mErrorLayout.setVisibility(View.GONE);
					mAdapter.getmItems().clear();
					mAdapter.notifyDataSetChanged();
					getContent();
				}
			});
			mErrorLayout.setVisibility(View.VISIBLE);
			((TextView) mErrorLayout.findViewById(R.id.errorlayoutmessage)).setText(errorMessage);
		} else {
			Toast.makeText(mActivity, errorMessage, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
		view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
		mErrorLayout = (RelativeLayout)view.findViewById(R.id.errorlayout);

		setListAdapter(mAdapter);

		String showthisname;
		try {
			showthisname = categorynames.get(selected_category);
		} catch (NullPointerException e) {
			showthisname = "Error-name";
			e.printStackTrace();
		}

		if(hasErrors) {
			showErrorBox();
		}

		TextView header = (TextView) view.findViewById(R.id.headerleft);
		TextView headerright = (TextView) view.findViewById(R.id.headerright);
		header.setText(showthisname);
		headerright.setText("");

		return view;

	}
}
