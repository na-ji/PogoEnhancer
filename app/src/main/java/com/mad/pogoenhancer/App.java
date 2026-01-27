package com.mad.pogoenhancer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.StrictMode;

import com.topjohnwu.superuser.Shell;

import timber.log.Timber;


public class App extends Application {

    public static final String CHANNEL_ID = "serviceChannel";
    public static final String HEADS_UP_CHANNEL_ID = "headsupChannel";
    public static final String ORDINARY_CHANNEL_ID = "ordinaryChannel";

    @Override
    public void onCreate() {
        super.onCreate();
//        SC.init(getApplicationContext());
        createNotificationChannel();
        Logger.info("ProtoHookJ", "PogoEnhancer starting");
        /*StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Pogo information processing service",
                NotificationManager.IMPORTANCE_HIGH
        );


        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(serviceChannel);

        NotificationChannel headsUpChannel = new NotificationChannel(
                HEADS_UP_CHANNEL_ID,
                "Pogodroid headsup-notifications",
                NotificationManager.IMPORTANCE_HIGH
        );


        notificationManager.createNotificationChannel(headsUpChannel);

        NotificationChannel ordinaryChannel = new NotificationChannel(
                ORDINARY_CHANNEL_ID,
                "Pogodroid ordinary-notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );


        notificationManager.createNotificationChannel(ordinaryChannel);
    }
}

