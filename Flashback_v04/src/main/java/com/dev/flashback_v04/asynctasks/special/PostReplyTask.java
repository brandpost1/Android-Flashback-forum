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
import com.dev.flashback_v04.interfaces.UpdateStuff;

import java.io.IOException;

/**
 * Created by Viktor on 2013-11-29.
 */
public class PostReplyTask extends AsyncTask<String, String, Boolean> {
    Context mContext;
    Parser mParser;

    private ProgressDialog dialog;
    private UpdateStuff mCallback;
    int returnedPages = 1;
    String threadUrl = "";
    String message = "";
    String threadName = "";
    int currentPage = 0;

    public PostReplyTask(Context context, Bundle args) {
        mParser = new Parser(context);
        mContext = context;
        mCallback = (UpdateStuff)mContext;
        dialog = new ProgressDialog(mContext);

        threadUrl = args.getString("Url");
        currentPage = args.getInt("CurrentPage");
        threadName = args.getString("ThreadName");
    }

    @Override
    protected Boolean doInBackground(String... strings) {

        message = strings[0];
        boolean success;
        try {
            // Url, Message, Context
            try {
                success = LoginHandler.testPost(threadUrl, message, mContext);
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
    protected void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);
        if(bool == true) {
            Bundle args = new Bundle();
            args.putString("BundleType", "UpdateThread");
            args.putInt("NumPages", returnedPages);
            args.putString("Url", threadUrl);
            args.putInt("CurrentPage", currentPage);
            args.putString("ThreadName", threadName);

            Toast.makeText(mContext, "Svar skickat! Meddelandet sparat.", Toast.LENGTH_SHORT).show();

            try {
                ((MainActivity)mCallback).updateTaskQueue.put(args);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "Kunde inte skicka svar. Ditt meddelande har sparats dock!", Toast.LENGTH_SHORT).show();

            // Save message if failed
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putString("LastMessage", message)
                    .commit();
        }

    }
}
