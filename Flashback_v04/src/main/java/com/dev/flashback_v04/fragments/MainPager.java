package com.dev.flashback_v04.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viktor on 2014-03-12.
 */
public class MainPager extends Fragment {

	// Retrieves the number of pages for the PagerAdapter
	public class GetSizeTask extends AsyncTask<String, String, Map<String, Integer>> {

		private final ProgressDialog dialog;
		private Parser mParser;
		private Callback mCallback;

		public GetSizeTask(Callback callback) {
			mParser = new Parser(mActivity);
			mCallback = callback;
			dialog = new ProgressDialog(mActivity);
			dialog.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Laddar..");
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Map<String, Integer> doInBackground(String... strings) {
			Map<String, Integer> result = new HashMap<String, Integer>();

			switch (fragmentType) {
				case 2:
					// Open forum
					result.put("PageCount", mParser.getForumNumPages(url));
					break;
				case 3:
					// Open thread
					result.put("PageCount", mParser.getThreadNumPages(url));
					// Only get pagenumber if needed.
					if(currentPage == 0)
						result.put("PageNumber", mParser.getThreadPosition(url));
					result.put("ThreadId", mParser.getThreadId(url));
					break;
			}

			return result;
		}

		@Override
		protected void onPostExecute(Map<String, Integer> result) {
            try {
                dialog.dismiss();
            } catch (Exception e) {

            }
			mCallback.onTaskComplete(result);
		}

	}

	private ViewPager mViewPager;
	private Activity mActivity;
	private Callback mCallback;
	private MainPagerAdapter mPagerAdapter;
	private Bundle mPagerBundle;

	int numberOfPages;
	int currentPage;
	int fragmentType;
	String url;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("NumberOfPages", numberOfPages);
		outState.putInt("CurrentPage", currentPage);
		outState.putInt("FragmentType", fragmentType);
		outState.putString("Url", url);
	}

	// https://www.flashback.org/forumdisplay.php?f=17&daysprune=-1
	// https://www.flashback.org/showthread.php?t=1776145&pp=20&page=23

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mPagerBundle = new Bundle();
		mPagerAdapter = new MainPagerAdapter(getChildFragmentManager());


		if(savedInstanceState == null) {
			if (getArguments().getInt("NumberOfPages") == 0) {
				// First get what we do have from the arguments
				fragmentType = getArguments().getInt("FragmentType");
				url = getArguments().getString("Url");
				currentPage = getArguments().getInt("CurrentPage");

				if(fragmentType == 2) {
					int index = getArguments().getString("Url").indexOf("/f") + 2;
					String forum_id = getArguments().getString("Url").substring(index);
					url = getArguments().getString("Url");
					url = "https://www.flashback.org/forumdisplay.php?daysprune=-1&f="+ forum_id +"";
				}

				// We don't know the number of pages for the ViewPager. So we have to get that info.
				// Create a callback which will return the data to us
				Callback sizeCallback = new Callback<Map<String, Integer>>() {
					@Override
					public void onTaskComplete(Map<String, Integer> data) {
						int numPages = data.get("PageCount");
						numberOfPages = numPages;

						// We might not have a correct pagenumber, if currentPage == -1. Let's check if one is returned to us!
						if(currentPage == 0 && data.get("PageNumber") != null) {
							currentPage = data.get("PageNumber");
							if(fragmentType == 3 && data.get("ThreadId") != null) {
								int threadId;
								threadId = data.get("ThreadId");
								url = "https://www.flashback.org/showthread.php?t="+ threadId;
							}
						}

						mPagerAdapter.setData(numPages, fragmentType);
						mPagerAdapter.notifyDataSetChanged();
						if(data.get("PageNumber") != null) {
							mViewPager.setCurrentItem(data.get("PageNumber") - 1, true);
						} else {
							mViewPager.setCurrentItem(currentPage - 1, true);
						}
					}
				};

				// Start asynctask which will get the info and return it via the callback.
				GetSizeTask getSizeTask = new GetSizeTask(sizeCallback);
				getSizeTask.execute();
			} else {
				// We know the number of pages to use in the ViewPager. We don't need to fetch it somewhere else.
				numberOfPages = getArguments().getInt("NumberOfPages");
				fragmentType = getArguments().getInt("FragmentType");
				currentPage = getArguments().getInt("CurrentPage");
				if(fragmentType == 3) {
					// Rewrite thread url of this form: https://www.flashback.org/t2389893
					int index = getArguments().getString("Url").indexOf("/t") + 2;
					String thread_id = getArguments().getString("Url").substring(index);
					url = "https://www.flashback.org/showthread.php?t="+ thread_id;
				} else {
					url = getArguments().getString("Url");
				}
			}
		} else {
			numberOfPages = savedInstanceState.getInt("NumberOfPages");
			currentPage = savedInstanceState.getInt("CurrentPage");
			fragmentType = savedInstanceState.getInt("FragmentType");
			url = savedInstanceState.getString("Url");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.wrapper_layout, null);
		mViewPager = (ViewPager)v.findViewById(R.id.viewpager);
		mViewPager.setAdapter(mPagerAdapter);

		if(savedInstanceState == null) {
			if(numberOfPages != 0) {
				mPagerAdapter.setData(numberOfPages, fragmentType);
				mPagerAdapter.notifyDataSetChanged();
				mViewPager.setCurrentItem(currentPage - 1, true);
			}
		} else {
			mPagerAdapter.setData(numberOfPages, fragmentType);
			mPagerAdapter.notifyDataSetChanged();
			mViewPager.setCurrentItem(currentPage - 1, true);
		}



		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				currentPage = position;
			}

			@Override
			public void onPageSelected(int position) {
				currentPage = position;
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		return v;
	}

	public class MainPagerAdapter extends FragmentStatePagerAdapter {

		int NUM_PAGES = 0;
		int FRAG_TYPE;

		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public void setData(int numpages, int fragtype) {
			NUM_PAGES = numpages;
			FRAG_TYPE = fragtype;
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
					forumargs.putStringArrayList("Categories", getArguments().getStringArrayList("Categories"));
					forumargs.putStringArrayList("CategoryNames", getArguments().getStringArrayList("CategoryNames"));
					ShowForumsFragment forumsFragment = new ShowForumsFragment();
					forumsFragment.setArguments(forumargs);
					return forumsFragment;
				case 2:
					Bundle threadsargs = new Bundle();
					threadsargs.putInt("index", index);
					threadsargs.putInt("NumberOfPages", NUM_PAGES);
					threadsargs.putString("ForumName", getArguments().getString("ForumName"));
					threadsargs.putString("Url", url);
					ShowThreadsFragment threadsFragment = new ShowThreadsFragment();
					threadsFragment.setArguments(threadsargs);
					return threadsFragment;
				case 3:
					Bundle postsargs = new Bundle();
					postsargs.putInt("index", index);
					postsargs.putString("Url", url);
					postsargs.putInt("NumberOfPages", NUM_PAGES);
					postsargs.putString("ThreadName", getArguments().getString("ThreadName"));
					postsargs.putInt("ThreadId", getArguments().getInt("ThreadId"));
					postsargs.putInt("ThreadPosition", getArguments().getInt("Position"));
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