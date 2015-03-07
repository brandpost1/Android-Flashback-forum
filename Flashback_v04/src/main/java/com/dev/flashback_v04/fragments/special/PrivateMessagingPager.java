package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;

/**
 * Created by Viktor on 2014-01-21.
 */
public class PrivateMessagingPager extends Fragment {

    private MainActivity mActivity;
    private PrivateMessagesPagingAdapter mPrivateMessagesPagingAdapter;
    private ViewPager mViewPager;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
        mPrivateMessagesPagingAdapter = new PrivateMessagesPagingAdapter(getChildFragmentManager());

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.messages_layout, null);
        mViewPager = (ViewPager) v.findViewById(R.id.viewpager);
		mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(mPrivateMessagesPagingAdapter);

		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				updateFragment();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

        return v;
    }

	private void updateFragment() {
		// Could break if tag is changed.
		Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + mViewPager.getCurrentItem());

		if (page != null) {
			((PMFragment)page).getData();
		}
	}

    @Override
    public void onPause() {
        mActivity.getSupportActionBar().setTitle("");
        super.onPause();
    }

    private class PrivateMessagesPagingAdapter extends FragmentPagerAdapter {

        int NUM_PAGES = 2;

        public PrivateMessagesPagingAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Inbox";
                case 1:
                    return "Skickade";
            }
            return super.getPageTitle(position);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
					Bundle inboxbundle = new Bundle();
					inboxbundle.putInt("PageNumber", 1);
					inboxbundle.putInt("NumPages", 1);
					inboxbundle.putInt("FragmentType", 0);
					inboxbundle.putBoolean("ShowInstant", true);
                    // Inbox fragment
                    PMFragment inbox = new PMFragment();
					inbox.setArguments(inboxbundle);
                    return inbox;
                case 1:
					Bundle outboxbundle = new Bundle();
					outboxbundle.putInt("PageNumber", 1);
					outboxbundle.putInt("NumPages", 1);
					outboxbundle.putInt("FragmentType", -1);
					outboxbundle.putBoolean("ShowInstant", false);
                    // Outbox fragment
					PMFragment outbox = new PMFragment();
					outbox.setArguments(outboxbundle);
                return outbox;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
