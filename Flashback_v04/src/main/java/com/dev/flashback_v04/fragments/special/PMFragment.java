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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.PrivateMessagesAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2014-03-09.
 */
public class PMFragment extends ListFragment {

    public class GetPMTask extends AsyncTask<String, HashMap<String, String>, Boolean> {

        Callback mProgressUpdate;
        Callback mCallback;
        Parser mParser;
        Context mContext;

        public GetPMTask(Context context, Callback callback) {
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
            mParser.getPrivateMessages(strings[0], mProgressUpdate);
            return null;
        }

        @Override
        protected void onProgressUpdate(HashMap<String, String>... values) {
            // Hand out data to Fragment
            mCallback.onTaskComplete(values[0]);
        }
    }

    private Activity mActivity;
    private PrivateMessagesAdapter privateMessagesAdapter;
    private GetPMTask mGetPMsTask;
    private Callback messageFetched;
	private Button mLoadButton;

    private int pageNumber;
    private int numPages;
	private int fragmentType;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("AdapterValues", privateMessagesAdapter.getItems());
        outState.putInt("PageNumber", pageNumber);
        outState.putInt("NumPages", numPages);
		outState.putInt("FragmentType", fragmentType);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		privateMessagesAdapter = new PrivateMessagesAdapter(mActivity);

        if(savedInstanceState != null) {
			// Restore adapter
			ArrayList<HashMap<String, String>> savedValues = (ArrayList<HashMap<String, String>>) savedInstanceState.get("AdapterValues");
			privateMessagesAdapter.setItems(savedValues);
			privateMessagesAdapter.notifyDataSetChanged();
		}

        setListAdapter(privateMessagesAdapter);
		messageFetched = new Callback<HashMap<String, String>>() {
			@Override
			public void onTaskComplete(HashMap<String, String> data) {
				privateMessagesAdapter.addItem(data);
				privateMessagesAdapter.notifyDataSetChanged();
			}
		};

		if(savedInstanceState == null) {
			pageNumber = getArguments().getInt("PageNumber");
			numPages = getArguments().getInt("NumPages");
			fragmentType = getArguments().getInt("FragmentType");
			loadPmList();
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

        header = (TextView)view.findViewById(R.id.headerleft);
        headerRight = (TextView)view.findViewById(R.id.headerright);
        header.setVisibility(View.GONE);
        headerRight.setVisibility(View.GONE);
        return view;
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.messaging_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.refresh_private_messages:
				clearPmList();
				loadPmList();
				break;
			case R.id.new_private_message:

				break;
		}
 		return super.onOptionsItemSelected(item);
	}

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
		String url = ((HashMap<String, String>)privateMessagesAdapter.getItem(position)).get("Link");
		// Get PMId from url
		String pmId;
		try {
			pmId = url.substring(url.indexOf("pmid="), url.indexOf("&", url.indexOf("pmid="))).split("=")[1];
		} catch (Exception e) {

		}

		((MainActivity)mActivity).openPrivateMessage(url);
    }

	private void clearPmList() {
		privateMessagesAdapter.clearListData();
		privateMessagesAdapter.notifyDataSetChanged();
	}

	private void loadPmList() {
		mGetPMsTask = new GetPMTask(mActivity, messageFetched);
		mGetPMsTask.execute("https://www.flashback.org/private.php?pp=100&folderid=" + fragmentType + "&page=" + pageNumber);
	}
}
