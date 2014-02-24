package com.dev.flashback_v04.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.flashback_v04.GlobalHelper;
import com.dev.flashback_v04.R;

import java.io.Serializable;

/**
 * Created by Viktor on 2013-07-16.
 */
public class WrapperFragment extends Fragment {

	private MyFragmentPagerAdapter mMyMyFragmentPagerAdapter;
	private ViewPager mViewPager;

	public WrapperFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        /*
		* Initialize the Fragmentadapter.
		* */
        mMyMyFragmentPagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager());


		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		/*
		* Inflate the fragmentlayout.
		* */
		View v = inflater.inflate(R.layout.wrapper_layout, null);

		/*
		* Initialize the viewpager.
		* */
		mViewPager = (ViewPager)v.findViewById(R.id.viewpager);
        mViewPager.setAdapter(mMyMyFragmentPagerAdapter);
        mViewPager.setCurrentItem(getArguments().getInt("Position"));

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);


	}

    @Override
    public void onResume() {

        super.onResume();
    }



    /*
     * Inner PagerAdapter-class
     */
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

		private int FRAG_TYPE = 0;
		public int NUM_PAGES = 3;
		Bundle arguments;

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
			FRAG_TYPE = getArguments().getInt("FragmentType");
			NUM_PAGES = getArguments().getInt("NumPages");
            GlobalHelper.setPagerAdapter(this);

			notifyDataSetChanged();
		}

		@Override
		public Fragment getItem(int index) {

			switch (FRAG_TYPE) {
				case 0:
					Bundle catargs = new Bundle();
					catargs.putInt("index", index);
					ShowCategoriesFragment categoriesFragment = new ShowCategoriesFragment();
					categoriesFragment.setArguments(arguments);
					return categoriesFragment;
				case 1:
					Bundle forumargs = new Bundle();
					forumargs.putInt("index", index);
					forumargs.putStringArrayList("Categories", getArguments().getStringArrayList("Categories"));
                    forumargs.putStringArrayList("CategoryNames", getArguments().getStringArrayList("CategoryNames"));
					ShowForumsFragment forumsFragment = new ShowForumsFragment();
					forumsFragment.setArguments(forumargs);
					return forumsFragment;
				case 2:
					Bundle threadsargs = new Bundle();
					threadsargs.putInt("index", index);
					threadsargs.putInt("NumPages", getArguments().getInt("NumPages"));
                    threadsargs.putString("ForumName", getArguments().getString("ForumName"));
					threadsargs.putString("Url", getArguments().getString("Url"));
					ShowThreadsFragment threadsFragment = new ShowThreadsFragment();
					threadsFragment.setArguments(threadsargs);
					return threadsFragment;
				case 3:
					Bundle postsargs = new Bundle();
					postsargs.putInt("index", index);
					postsargs.putString("Url", getArguments().getString("Url"));
                    postsargs.putInt("NumPages", getArguments().getInt("NumPages"));
                    postsargs.putString("ThreadName", getArguments().getString("ThreadName"));
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
