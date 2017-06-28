package com.kgdsoftware.foobar;

import android.util.Log;

/**
 * Created by public on 12/6/16.
 */

public class FooBar {
    public static final String TAG = "LA";

    public FooBar() {
        Log.v(TAG, "Create FooBar");
    }

    public void method() {
        Log.v(TAG, "FooBar.method");
    }

    public void method(String thing) {
        Log.v(TAG, "FooBar.method( " + thing + ")");
    }
}
