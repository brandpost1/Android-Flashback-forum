package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.R;
import com.dev.flashback_v04.asynctasks.special.PostReplyTask;

import java.util.HashMap;

/**
 * Created by Viktor on 2013-11-20.
 */
public class PostReplyFragment extends Fragment {

    private String threadUrl = "";
    private int currentPage = 0;
	private Context mContext;
    EditText messageArea;


    public PostReplyFragment(Context context) {
		mContext = context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.postreply_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newreply_send_message:
                Bundle args = new Bundle();
                args.putString("Url", getArguments().getString("Url"));
                args.putInt("CurrentPage", getArguments().getInt("CurrentPage"));
                args.putString("ThreadName", getArguments().getString("ThreadName"));

				// Close keyboard
				InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(messageArea.getWindowToken(), 0);

                PostReplyTask replyTask = new PostReplyTask(getActivity(), args);

                // Retrieve message
                String message = messageArea.getText().toString();
                try {
                    /*
                    // Save draft
                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .edit()
                            .putString("LastMessage", message)
                            .commit();
                    */
                    // Send reply
                    replyTask.execute(message);
                } catch(IllegalStateException e) {
                    Toast.makeText(getActivity(), "Spara ditt meddelande och försök igen.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            case R.id.newreply_load_draft:
                String lastMessage = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString("LastMessage", "");

                messageArea.setText(lastMessage);

                Toast.makeText(getActivity(), "Message loaded", Toast.LENGTH_SHORT).show();
                break;
            case R.id.newreply_save_draft:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString("LastMessage", messageArea.getText().toString())
                        .commit();
                Toast.makeText(getActivity(), "Message saved", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.newpost, container, false);
        TextView replyHeader = (TextView)view.findViewById(R.id.newpost_header);
        messageArea = (EditText)view.findViewById(R.id.newpost_reply_textbox);

        String quote = getArguments().getString("Quote");
        String author = getArguments().getString("Author");
        HashMap<String, String[]> arr = null;

        arr = (HashMap<String, String[]>)getArguments().getSerializable("PlusQuotes");

        // Add any quotes to the messagebox
        if(arr != null) {
            for(String key : arr.keySet()) {
                String plusauthor = arr.get(key)[0];
                String plusquote = arr.get(key)[1];

                messageArea.append("[QUOTE="+plusauthor+"]"+plusquote+"[/QUOTE]");
                messageArea.append("\n\n");
            }
        }

        // Set threadname as header
        replyHeader.setText(getArguments().getString("ThreadName"));

        // Add proper tags around quotes
        if(quote != null && author != null) {
            messageArea.append("[QUOTE="+author+"]"+quote+"[/QUOTE]");
        }
        if(quote != null && author == null) {
            messageArea.setText("[QUOTE]"+quote+"[/QUOTE]");
        }

        return view;
    }

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
