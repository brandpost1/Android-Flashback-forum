package com.dev.flashback_v04.asynctasks.special;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.activities.MainActivity;

import java.io.IOException;

/**
 * Created by Viktor on 2014-02-27.
 */
public class NewThreadTask extends AsyncTask<String, String, Boolean> {

    String forumId;
    String forumLink;
    String threadHeader;
    String threadMessage;
    String redirectLink;
    String forumName;
    Context mContext;
    ProgressDialog mDialog;

    public NewThreadTask(Context context, Bundle args) {
        forumId = args.getString("ForumId");
        forumLink = args.getString("ForumUrl");
        threadHeader = args.getString("ThreadHeader");
        threadMessage = args.getString("ThreadMessage");
        forumName = args.getString("ForumName");

        mContext = context;
        mDialog = new ProgressDialog(mContext);

    }

    @Override
    protected Boolean doInBackground(String... strings) {

        boolean success = false;

        try {

            if(threadHeader.isEmpty() || threadMessage.isEmpty()) {
                ((MainActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Rubrik eller meddelande saknas.", Toast.LENGTH_SHORT).show();
                    }
                });

                return success;
            }
            try {
                success = LoginHandler.postNewThread(forumId, threadHeader, threadMessage, mContext);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            if(mDialog.isShowing())
                mDialog.dismiss();
        }
        return success;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.mDialog.setMessage("Skapar tråd..");
        this.mDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);

        if(success) {
            Bundle args = new Bundle();
            args.putString("BundleType", "ThreadCreated");
            args.putString("ThreadUrl", redirectLink);
            args.putString("ForumUrl", forumLink);
            args.putString("ForumName", forumName);
            args.putInt("NumPages", 1);
            Toast.makeText(mContext, "Tråd skapad!", Toast.LENGTH_SHORT).show();

            ((MainActivity)mContext).updateForum(args, false);
        } else {
            Toast.makeText(mContext, "Något gick fel..", Toast.LENGTH_SHORT).show();

        }
    }
}
