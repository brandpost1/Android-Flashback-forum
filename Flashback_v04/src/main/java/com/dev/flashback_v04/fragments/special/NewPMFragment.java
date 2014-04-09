package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.Post;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.asynctasks.special.NewThreadTask;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;

/**
 * Created by Viktor on 2014-02-27.
 */
public class NewPMFragment extends Fragment {


    ActionBarActivity mActivity;
    EditText messageHeader;
    EditText messageText;
    EditText messageRecipients;
	CheckBox saveCopy;
	CheckBox urlToLink;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.newprivatemessage, container, false);
        messageHeader = (EditText)v.findViewById(R.id.newpm_header_textbox);
        messageText = (EditText)v.findViewById(R.id.newpm_message_textbox);
		messageRecipients = (EditText)v.findViewById(R.id.newpm_recipients_textbox);
		saveCopy = (CheckBox)v.findViewById(R.id.newpm_savecopy_chkbox);
		urlToLink = (CheckBox)v.findViewById(R.id.newpm_url_2_link_chkbox);

		String recipient = (getArguments().getString("Recipient") != null) ? getArguments().getString("Recipient") : null;
		String header = (getArguments().getString("Header") != null) ? getArguments().getString("Header") : null;
		String message = (getArguments().getString("Message") != null) ? getArguments().getString("Message") : null;

		if(recipient != null)
			messageRecipients.setText(recipient);
		if(header != null)
			messageHeader.setText(header);
		if(message != null)
			messageText.setText(message);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.newpm_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.newpm_send:
                Bundle bundle = new Bundle();
                bundle.putString("Recipients", messageRecipients.getText().toString());
                bundle.putString("Header", messageHeader.getText().toString());
                bundle.putString("Message", messageText.getText().toString());
                bundle.putBoolean("SaveCopy", saveCopy.isChecked());
                bundle.putBoolean("ConvertLinks", urlToLink.isChecked());
				bundle.putString("PMId", getArguments().getString("PMId"));
				bundle.putString("Forward", getArguments().getString("Forward"));

				// Close keyboard
				InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(messageText.getWindowToken(), 0);

				((MainActivity)mActivity).sendPrivateMessage(bundle);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
