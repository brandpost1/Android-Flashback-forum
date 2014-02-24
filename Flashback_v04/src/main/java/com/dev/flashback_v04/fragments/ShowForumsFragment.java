package com.dev.flashback_v04.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.R;

import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.ShowForumsAdapter;
import com.dev.flashback_v04.asynctasks.LoginTask;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-25.
 */
public class ShowForumsFragment extends ListFragment {

	ShowForumsAdapter mAdapter;
	int selected_category;
	ArrayList<String> categories;
    ArrayList<String> categorynames;
	String category_url = null;

	public ShowForumsFragment() {

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList("SavedCategories", categories);
		outState.putString("SavedUrl", category_url);
		outState.putInt("SavedSelected", selected_category);
        outState.putStringArrayList("CategoryNames", categorynames);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		if(mAdapter == null) {
			mAdapter = new ShowForumsAdapter(getActivity(), category_url);
			setListAdapter(mAdapter);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}



	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String url = mAdapter.getForums().get(position).getUrl();
        String forumname = mAdapter.getForums().get(position).getName();
		((MainActivity)getParentFragment().getActivity()).openForum(url, 1, forumname);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            categories = getArguments().getStringArrayList("Categories");
            selected_category = getArguments().getInt("index");
            categorynames = getArguments().getStringArrayList("CategoryNames");
            category_url = categories.get(selected_category);

        } else {
            categories = savedInstanceState.getStringArrayList("SavedCategories");
            category_url = savedInstanceState.getString("SavedUrl");
            selected_category = savedInstanceState.getInt("SavedSelected");
            categorynames = savedInstanceState.getStringArrayList("CategoryNames");
        }

		View view = inflater.inflate(R.layout.list_fragment_layout, container, false);

        int len = getResources().getInteger(R.integer.header_max_length);
        String showthisname;

        try {
            if (categorynames.get(selected_category).length() >= len) {
                showthisname = categorynames.get(selected_category).substring(0, len)+ "...";
            } else {
                showthisname = categorynames.get(selected_category);
            }
        } catch (NullPointerException e) {
            showthisname = "Error-name";
            e.printStackTrace();
        }


        TextView header = (TextView)view.findViewById(R.id.headerleft);
        TextView headerright = (TextView)view.findViewById(R.id.headerright);
        header.setText(showthisname);
        headerright.setText("");
		return view;

	}
}
