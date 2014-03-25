package com.dev.flashback_v04;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.fragments.SecondaryPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viktor on 2013-06-19.
 */
public class Post {

	ArrayList<String[]> postRows;
	String author;
	String userID;
	String threadID;
	String numposts;
	String membertype;
	String orderNr;
	String date;
	String avatarurl;
	String online;
	String userProfileUrl;
	String regdate;


	PopupMenu mPopup;
	boolean popupInit = false;
    String postUrl;

	public Post() {
		postRows = new ArrayList<String[]>();

	}

	public void addRow(String type, String text) {
		postRows.add(new String[5]);
		postRows.get(postRows.size()-1)[0] = type;
		postRows.get(postRows.size()-1)[1] = text;
	}

    public String getQuote() {
        String quote = "";
        for(String[] row : postRows) {
            if(row[0].equals("[MESSAGE]") || row[0].equals("[SPOILER]")) {
                if(row[0].equals("[SPOILER]")) {
                    quote += "\n[SPOILER]";
                }

                quote += row[1];

                if(row[0].equals("[SPOILER]")) {
                    quote += "[/SPOILER]\n";
                }
            }
        }
        return quote;
    }

	public void initPopup(final Context context, View anchor) {
		//TODO: Place this somewhere else.. I don't want this here.
		popupInit = true;
		mPopup = new PopupMenu(context, anchor);
		PopupMenu.OnMenuItemClickListener clickListener = new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				int item = menuItem.getItemId();
				Bundle bundle;
				switch (item) {
					case 1:

						break;
					case 2:
						SecondaryPager userThreadsPager = new SecondaryPager();
						userID = userProfileUrl.split("/u")[1];
						bundle = new Bundle();
						bundle.putInt("FragmentType", 1);
						bundle.putString("UserId", userID);
						userThreadsPager.setArguments(bundle);
						try {
							((MainActivity)context).getSupportFragmentManager().beginTransaction()
									.addToBackStack("UserThreads")
									.replace(R.id.fragmentcontainer, userThreadsPager)
									.commit();
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case 3:
						SecondaryPager userPostsPager = new SecondaryPager();
						userID = userProfileUrl.split("/u")[1];
						bundle = new Bundle();
						bundle.putInt("FragmentType", 2);
						bundle.putString("UserId", userID);
						userPostsPager.setArguments(bundle);
						try {
							((MainActivity)context).getSupportFragmentManager().beginTransaction()
									.addToBackStack("UserPosts")
									.replace(R.id.fragmentcontainer, userPostsPager)
									.commit();
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case 4:
						SecondaryPager userThreadPosts = new SecondaryPager();
						userID = userProfileUrl.split("/u")[1];
						bundle = new Bundle();
						bundle.putInt("FragmentType", 2);
						bundle.putString("UserId", userID);
						bundle.putString("ThreadId", threadID);
						userThreadPosts.setArguments(bundle);
						try {
							((MainActivity)context).getSupportFragmentManager().beginTransaction()
									.addToBackStack("UserThreadPosts")
									.replace(R.id.fragmentcontainer, userThreadPosts)
									.commit();
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
				}
				return false;
			}
		};

		mPopup.setOnMenuItemClickListener(clickListener);
		mPopup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Skicka PM").setEnabled(false);
		mPopup.getMenu().add(Menu.NONE, 2, Menu.NONE, "Hitta alla ämnen");
		mPopup.getMenu().add(Menu.NONE, 3, Menu.NONE, "Hitta alla inlägg");
		mPopup.getMenu().add(Menu.NONE, 4, Menu.NONE, "Hitta alla inlägg i detta ämne");

	}
	public void showPopup() {
		mPopup.show();
	}
    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }
    public String getPostUrl() {
        return postUrl;
    }
    public ArrayList<String[]> getPostRows() {
		return postRows;
	}

	public String getAvatarUrl() {
		return avatarurl;
	}
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getNumposts() {
		return numposts;
	}

	public void setNumposts(String numposts) {
		this.numposts = numposts;
	}

	public String getMembertype() {
		return membertype;
	}

	public void setMembertype(String membertype) {
		this.membertype = membertype;
	}

	public String getOrderNr() {
		return orderNr;
	}

	public void setOrderNr(String orderNr) {
		this.orderNr = orderNr;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setAvatarurl(String avatarurl) {
		this.avatarurl = avatarurl;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	public void setUserProfileUrl(String userProfileUrl) {
		this.userProfileUrl = userProfileUrl;
	}

	public String getRegdate() {
		return regdate;
	}

	public void setRegdate(String regdate) {
		this.regdate = regdate;
	}

	public void setThreadId(String threadId) {
		threadID = threadId;
	}
}
