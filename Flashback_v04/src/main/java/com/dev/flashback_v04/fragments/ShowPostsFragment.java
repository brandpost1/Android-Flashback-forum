package com.dev.flashback_v04.fragments;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowPostsAdapter;
import com.dev.flashback_v04.interfaces.PostsFragCallback;

import android.support.v7.widget.ShareActionProvider;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowPostsFragment extends ListFragment implements PostsFragCallback<Bundle> {

    ShowPostsAdapter mAdapter;
	int selected_page;
	int selected_page_base;
    int numPages;
	String thread_url = null;
	String thread_url_withpagenr = null;

    ShareActionProvider mShare;

    Activity mActivity;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Show updatebutton
        menu.clear();
        inflater.inflate(R.menu.thread_default_menu, menu);

        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean shareActivated = appPrefs.getBoolean("thread_sharebutton", false);

        // Setup share button if activated in Preferences
        if(shareActivated) {
            // Inflate menuitem
            inflater.inflate(R.menu.share, menu);

            //
            MenuItem shareButton = menu.findItem(R.id.thread_share);
            mShare = (ShareActionProvider) MenuItemCompat.getActionProvider(shareButton);

            // Intent for share button
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, thread_url_withpagenr);
            mShare.setShareIntent(shareIntent);
        }

		// Set up search
		MenuItem searchItem = menu.findItem(R.id.thread_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String searchString) {
				int threadId = getArguments().getInt("ThreadId");
				String query = "https://www.flashback.org/sok/"+ searchString +"?sp=1&t=" + threadId;
				query = query.replace(" ", "+");
				((MainActivity)mActivity).searchThread(query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				return false;
			}
		});

        // If logged in, show new reply button in thread.
        try {
            if(LoginHandler.loggedIn(mActivity) && mAdapter.getCount() > 0)
                inflater.inflate(R.menu.thread_reply, menu);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
		super.onCreateOptionsMenu(menu, inflater);
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
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		thread_url = getArguments().getString("Url");
		selected_page = getArguments().getInt("index") + 1;
		selected_page_base = getArguments().getInt("index");
		thread_url_withpagenr = thread_url.concat("p"+selected_page);

        if(mAdapter == null) {		// Don't know if this is needed
            mAdapter = new ShowPostsAdapter(mActivity,this, thread_url_withpagenr);
            setListAdapter(mAdapter);
        }
        super.onCreate(savedInstanceState);
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.thread_new_reply:
                Bundle args = new Bundle();
                args.putString("Url", thread_url);
                args.putInt("CurrentPage", selected_page_base);
                args.putString("ThreadName", getArguments().getString("ThreadName"));
                ((MainActivity)mActivity).onOptionSelected(item.getItemId(), args);
                break;
             case R.id.thread_update:
                Bundle updatebundle = new Bundle();
                updatebundle.putInt("CurrentPage", selected_page_base);
                updatebundle.putString("Url", thread_url);
                updatebundle.putString("ThreadName", getArguments().getString("ThreadName"));
                ((MainActivity)mActivity).updateThread(updatebundle);
                break;
            case R.id.thread_openinbrowser:
                String url = thread_url_withpagenr;
                Intent openbrowserIntent = new Intent(Intent.ACTION_VIEW);
                openbrowserIntent.setData(Uri.parse(url));
                startActivity(openbrowserIntent);
                break;
            case R.id.thread_gotopage:
                LayoutInflater inflater = mActivity.getLayoutInflater();
                View v = inflater.inflate(R.layout.pagepicker_dialog, null);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    final NumberPicker picker = (NumberPicker) v.findViewById(R.id.page_scroll_picker);
                    picker.setMaxValue(numPages);
                    picker.setMinValue(1);
                    picker.setValue(1);

                    AlertDialog pagepicker = new AlertDialog.Builder(mActivity)
                            .setTitle("Välj sida")
                            .setView(v)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bundle gotobundle = new Bundle();
                                    gotobundle.putInt("CurrentPage", picker.getValue());
                                    gotobundle.putString("Url", thread_url);
                                    gotobundle.putString("ThreadName", getArguments().getString("ThreadName"));
                                    ((MainActivity)mActivity).updateThread(gotobundle);
                                }
                            }).create();
                    pagepicker.show();
                } else {
                    final EditText pick = (EditText)v.findViewById(R.id.edittext_picker);
                    TextView right = (TextView)v.findViewById(R.id.picker_right);
                    right.setText(numPages);
                    AlertDialog pagepicker = new AlertDialog.Builder(mActivity)
                            .setTitle("Välj sida")
                            .setView(v)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    int page;
                                    try {
                                        page = Integer.parseInt(pick.getText().toString());
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        Toast.makeText(mActivity, "Felaktig input." , Toast.LENGTH_SHORT).show();
                                        page = -1;
                                    }
                                    if(page > 0 && page <= numPages) {
                                        // In range
                                        Bundle gotobundle = new Bundle();
                                        gotobundle.putInt("CurrentPage", page-1);
                                        gotobundle.putString("Url", thread_url);
                                        gotobundle.putString("ThreadName", getArguments().getString("ThreadName"));
                                        ((MainActivity)mActivity).updateThread(gotobundle);
                                    } else {
                                        Toast.makeText(mActivity, "Sidan existerar inte", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).create();
                    pagepicker.show();
                }



                break;

        }
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setItemsCanFocus(true);
		getListView().setDivider(null);

        hasOptionsMenu();

		getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
					return true;
			}
		});
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);

        TextView header = (TextView)view.findViewById(R.id.headerleft);
        TextView headerright = (TextView)view.findViewById(R.id.headerright);
        numPages = getArguments().getInt("NumPages");
        String page = "Sida " + (getArguments().getInt("index") + 1) + " av " + numPages;
        String threadname = getArguments().getString("ThreadName");

        if(threadname == null) {
            threadname = "Error-name";
        }

        header.setText(threadname);
        headerright.setText(page);

		return view;

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Actions");
		MenuInflater inflater = mActivity.getMenuInflater();
		inflater.inflate(R.menu.thread_context, menu);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			switch (item.getItemId()) {
				case R.id.gotolastpage:

					return true;
				default:
					return super.onContextItemSelected(item);
			}

	}

    @Override
    public void sendQuote(Bundle b) {
        b.putString("Url", thread_url);
        b.putInt("CurrentPage", selected_page_base);
        b.putString("ThreadName", getArguments().getString("ThreadName"));

        ((MainActivity)mActivity).onOptionSelected(R.id.thread_new_reply, b);
    }

    @Override
    public void sendPlusQuote(Bundle b) {
        int key = b.getInt("PostNumber");
        String[] authorQuote = {"test", "bla"};
    }
}
