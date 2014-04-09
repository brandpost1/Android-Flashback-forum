package com.dev.flashback_v04.asynctasks.special;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.dev.flashback_v04.LoginHandler;

public class PutEditedPostTask extends AsyncTask<Bundle, Void, Boolean> {

	private Context mContext;
	private ProgressDialog dialog;

	public PutEditedPostTask(Context context) {
		mContext = context;
		dialog = new ProgressDialog(context);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.dialog.setMessage("Skickar ändringar..");
		this.dialog.show();
	}

	@Override
	protected Boolean doInBackground(Bundle... bundles) {
		return LoginHandler.editPost(mContext, bundles[0]);
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		if(dialog.isShowing())
			this.dialog.dismiss();

		if(success) {
			Toast.makeText(mContext, "Ändringar sparade!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, "Något gick fel..", Toast.LENGTH_SHORT).show();
		}
	}
}