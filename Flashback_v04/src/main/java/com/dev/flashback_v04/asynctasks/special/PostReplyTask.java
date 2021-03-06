package com.dev.flashback_v04.asynctasks.special;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.activities.MainActivity;

import java.io.IOException;

/**
 * Created by Viktor on 2013-11-29.
 */
public class PostReplyTask extends AsyncTask<String, String, Boolean> {
    private Context mContext;
	private Parser mParser;

	private ProgressDialog dialog;
	private int returnedPages = 1;
	private String threadUrl = "";
	private String message = "";
	private String threadName = "";
	private int currentPage = 0;

	Bundle forwardBundle;

    public PostReplyTask(Context context, Bundle args) {
        mParser = new Parser(context);
        mContext = context;
        dialog = new ProgressDialog(mContext);

        threadUrl = args.getString("Url");
        currentPage = args.getInt("CurrentPage");
        threadName = args.getString("ThreadName");

		forwardBundle = new Bundle();
		forwardBundle.putString("HideSmileys", args.getString("HideSmileys"));
		forwardBundle.putString("ConvertLinks", args.getString("ConvertLinks"));

	}

    @Override
    protected Boolean doInBackground(String... strings) {
        message = strings[0];

		forwardBundle.putString("ThreadUrl", threadUrl);
		forwardBundle.putString("Message",  message);

        boolean success;
        try {
            // Url, Message, Context
            try {
                success = LoginHandler.postReply(forwardBundle, mContext);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setMessage("Uppdaterar..");
                }
            });

            // Get new number of pages. It might have changed.
            // Used when the thread is reloaded
            try {
                returnedPages = mParser.getThreadNumPages(threadUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        } finally {
            if(dialog.isShowing())
                this.dialog.dismiss();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.dialog.setMessage("Skickar svar..");
        this.dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if(success) {
            Bundle args = new Bundle();
            args.putString("BundleType", "UpdateThread");
            args.putInt("NumPages", returnedPages);
            args.putString("Url", threadUrl);
            args.putInt("CurrentPage", currentPage);
            args.putString("ThreadName", threadName);

            Toast.makeText(mContext, "Svar skickat!", Toast.LENGTH_SHORT).show();

            ((MainActivity)mContext).updateThread(args);

        } else {
            Toast.makeText(mContext, "Något hände.. Försök igen..", Toast.LENGTH_SHORT).show();

            // Save message if failed
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putString("LastMessage", message)
                    .commit();
        }

    }
}
