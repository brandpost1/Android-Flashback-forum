package com.dev.flashback_v04;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Viktor on 2013-11-13.
 */
public class SharedPrefs {

    public static void savePreference(Context context, String file, String key, int value) {
        SharedPreferences sessionPrefs = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sessionPrefs.edit();

        editor.putInt(key, value);

        editor.commit();
    }

    public static int getPreference(Context context, String file, String key) {
        SharedPreferences sessionPrefs = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        return sessionPrefs.getInt(key, -1);
    }


}
