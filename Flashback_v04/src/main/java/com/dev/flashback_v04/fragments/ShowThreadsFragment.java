package com.dev.flashback_v04.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.*;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowThreadsAdapter;
import com.dev.flashback_v04.interfaces.Callback;
import com.dev.flashback_v04.interfaces.ErrorHandler;
import com.dev.flashback_v04.interfaces.OnOptionSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowThreadsFragment extends ListFragment {

	public class ContentParserTask extends AsyncTask<String, Object, String> {

		private final int TYPE_FORUM = 0;
		private final int TYPE_THREAD = 1;

        Context mContext;
        Callback forumfetched;
        Callback threadfetched;
        Callback<HashMap<String, String>> forumProgressUpdate;
        Callback<HashMap<String, String>> threadProgressUpdate;
        Parser mParser;
		ErrorHandler errorHandler;

        public ContentParserTask(Context context, Callback forumFetched, Callback threadFetched, ErrorHandler eHandler) {
            mParser = new Parser(context);
            mContext = context;
			forumfetched = forumFetched;
			threadfetched = threadFetched;
			errorHandler = eHandler;

			// Send these to parser
            forumProgressUpdate = new Callback<HashMap<String, String>>() {
                @Override
                public void onTaskComplete(HashMap<String, String> data) {
                    publishProgress(data, TYPE_FORUM);
                }
            };
			threadProgressUpdate = new Callback<HashMap<String, String>>() {
				@Override
				public void onTaskComplete(HashMap<String, String> data) {
					publishProgress(data, TYPE_THREAD);
				}
			};
        }

        @Override
        protected void onProgressUpdate(Object... data) {
            super.onProgressUpdate(data[0]);
			// Decide what type of data it is
			switch ((Integer)data[1]) {
				case TYPE_FORUM:
					// Return subforum
					forumfetched.onTaskComplete(data[0]);
					break;
				case TYPE_THREAD:
					// Return thread
					threadfetched.onTaskComplete(data[0]);
					break;
			}
        }

        @Override
        protected String doInBackground(String... strings) {
			String errorMessage = null;
            try {
                // Url, Callback
                errorMessage = mParser.getSubforumsAndThreads(strings[0], forumProgressUpdate, threadProgressUpdate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return errorMessage;
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

	private ContentParserTask contentParserTask;
    private Callback<HashMap<String, String>> threadFetched;
	private Callback<HashMap<String, String>> forumFetched;
	private int POSTS_PER_PAGE = 12;


	private ErrorHandler mErrorHandler;
	private boolean hasErrors;
	private String errorMessage;
	private RelativeLayout mErrorLayout;

	private ShowThreadsAdapter mAdapter;
	int selected_page;
	private String forum_url = null;
	private String base_url;
	private String forum_name;
	private String forum_id;
    int num_pages;
	private TextView header;
	private TextView headerright;

	private OnOptionSelectedListener mListener;
	private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.clear();
		mActivity.getMenuInflater().inflate(R.menu.forum_default_menu, menu);

		try {
			if(LoginHandler.loggedIn(mActivity) && mAdapter.getThreads().size() > 0)
				mActivity.getMenuInflater().inflate(R.menu.forum_loggedin_menu, menu);
		} catch(NullPointerException e) {
			e.printStackTrace();
		}

		// Set up search
		MenuItem searchItem = menu.findItem(R.id.forum_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String searchString) {
				int forumId = -1;
				try {
					forumId = Integer.parseInt(forum_id);
				} catch (NumberFormatException e) {}

				if(forumId != -1) {
					String query = "https://www.flashback.org/sok/" + searchString + "?f=" + forumId;
					query = query.replace(" ", "+");
					((MainActivity) mActivity).searchForum(query);
				} else {
					Toast.makeText(mActivity, "Hittade inte ForumId. Kunde inte söka.", Toast.LENGTH_SHORT).show();
				}
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				return false;
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.forum_new_thread:
                Bundle args = new Bundle();
                args.putString("ForumName", forum_name);
                args.putString("ForumId", forum_id);
                args.putString("ForumUrl", base_url);
                ((MainActivity)mActivity).onOptionSelected(item.getItemId(), args);
                break;
            case R.id.forum_update:
                Bundle bundle = new Bundle();
				bundle.putString("ForumUrl", base_url);
				bundle.putString("ForumName", forum_name);
				bundle.putInt("NumberOfPages", -1);
				((MainActivity)mActivity).onOptionSelected(item.getItemId(), bundle);
                break;
        }
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("BaseUrl", base_url);
        outState.putString("ForumUrl", forum_url);
        outState.putString("Forumid", forum_id);
        outState.putString("ForumName", forum_name);
        outState.putInt("SelectedPage", selected_page);
        outState.putInt("NumberOfPages", num_pages);
        outState.putSerializable("AdapterForumValues", mAdapter.getForums());
        outState.putSerializable("AdapterThreadValues", mAdapter.getThreads());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new ShowThreadsAdapter(mActivity);

		forumFetched = new Callback<HashMap<String, String>>() {
			@Override
			public void onTaskComplete(HashMap<String, String> data) {
				mAdapter.addForumItem(data);
				mAdapter.notifyDataSetChanged();
			}
		};

		threadFetched = new Callback<HashMap<String, String>>() {
			boolean invalidated = false;
			@Override
			public void onTaskComplete(HashMap<String, String> data) {
				mAdapter.addThreadItem(data);
				mAdapter.notifyDataSetChanged();
				if(!invalidated) {
					((MainActivity) mActivity).supportInvalidateOptionsMenu();
					invalidated = true;
				}
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

        if(savedInstanceState != null) {
            // Restore adapter
            ArrayList<HashMap<String, String>> savedForumValues = (ArrayList<HashMap<String, String>>) savedInstanceState.get("AdapterForumValues");
            ArrayList<HashMap<String, String>> savedThreadValues = (ArrayList<HashMap<String, String>>) savedInstanceState.get("AdapterThreadValues");
            mAdapter.addForumItems(savedForumValues);
            mAdapter.addThreadItems(savedThreadValues);
            mAdapter.notifyDataSetChanged();
            // Restore other
            base_url = savedInstanceState.getString("BaseUrl");
            forum_url = savedInstanceState.getString("ForumUrl");
            forum_id = savedInstanceState.getString("ForumId");
            forum_name = savedInstanceState.getString("ForumName");
            selected_page = savedInstanceState.getInt("SelectedPage");
            num_pages = savedInstanceState.getInt("NumberOfPages");
        } else {
            try {
                num_pages = getArguments().getInt("NumberOfPages");
                base_url = getArguments().getString("Url");
                forum_url = getArguments().getString("Url");
                forum_id = forum_url.split("f=", 2)[1];
                forum_name = getArguments().getString("ForumName");
                // +1 Because the pages start their index at 1. 0 works, but it just shows the first page of threads, so if you scrolled the next page would be the same as the first.
                selected_page = getArguments().getInt("index") + 1;
                forum_url = forum_url.concat("&page="+selected_page);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
			getContent();
        }
        setListAdapter(mAdapter);
    }

	private void showErrorBox() {
		if(mErrorLayout != null) {
			if(mErrorLayout.getVisibility() != View.VISIBLE) {
				final Button retrybutton = (Button) mErrorLayout.findViewById(R.id.retrybutton);
				retrybutton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mErrorLayout.setVisibility(View.GONE);
						mAdapter.getThreads().clear();
						mAdapter.getForums().clear();
						mAdapter.notifyDataSetChanged();
						getContent();
					}
				});
				mErrorLayout.setVisibility(View.VISIBLE);
				((TextView) mErrorLayout.findViewById(R.id.errorlayoutmessage)).setText(errorMessage);
			}
		} else {
			Toast.makeText(mActivity, errorMessage, Toast.LENGTH_SHORT).show();
		}
	}

	private void getContent() {
		contentParserTask = new ContentParserTask(mActivity, forumFetched, threadFetched, mErrorHandler);
		contentParserTask.execute(forum_url);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
        String showthisname;

        header = (TextView)view.findViewById(R.id.headerleft);
        headerright = (TextView)view.findViewById(R.id.headerright);
		mErrorLayout = (RelativeLayout)view.findViewById(R.id.errorlayout);

        try {
            showthisname = forum_name;
        } catch (NullPointerException e) {
            showthisname = "Error-name";
            e.printStackTrace();
        }

		if(hasErrors) {
			showErrorBox();
		}

        header.setText(showthisname);
		if(num_pages == 1) {
			headerright.setText("");
		} else {
			headerright.setText("Sida " + selected_page + " av " + num_pages);
		}

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(mAdapter.getForums().size() > position) {
			String url = mAdapter.getForums().get(position).get("ForumLink");
            String forumname = mAdapter.getForums().get(position).get("ForumName");
			int numThreads = Integer.parseInt(mAdapter.getForums().get(position).get("NumberOfThreads"));
			int numPages = (int)Math.ceil(numThreads / 50.0);

			((MainActivity)mActivity).openForum(url, -1, forumname);
		} else {
			String url = mAdapter.getThreads().get(position-mAdapter.getForums().size()).get("ThreadLink");
			String threadname = mAdapter.getThreads().get(position-mAdapter.getForums().size()).get("ThreadName");
			String numPosts = mAdapter.getThreads().get(position-mAdapter.getForums().size()).get("ThreadNumReplies");
			int pageCount = 1;

			SharedPreferences preferences = mActivity.getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);
			POSTS_PER_PAGE = preferences.getInt("Thread_Max_Posts_Page", 12);

			if(!numPosts.equals("-")) {
				pageCount = (int) (Math.ceil((Integer.parseInt(numPosts.replaceAll("\\s+", "")) + 1) / (float)POSTS_PER_PAGE));

				((MainActivity)mActivity).openThread(url, pageCount, 1, threadname);
			} else {
				((MainActivity)mActivity).openThread(url, 0, 1, threadname);
			}

		}
	}

}
