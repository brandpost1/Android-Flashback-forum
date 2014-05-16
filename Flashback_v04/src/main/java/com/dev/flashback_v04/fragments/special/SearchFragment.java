package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.dev.flashback_v04.R;

/**
 * Created by Viktor on 2013-11-20.
 */
public class SearchFragment extends Fragment {

    Activity mActivity;
	private Spinner keywordSpinner;
	private Spinner dateSpinner1;
	private Spinner dateSpinner2;
	private Spinner sortSpinner2;
	private Spinner sortSpinner1;
	private Spinner forumSpinner;
	private EditText keywordText;
	private EditText usernameText;

	public SearchFragment() {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_layout, container, false);
		fillForm(view);
        return view;
    }

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        mActivity = a;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

	private void fillForm(View v) {
		keywordText = (EditText)v.findViewById(R.id.keywordedit);
		usernameText = (EditText)v.findViewById(R.id.usernameedit);

		keywordSpinner = (Spinner)v.findViewById(R.id.keywordspinner);
		//String[] adaptervalues = {"Red","Blue"};
		//ArrayAdapter<String> keywordAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, adaptervalues);
		//keywordSpinner.setAdapter(keywordAdapter);
		System.out.println(keywordSpinner.getSelectedItemId());

		dateSpinner1 = (Spinner)v.findViewById(R.id.datespinner1);

		dateSpinner2 = (Spinner)v.findViewById(R.id.datespinner2);

		sortSpinner1 = (Spinner)v.findViewById(R.id.sortspinner1);

		sortSpinner1 = (Spinner)v.findViewById(R.id.sortspinner2);

		forumSpinner = (Spinner)v.findViewById(R.id.forumspinner);


	}
}
