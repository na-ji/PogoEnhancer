package com.mad.pogoenhancer;

import android.app.Activity;
import android.content.Intent;

import java.util.Arrays;


public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    Activity activity;

    public DefaultExceptionHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra("App crashed...", ex);
        Logger.fatal("PogoEnhancerJ", ex.toString());
        Logger.fatal("PogoEnhancerJ", Arrays.toString(ex.getStackTrace()));
        activity.startActivity(intent);
        activity.finish();

        System.exit(0);
    }
}