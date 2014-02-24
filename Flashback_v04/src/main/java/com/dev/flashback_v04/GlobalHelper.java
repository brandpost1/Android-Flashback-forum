package com.dev.flashback_v04;

import com.dev.flashback_v04.fragments.WrapperFragment;

/**
 * Created by Viktor on 2013-11-13.
 */
public class GlobalHelper {

    /*
    * Since the app has to enter a forum in a category to be able to get the correct number of pages in that forum,
    * the app can only get that information after it has parsed the first page of that forum.
    * Therefore the threadparsertask has to update NUM_PAGES in the FragmentPagerAdapter in the OnPostExecute-method
    * The following static functions and variable provide a convenient, but ugly, way of handling this special case.
    * */
    private static WrapperFragment.MyFragmentPagerAdapter mPagerAdapter;

    public static void setPagerAdapter(WrapperFragment.MyFragmentPagerAdapter pageradapter) {
        mPagerAdapter = pageradapter;
    }

    public static WrapperFragment.MyFragmentPagerAdapter getPagerAdapter() {

        return mPagerAdapter;
    }
}
