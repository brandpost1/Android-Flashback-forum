package com.dev.flashback_v04.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.activities.MainActivity;


/**
 * Created by Viktor on 2013-06-24.
 */
public class LoginTask extends AsyncTask<String, String, Boolean> {

	Context mContext;
    Parser parser;
    private ProgressDialog dialog;
    TextView view;

    //TODO: Improve this
	public LoginTask(Context context, TextView text) {
		mContext = context;
        this.view = text;
        dialog = new ProgressDialog(mContext);
        parser = new Parser(context);
	}
    public LoginTask(Context context) {
        mContext = context;
        dialog = new ProgressDialog(mContext);
    }

	@Override
	protected void onPostExecute(Boolean aBoolean) {
		super.onPostExecute(aBoolean);
		if(aBoolean.equals(Boolean.TRUE)) {
			Toast.makeText(mContext, "Logged in.", Toast.LENGTH_SHORT).show();
            view.setText("Logga ut");
            ((MainActivity)mContext).getSupportFragmentManager().popBackStack("Start", 0);
            ((MainActivity)mContext).refreshDrawer();
		} else {
			Toast.makeText(mContext, "Could not login.", Toast.LENGTH_SHORT).show();

		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
        this.dialog.setMessage("Loggar in..");
        this.dialog.show();
	}

	@Override
	protected Boolean doInBackground(String... strings) {
        try {
            if(LoginHandler.login(strings[0], strings[1], mContext)) {
                if(parser.setUserId()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if(dialog.isShowing()) this.dialog.dismiss();
        }

    }

}
