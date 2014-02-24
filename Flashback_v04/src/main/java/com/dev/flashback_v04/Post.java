package com.dev.flashback_v04;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viktor on 2013-06-19.
 */
public class Post {

	ArrayList<String[]> postRows;
	String author;
	String numposts;
	String membertype;
	String orderNr;
	String date;
	String avatarurl;
	String online;
	String userProfileUrl;
	String regdate;

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

	public ArrayList<String[]> getPostRows() {
		return postRows;
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
}
