package com.dev.flashback_v04.asynctasks.special;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.interfaces.UpdateStuff;

import java.io.IOException;

/**
 * Created by Viktor on 2013-11-29.
 */
public class PostThreadTask extends AsyncTask<String, String, Boolean> {
    Context mContext;
    Parser mParser;

    private ProgressDialog dialog;
    private UpdateStuff mCallback;
    int returnedPages = 1;
    String threadUrl = "";
    String message = "";
    int currentPage = 0;

    public PostThreadTask(Context context, Bundle args) {
        mParser = new Parser(context);
        mContext = context;
        mCallback = (UpdateStuff)mContext;
        dialog = new ProgressDialog(mContext);

        threadUrl = args.getString("Url");
        currentPage = args.getInt("CurrentPage");
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        message = strings[0];

        try {
            // Url, Message, Context
            LoginHandler.postThread(threadUrl, mContext);
            // Get new number of pages.
            // Used when the thread is reloaded
            returnedPages = 1;
            return true;
        } catch (IOException e) {
            return false;
        } catch (Exception e){
            return true;
        } finally {
            if(dialog.isShowing())
                this.dialog.dismiss();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.dialog.setMessage("Skickar tråd..");
        this.dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);
        if(bool == true) {
            Bundle args = new Bundle();
            args.putInt("NumPages", returnedPages);
            args.putString("Url", threadUrl);
            args.putInt("CurrentPage", currentPage);
            /*
            System.out.println("Pages: " +returnedPages);
            System.out.println("Url: " +threadUrl);
            System.out.println("Currentpage: " +currentPage);
            */
            Toast.makeText(mContext, "Reply sent!", Toast.LENGTH_SHORT).show();
            mCallback.updateThread(args);
        } else {
            Toast.makeText(mContext, "Could not post reply..", Toast.LENGTH_SHORT).show();
        }

    }
}
