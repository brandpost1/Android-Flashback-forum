package com.dev.flashback_v04.fragments;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.Post;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowPostsAdapter;
import com.dev.flashback_v04.asynctasks.special.AddSubsTask;
import com.dev.flashback_v04.interfaces.Callback;
import com.dev.flashback_v04.interfaces.ErrorHandler;
import com.dev.flashback_v04.interfaces.OnTaskComplete;
import com.dev.flashback_v04.interfaces.PostsFragCallback;

import android.support.v7.widget.ShareActionProvider;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowPostsFragment extends ListFragment {

	public class PostsParserTask extends AsyncTask<String, ArrayList<Post>, String> {

		private Parser mParser;
		private Callback<ArrayList<Post>> postsFetched;
		private Callback<ArrayList<Post>> postsUpdate;
		private ErrorHandler errorHandler;
		private String errormsg;

		public PostsParserTask(Context context, Callback taskComplete, ErrorHandler handler) {
			mParser = new Parser(context);
			postsFetched = taskComplete;
			errorHandler = handler;

			postsUpdate = new Callback<ArrayList<Post>>() {
				@Override
				public void onTaskComplete(ArrayList<Post> data) {
					publishProgress(data);
				}
			};
		}

		@Override
		protected void onProgressUpdate(ArrayList<Post>... values) {
			super.onProgressUpdate(values);
			postsFetched.onTaskComplete(values[0]);
		}

		@Override
		protected String doInBackground(String... strings) {
			try {
				System.out.println("Fetching page! - " + thread_url_withpagenr);
				errormsg = mParser.getThreadContent(thread_url_withpagenr, postsUpdate);

			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			return errormsg;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result != null) {
				errorHandler.handleError(result);
			} else {
				hasErrors = false;
			}
		}

	}

	private boolean hasErrors;
	private Callback<ArrayList<Post>> postsFetched;
	private PostsFragCallback<Bundle> postsFragCallback;
	private ErrorHandler mErrorHandler;
	private String errorMessage;
	private RelativeLayout mErrorLayout;

	private PostsParserTask postsParserTask;
	private ShowPostsAdapter mAdapter;
	private int selected_page;
	private int selected_page_base;
	private int numPages;
	private String thread_url = null;
	private String thread_url_withpagenr = null;
	private String base_url = null;
	private int thread_id;
	private int POSTS_PER_PAGE = 12;

	private ShareActionProvider mShare;

	private MainActivity mActivity;


	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity)activity;
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

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
				int threadId = thread_id;
				String query = "https://www.flashback.org/sok/"+ searchString +"?sp=1&t=" + threadId;
				query = query.replace(" ", "+");
				(mActivity).searchThread(query);
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
	}

    @Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		SharedPreferences preferences = mActivity.getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);
		POSTS_PER_PAGE = preferences.getInt("Thread_Max_Posts_Page", 12);

		thread_url = getArguments().getString("Url");
		selected_page = getArguments().getInt("index") + 1;
		selected_page_base = getArguments().getInt("index");
		thread_url_withpagenr = thread_url.concat("&pp="+ POSTS_PER_PAGE +"&page=" + selected_page);

		int index = thread_url.indexOf("t=") + 2;
		thread_id = Integer.parseInt(thread_url.substring(index));


		postsFragCallback = new PostsFragCallback<Bundle>() {
			@Override
			public void sendQuote(Bundle b) {
				b.putString("Url", thread_url);
				b.putInt("CurrentPage", selected_page);
				b.putString("ThreadName", getArguments().getString("ThreadName"));

				mActivity.onOptionSelected(R.id.thread_new_reply, b);
			}
		};

		postsFetched = new Callback<ArrayList<Post>>() {
			@Override
			public void onTaskComplete(ArrayList<Post> data) {
				mAdapter.updatePosts(data);
				mAdapter.notifyDataSetChanged();
				// Invalidate optionsmenu since newreply and subscribe-buttons rely on there existing posts in the adapter, which has now been updated.
				mActivity.supportInvalidateOptionsMenu();
			}
		};

		mErrorHandler = new ErrorHandler() {
			@Override
			public void handleError(String errormessage) {
				hasErrors = true;
				errorMessage = errormessage;
				showErrorBox();

			}
		};

        mAdapter = new ShowPostsAdapter(mActivity, postsFragCallback);
        setListAdapter(mAdapter);

		getContent();

        super.onCreate(savedInstanceState);
	}

	private void showErrorBox() {
		if(mErrorLayout != null) {
			if (mErrorLayout.getVisibility() != View.VISIBLE) {
				final Button retrybutton = (Button) mErrorLayout.findViewById(R.id.retrybutton);
				retrybutton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mErrorLayout.setVisibility(View.GONE);
						mAdapter.clearData();
						mAdapter.notifyDataSetChanged();
						getContent();
					}
				});
				mErrorLayout.setVisibility(View.VISIBLE);
				((TextView) mErrorLayout.findViewById(R.id.errorlayoutmessage)).setText(errorMessage);
			}
		} else {
			Toast.makeText(mActivity, errorMessage, Toast.LENGTH_SHORT).show();
		}
	}

	public void getContent() {
		postsParserTask = new PostsParserTask(mActivity, postsFetched, mErrorHandler);
		postsParserTask.execute();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
			case R.id.thread_subscribe:
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Prenumeration")
						.setMessage("Vill du prenumerera på den här tråden?")
						.setPositiveButton("Bekräfta", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Bundle bundle = new Bundle();
								bundle.putString("ThreadId", String.valueOf(thread_id));
								AddSubsTask addSubsTask = new AddSubsTask(mActivity, bundle);
								addSubsTask.execute();
							}
						})
						.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
				builder.create().show();
				break;
            case R.id.thread_new_reply:
                Bundle args = new Bundle();
                args.putString("Url", thread_url);
                args.putInt("CurrentPage", selected_page);
                args.putString("ThreadName", getArguments().getString("ThreadName"));
                ((MainActivity)mActivity).onOptionSelected(item.getItemId(), args);
                break;
             case R.id.thread_update:
                Bundle updatebundle = new Bundle();
                updatebundle.putInt("CurrentPage", selected_page);
                updatebundle.putString("Url", thread_url_withpagenr);
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
				final ViewPager viewPager = (ViewPager)getView().getParent();

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
									if(viewPager != null) {
										viewPager.setCurrentItem(picker.getValue() - 1);
									}
                                }
                            }).create();
                    pagepicker.show();
                } else {
                    final EditText pick = (EditText)v.findViewById(R.id.edittext_picker);
                    TextView right = (TextView)v.findViewById(R.id.picker_right);
                    right.setText(String.valueOf(numPages));
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
										if(viewPager != null) {
											viewPager.setCurrentItem(page - 1);
										}
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
		mErrorLayout = (RelativeLayout)view.findViewById(R.id.errorlayout);
        TextView header = (TextView)view.findViewById(R.id.headerleft);
        TextView headerright = (TextView)view.findViewById(R.id.headerright);

        numPages = getArguments().getInt("NumberOfPages");
        String page = "Sida " + (getArguments().getInt("index") + 1) + " av " + numPages;
        String threadname = getArguments().getString("ThreadName");

        if(threadname == null) {
            threadname = "Error-name";
        }

		if(hasErrors) {
			showErrorBox();
		}

        header.setText(threadname);
        headerright.setText(page);

		return view;

	}
}
