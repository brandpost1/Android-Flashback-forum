package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.HashMap;

/**
 * Created by Viktor on 2014-02-27.
 */
public class EditPostFragment extends Fragment {

	public class GetEditPostTask extends AsyncTask<String, Void, HashMap<String,String>> {

		private Context mContext;
		private Callback<HashMap<String, String>> updateCallback;
		private ProgressDialog dialog;

		public GetEditPostTask(Context context, Callback callback) {
			mContext = context;
			updateCallback = callback;
			dialog = new ProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.dialog.setMessage("Hämtar inlägg..");
			this.dialog.show();
		}

		@Override
		protected HashMap<String, String> doInBackground(String... strings) {
			Parser parser = new Parser(mContext);
			return parser.getEditPostContent(strings[0]);
		}

		@Override
		protected void onPostExecute(HashMap<String, String> values) {
			super.onPostExecute(values);
			if(dialog.isShowing())
				this.dialog.dismiss();
			updateCallback.onTaskComplete(values);
		}
	}

	ActionBarActivity mActivity;
	EditText editPostHeader;
	EditText editPostMessage;
	EditText editPostReason;
	Callback<HashMap<String, String>> mCallback;
	String postId;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Activity activity) {
		mActivity = (ActionBarActivity) activity;
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.edit_post, container, false);
		editPostReason = (EditText)v.findViewById(R.id.reason_textbox);
		editPostHeader = (EditText)v.findViewById(R.id.newthread_header_textbox);
		editPostMessage = (EditText)v.findViewById(R.id.newthread_message_textbox);

		if(savedInstanceState == null) {
			String url = getArguments().getString("EditPostUrl");
			postId = url.split("p=")[1];
			mCallback = new Callback<HashMap<String, String>>() {
				@Override
				public void onTaskComplete(HashMap<String, String> data) {
					editPostReason.setText(data.get("Reason"));
					editPostHeader.setText(data.get("Title"));
					editPostMessage.setText(data.get("Message"));
				}
			};
			GetEditPostTask getEditPostTask = new GetEditPostTask(mActivity, mCallback);
			getEditPostTask.execute(url);
		}
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.editpost_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
			case R.id.post_edit:
				Bundle bundle = new Bundle();
				String reason = editPostReason.getText().toString();
				String title = editPostHeader.getText().toString();
				String message = editPostMessage.getText().toString();

				bundle.putString("Reason", reason);
				bundle.putString("Title", title);
				bundle.putString("Message", message);
				bundle.putString("PostId", postId);

				// Close keyboard
				InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editPostMessage.getWindowToken(), 0);

				((MainActivity)mActivity).putEditedPost(bundle);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}

