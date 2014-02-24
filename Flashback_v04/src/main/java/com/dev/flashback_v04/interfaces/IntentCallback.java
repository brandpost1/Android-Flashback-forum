package com.dev.flashback_v04.interfaces;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-25.
 */
public interface IntentCallback {
	public void openCategory(ArrayList<String> urls);
	public void openForum(String url);
	public void openThread(String url);
	public void setSize(int size);
	public void startNewActivity(String url, int position, ArrayList<String> urls);
}
