package com.dev.flashback_v04.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import com.dev.flashback_v04.interfaces.OnOptionSelectedListener;

import java.lang.Thread;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowThreadsFragment extends ListFragment {

	ShowThreadsAdapter mAdapter;
	int selected_page;
	String forum_url = null;
    String base_url;
    String forum_name;
    String forum_id;
    TextView header;
    TextView headerright;
    Thread headerupdate;

    OnOptionSelectedListener mListener;
    Activity mActivity;
    Menu menu;
	public ShowThreadsFragment() {

	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try{
            mListener = (OnOptionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnOptionSelectedListener");
        }
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        // Show updatebutton
        inflater.inflate(R.menu.forum_default_menu, menu);

        try {
            if(LoginHandler.loggedIn(mActivity) && mAdapter.getThreads().size() > 0)
                inflater.inflate(R.menu.forum_loggedin_menu, menu);
        } catch(NullPointerException e) {
            e.printStackTrace();
        }


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
                mListener.onOptionSelected(item.getItemId(), args);
                break;
            case R.id.forum_update:
                Bundle bundle = new Bundle();
                Toast.makeText(mActivity,"Ej implementerad än", Toast.LENGTH_SHORT ).show();
                break;
        }
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
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

        if(mAdapter == null) {		// Don't know if this is needed
            mAdapter = new ShowThreadsAdapter(mActivity, forum_url);
            setListAdapter(mAdapter);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
        registerForContextMenu(getListView());

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(mAdapter.getForums().size() > position) {
			String url = mAdapter.getForums().get(position).getUrl();
            String forumname = mAdapter.getForums().get(position).getName();
			((MainActivity)mActivity).openForum(url, 1, forumname);
		} else {
			String url = mAdapter.getThreads().get(position-mAdapter.getForums().size()).getThreadLink();
            int numpages = Integer.parseInt(mAdapter.getThreads().get(position-mAdapter.getForums().size()).getNumPages());
            String threadname = mAdapter.getThreads().get(position-mAdapter.getForums().size()).getThreadName();
            try {
                ((MainActivity)mActivity).openThread(url, numpages, 0, threadname);

            } catch(Exception e) {
                e.printStackTrace();
            }


		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mAdapter == null) {		// Don't know if this is needed
            forum_url = getArguments().getString("Url");
            forum_name = getArguments().getString("ForumName");
            // +1 Because the pages start their index at 1. 0 works, but it just shows the first page of threads, so if you scrolled the next page would be the same as the first.
            selected_page = getArguments().getInt("index") + 1;
            forum_url = forum_url.concat("p"+selected_page);
        }

        View view = inflater.inflate(R.layout.list_fragment_layout, container, false);
        int len = getResources().getInteger(R.integer.header_max_length);
        String showthisname;

        header = (TextView)view.findViewById(R.id.headerleft);
        headerright = (TextView)view.findViewById(R.id.headerright);
        final String numpages = Integer.toString(SharedPrefs.getPreference(mActivity, "forum_size", "size"));

        if(mAdapter != null) {
            if(!mAdapter.updatedthreads) {
                headerupdate = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(!mAdapter.updatedthreads) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(mAdapter.getThreads().isEmpty()) {
                                        headerright.setText("");
                                    } else {

                                        headerright.setText("Sida "+ selected_page +" av "+ numpages);
                                    }
                                }
                            });
                            // If you press the back button before this thread is finished, this exception will be thrown
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                });
                headerupdate.start();
            } else {
                if(mAdapter.getThreads().isEmpty()) {
                    headerright.setText("");
                } else {
                    headerright.setText("Sida "+ selected_page +" av " + numpages);
                }
            }
        }

        try {
            if (forum_name.length() >= len) {
                showthisname = forum_name.substring(0, len)+ "...";
            } else {
                showthisname = forum_name;
            }
        } catch (NullPointerException e) {
            showthisname = "Error-name";
            e.printStackTrace();
        }

        header.setText(showthisname);


		return view;

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = mActivity.getMenuInflater();
		inflater.inflate(R.menu.thread_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
        // Workaround. The wrong fragment was being accessed by the contextmenu
        // http://stackoverflow.com/questions/5297842/how-to-handle-oncontextitemselected-in-a-multi-fragment-activity
        if( getUserVisibleHint() == false )
        {
            return false;
        }
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			switch (item.getItemId()) {
				case R.id.gotolastpage:
                    int position = info.position;
                    String url = this.mAdapter.getThreads().get(position-this.mAdapter.getForums().size()).getThreadLink();
                    int numpages = Integer.parseInt(this.mAdapter.getThreads().get(position-this.mAdapter.getForums().size()).getNumPages());
                    String threadname = this.mAdapter.getThreads().get(position-this.mAdapter.getForums().size()).getThreadName();
                    Bundle args = new Bundle();
                    args.putInt("NumPages", numpages);
                    args.putString("Url", url);
                    args.putString("ThreadName", threadname);

                    mListener.onOptionSelected(item.getItemId(), args);

					return true;
				default:
					return super.onContextItemSelected(item);
			}

	}

    @Override
    public void onResume() {
        super.onResume();
    }
}
