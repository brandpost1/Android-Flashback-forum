package com.dev.flashback_v04;

import android.content.Context;
import android.content.Intent;
import android.text.style.ClickableSpan;
import android.view.View;

import com.dev.flashback_v04.activities.DisplayImageActivity;

/**
 * Created by Viktor on 2014-04-23.
 */
public class InternalLink extends ClickableSpan {

	private String linkUrl;
	private Context mContext;

	public InternalLink(Context context, String url) {
		super();
		linkUrl = url;
		mContext = context;
	}

	public String getUrl() {
		return linkUrl;
	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent(mContext, DisplayImageActivity.class);
		intent.putExtra("ImageUrl", linkUrl);
		mContext.startActivity(intent);
	}

}
