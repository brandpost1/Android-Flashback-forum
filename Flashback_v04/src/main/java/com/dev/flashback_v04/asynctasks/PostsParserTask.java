package com.dev.flashback_v04.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dev.flashback_v04.*;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.interfaces.OnTaskComplete;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-28.
 */
public class PostsParserTask extends AsyncTask<String, String, ArrayList<Post>> {


	private Parser mParser;
	private OnTaskComplete showPostsAdapter;
	private Context mContext;
	private ArrayList<Post> mPosts;
    private boolean isLoggedIn;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(ArrayList<Post> posts) {
		super.onPostExecute(posts);
        // Försökte troligtvis öppna en flyttad tråd utan att vara inloggad.
        if(posts.isEmpty() && !isLoggedIn) {
            Toast.makeText(mContext, "Tror det blev något fel.\nInga inlägg hittades...", Toast.LENGTH_SHORT).show();
        }

		showPostsAdapter.updatePosts(mPosts);
	}

	public PostsParserTask(OnTaskComplete taskComplete, Context context) {
		mParser = new Parser(context);
		showPostsAdapter = taskComplete;
		mContext = context;
		mPosts = new ArrayList<Post>();

        SharedPreferences myPrefs = mContext.getSharedPreferences("session_cookie", Context.MODE_PRIVATE);
        String loggedin = myPrefs.getString("vbscanpassword",null);
        if(loggedin == null) {
            isLoggedIn = false;
        } else {
            isLoggedIn = true;
        }
	}

	@Override
	protected ArrayList<Post> doInBackground(String... strings) {
        try {
		    mPosts = mParser.getThreadContent(strings[0]);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
		return mPosts;
	}

}
