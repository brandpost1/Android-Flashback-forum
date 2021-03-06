package com.dev.flashback_v04.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.Constants;
import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.SharedPrefs;
import com.dev.flashback_v04.adapters.DrawerAdapter;
import com.dev.flashback_v04.adapters.ShowPostsAdapter;
import com.dev.flashback_v04.asynctasks.LoginTask;
import com.dev.flashback_v04.asynctasks.special.DeleteSubsTask;
import com.dev.flashback_v04.asynctasks.special.PutEditedPostTask;
import com.dev.flashback_v04.asynctasks.special.SendPMTask;
import com.dev.flashback_v04.fragments.MainPager;
import com.dev.flashback_v04.fragments.SecondaryPager;
import com.dev.flashback_v04.fragments.ShowCategoriesFragment;
import com.dev.flashback_v04.fragments.special.CreateThreadFragment;
import com.dev.flashback_v04.fragments.special.CurrentThreadsFragment;
import com.dev.flashback_v04.fragments.special.EditPostFragment;
import com.dev.flashback_v04.fragments.special.NewPMFragment;
import com.dev.flashback_v04.fragments.special.NewPostsFragment;
import com.dev.flashback_v04.fragments.special.NewThreadsFragment;
import com.dev.flashback_v04.fragments.special.PostReplyFragment;
import com.dev.flashback_v04.fragments.special.PrivateMessagingPager;
import com.dev.flashback_v04.fragments.special.ViewPMFragment;
import com.dev.flashback_v04.interfaces.OnOptionSelectedListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Viktor on 2013-07-13.
 */
public class MainActivity extends ActionBarActivity implements OnOptionSelectedListener {

	public FragmentManager fragmentManager;

    /*
    Banner ads
    * */
    AdView adView;
    boolean ADS_ACTIVATED;

	/*
	Navigation drawer
	* */
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
    DrawerAdapter mAdapter;

    private Activity activity;
	/*
	* Constructor
	* */
	public MainActivity() {
        activity = this;
	}

