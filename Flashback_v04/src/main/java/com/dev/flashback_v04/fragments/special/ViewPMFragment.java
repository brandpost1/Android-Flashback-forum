package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.Post;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.adapters.special.ViewPMAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;

/**
 * Created by Viktor on 2014-03-21.
 */
public class ViewPMFragment extends ListFragment {

	public class GetPMTask extends AsyncTask<String, String, ArrayList<Post>> {

		private Parser mParser;
		private Callback mCallback;

		public GetPMTask(Context mContext, Callback callback) {
			mParser = new Parser(mContext);
			mCallback = callback;
		}

		@Override
		protected ArrayList<Post> doInBackground(String... strings) {
			ArrayList<Post> result;
			String url = strings[0];

			result = mParser.getPrivateMessageContent(url);

			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<Post> result) {
			if(result.isEmpty()) {
				Toast.makeText(mContext, "Gå tillbaka och ladda om Inbox/Outbox och öppna meddelandet på nytt. Flashbacks PM-system krånglar troligtvis lite.", Toast.LENGTH_LONG).show();
			} else {
				mCallback.onTaskComplete(result);
			}
		}

	}

	private Context mContext;
	private ViewPMAdapter pmAdapter;
	private Callback mCallback;


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

		pmAdapter = new ViewPMAdapter(mContext);
		mCallback = new Callback<ArrayList<Post>>() {
			@Override
			public void onTaskComplete(ArrayList<Post> data) {
				pmAdapter.setData(data);
				pmAdapter.notifyDataSetChanged();
			}
		};
		setListAdapter(pmAdapter);

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

		headerLeft.setText("Privat meddelande");


		return view;
	}


}
