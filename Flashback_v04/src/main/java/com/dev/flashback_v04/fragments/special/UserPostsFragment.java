package com.dev.flashback_v04.fragments.special;

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

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.SharedPrefs;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.MyPostsAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2014-03-09.
 */
public class UserPostsFragment extends ListFragment {

	public class GetMyPostsTask extends AsyncTask<String, HashMap<String, String>, Boolean> {

		Callback mProgressUpdate;
		Callback mCallback;
		Parser mParser;
		Context mContext;

		public GetMyPostsTask(Context context, Callback callback) {
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
			mParser.getMyPosts(strings[0], mProgressUpdate);
			return null;
		}

		@Override
		protected void onProgressUpdate(HashMap<String, String>... values) {
			super.onProgressUpdate(values);
			mCallback.onTaskComplete(values[0]);
		}
	}

	private View.OnClickListener openForumListener;
	private Activity mActivity;
	private MyPostsAdapter myPostsAdapter;
	private GetMyPostsTask getMyPostsTask;
	private Callback postFetched;

	private int pageNumber;
	private int numPages;
	private String userId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("AdapterValues", myPostsAdapter.getItems());
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
				((MainActivity)mActivity).openForum(forumUrl, forumName);
			}
		};

		myPostsAdapter = new MyPostsAdapter(mActivity, openForumListener);

		if(savedInstanceState != null) {
			// Restore adapter
			ArrayList<HashMap<String, String>> savedValues = (ArrayList<HashMap<String, String>>) savedInstanceState.get("AdapterValues");
			myPostsAdapter.setItems(savedValues);
			myPostsAdapter.notifyDataSetChanged();
		}
		setListAdapter(myPostsAdapter);

		postFetched = new Callback<HashMap<String, String>>() {
			@Override
			public void onTaskComplete(HashMap<String, String> data) {
				myPostsAdapter.addItem(data);
				myPostsAdapter.notifyDataSetChanged();
			}
		};

		if(savedInstanceState == null) {
			String executeString;
			String threadId = getArguments().getString("ThreadId");
			userId = getArguments().getString("UserId");

			pageNumber = getArguments().getInt("PageNumber");
			numPages = getArguments().getInt("NumPages");

			if(getArguments().getString("Search") != null) {
				executeString = getArguments().getString("Search");
			} else {
				if(threadId == null) {
					executeString = "https://www.flashback.org/find_posts_by_user.php?userid="+ userId +"&page=" + pageNumber;
				} else {
					executeString = "https://www.flashback.org/find_posts_by_user.php?userid="+ userId +"&threadid="+ threadId +"&page=" + pageNumber;
				}
			}


			getMyPostsTask = new GetMyPostsTask(mActivity, postFetched);
			getMyPostsTask.execute(executeString);
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

		headerRight = (TextView)view.findViewById(R.id.headerright);
        header = (TextView)view.findViewById(R.id.headerleft);

		if(userId != null) {
			if(userId.equals(Integer.toString(SharedPrefs.getPreference(mActivity, "user", "ID")))) {
				header.setText("Mina inlägg");
			} else {
				header.setText("Inlägg");
			}
		} else {
			header.setText("Sökresultat");
		}

		headerRight.setText(numPagesText);

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

		String threadUrl = (String)v.getTag(R.id.OPEN_THREAD);
		String threadName = (String)v.getTag(R.id.OPEN_THREAD_NAME);

		// -1 because the page is unknown from this type of url
		((MainActivity)mActivity).openThread(threadUrl, -1, threadName);

    }
}
