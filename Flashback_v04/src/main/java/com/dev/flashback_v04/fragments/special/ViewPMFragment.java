package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.Post;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.ViewPMAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;

/**
 * Created by Viktor on 2014-03-21.
 */
public class ViewPMFragment extends ListFragment {

	public class GetPMTask extends AsyncTask<String, ArrayList<Post>, String> {

		private Parser mParser;
		private Callback<ArrayList<Post>> mCallback;
		private Callback<ArrayList<Post>> progressUpdate;

		public GetPMTask(Context mContext, Callback<ArrayList<Post>> callback) {
			mParser = new Parser(mContext);
			mCallback = callback;

			progressUpdate = new Callback<ArrayList<Post>>() {
				@Override
				public void onTaskComplete(ArrayList<Post> data) {
					publishProgress(data);
				}
			};
		}

		@Override
		protected void onProgressUpdate(ArrayList<Post>... result) {
			super.onProgressUpdate(result);
			if(result[0].isEmpty()) {
				Toast.makeText(mContext, "Gå tillbaka och ladda om Inbox/Outbox och öppna meddelandet på nytt. Flashbacks PM-system krånglar troligtvis lite.", Toast.LENGTH_LONG).show();
			} else {
				mCallback.onTaskComplete(result[0]);
			}
		}

		@Override
		protected String doInBackground(String... strings) {
			String errorMessage;
			String url = strings[0];

			errorMessage = mParser.getPrivateMessageContent(url, progressUpdate);

			return errorMessage;
		}

		@Override
		protected void onPostExecute(String result) {

		}

	}

	private Context mContext;
	private ViewPMAdapter pmAdapter;
	private Callback mCallback;
	private String pmID;
	private String from;
	private String header;
	private boolean messageLoaded;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		pmAdapter = new ViewPMAdapter(mContext);
		mCallback = new Callback<ArrayList<Post>>() {
			@Override
			public void onTaskComplete(ArrayList<Post> data) {
				pmAdapter.setData(data);
				pmAdapter.notifyDataSetChanged();
				messageLoaded = true;
				((MainActivity)mContext).supportInvalidateOptionsMenu();
			}
		};
		setListAdapter(pmAdapter);

		pmID = getArguments().getString("PMId");
		from = getArguments().getString("From");
		header = getArguments().getString("Header");

		String url = getArguments().getString("Link");
		GetPMTask task = new GetPMTask(mContext, mCallback);
		task.execute(url);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
		TextView headerLeft = (TextView)view.findViewById(R.id.headerleft);
		TextView headerRight = (TextView)view.findViewById(R.id.headerright);
		headerRight.setText("");

		headerLeft.setText(header);


		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(messageLoaded) {
			inflater.inflate(R.menu.viewpmmenu, menu);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.newpm_forward:
				Bundle newpmforward = new Bundle();
				newpmforward.putString("Forward", "1");
				newpmforward.putString("PMId", pmID);

				if( header.startsWith("VB: ") || header.startsWith("Sv: ") ) header = header.substring(4);

				newpmforward.putString("Header", "VB: " + header);
				newpmforward.putString("Message", "[QUOTE="+from+"]" + ((Post)pmAdapter.getItem(0)).getQuote() + "[/QUOTE]");
				((MainActivity)mContext).onOptionSelected(R.id.new_private_message, newpmforward);
				break;
			case R.id.newpm_reply:
				Bundle newpmreply = new Bundle();
				newpmreply.putString("Recipient", from);
				newpmreply.putString("PMId", pmID);

				if( header.startsWith("Sv: ") || header.startsWith("VB: ") ) header = header.substring(4);

				newpmreply.putString("Header", "Sv: " + header);
				newpmreply.putString("Message", "[QUOTE="+from+"]" + ((Post)pmAdapter.getItem(0)).getQuote() + "[/QUOTE]");
				((MainActivity)mContext).onOptionSelected(R.id.new_private_message, newpmreply);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

}
