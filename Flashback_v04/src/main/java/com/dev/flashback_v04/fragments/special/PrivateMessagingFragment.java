package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import android.support.v4.view.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;


import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.fragments.ShowCategoriesFragment;

/**
 * Created by Viktor on 2014-01-21.
 */
public class PrivateMessagingFragment extends Fragment {

    private MainActivity mActivity;
    private PrivateMessagesPagingAdapter mPrivateMessagesPagingAdapter;
    private ViewPager mViewPager;

    public PrivateMessagingFragment() {

    }

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
        mViewPager.setAdapter(mPrivateMessagesPagingAdapter);
        mActivity.getSupportActionBar().setTitle("Meddelanden");

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.messaging_menu, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        mActivity.getSupportActionBar().setTitle("");
        super.onPause();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
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
                    // Inbox fragment
                    ShowCategoriesFragment frag1 = new ShowCategoriesFragment();
                    return frag1;
                case 1:
                    // Outbox fragment
                    ShowCategoriesFragment frag2 = new ShowCategoriesFragment();
                return frag2;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}