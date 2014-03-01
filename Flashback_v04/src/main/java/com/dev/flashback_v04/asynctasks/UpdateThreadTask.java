package com.dev.flashback_v04.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.interfaces.UpdateStuff;

/**
 * Created by Viktor on 2013-06-26.
 */
public class UpdateThreadTask extends AsyncTask<String, String, Integer> {
	Parser mParser;
    Bundle bundle;
    UpdateStuff mCallback;
    private ProgressDialog dialog;
    String url;

	public UpdateThreadTask(Context context, Bundle args) {
        bundle = args;
        mCallback = (UpdateStuff)context;
		mParser = new Parser(context);
        dialog = new ProgressDialog(context);
        url = args.getString("Url");
    }

	@Override
	protected void onPostExecute(Integer size) {
		super.onPostExecute(size);
        bundle.putInt("NumPages", size);
        bundle.putString("BundleType", "UpdateThread");

        ((MainActivity)mCallback).updateThread(bundle);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.dialog.setMessage("Uppdaterar..");
        this.dialog.show();
    }

	@Override
	protected Integer doInBackground(String... strings) {
        int size = 1;
        size = mParser.getThreadNumPages(url);

        if(dialog.isShowing())
            this.dialog.dismiss();

        return size;
	}
}
