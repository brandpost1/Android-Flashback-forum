package com.dev.flashback_v04.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.*;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowThreadsAdapter;
import com.dev.flashback_v04.interfaces.Callback;
import com.dev.flashback_v04.interfaces.OnOptionSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowThreadsFragment extends ListFragment {

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

    public class ThreadsParserTask extends AsyncTask<String, HashMap<String, String>, Boolean> {

        Context mContext;
        Parser mParser;
        Callback mCallback;
        Callback mProgressUpdate;

        public ThreadsParserTask(Context context, Callback callback) {
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
                //mParser.getForumContents(strings[0], mProgressUpdate);
				NewParser parser = new NewParser(mContext);
				parser.getThreads(strings[0], mProgressUpdate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(HashMap<String, String>... threadItem) {
            super.onProgressUpdate(threadItem[0]);
            mCallback.onTaskComplete(threadItem[0]);
        }
    }

    ThreadsParserTask threadsParserTask;
    ForumsParserTask forumsParserTask;
    private Callback<HashMap<String, String>> threadFetched;
    Callback<HashMap<String, String>> forumFetched;

	ShowThreadsAdapter mAdapter;
	int selected_page;
	String forum_url = null;
    String base_url;
    String forum_name;
    String forum_id;
    int num_pages;
    TextView header;
    TextView headerright;

    OnOptionSelectedListener mListener;
    Activity mActivity;
    Menu menu;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)mActivity).supportInvalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)mActivity).supportInvalidateOptionsMenu();
    }
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        menu.clear();
		inflater.inflate(R.menu.forum_default_menu, menu);

        try {
            if(LoginHandler.loggedIn(mActivity) && mAdapter.getThreads().size() > 0)
                inflater.inflate(R.menu.forum_loggedin_menu, menu);
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

		// Set up search
		MenuItem searchItem = menu.findItem(R.id.thread_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String searchString) {
				int forumId = Integer.parseInt(forum_id);
				String query = "https://www.flashback.org/sok/"+ searchString +"?f=" + forumId;
				query = query.replace(" ", "+");
				((MainActivity)mActivity).searchForum(query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				return false;
			}
		});

		super.onCreateOptionsMenu(menu, inflater);
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
                Toast.makeText(mActivity,"Ej implementerad än", Toast.LENGTH_SHORT ).show();
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
        outState.putInt("NumPages", num_pages);
        outState.putSerializable("AdapterForumValues", mAdapter.getForums());
        outState.putSerializable("AdapterThreadValues", mAdapter.getThreads());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new ShowThreadsAdapter(mActivity);

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
            num_pages = savedInstanceState.getInt("NumPages");
        } else {
            try {
                num_pages = getArguments().getInt("NumPages");
                base_url = getArguments().getString("Url");
                forum_url = getArguments().getString("Url");
                forum_id = forum_url.split("/f", 2)[1];
                forum_name = getArguments().getString("ForumName");
                // +1 Because the pages start their index at 1. 0 works, but it just shows the first page of threads, so if you scrolled the next page would be the same as the first.
                selected_page = getArguments().getInt("index") + 1;
                forum_url = forum_url.concat("p"+selected_page);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

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

            forumsParserTask = new ForumsParserTask(mActivity, forumFetched);
            threadsParserTask = new ThreadsParserTask(mActivity, threadFetched);
            forumsParserTask.execute(forum_url);
            threadsParserTask.execute(forum_url);
        }
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
        String showthisname;

        header = (TextView)view.findViewById(R.id.headerleft);
        headerright = (TextView)view.findViewById(R.id.headerright);

        try {
            showthisname = forum_name;
        } catch (NullPointerException e) {
            showthisname = "Error-name";
            e.printStackTrace();
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
			((MainActivity)mActivity).openForum(url, forumname);
		} else {
			String url = mAdapter.getThreads().get(position-mAdapter.getForums().size()).get("ThreadLink");
			String threadname = mAdapter.getThreads().get(position-mAdapter.getForums().size()).get("ThreadName");
			try {
				((MainActivity)mActivity).openThread(url, 0, threadname);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
