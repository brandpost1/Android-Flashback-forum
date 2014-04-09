package com.dev.flashback_v04.fragments.special;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.PopupMenu;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
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


	public interface CheckboxListener {
		public void onChange(View v, int position, boolean isChecked, String value);
	}

	private Activity mActivity;
	private MySubsAdapter mySubsAdapter;
	private GetSubsTask mGetQuotesTask;
	private Callback threadFetched;
	private int pageNumber;
	private int numPages;

	private CheckboxListener checkboxListener;
	private HashMap<Integer, String> mCheckedMap;
	private ActionMode.Callback mActionModeCallback;
	private ActionMode mActionMode;

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
		outState.putSerializable("CheckedList", mCheckedMap);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if(!mCheckedMap.isEmpty()) {
				mActionMode = mActivity.startActionMode(mActionModeCallback);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState == null) {
			mCheckedMap = new HashMap<Integer, String>();
		} else {
			mCheckedMap = (HashMap<Integer, String>) savedInstanceState.getSerializable("CheckedList");
		}



		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			initActionModeCallback();
		}

		checkboxListener = new CheckboxListener() {
			@Override
			public void onChange(View anchor, int position, boolean isChecked, String value) {
				if(isChecked) {
					mCheckedMap.put(position, value);
				} else {
					mCheckedMap.remove(position);
				}
				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					if (!mCheckedMap.isEmpty()) {
						if (mActionMode == null) {
							mActionMode = mActivity.startActionMode(mActionModeCallback);
						}
					} else {
						if(mActionMode != null)
							mActionMode.finish();
					}
				} else {
					if(isChecked) {
						PopupMenu popup;
						popup = new PopupMenu(mActivity, anchor);
						PopupMenu.OnMenuItemClickListener clickListener = new PopupMenu.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem menuItem) {
								int item = menuItem.getItemId();
								switch (item) {
									case 0:
										// Delete option
										Bundle bundle = new Bundle();
										ArrayList<String> deleteboxes = new ArrayList<String>();
										for(String value : mCheckedMap.values()) {
											deleteboxes.add(value);
										}
										bundle.putStringArrayList("DeleteBoxes", deleteboxes);
										((MainActivity)mActivity).deleteSubscription(bundle);
										break;
								}
								return false;
							}
						};
						popup.getMenu().add(Menu.NONE, 0, Menu.NONE, "Avsluta prenumeration");
						popup.setOnMenuItemClickListener(clickListener);
						popup.show();
					}
				}
			}
		};

		mySubsAdapter = new MySubsAdapter(mActivity, checkboxListener);

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

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (mActionMode != null) mActionMode.finish();
		}

		((MainActivity)mActivity).openThread(threadUrl, 1, threadName);
	}

	// For contextual action mode
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initActionModeCallback() {
		mActionModeCallback = new ActionMode.Callback() {
			@Override
			public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
				MenuInflater inflater = actionMode.getMenuInflater();
				inflater.inflate(R.menu.subscriptions_menu, menu);
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
				switch (menuItem.getItemId()) {
					case R.id.delete_subscription:
						Bundle bundle = new Bundle();
						ArrayList<String> deleteboxes = new ArrayList<String>();

						if(mActionMode != null)
							mActionMode.finish();

						for(String value : mCheckedMap.values()) {
							deleteboxes.add(value);
						}
						bundle.putStringArrayList("DeleteBoxes", deleteboxes);
						((MainActivity)mActivity).deleteSubscription(bundle);

						break;
				}
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode actionMode) {
				mActionMode = null;
			}
		};
	}


}
