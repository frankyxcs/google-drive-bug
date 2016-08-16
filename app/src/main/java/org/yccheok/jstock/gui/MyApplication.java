package org.yccheok.jstock.gui;

import android.annotation.SuppressLint;
import android.app.Application;

/**
 * Created by yccheok on 16/8/2016.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        me = this;
    }

    private static MyApplication me;

    public static MyApplication instance() {
        return me;
    }
}
