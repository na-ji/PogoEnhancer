package com.mad.pogoenhancer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.shared.utils.Runlevel;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class SystemServerPatcher {
    private String mProcessNameBeingWatched = null;
    private boolean mRunning = false;
    private SystemServerPatchingThread mSystemServerPatchingThread = null;

    public SystemServerPatcher() {
        this.mProcessNameBeingWatched = "system_server";

    }

    public synchronized void start(Context context) {
        Logger.info("PogoEnhancerJ", "Starting system patcher");
        this.stop();
        if (this.mSystemServerPatchingThread == null) {
            this.mSystemServerPatchingThread = new SystemServerPatchingThread(this,
                    context);
        }
        this.mSystemServerPatchingThread.start();
        Logger.info("PogoEnhancerJ", "Started system patcher");
    }

    public synchronized void stop() {
        if (this.mSystemServerPatchingThread != null) {
            this.mSystemServerPatchingThread.interrupt();
        }
//        while(this.mRunning) {
//            try {
//                Thread.sleep(500L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        this.mStopPatcher = false;
    }


    private static class SystemServerPatchingThread extends Thread {
        final String TAG = "HookReceiverService";

        private SystemServerPatcher mSystemServerPatcher = null;
        private SharedPreferences mSharedPreferences = null;
        private String mInjectorPath = null;
        private boolean arm64System = false;
        private boolean arm64PogodroidInstalled = false;
        private ShellHelper _shellHelper = null;

        SystemServerPatchingThread(SystemServerPatcher owningPatcher,
                                   Context context) {
            this._shellHelper = new ShellHelper();
            this.mSystemServerPatcher = owningPatcher;
            this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            this.arm64PogodroidInstalled = Constants.is64BitPogoenhancerInstalled(context);
            this.arm64System = Constants.isArm64System();

            String injector = "libf.so";
            if(this.arm64System && this.arm64PogodroidInstalled) {
                injector = "libg.so";
                this.mInjectorPath = context.getPackageCodePath().replace("base.apk",
                        "lib/arm64/" + injector);
            } else if (this.arm64System) {
                injector = "libg.so";
                this.mInjectorPath = context.getPackageCodePath().replace("base.apk",
                        "lib/arm/" + injector);
            } else {
                this.mInjectorPath = context.getPackageCodePath().replace("base.apk",
                        "lib/arm/" + injector);
            }
        }

        private long getTimeStarted(int pid) {
            ArrayList<String> strings = _shellHelper.runCommand(Runlevel.su, "stat -c%X /proc/" + pid);
            if (strings == null || strings.size() != 1 || strings.get(0).isEmpty()) {
                return -1;
            } else {
                try {
                    return Long.parseLong(strings.get(0));
                } catch (NumberFormatException ex) {
                    Logger.error("PogoEnhancerJ", "Could not retrieve duration system has been running");
                    return -1;
                }
            }
        }

        private int getLatestSystemServerPid() {
            ArrayList<String> systemServerPids = _shellHelper.getProcessIdsOfPackage(
                    Runlevel.su, this.mSystemServerPatcher.mProcessNameBeingWatched);
            if (systemServerPids.isEmpty()) {
                return -1;
            } else if (systemServerPids.size() > 1) {
                Logger.fatal("PogoEnhancerJ", "Got more than one process...");
                return -1;
            } else {
                try {
                    return Integer.parseInt(systemServerPids.get(0));
                } catch (NumberFormatException ex) {
                    Logger.error("PogoEnhancerJ", "Could not retrieve PID of system");
                    return -1;
                }
            }
        }

        private boolean inject(int pid) {
            String deviceId = this.mSharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID,
                    Constants.DEFAULT_VALUES.DEFAULT_DEVICE_ID);
            if (deviceId == null || deviceId.isEmpty()) {
                Logger.warning("PogoEnhancerJ", "Could not read deviceID");
                return false;
            }

            String userIdEncoded = null;
            String deviceIdEncoded = null;
            try {
                deviceIdEncoded = Constants.hexadecimal(deviceId.trim());
            } catch (UnsupportedEncodingException e) {
                Logger.fatal("PogoEnhancerJ", "Failed converting to hex when starting the system patch");
                return false;
            }


            String command = this.mInjectorPath.trim() + " "
                    + pid + " "
                    + "foo" + "," + deviceIdEncoded + "," + "bar";

            ArrayList<String> commands = new ArrayList<>();
            boolean success = false;

            commands.add(command);
            commands.add("echo $?");
            ArrayList<String> output = _shellHelper.runCommands(Runlevel.su, commands);
            Logger.debug("PogoEnhancerJ", "Injection resulted in: "
                    + output.toString().replaceAll("frida", ""));
            if (output.size() > 0 && output.get(output.size() - 1).equals("0")) {
                success = true;
            } else {
                Logger.warning("PogoEnhancerJ", "Injecting system failed with: "
                        + output.toString().replaceAll("frida", ""));
                //Injecting system_server failed...
            }

            return success;
        }

        @Override
        public void run() {
            this.mSystemServerPatcher.mRunning = true;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!this.isInterrupted()) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                Logger.debug("PogoEnhancerJ", "Checking system PID");
                int latestSystemServerPid = getLatestSystemServerPid();
                int lastInjectedPid = this.mSharedPreferences.getInt(
                        Constants.SHAREDPERFERENCES_KEYS.LAST_SYSTEM_PID_INJECTED,
                        Constants.DEFAULT_VALUES.LAST_SYSTEM_PID_INJECTED);

                Logger.debug("PogoEnhancerJ", "Checking system started timestamp");
                long timeStarted = this.getTimeStarted(latestSystemServerPid);
                long lastTimeInjected = this.mSharedPreferences.getLong(
                        Constants.SHAREDPERFERENCES_KEYS.LAST_SYSTEM_PATCH_TIMESTAMP,
                        Constants.DEFAULT_VALUES.LAST_SYSTEM_PATCH_TIMESTAMP
                );

                if (lastInjectedPid != latestSystemServerPid || timeStarted > lastTimeInjected) {
                    //we can now inject...
                    Logger.info("PogoEnhancerJ", "Trying to patch the system");
                    boolean injected = this.inject(latestSystemServerPid);
                    if (injected) {
                        Logger.info("PogoEnhancerJ", "System patched successfully");
                        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
                        editor.putInt(Constants.SHAREDPERFERENCES_KEYS.LAST_SYSTEM_PID_INJECTED,
                                latestSystemServerPid);
                        long now = System.currentTimeMillis() / 1000L;
                        editor.putLong(Constants.SHAREDPERFERENCES_KEYS.LAST_SYSTEM_PATCH_TIMESTAMP,
                                now);
                        editor.apply();
                    }
                } else {
                    Logger.debug("PogoEnhancerJ", "System patched previously");
                }

                try {
                    Thread.sleep(18000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            this.mSystemServerPatcher.mRunning = false;
        }
    }
}
