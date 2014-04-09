package com.dev.flashback_v04.asynctasks.special;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.Parser;

public class SendPMTask extends AsyncTask<String, String, Boolean> {

	private Parser mParser;
	private Bundle bundle;
	private ProgressDialog dialog;
	private Context mContext;

	public SendPMTask(Context context, Bundle args) {
		mParser = new Parser(context);
		bundle = args;
		dialog = new ProgressDialog(context);
		mContext = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.dialog.setMessage("Skickar pm..");
		this.dialog.show();
	}

	@Override
	protected Boolean doInBackground(String... strings) {
		String token = mParser.getNewPrivateMessageToken();
		bundle.putString("Token", token);

		return LoginHandler.sendPrivateMessage(mContext, bundle);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(dialog.isShowing())
			this.dialog.dismiss();

	}

}