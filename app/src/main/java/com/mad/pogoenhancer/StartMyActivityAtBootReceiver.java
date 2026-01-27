package com.mad.pogoenhancer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.mad.pogoenhancer.services.HookReceiverService;


public class StartMyActivityAtBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.debug("PogoEnhancerJ", "StartActivityAtBoot");

        if (intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            Logger.fatal("PogoEnhancerJ", "Starting after replacement...");
            Intent restartIntent = new Intent(context, LoginActivity.class);
            restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            restartIntent.putExtra("classFrom", StartMyActivityAtBootReceiver.class.toString());
            restartIntent.putExtra("restart", true);
            context.startActivity(restartIntent);
            return;
        }


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean(Constants.SHAREDPERFERENCES_KEYS.FULL_DAEMON,
                Constants.DEFAULT_VALUES.FULL_DAEMON)) {
            Logger.debug("PogoEnhancerJ", "FULL_DAEMON not set");
            return;
        }
        String intentRestart = context.getPackageName() + context.getPackageName();
        if (intent.getAction() != null && intent.getAction().equals(intentRestart)) {
            Logger.fatal("PogoEnhancerJ", "(Re-)starting PogoEnhancer by intent...");
            Intent restartIntent = new Intent(context, LoginActivity.class);
            restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            restartIntent.putExtra("classFrom", StartMyActivityAtBootReceiver.class.toString());
            restartIntent.putExtra("restart", true);
            context.startActivity(restartIntent);
            return;
        }

        String packageName = context.getPackageName() + ".restartservices";
        if (intent.getAction().equals(packageName)) {
            Logger.fatal("PogoEnhancerJ", "(Re-)starting PogoEnhancer by intent...");
            Intent restartIntent = new Intent(context, LoginActivity.class);
            restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            restartIntent.putExtra("classFrom", StartMyActivityAtBootReceiver.class.toString());
            restartIntent.putExtra("restart", true);
            context.startActivity(restartIntent);
            return;
        }
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case "android.intent.action.QUICKBOOT_POWERON":
                Logger.fatal("ProtoHook", "OnBoot starting PogoEnhancer...");
                String bootDelay = prefs.getString(Constants.SHAREDPERFERENCES_KEYS.BOOT_DELAY, "0");
                Logger.fatal("ProtoHook", "OnBoot starting Pogodroid with a delay of " + bootDelay + " seconds");

                Intent startServiceIntent = new Intent(context, HookReceiverService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(startServiceIntent);
                } else {
                    context.startService(startServiceIntent);
                }

                int mPendingIntentId = 11111111;
                PendingIntent mPendingIntent = PendingIntent.getService(context, mPendingIntentId, startServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                assert bootDelay != null;
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + Long.valueOf(bootDelay) * 1000, mPendingIntent);
                Logger.warning("PogoEnhancerJ", "Done setting the intent");

                break;
            case "RESTART_POGO_SERVICE":
                Logger.fatal("PogoEnhancerJ", "(Re-)starting PogoEnhancer by intent...");
                Intent restartIntent = new Intent(context, LoginActivity.class);
                restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                restartIntent.putExtra("classFrom", StartMyActivityAtBootReceiver.class.toString());
                restartIntent.putExtra("restart", true);
                context.startActivity(restartIntent);
                break;
            default:
                Logger.debug("PogoEnhancerJ", "Just return...");
                return;
        }


    }
}
