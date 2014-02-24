package com.dev.flashback_v04.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowCategoriesAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowCategoriesFragment extends ListFragment {

	ShowCategoriesAdapter mAdapter;
	ArrayList<String> categories = new ArrayList<String>();
    ArrayList<String> categorynames = new ArrayList<String>();

	public ShowCategoriesFragment() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new ShowCategoriesAdapter(getActivity());
		setListAdapter(mAdapter);
		for(int i = 0; i < mAdapter.getCategories().size(); i++) {
			categories.add(mAdapter.getCategories().get(i).getLink());
            categorynames.add(mAdapter.getCategories().get(i).getName());
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String url = mAdapter.getCategories().get(position).getLink();

		((MainActivity)getParentFragment().getActivity()).openCategory(url, position, categories, categorynames);
        v.setSelected(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list_fragment_layout, container, false);
        view.findViewById(R.id.headerleft).setVisibility(View.GONE);
        view.findViewById(R.id.headerright).setVisibility(View.GONE);

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
