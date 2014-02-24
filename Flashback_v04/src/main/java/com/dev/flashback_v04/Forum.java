package com.dev.flashback_v04;

import java.util.ArrayList;


/**
 * Created by Viktor on 2013-06-07.
 */
public class Forum {

	private Category mCategory;
	private Forum mParent = null;
	private String mUrl = null;
	private String mName = null;
	private String mInfo = null;
	private int mForumSize = 0;
	private ArrayList<Forum> mChildren;
	private int mSize;


	public Forum() {
		mChildren = new ArrayList<Forum>();
	}

	// Might use later. Keep just in case.
	public ArrayList<Forum> getSubForums() {
		return mChildren;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public String getInfo() {
		return mInfo;
	}

	public void setInfo(String info) {
		mInfo = info;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public void addSubForum(Forum sub) {
		mChildren.add(sub);
	}

	@Override
	public String toString() {
		return mName+" || "+mUrl;
	}

	public void setSize(int size) {
		mSize = size;
	}

	public int getSize() {
		return mSize;
	}
}
