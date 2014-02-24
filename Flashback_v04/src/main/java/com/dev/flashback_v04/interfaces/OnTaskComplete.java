package com.dev.flashback_v04.interfaces;


import com.dev.flashback_v04.Forum;
import com.dev.flashback_v04.Post;
import com.dev.flashback_v04.Thread;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-19.
 */
public interface OnTaskComplete {
	public void updateForums(ArrayList<Forum> forums);
	public void updateThreads(ArrayList<Thread> mThreads);
	public void updatePosts(ArrayList<Post> mPosts);
    public void updateSize(int size);
}
