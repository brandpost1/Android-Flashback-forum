package com.dev.flashback_v04.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.DrawerLayout;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.adapters.DrawerAdapter;
import com.dev.flashback_v04.asynctasks.LoginTask;
import com.dev.flashback_v04.fragments.MainPager;
import com.dev.flashback_v04.fragments.special.PostReplyFragment;
import com.dev.flashback_v04.interfaces.OnOptionSelectedListener;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-07-13.
 */
public class MainActivity_test extends ActionBarActivity implements OnOptionSelectedListener {

	public FragmentManager fragmentManager;

    private static final int CONFIG_CAT_FORUMS = 0;
    private static final int CONFIG_FORUMS_THREADS = 1;
    private static final int CONFIG_THREADS_POSTS = 2;
    private static int CURRENT_APP_CONFIG;
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
	public MainActivity_test() {
        CURRENT_APP_CONFIG = CONFIG_CAT_FORUMS;

        activity = this;
	}

    public void reportPost(View v) {
        Toast.makeText(this, "Report post", Toast.LENGTH_SHORT).show();
    }

    public void normalquote(View v) {
        Toast.makeText(this, (String)v.getTag(R.id.QUOTE_MESSAGE_TAG), Toast.LENGTH_SHORT).show();
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
	public void openThread(String url, int numpages, int position) {
		Fragment fragment = new MainPager();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 3);
		args.putInt("NumPages", numpages);
        args.putInt("Position", position);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	* Call this to open a specific forum
	* */
	public void openForum(String url, int size) {
		Fragment fragment = new MainPager();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 2);
		args.putInt("NumPages", size); //TODO: get actual number of pages in that forum. Currently set to 5
		args.putString("Url", url);
		fragment.setArguments(args);


		/*
		* Replace the old fragment
		* */

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(CURRENT_APP_CONFIG == CONFIG_CAT_FORUMS) {
                // Open the forum in the rightmost container, and replace what is in the left container with what is in the right one

                try {
                    fragmentManager.beginTransaction()
                            .addToBackStack("Forums")
                            .replace(R.id.rightfragment, fragment)
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }


	}

	/*
	* Call this to open a specific category
	* */
	public void openCategory(String url, int position, ArrayList<String> categories) {
		/*
		* Initialize fragment.
		* Pass along arguments etc..
		* */
		Fragment fragment = new MainPager();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 1);
		args.putInt("NumPages", 14);
		args.putStringArrayList("Categories", categories);
		args.putString("Url", url);
		args.putInt("Position", position);
		fragment.setArguments(args);


        /*
		* Replace the old fragment
		* */
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Open the category in the rightmost container
            try {
                fragmentManager.beginTransaction()
                        .addToBackStack("Categories")

                        .replace(R.id.rightfragment, fragment, "forums_tag")
                        .commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Open the category in the current container, since it is portrait
        try {
            fragmentManager.beginTransaction()
                    .addToBackStack("Categories")
                    .replace(R.id.fragmentcontainer, fragment)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 1:
                        // Go back to start
                        getSupportFragmentManager().popBackStack("Categories", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 2:

                        break;
                    case 7:
                        TextView text = (TextView)view.findViewById(R.id.item_text);
                        if(!LoginHandler.loggedIn(activity)) {
                            showLoginDialog(text);
                        } else {
                            LoginHandler.logout(activity);
                            text.setText("Logga in");
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
				getActionBar().setTitle("");
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle("Meny");
				invalidateOptionsMenu();
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
			Fragment fragment = new MainPager();

			Bundle args = new Bundle();
			args.putInt("FragmentType", 0);
			args.putInt("NumPages", 1);

			fragment.setArguments(args);

			fragmentManager
					.beginTransaction()
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
        From ShowPostsFragment
        itemId is for example, R.id.thread_new_reply
    */
    @Override
    public void onOptionSelected(int itemId, Bundle args) {
        switch (itemId) {
            case R.id.thread_new_reply:
                PostReplyFragment fragment = new PostReplyFragment();
                try {
                    getSupportFragmentManager().beginTransaction()
                            .addToBackStack("Thread")
                            .replace(R.id.fragmentcontainer, fragment)
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.gotolastpage:
                openThread(args.getString("Url"), args.getInt("NumPages"), args.getInt("NumPages"));
                break;
        }
    }
}
