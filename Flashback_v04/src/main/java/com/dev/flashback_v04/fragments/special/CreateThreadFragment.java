package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.R;
import com.dev.flashback_v04.asynctasks.special.NewThreadTask;

/**
 * Created by Viktor on 2014-02-27.
 */
public class CreateThreadFragment extends Fragment {

	private ActionBarActivity mActivity;
	private EditText threadHeader;
	private EditText threadMessage;
	private String forumID;
	private String forumName;
	private String forumLink;
	private CheckBox showSignature;
	private CheckBox convertLinks;
	private CheckBox hideSmileys;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = (ActionBarActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forumName = getArguments().getString("ForumName");
		if(forumName == null)
			forumName = "DefaultForumName";
        forumID = getArguments().getString("ForumId");
		if(forumID == null)
			forumID = "-1";
        forumLink = getArguments().getString("ForumUrl");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.newthread, container, false);
        TextView forumName = (TextView)v.findViewById(R.id.newthread_header);
        threadHeader = (EditText)v.findViewById(R.id.newthread_header_textbox);
        threadMessage = (EditText)v.findViewById(R.id.newthread_message_textbox);

		showSignature = (CheckBox)v.findViewById(R.id.newthread_showsignature);
		convertLinks = (CheckBox)v.findViewById(R.id.newthread_url_2_link_chkbox);
		hideSmileys = (CheckBox)v.findViewById(R.id.newthread_smiley_2_text_chkbox);

        // Set header as current forum name
        forumName.setText(this.forumName);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.postnewthread_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.newthread_create:
                Bundle bundle = new Bundle();
                bundle.putString("ForumId", forumID);
                bundle.putString("ThreadHeader", threadHeader.getText().toString());
                bundle.putString("ThreadMessage", threadMessage.getText().toString());
                bundle.putString("ForumUrl", forumLink);
                bundle.putString("ForumName", forumName);
				bundle.putString("ShowSignature", showSignature.isChecked() ? "1" : "0");
				bundle.putString("ConvertLinks", convertLinks.isChecked() ? "1" : "0");
				bundle.putString("HideSmileys", hideSmileys.isChecked() ? "1" : "0");

				// Close keyboard
				InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(threadMessage.getWindowToken(), 0);

                NewThreadTask newThreadTask = new NewThreadTask(mActivity, bundle);

                try {
                    newThreadTask.execute();
                } catch (IllegalStateException e) {
                    Toast.makeText(getActivity(), "Spara ditt meddelande och försök igen.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }


                break;
            case R.id.newthread_save_draft:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString("LastNewthreadMessage", threadMessage.getText().toString())
                        .putString("LastNewthreadHeader", threadHeader.getText().toString())
                        .commit();
                Toast.makeText(getActivity(), "Utkast sparat", Toast.LENGTH_SHORT).show();
                break;
            case R.id.newthread_load_draft:
                String savedMessage = PreferenceManager.getDefaultSharedPreferences(mActivity)
                        .getString("LastNewthreadMessage", "");
                String savedHeader = PreferenceManager.getDefaultSharedPreferences(mActivity)
                        .getString("LastNewthreadHeader", "");
                threadMessage.setText(savedMessage);
                threadHeader.setText(savedHeader);

                Toast.makeText(getActivity(), "Utkast laddat", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