	public void openPrivateMessage(Bundle args) {
		ViewPMFragment fragment = new ViewPMFragment();
		fragment.setArguments(args);
		try {
			fragmentManager.beginTransaction()
					.addToBackStack("Message")
					.replace(R.id.fragmentcontainer, fragment)
					.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void searchThread(String query) {
		Fragment fragment = new SecondaryPager();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 2);
		args.putString("SearchQuery", query);
		fragment.setArguments(args);

		try {
			fragmentManager.beginTransaction()
					.addToBackStack("ThreadSearch")
					.replace(R.id.fragmentcontainer, fragment)
					.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void searchForum(String query) {
		Fragment fragment = new SecondaryPager();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 1);
		args.putString("SearchQuery", query);
		fragment.setArguments(args);

		try {
			fragmentManager.beginTransaction()
					.addToBackStack("ThreadSearch")
					.replace(R.id.fragmentcontainer, fragment)
					.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	* Call this to open a specific thread
	* */
	public void openThread(String url, int numPages, int position, String threadname) {
		Fragment fragment = new MainPager();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 3);
		args.putInt("CurrentPage", position);
		if(numPages != 0)
			args.putInt("NumberOfPages", numPages);
        args.putString("ThreadName", threadname);
		args.putString("Url", url);
		fragment.setArguments(args);

		/*
		* Replace the old fragment
		* */
		try {
			fragmentManager.beginTransaction()
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
	public void openForum(String url, int numpages, String forumname) {
		Fragment fragment = new MainPager();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 2);
        args.putString("ForumName", forumname);
		if(numpages != -1)
			args.putInt("NumberOfPages", numpages);
		args.putInt("CurrentPage", 1);
		args.putString("Url", url);
		fragment.setArguments(args);
		/*
		* Replace the old fragment
		* */

 		try {
			fragmentManager.beginTransaction()
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
		Fragment fragment = new MainPager();
		Bundle args = new Bundle();
		args.putInt("FragmentType", 1);
		args.putInt("NumberOfPages", 15);
        args.putStringArrayList("CategoryNames", catName);
		args.putStringArrayList("Categories", categories);
		args.putString("Url", url);
		args.putInt("CurrentPage", position);
		fragment.setArguments(args);

        /*
		* Replace the old fragment
		* */
        try {
            fragmentManager.beginTransaction()
                    .addToBackStack("Categories")
                    .replace(R.id.fragmentcontainer, fragment)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putInt("VersionCode", nextVersion)
                    .commit();


			Calendar nowdate = Calendar.getInstance();
			String toDateAsString = "12/01/2014";
			Date toDate = null;
			try {
				toDate = new SimpleDateFormat("MM/dd/yyyy").parse(toDateAsString);
			} catch (ParseException e) {
				e.printStackTrace();
			}


			if (Constants.type == Constants.Type.FREE) {
				if (nowdate.getTimeInMillis()*1000 < toDate.getTime()*1000) {
					View sale = getLayoutInflater().inflate(R.layout.eastersale, null);
					ImageView bunny = (ImageView) sale.findViewById(R.id.bunnylink);
					bunny.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							try {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.brandpost.flashback")));
							} catch (android.content.ActivityNotFoundException anfe) {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.brandpost.flashback")));
							}
						}
					});
					final AlertDialog easterSale = new AlertDialog.Builder(this)
							.setView(sale)
							.create();
					easterSale.show();
				}
			}

        }

    }

	@Override
	protected void onStop() {
		super.onStop();

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			HttpResponseCache cache = HttpResponseCache.getInstalled();
			if (cache != null) {
				cache.flush();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		if(Constants.type == Constants.Type.FREE) {
			ADS_ACTIVATED = true;
		} else {
			ADS_ACTIVATED = false;
		}

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
            String testDeviceEmu = this.getResources().getString(R.string.test_device_emu);
            String testDeviceEmu2 = this.getResources().getString(R.string.test_device_emu2);
            AdRequest request = new AdRequest.Builder()
                    .addTestDevice(testDevice)
					.addTestDevice(testDeviceEmu)
					.addTestDevice(testDeviceEmu2)
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
		* Set up the navigation-drawer
		* */

 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mAdapter = new DrawerAdapter(this);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                int i = (int)adapterView.getItemIdAtPosition(position);
                int backstackcount;
				String userID;
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
                        //Toast.makeText(getBaseContext(), "Sök finns för tillfället bara inuti forum samt inuti trådar. Avancerad sökning kommer eventuellt senare.", Toast.LENGTH_LONG).show();

						final View searchview = getLayoutInflater().inflate(R.layout.searchpopup, null);

						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						builder.setView(searchview);
						builder.setTitle("Sök i hela forumet");
						builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String query = "";
								EditText textbox = (EditText)searchview.findViewById(R.id.searchBox);
								RadioGroup group = (RadioGroup)searchview.findViewById(R.id.searchgroup);
								int selected = group.getCheckedRadioButtonId();
								if(selected == R.id.searchPosts) {
									query = "https://www.flashback.org/sok/" + textbox.getText() + "?sp=1";
									query = query.replace(" ", "+");
									searchThread(query);
								} else if(selected == R.id.searchThreads) {
									query = "https://www.flashback.org/sok/" + textbox.getText();
									query = query.replace(" ", "+");
									searchForum(query);
								}
							}
						});

						AlertDialog sdialog = builder.create();
						sdialog.show();
                        mDrawerLayout.closeDrawer(Gravity.LEFT);

                        break;
                    case 5:
						SecondaryPager myPostsPager = new SecondaryPager();
						userID = Integer.toString(SharedPrefs.getPreference(activity, "user", "ID"));
						Bundle myPostsBundle = new Bundle();
						myPostsBundle.putInt("FragmentType", 2);
						myPostsBundle.putString("UserId", userID);
						myPostsPager.setArguments(myPostsBundle);
						backstackcount = getSupportFragmentManager().getBackStackEntryCount();

						// Same as above
						try {
							if(getSupportFragmentManager().getBackStackEntryAt(backstackcount-1).getName().equals("MyPosts")
									|| getSupportFragmentManager().getBackStackEntryAt(backstackcount-2).getName().equals("MyPosts")) {
								getSupportFragmentManager().popBackStack("MyPosts", FragmentManager.POP_BACK_STACK_INCLUSIVE);
							}
						} catch (Exception e) {

						}

                        try {
                            fragmentManager.beginTransaction()
                                    .addToBackStack("MyPosts")
                                    .replace(R.id.fragmentcontainer, myPostsPager)
                                    .commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 6:
                        SecondaryPager myThreadsPager = new SecondaryPager();
						userID = Integer.toString(SharedPrefs.getPreference(activity, "user", "ID"));
                        Bundle myThreadsBundle = new Bundle();
                        myThreadsBundle.putInt("FragmentType", 1);
						myThreadsBundle.putString("UserId", userID);
                        myThreadsPager.setArguments(myThreadsBundle);
						backstackcount = getSupportFragmentManager().getBackStackEntryCount();

						// Same as above
						try {
							if(getSupportFragmentManager().getBackStackEntryAt(backstackcount-1).getName().equals("MyThreads")
									|| getSupportFragmentManager().getBackStackEntryAt(backstackcount-2).getName().equals("MyThreads")) {
								getSupportFragmentManager().popBackStack("MyThreads", FragmentManager.POP_BACK_STACK_INCLUSIVE);
							}
						} catch (Exception e) {

						}
                        try {
                            fragmentManager.beginTransaction()
                                    .addToBackStack("MyThreads")
                                    .replace(R.id.fragmentcontainer, myThreadsPager)
                                    .commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 7:
                        SecondaryPager myQuotesPager = new SecondaryPager();
                        Bundle myQuotesBundle = new Bundle();
                        myQuotesBundle.putInt("FragmentType", 0);
                        myQuotesPager.setArguments(myQuotesBundle);
						backstackcount = getSupportFragmentManager().getBackStackEntryCount();

						// Same as above
						try {
							if(getSupportFragmentManager().getBackStackEntryAt(backstackcount-1).getName().equals("MyQuotes")
									|| getSupportFragmentManager().getBackStackEntryAt(backstackcount-2).getName().equals("MyQuotes")) {
								getSupportFragmentManager().popBackStack("MyQuotes", FragmentManager.POP_BACK_STACK_INCLUSIVE);
							}
						} catch (Exception e) {

						}

                        try {
                            fragmentManager.beginTransaction()
                                    .addToBackStack("MyQuotes")
                                    .replace(R.id.fragmentcontainer, myQuotesPager)
                                    .commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 8:
                        PrivateMessagingPager mPrivateMessagingPager = new PrivateMessagingPager();

						backstackcount = getSupportFragmentManager().getBackStackEntryCount();

						// Same as above
						try {
							if(getSupportFragmentManager().getBackStackEntryAt(backstackcount-1).getName().equals("MyMessages")
									|| getSupportFragmentManager().getBackStackEntryAt(backstackcount-2).getName().equals("MyMessages")) {
								getSupportFragmentManager().popBackStack("MyMessages", FragmentManager.POP_BACK_STACK_INCLUSIVE);
							}
						} catch (Exception e) {

						}
                        try {
                            fragmentManager.beginTransaction()
                                    .addToBackStack("MyMessages")
                                    .replace(R.id.fragmentcontainer, mPrivateMessagingPager)
                                    .commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 9:
                        try {
                            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                            startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case 10:
                        TextView text = (TextView)view.findViewById(R.id.item_text);
                        if(!LoginHandler.loggedIn(activity)) {
                            showLoginDialog(text);
                        } else {
                            LoginHandler.logout(activity);
                            text.setText("Logga in");
                            getSupportFragmentManager().popBackStack("Start", 0);
                            mDrawerLayout.closeDrawer(Gravity.LEFT);
                            refreshDrawer();
                        }
                        break;
					case 11:
						SecondaryPager mySubscriptionsPager = new SecondaryPager();
						Bundle mySubsBundle = new Bundle();
						mySubsBundle.putInt("FragmentType", 3);
						mySubscriptionsPager.setArguments(mySubsBundle);

						backstackcount = getSupportFragmentManager().getBackStackEntryCount();

						// Same as above
						try {
							if(getSupportFragmentManager().getBackStackEntryAt(backstackcount-1).getName().equals("MySubscriptions")
									|| getSupportFragmentManager().getBackStackEntryAt(backstackcount-2).getName().equals("MySubscriptions")) {
								getSupportFragmentManager().popBackStack("MySubscriptions", FragmentManager.POP_BACK_STACK_INCLUSIVE);
							}
						} catch (Exception e) {

						}
						try {
							fragmentManager.beginTransaction()
									.addToBackStack("MySubscriptions")
									.replace(R.id.fragmentcontainer, mySubscriptionsPager)
									.commit();
						} catch (Exception e) {
							e.printStackTrace();
						}
						mDrawerLayout.closeDrawer(Gravity.LEFT);
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
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		mDrawerLayout.setScrimColor(getResources().getColor(R.color.transparent));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

		/*
		* Add the wrapperfragment.
		* Each wrapperfragment contains its own viewpager.
		* */
		if(savedInstanceState == null) {
			Fragment fragment = new ShowCategoriesFragment();

			fragmentManager
					.beginTransaction()
					.addToBackStack("Start")
					.replace(R.id.fragmentcontainer, fragment)
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
			case R.id.new_private_message:
				NewPMFragment newPMFragment = new NewPMFragment();
				newPMFragment.setArguments(args);
				try {
					fragmentManager.beginTransaction()
							.addToBackStack("NewPrivateMessage")
							.replace(R.id.fragmentcontainer, newPMFragment)
							.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.forum_update:
				updateForum(args, true);
				break;
            case R.id.gotolastpage:
                openThread(args.getString("Url"), args.getInt("NumberOfPages"), args.getInt("LastPage"), args.getString("ThreadName"));
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
    public void updateThread(final Bundle bundle) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Pops back to the "Thread-view" of the current forum
                getSupportFragmentManager().popBackStack("Threads", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                // Retrieve bundle info
                String url = bundle.getString("Url");
                int position = bundle.getInt("CurrentPage");

                String threadname = bundle.getString("ThreadName");
                // Reload thread
                openThread(url, 0, position, threadname);
            }
        });
    }


    public void updateForum(final Bundle o, final boolean onestep) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
				if(onestep) {
					getSupportFragmentManager().popBackStack();
				} else {
					// Pop back two steps
					getSupportFragmentManager().popBackStack("Forums", FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}

                // Retrieve stuff in bundle
                String url = o.getString("ForumUrl");
                String forumname = o.getString("ForumName");
				int numpages = o.getInt("NumberOfPages");

                // Reopen the forum.
                openForum(url, numpages, forumname);
            }
        });
    }

    public void refreshDrawer() {
        mDrawerList.setAdapter(new DrawerAdapter(this));
    }

	public void sendPrivateMessage(Bundle bundle) {
		// Pop back stack
		getSupportFragmentManager().popBackStack();

		SendPMTask sendPMTask = new SendPMTask(this, bundle);

		try {
			sendPMTask.execute();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void deleteSubscription(Bundle bundle) {
		// Pop back stack
		getSupportFragmentManager().popBackStack();

		// Remove selected subs
		DeleteSubsTask deleteSubsTask = new DeleteSubsTask(activity, bundle);
		try {
			deleteSubsTask.execute();
		} catch (Exception e) {
			Toast.makeText(activity, "Kunde inte avsluta..", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}


		// Reopen
		SecondaryPager mySubscriptionsPager = new SecondaryPager();
		Bundle mySubsBundle = new Bundle();
		mySubsBundle.putInt("FragmentType", 3);
		mySubscriptionsPager.setArguments(mySubsBundle);

		try {
			fragmentManager.beginTransaction()
					.addToBackStack("MySubscriptions")
					.replace(R.id.fragmentcontainer, mySubscriptionsPager)
					.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void editPost(Bundle bundle) {
		EditPostFragment editPostFragment = new EditPostFragment();
		editPostFragment.setArguments(bundle);
		try {
			fragmentManager.beginTransaction()
					.addToBackStack("EditPost")
					.replace(R.id.fragmentcontainer, editPostFragment)
					.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void putEditedPost(Bundle bundle) {
		// Pop back stack
		getSupportFragmentManager().popBackStack();

		PutEditedPostTask putEditedPostTask = new PutEditedPostTask(activity);
		try {
			putEditedPostTask.execute(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
