package com.dev.flashback_v04.fragments;


import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowCategoriesAdapter;

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("Categories", categories);
        outState.putStringArrayList("CategoryNames", categorynames);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ShowCategoriesAdapter(getActivity());

        if(savedInstanceState == null) {
            for(int i = 0; i < mAdapter.getCategories().size(); i++) {
                categories.add(mAdapter.getCategories().get(i).get("Link"));
                categorynames.add(mAdapter.getCategories().get(i).get("Name"));
            }
        } else {
            // Restore categories
            categories = savedInstanceState.getStringArrayList("Categories");
            categorynames = savedInstanceState.getStringArrayList("CategoryNames");
        }
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
        view.findViewById(R.id.headerleft).setVisibility(View.GONE);
        view.findViewById(R.id.headerright).setVisibility(View.GONE);
        return view;
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String url = categories.get(position);
		((MainActivity)getParentFragment().getActivity()).openCategory(url, position+1, categories, categorynames);
	}




}
