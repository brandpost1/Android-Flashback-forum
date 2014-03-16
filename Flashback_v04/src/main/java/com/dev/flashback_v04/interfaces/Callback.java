package com.dev.flashback_v04.interfaces;

/**
 * Created by Viktor on 2014-03-12.
 */
public interface Callback<T> {
    public void onTaskComplete(T data);
}