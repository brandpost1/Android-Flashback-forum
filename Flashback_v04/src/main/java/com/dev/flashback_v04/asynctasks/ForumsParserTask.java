package com.dev.flashback_v04.asynctasks;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dev.flashback_v04.Forum;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.SharedPrefs;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.fragments.WrapperFragment;
import com.dev.flashback_v04.interfaces.OnTaskComplete;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-19.
 */

public class ForumsParserTask extends AsyncTask<String, String, ArrayList<Forum>> {

	Context mContext;
	OnTaskComplete showForumsAdapter;
	Document currentSite;
	ArrayList<Forum> forums;
	ProgressDialog progressDialog;
	Parser mParser;
	ProgressBar mProgressBar;

	public ForumsParserTask(OnTaskComplete taskComplete, Context context) {
		mParser = new Parser(context);

		this.showForumsAdapter = taskComplete;
		this.forums = new ArrayList<Forum>();
		this.mContext = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(ArrayList<Forum> forums) {
		super.onPostExecute(forums);
        if(forums.isEmpty()) {
            //Toast.makeText(mContext, "Tror det blev något fel.\nInga forum hittades...", Toast.LENGTH_SHORT).show();
        }

		showForumsAdapter.updateForums(forums);
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
	}

	@Override
	protected ArrayList<Forum> doInBackground(String... strings) {
        try {
            forums = mParser.getCategoryContent(strings[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

		return forums;
	}

}
