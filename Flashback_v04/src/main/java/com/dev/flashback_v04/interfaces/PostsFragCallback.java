package com.dev.flashback_v04.interfaces;

/**
 * Created by Viktor on 2013-12-10.
 */
public interface PostsFragCallback<T> {
    public void sendQuote(T object);
    public void sendPlusQuote(T object);

}
