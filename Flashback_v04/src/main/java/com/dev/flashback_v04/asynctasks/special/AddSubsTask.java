package com.dev.flashback_v04.asynctasks.special;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.dev.flashback_v04.LoginHandler;

public class AddSubsTask extends AsyncTask<String, Boolean, Boolean> {
	private Context mContext;
	private Bundle mBundle;
	private ProgressDialog dialog;

	public AddSubsTask(Context context, Bundle b) {
		mContext = context;
		mBundle = b;
		dialog = new ProgressDialog(context);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.dialog.setMessage("Startar prenumeration..");
		this.dialog.show();
	}

	@Override
	protected Boolean doInBackground(String... strings) {
		LoginHandler.addSubscription(mContext, mBundle);
		return null;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(dialog.isShowing())
			this.dialog.dismiss();

	}
}