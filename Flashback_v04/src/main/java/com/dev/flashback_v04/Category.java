package com.dev.flashback_v04;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-07.
 */
public class Category {
	/*
	* Keeps all of the direct subforums of this category
	* */
	private ArrayList<Forum> mForums;

	/*
	* Name of this Category
	* */
	private String mName;
	private int mID;
	private String mLink;

	/*
	* Constructor
	* */
	public Category() {

	}

	public void setName(String name) {
		mName = name;
	}

	public void setForums(ArrayList<Forum> forums) {
		mForums = forums;
	}

	public String getName() {
		return mName;
	}

	public ArrayList<Forum> getForums() {
		return mForums;
	}

	public void setID(int ID) {
		mID = ID;
	}

	public String getLink() {
		return mLink;
	}

	public void setLink(String link) {
		mLink = link;
	}

	@Override
	public String toString() {
		return "ID:"+ mID +"| Name: "+mName + "| Link: "+ mLink;
	}
}


