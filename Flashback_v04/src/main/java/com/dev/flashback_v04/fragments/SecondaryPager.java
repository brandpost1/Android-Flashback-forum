package com.dev.flashback_v04.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.fragments.special.UserPostsFragment;
import com.dev.flashback_v04.fragments.special.MyQuotesFragment;
import com.dev.flashback_v04.fragments.special.MySubscriptionsFragment;
import com.dev.flashback_v04.fragments.special.UserThreadsFragment;
import com.dev.flashback_v04.interfaces.Callback;

/**
 * Created by Viktor on 2014-03-12.
 */
public class SecondaryPager extends Fragment {

    // Retrieves the number of pages for the PagerAdapter
    public class GetSizeTask extends AsyncTask<String, String, Integer> {

        private final ProgressDialog dialog;
        private Parser mParser;
        private Callback mCallback;

        public GetSizeTask(Activity mActivity, Callback callback) {
            mParser = new Parser(mActivity);
            mCallback = callback;
            dialog = new ProgressDialog(mActivity);
            dialog.setCancelable(false);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int result = 1;
            int fragType = Integer.parseInt(strings[0]);
            String url = strings[1];
            switch (fragType) {
                // MyQuotes
                case 0:
                    result = mParser.myQuotesPages(url);
                    break;
                // MyThreads
                case 1:
                    result = mParser.myThreadsPages(url);
                    break;
                // MyPosts
                case 2:
                    result = mParser.myPostsPages(url);
                    break;
				// Subscriptions
				case 3:
					result = mParser.getSubscriptionPages(url);
					break;
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Laddar..");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(dialog.isShowing()) {
                dialog.dismiss();
            }
            mCallback.onTaskComplete(result);
        }

    }

    private ViewPager mViewPager;
    private Activity mActivity;
    private Callback mCallback;
    private SecondaryPagerAdapter mPagerAdapter;
    private GetSizeTask mGetSizeTask;
    private Bundle mPagerBundle;

    int numPages;
    int pageNumber;
    int fragmentType;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("NumPages", numPages);
        outState.putInt("Page", pageNumber);
        outState.putInt("FragmentType", fragmentType);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mPagerBundle = getArguments();
        mPagerAdapter = new SecondaryPagerAdapter(getChildFragmentManager());

        if(savedInstanceState == null) {
            pageNumber = 0;
            fragmentType = mPagerBundle.getInt("FragmentType");
            mCallback = new Callback<Integer>() {
                @Override
                public void onTaskComplete(Integer data) {
                    numPages = data;
                    mPagerBundle.putInt("NumPages", data);
                    mPagerAdapter.setData(mPagerBundle);
                    mPagerAdapter.notifyDataSetChanged();
                    mViewPager.setCurrentItem(pageNumber);
                }
            };
            String fragmentType = String.valueOf(this.fragmentType);
            String url = "";

            String userId = getArguments().getString("UserId");
			String searchQuery = getArguments().getString("SearchQuery");
            switch (this.fragmentType) {
                case 0:
                    String userName = PreferenceManager.getDefaultSharedPreferences(mActivity).getString("UserName", "");
                    url = "https://www.flashback.org/sok/quote=" + userName + "?sp=1&so=d";
                    break;
                case 1:
					if(searchQuery != null) {
						url = searchQuery;
						mPagerBundle.putString("Search", searchQuery);
						break;
					}
                    url = "https://www.flashback.org/find_threads_by_user.php?userid=" + userId;
                    break;
                case 2:
					String threadId = getArguments().getString("ThreadId");
					if(searchQuery != null) {
						url = searchQuery;
						mPagerBundle.putString("Search", searchQuery);
						break;
					}
					if(threadId == null) {
						url = "https://www.flashback.org/find_posts_by_user.php?userid=" + userId;
					} else {
						url = "https://www.flashback.org/find_posts_by_user.php?userid="+ userId +"&threadid=" + threadId;
					}
                    break;
				case 3:
					url = "https://www.flashback.org/subscription.php?folderid=all";
					break;
            }

            mGetSizeTask = new GetSizeTask(mActivity, mCallback);
            mGetSizeTask.execute(fragmentType, url);
        } else {
            numPages = savedInstanceState.getInt("NumPages");
            pageNumber = savedInstanceState.getInt("Page");
            fragmentType = savedInstanceState.getInt("FragmentType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.wrapper_layout, null);
        mViewPager = (ViewPager)v.findViewById(R.id.viewpager);
        if(savedInstanceState != null) {
            mPagerAdapter.setData(savedInstanceState);
            mPagerAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(savedInstanceState.getInt("Position"));
        }
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pageNumber = position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return v;
    }

    public class SecondaryPagerAdapter extends FragmentPagerAdapter {

        int NUM_PAGES = 0;
        int FRAG_TYPE;
        Bundle mData;

        public SecondaryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setData(Bundle data) {
            NUM_PAGES = data.getInt("NumPages");
            FRAG_TYPE = data.getInt("FragmentType");
            mData = data;
        }

        @Override
        public Fragment getItem(int index) {

            switch (FRAG_TYPE) {
                case 0:
                    MyQuotesFragment myQuotes = new MyQuotesFragment();
                    Bundle myQuotesBundle = new Bundle();
                    myQuotesBundle.putInt("PageNumber", index+1);
                    myQuotesBundle.putInt("NumPages", NUM_PAGES);
                    myQuotes.setArguments(myQuotesBundle);
                    return myQuotes;
                case 1:
                    UserThreadsFragment myThreads = new UserThreadsFragment();
                    Bundle myThreadsBundle = new Bundle();
                    myThreadsBundle.putInt("PageNumber", index+1);
                    myThreadsBundle.putInt("NumPages", NUM_PAGES);
                    myThreadsBundle.putString("ForumId", mData.getString("ForumId"));
					myThreadsBundle.putString("UserId", mData.getString("UserId"));
					myThreadsBundle.putString("Search", mData.getString("Search"));
					myThreads.setArguments(myThreadsBundle);
                    return myThreads;
                case 2:
					UserPostsFragment myPosts = new UserPostsFragment();
					Bundle myPostsBundle = new Bundle();
					myPostsBundle.putInt("PageNumber", index+1);
					myPostsBundle.putInt("NumPages", NUM_PAGES);
					myPostsBundle.putString("UserId", mData.getString("UserId"));
					myPostsBundle.putString("ThreadId", mData.getString("ThreadId"));
					myPostsBundle.putString("Search", mData.getString("Search"));
					myPosts.setArguments(myPostsBundle);
					return myPosts;
				case 3:
					MySubscriptionsFragment mySubs = new MySubscriptionsFragment();
					Bundle mySubsBundle = new Bundle();
					mySubsBundle.putInt("PageNumber", index+1);
					mySubsBundle.putInt("NumPages", NUM_PAGES);
					mySubs.setArguments(mySubsBundle);
					return mySubs;

            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
