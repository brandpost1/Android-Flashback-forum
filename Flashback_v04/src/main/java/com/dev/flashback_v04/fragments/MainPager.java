package com.dev.flashback_v04.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.interfaces.Callback;

/**
 * Created by Viktor on 2014-03-12.
 */
public class MainPager extends Fragment {

    // Retrieves the number of pages for the PagerAdapter
    public class GetSizeTask extends AsyncTask<String, String, Integer[]> {

        private final ProgressDialog dialog;
        private Parser mParser;
        private Callback mCallback;
        private boolean fixedNumPages;

        public GetSizeTask(Activity mActivity, Callback callback, boolean fixedNumPages) {
            mParser = new Parser(mActivity);
            mCallback = callback;
            dialog = new ProgressDialog(mActivity);
            dialog.setCancelable(false);
            this.fixedNumPages = fixedNumPages;
        }

        @Override
        protected Integer[] doInBackground(String... strings) {
			Integer[] resultArr = new Integer[3];
            int result = 1;
			int threadId = 0;
			int threadPos = 0;
            int forumType = Integer.parseInt(strings[0]);
            String url = strings[1];
            switch (forumType) {
                case 0:
                    result = 1;
                    break;
                case 1:
                    // Open category
                    result = 15;
                    break;
                case 2:
                    // Open forum
                    result = mParser.getForumNumPages(url);
                    break;
                case 3:
                    // Open thread
                    result = mParser.getThreadNumPages(url);
					threadId = mParser.getThreadId(url);
					threadPos = mParser.getThreadPosition(url);
                    break;
            }
			resultArr[0] = result;
			resultArr[1] = threadId;
			resultArr[2] = threadPos;
			return resultArr;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Ett ögonblick bara..");
            if(!fixedNumPages)
                dialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer[] result) {
            if(dialog.isShowing()) {
                dialog.dismiss();
            }
            mCallback.onTaskComplete(result);
        }

    }

    private ViewPager mViewPager;
    private Activity mActivity;
    private Callback mCallback;
    private MainPagerAdapter mPagerAdapter;
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

        switch(mPagerBundle.getInt("FragmentType")) {
            case 0:
                break;
            case 1:
                outState.putStringArrayList("Categories", mPagerBundle.getStringArrayList("Categories"));
                outState.putStringArrayList("CategoryNames", mPagerBundle.getStringArrayList("CategoryNames"));
                break;
            case 2:
                outState.putString("ForumName", mPagerBundle.getString("ForumName"));
                outState.putString("Url", mPagerBundle.getString("Url"));
                break;
            case 3:
                outState.putString("ThreadName", mPagerBundle.getString("ThreadName"));
                outState.putString("Url", mPagerBundle.getString("Url"));
                break;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mPagerBundle = getArguments();
        mPagerAdapter = new MainPagerAdapter(getChildFragmentManager());

        if(savedInstanceState == null) {
            pageNumber = mPagerBundle.getInt("Position");
            fragmentType = mPagerBundle.getInt("FragmentType");
            mCallback = new Callback<Integer[]>() {
                @Override
                public void onTaskComplete(Integer[] data) {
                    numPages = data[0];
                    mPagerBundle.putInt("NumPages", data[0]);
					if(mPagerBundle.getInt("FragmentType") == 3)
						mPagerBundle.putInt("ThreadId", data[1]);
					// if opening a thread and pageNumber equals -1, then we don't know the pagenumber beforehand.
					if(mPagerBundle.getInt("Position") == -1 && mPagerBundle.getInt("FragmentType") == 3) {
						pageNumber = data[2];
						mPagerBundle.putInt("Position", data[2]);
						String newUrl = "https://www.flashback.org/t" + data[1];
						mPagerBundle.putString("Url", newUrl);
					}
                    mPagerAdapter.setData(mPagerBundle);
                    mPagerAdapter.notifyDataSetChanged();
                    mViewPager.setCurrentItem(mPagerBundle.getInt("Position")-1);
                }
			};
            String fragmentType = String.valueOf(mPagerBundle.getInt("FragmentType"));
            String url = mPagerBundle.getString("Url");
            boolean fixedNumPages = false;

            // True for initial view and the following view if a category is clicked
            if(mPagerBundle.getInt("NumPages") != 0) {
                fixedNumPages = true;
            }

            mGetSizeTask = new GetSizeTask(mActivity, mCallback, fixedNumPages);
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

    public class MainPagerAdapter extends FragmentPagerAdapter {

        int NUM_PAGES = 0;
        int FRAG_TYPE;
        Bundle mData;

        public MainPagerAdapter(FragmentManager fm) {
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
                    Bundle catargs = new Bundle();
                    catargs.putInt("index", index);
                    ShowCategoriesFragment categoriesFragment = new ShowCategoriesFragment();
                    categoriesFragment.setArguments(catargs);
                    return categoriesFragment;
                case 1:
                    Bundle forumargs = new Bundle();
                    forumargs.putInt("index", index);
                    forumargs.putStringArrayList("Categories", mData.getStringArrayList("Categories"));
                    forumargs.putStringArrayList("CategoryNames", mData.getStringArrayList("CategoryNames"));
                    ShowForumsFragment forumsFragment = new ShowForumsFragment();
                    forumsFragment.setArguments(forumargs);
                    return forumsFragment;
                case 2:
                    Bundle threadsargs = new Bundle();
                    threadsargs.putInt("index", index);
                    threadsargs.putInt("NumPages", mData.getInt("NumPages"));
                    threadsargs.putString("ForumName", mData.getString("ForumName"));
                    threadsargs.putString("Url", mData.getString("Url"));
                    ShowThreadsFragment threadsFragment = new ShowThreadsFragment();
                    threadsFragment.setArguments(threadsargs);
                    return threadsFragment;
                case 3:
                    Bundle postsargs = new Bundle();
                    postsargs.putInt("index", index);
                    postsargs.putString("Url", mData.getString("Url"));
                    postsargs.putInt("NumPages", mData.getInt("NumPages"));
                    postsargs.putString("ThreadName", mData.getString("ThreadName"));
					postsargs.putInt("ThreadId", mData.getInt("ThreadId"));
					postsargs.putInt("ThreadPosition", mData.getInt("Position"));
                    ShowPostsFragment postsFragment = new ShowPostsFragment();
                    postsFragment.setArguments(postsargs);
                    return postsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
