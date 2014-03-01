package com.dev.flashback_v04.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.*;
import com.dev.flashback_v04.adapters.DrawerAdapter;
import com.dev.flashback_v04.adapters.ShowPostsAdapter;
import com.dev.flashback_v04.asynctasks.LoginTask;
import com.dev.flashback_v04.fragments.WrapperFragment;
import com.dev.flashback_v04.fragments.special.CreateThreadFragment;
import com.dev.flashback_v04.fragments.special.CurrentThreadsFragment;
import com.dev.flashback_v04.fragments.special.NewPostsFragment;
import com.dev.flashback_v04.fragments.special.NewThreadsFragment;
import com.dev.flashback_v04.fragments.special.PostReplyFragment;
import com.dev.flashback_v04.interfaces.OnOptionSelectedListener;
import com.dev.flashback_v04.interfaces.UpdateForum;
import com.dev.flashback_v04.interfaces.UpdateStuff;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


import java.lang.Thread;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

class PollingThread extends Thread {

    LinkedBlockingQueue<Bundle> queue;
    MainActivity mContext;

    public PollingThread(MainActivity context, LinkedBlockingQueue<Bundle> updateTaskQueue) {
        queue = updateTaskQueue;
        mContext = context;
    }

    /*
    * Will wait for bundles to appear in the queue
    * Should be put in there from the asynctasks
    * */
    @Override
    public void run() {
        Bundle bundle;
        try {
            while (!Thread.interrupted()) {
                bundle = queue.take();
                String bundletype = bundle.getString("BundleType");
                if(bundletype.equals("UpdateThread")) {
                    mContext.updateThread(bundle);
                }
                if(bundletype.equals("ThreadCreated")) {
                    mContext.updateForum(bundle);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

/**
 * Created by Viktor on 2013-07-13.
 */
public class MainActivity extends ActionBarActivity implements OnOptionSelectedListener, UpdateStuff<Bundle>, UpdateForum<Bundle> {

	public FragmentManager fragmentManager;

    public LinkedBlockingQueue<Bundle> updateTaskQueue;
    PollingThread pollingThread;

    /*
    Banner ads
    * */
    AdView adView;
    final boolean ADS_ACTIVATED = false;

	/*
	Navigation drawer
	* */
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private String[] mDrawerItems;

    private Activity activity;
	/*
	* Constructor
	* */
	public MainActivity() {
        activity = this;
        updateTaskQueue = new LinkedBlockingQueue<Bundle>();
        pollingThread = new PollingThread(this, updateTaskQueue);
	}


    public void reportPost(View v) {
        Toast.makeText(this, "Report post", Toast.LENGTH_SHORT).show();
    }

    public void normalquote(View v) {
        Toast.makeText(this, (String)v.getTag(R.id.QUOTE_MESSAGE_TAG) + ", " + (String)v.getTag(R.id.QUOTE_AUTHOR_TAG), Toast.LENGTH_SHORT).show();
    }

    public void plusquote(View v) {
        Toast.makeText(this, "Plusquote", Toast.LENGTH_SHORT).show();
    }

    public void openThreadLastPage(MenuItem item) {

        Toast.makeText(this, "Open last page", Toast.LENGTH_SHORT).show();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	/*
	* Call this to open a specific thread
	* */
	public void openThread(String url, int numpages, int position, String threadname) {
		Fragment fragment = new WrapperFragment();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 3);
		args.putInt("NumPages", numpages);
        args.putInt("Position", position);
        args.putString("ThreadName", threadname);
		args.putString("Url", url);
		fragment.setArguments(args);

		/*
		* Replace the old fragment
		* */
		try {
			fragmentManager.beginTransaction()
					//.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
					.addToBackStack("Threads")
					.replace(R.id.fragmentcontainer, fragment)
					.commit();
            // INTE särskilt snyggt, men nu får det vara såhär...
            ShowPostsAdapter.mPlusQuotes.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	* Call this to open a specific forum
	* */
	public void openForum(String url, int size, String forumname) {
		Fragment fragment = new WrapperFragment();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 2);
		args.putInt("NumPages", size);
        args.putString("ForumName", forumname);
		args.putString("Url", url);
		fragment.setArguments(args);
		/*
		* Replace the old fragment
		* */

 		try {
			fragmentManager.beginTransaction()
					//.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
					.addToBackStack("Forums")
					.replace(R.id.fragmentcontainer, fragment)
					.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	* Call this to open a specific category
	* */
	public void openCategory(String url, int position, ArrayList<String> categories, ArrayList<String> catName) {
		/*
		* Initialize fragment.
		* Pass along arguments etc..
		* */
		Fragment fragment = new WrapperFragment();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 1);
		args.putInt("NumPages", 15);
        args.putStringArrayList("CategoryNames", catName);
		args.putStringArrayList("Categories", categories);
		args.putString("Url", url);
		args.putInt("Position", position);
		fragment.setArguments(args);

        /*
		* Replace the old fragment
		* */
        try {
            fragmentManager.beginTransaction()
                    //.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
                    .addToBackStack("Categories")
                    .replace(R.id.fragmentcontainer, fragment)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    @Override
    protected void onStop() {
       // pollingThread.interrupt();
        super.onStop();
    }

    @Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

    @Override
    protected void onResume() {
        super.onResume();
        if(ADS_ACTIVATED) {
            adView.resume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(ADS_ACTIVATED) {
            adView.pause();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ADS_ACTIVATED) {
            adView.destroy();
        }

    }

    private void showNewVersionDialog() {
        int prevVersion = PreferenceManager.getDefaultSharedPreferences(this)
                            .getInt("VersionCode", 0);
        int nextVersion = 0;

        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(info != null) {
            nextVersion = info.versionCode;
        }

        if(nextVersion > prevVersion) {
            final AlertDialog newsdialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.NEWSHEADLINE)
                    .setMessage(R.string.NEWSMESSAGE)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Should dismiss
                        }
                    }).create();
            newsdialog.show();

            AlertDialog infodialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.INFOHEADLINE)
                    .setMessage(R.string.INFOMESSAGE)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).create();
            infodialog.show();

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putInt("VersionCode", nextVersion)
                    .commit();
        }

    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// Start the background pollingthread
        pollingThread.start();

        // Set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        // Set theme
        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkTheme = appPrefs.getBoolean("theme_preference", false);

        if(darkTheme) {
            setTheme(R.style.Flashback_Dark);
        } else {
            setTheme(R.style.Flashback_Light);
        }

        super.onCreate(savedInstanceState);

        // Inflate main layout
        setContentView(R.layout.activity_main);

        // Show firstRun-dialog
        showNewVersionDialog();

        /*
        * Run if ads are activated
        * */
        adView = (AdView)this.findViewById(R.id.bannerAdvert);
        if(ADS_ACTIVATED) {
            String testDevice = this.getResources().getString(R.string.test_device);
            AdRequest request = new AdRequest.Builder()
                    .addTestDevice(testDevice)
                    .build();
            adView.loadAd(request);
        } else {
            adView.setVisibility(View.GONE);
        }


        getSupportActionBar().setTitle("");
		/*
		* Init fragmentmanager
		* */
		fragmentManager  = getSupportFragmentManager();

		/*
		* Temporary array for the drawer-list
		* */
		mDrawerItems = getResources().getStringArray(R.array.drawer);

		/*
		* Set up the navigation-drawer
		* */

 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
        final DrawerAdapter mAdapter = new DrawerAdapter(this);
		mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                int i = (int)adapterView.getItemIdAtPosition(position);
                int backstackcount;
                switch(i) {
                    case 0:
                        // Go back to start
                        getSupportFragmentManager().popBackStack("Start", 0);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                        // Aktuella ämnen
                    case 1:
                        CurrentThreadsFragment fragment = new CurrentThreadsFragment();
                        backstackcount = getSupportFragmentManager().getBackStackEntryCount();

                        // If opening "Aktuella ämnen" while in "Aktuella ämnen", pop back stack and remove old "Aktuella ämnen" from stack.
                        // Also if opening "Aktuella ämnen" while in a thread opened from "Aktuella ämnen".
                        // Do not want recursive calls.
                        try {
                            if(getSupportFragmentManager().getBackStackEntryAt(backstackcount-1).getName().equals("CurrentSubjects")
                                    || getSupportFragmentManager().getBackStackEntryAt(backstackcount-2).getName().equals("CurrentSubjects")) {
                                getSupportFragmentManager().popBackStack("CurrentSubjects", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        } catch (Exception e) {

                        }

                        try {
                            fragmentManager.beginTransaction()
                                    .addToBackStack("CurrentSubjects")
                                    .replace(R.id.fragmentcontainer, fragment)
                                    .commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 2:
                        NewThreadsFragment newThreadsFragment = new NewThreadsFragment();
                        backstackcount = getSupportFragmentManager().getBackStackEntryCount();

                        // Same as above
                        try {
                            if(getSupportFragmentManager().getBackStackEntryAt(backstackcount-1).getName().equals("NewSubjects")
                                    || getSupportFragmentManager().getBackStackEntryAt(backstackcount-2).getName().equals("NewSubjects")) {
                                getSupportFragmentManager().popBackStack("NewSubjects", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        } catch (Exception e) {

                        }

                        try {
                            fragmentManager.beginTransaction()
                                    .addToBackStack("NewSubjects")
                                    .replace(R.id.fragmentcontainer, newThreadsFragment)
                                    .commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 3:

                        NewPostsFragment newPostsFragment = new NewPostsFragment();
                        backstackcount = getSupportFragmentManager().getBackStackEntryCount();

                        // Same as above
                        try {
                            if(getSupportFragmentManager().getBackStackEntryAt(backstackcount-1).getName().equals("NewPosts")
                                    || getSupportFragmentManager().getBackStackEntryAt(backstackcount-2).getName().equals("NewPosts")) {
                                getSupportFragmentManager().popBackStack("NewPosts", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        } catch (Exception e) {

                        }

                        try {
                            fragmentManager.beginTransaction()
                                    .addToBackStack("NewPosts")
                                    .replace(R.id.fragmentcontainer, newPostsFragment)
                                    .commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 4:
                        Toast.makeText(getBaseContext(), "4.", Toast.LENGTH_SHORT).show();
                        // Hidden atm
                        break;
                    case 5:
                        Toast.makeText(getBaseContext(), "5.", Toast.LENGTH_SHORT).show();
                        // Hidden atm
                        break;
                    case 6:
                        Toast.makeText(getBaseContext(), "Under uppbyggnad.", Toast.LENGTH_SHORT).show();
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        /*
                        SearchFragment searchFragment = new SearchFragment();
                        try {
                            fragmentManager.beginTransaction()
                                    .addToBackStack("Search")
                                    .replace(R.id.fragmentcontainer, searchFragment)
                                    .commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        */
                        break;
                    case 7:
                        try {
                            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                            startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case 8:
                        TextView text = (TextView)view.findViewById(R.id.item_text);
                        if(!LoginHandler.loggedIn(activity)) {
                            showLoginDialog(text);
                        } else {
                            LoginHandler.logout(activity);
                            text.setText("Logga in");
                            getSupportFragmentManager().popBackStack("Start", 0);
                            mDrawerLayout.closeDrawer(Gravity.LEFT);
                        }
                        break;
                }
            }
        });
		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.drawer_open,
				R.string.drawer_close
		) {
			public void onDrawerClosed(View view) {
                //getSupportActionBar().setTitle("");
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				//getSupportActionBar().setTitle("Meny");
                supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

		/*
		* Add the wrapperfragment.
		* Each wrapperfragment contains its own viewpager.
		* */
		if(savedInstanceState == null) {
			Fragment fragment = new WrapperFragment();

			Bundle args = new Bundle();
			args.putInt("FragmentType", 0);
			args.putInt("NumPages", 1);

			fragment.setArguments(args);

			fragmentManager
					.beginTransaction()
                    .addToBackStack("Start")
					.add(R.id.fragmentcontainer, fragment)
					.commit();
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private void showLoginDialog(TextView text) {
        final LoginTask loginTask = new LoginTask(this, text);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.login_layout, null);
        final EditText user = (EditText)view.findViewById(R.id.login_edittext_username);
        final EditText pass = (EditText)view.findViewById(R.id.login_edittext_password);

        builder.setTitle("Logga in").setView(view);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String username = user.getText().toString();
                String password = pass.getText().toString();
                loginTask.execute(username, password);
                mDrawerLayout.closeDrawer(Gravity.LEFT);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    /*
        From ShowPostsFragment etc
        itemId is for example, R.id.thread_new_reply
    */
    @Override
    public void onOptionSelected(int itemId, Bundle args) {
        switch (itemId) {
            case R.id.thread_new_reply:
                PostReplyFragment fragment = new PostReplyFragment();
                fragment.setArguments(args);
                try {
                    getSupportFragmentManager().beginTransaction()
                            .addToBackStack("Thread")
                            .replace(R.id.fragmentcontainer, fragment)
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.forum_new_thread:
                CreateThreadFragment newthread = new CreateThreadFragment();
                newthread.setArguments(args);
                try {
                    fragmentManager.beginTransaction()
                            .addToBackStack("NewPost")
                            .replace(R.id.fragmentcontainer, newthread)
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.gotolastpage:
                openThread(args.getString("Url"), args.getInt("NumPages"), args.getInt("NumPages"), args.getString("ThreadName"));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        }
        super.onBackPressed();
    }


    // Bundle should include:
    // Url, number of pages, and current page
    @Override
    public void updateThread(final Bundle o) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Pops back to the "Thread-view" of the current forum
                getSupportFragmentManager().popBackStack("Threads", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                // Retrieve bundle info
                String url = o.getString("Url");
                int position = o.getInt("CurrentPage");
                int numpages = o.getInt("NumPages");
                String threadname = o.getString("ThreadName");
                // Reload thread
                openThread(url,numpages,position, threadname);
            }
        });
    }

    @Override
    public void updateForum(final Bundle o) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Pop back two steps
                FragmentManager f = getSupportFragmentManager();
                getSupportFragmentManager().popBackStack("Forums", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                // Retrieve stuff in bundle
                String url = o.getString("ForumUrl");
                String forumname = o.getString("ForumName");
                int numpages = o.getInt("NumPages");

                // Reopen the forum.
                openForum(url, numpages, forumname);
            }
        });

    }
}
