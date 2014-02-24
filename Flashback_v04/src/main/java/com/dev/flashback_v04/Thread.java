package com.dev.flashback_v04;

/**
 * Created by Viktor on 2013-06-17.
 */
public class Thread {

	String threadName;
	String threadAuthor;
	String threadReplies;
	String threadViews;
	String threadLink;
	String numPages;
	String lastPageUrl;
    Boolean sticky;
    Boolean locked;
    String lastPost;

    public String getNumPages() {

		return numPages;
	}

	public void setNumPages(String numPages) {
		this.numPages = numPages;
	}

	public String getLastPageUrl() {
		return lastPageUrl;
	}

	public void setLastPageUrl(String lastPageUrl) {
		this.lastPageUrl = lastPageUrl;
	}

	public String getThreadLink() {
		return threadLink;
	}

	public void setThreadLink(String threadLink) {
		this.threadLink = threadLink;
	}

	public Thread() {

	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getThreadAuthor() {
		return threadAuthor;
	}

	public void setThreadAuthor(String threadAuthor) {
		this.threadAuthor = threadAuthor;
	}

	public String getThreadReplies() {
		return threadReplies;
	}

	public void setThreadReplies(String threadReplies) {
		this.threadReplies = threadReplies;
	}

	public String getThreadViews() {
		return threadViews;
	}

	public void setThreadViews(String threadViews) {
		this.threadViews = threadViews;
	}

    public void setSticky(boolean b) {
        sticky = b;
    }
    public boolean getSticky() {
        return  sticky;
    }
    public void setLocked(boolean b) {
        locked = b;
    }
    public boolean getLocked() {
        return  locked;
    }

    public void setLastPost(String lastPost) {
        this.lastPost = lastPost;
    }

    public String getLastPost() {
        return lastPost;
    }
}
