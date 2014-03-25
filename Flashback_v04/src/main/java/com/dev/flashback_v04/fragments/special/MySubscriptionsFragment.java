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
import com.dev.flashback_v04.adapters.special.MySubsAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2014-03-20.
 */
public class MySubscriptionsFragment extends ListFragment {

	public class GetSubsTask extends AsyncTask<String, HashMap<String, String>, Boolean> {

		Callback mProgressUpdate;
		Callback mCallback;
		Parser mParser;
		Context mContext;

		public GetSubsTask(Context context, Callback callback) {
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
			mParser.getMySubscribedThreads(strings[0], mProgressUpdate);
			return null;
		}

		@Override
		protected void onProgressUpdate(HashMap<String, String>... values) {
			// Hand out data to Fragment
			mCallback.onTaskComplete(values[0]);
		}
	}

	private Activity mActivity;
	private MySubsAdapter mySubsAdapter;
	private GetSubsTask mGetQuotesTask;
	private Callback threadFetched;
	private int pageNumber;
	private int numPages;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("AdapterValues", mySubsAdapter.getItems());
		outState.putInt("PageNumber", pageNumber);
		outState.putInt("NumPages", numPages);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mySubsAdapter = new MySubsAdapter(mActivity);

		if(savedInstanceState != null) {
			// Restore adapter
			ArrayList<HashMap<String, String>> savedValues = (ArrayList<HashMap<String, String>>) savedInstanceState.get("AdapterValues");
			mySubsAdapter.setItems(savedValues);
			mySubsAdapter.notifyDataSetChanged();

		}
		setListAdapter(mySubsAdapter);
		threadFetched = new Callback<HashMap<String, String>>() {
			@Override
			public void onTaskComplete(HashMap<String, String> data) {
				mySubsAdapter.addItem(data);
				mySubsAdapter.notifyDataSetChanged();
			}
		};

		if(savedInstanceState == null) {
			pageNumber = getArguments().getInt("PageNumber");
			numPages = getArguments().getInt("NumPages");
			mGetQuotesTask = new GetSubsTask(mActivity, threadFetched);
			mGetQuotesTask.execute("https://www.flashback.org/subscription.php?do=viewsubscription&folderid=all&sort=lastpost&order=desc&page=" + pageNumber);
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
		header.setText("Prenumerationer - Trådar");
		headerRight.setText(numPagesText);
		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String threadUrl = ((HashMap<String, String>)mySubsAdapter.getItem(position)).get("Link");
		String threadName = ((HashMap<String, String>)mySubsAdapter.getItem(position)).get("Title");

		// -1 because the page is unknown from this type of url
		((MainActivity)mActivity).openThread(threadUrl, -1, threadName);
	}
}
