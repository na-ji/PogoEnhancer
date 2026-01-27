package com.mad.pogoenhancer.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;

import androidx.core.app.NotificationCompat;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.shared.utils.Runlevel;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.mad.pogoenhancer.App.HEADS_UP_CHANNEL_ID;

public class Injector {
    private static final Injector ourInstance = new Injector();
    private static final int RAW_PROTOS_ENABLED = 1;
    private SharedPreferences _sharedPreferences = null;
    private Context _applicationContext = null;
    private int lastPogoPidInjected = -1;

    private Injector() {
    }

    public static Injector getInstance() {
        return ourInstance;
    }

    public synchronized boolean injectWithDelay(Context applicationContext) {
        return injectWithDelay(false, applicationContext);
    }

    private synchronized boolean injectWithDelay(boolean again, Context applicationContext) {
        int pogoPid = this.getPogoPid();
        if (!checkPogoInjected(pogoPid)) {
            Logger.debug("PogoEnhancerJ", "Nothing to inject, still the same PID");
            return true;
        }

        if (pogoPid <= 0) {
            Logger.info("PogoEnhancerJ", "PID <= 0...");
            return false;
        }
        //PIDs are different, we have not injected into this process...
        //check time since start and wait till the time specified in settings is reached, then inject
        //TODO
        long timeRunningInSeconds = System.currentTimeMillis() / 1000L - getTimeStarted(pogoPid);
        long injectAfterSecs = 300L;
        try {
            injectAfterSecs = Long.parseLong(this._sharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.INJECT_AFTER_SECONDS,
                    Constants.DEFAULT_VALUES.INJECT_AFTER_SECONDS));
        } catch (NumberFormatException ex) {
            Logger.warning("PogoEnhancerJ", "Could not read INJECT_AFTER_SECONDS. Falling back to 300s");
        }
        if (timeRunningInSeconds > 0 && timeRunningInSeconds < injectAfterSecs) {
            try {
                long injectIn = injectAfterSecs - timeRunningInSeconds;
                if (injectIn < 1) {
                    injectIn = 0L;
                } else if (injectIn > injectAfterSecs) {
                    injectIn = injectAfterSecs;
                }
                Logger.debug("PogoEnhancerJ", "Waiting for injection");
                Thread.sleep(injectIn * 1000);
            } catch (InterruptedException e) {
                Logger.warning("PogoEnhancerJ", "Interrupted while waiting for injection time. Aborting");
                return false;
            }
        }

        return this.inject(pogoPid, again, applicationContext);
    }

    private synchronized boolean checkPogoInjected(int pid) {
        if (pid <= 0) {
            return false;
        }
        long timeStarted = this.getTimeStarted(pid);
        long lastTimeInjected = this._sharedPreferences.getLong(
                Constants.SHAREDPERFERENCES_KEYS.LAST_TIME_INJECTED,
                Constants.DEFAULT_VALUES.LAST_TIME_INJECTED
        );

        int lastInjectedPid = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_PID_INJECTED,
                Constants.DEFAULT_VALUES.LAST_PID_INJECTED);

        return lastInjectedPid != pid || timeStarted > lastTimeInjected;
    }

    private synchronized boolean inject(int pogoPid, boolean again, Context applicationContext) {
        if (pogoPid <= 0) {
            Logger.warning("PogoEnhancerJ", "Could not find pogo PID");
            return false;
        }

        if (!checkPogoInjected(pogoPid)) {
            Logger.debug("PogoEnhancerJ", "Previously injected, bye");
            return true;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                applicationContext
        );
        String deviceId = sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID,
                Constants.DEFAULT_VALUES.DEFAULT_DEVICE_ID);
//        String sessionId = BackendStorage.getInstance().getSessionId();
        if (deviceId == null || deviceId.isEmpty()) {
            Logger.warning("PogoEnhancerJ", "Could not read deviceID");
            return false;
        }

        this.copyInjectionLib();
        boolean runSuccess = this.runInjector(pogoPid, deviceId, again, applicationContext);
        //this.deleteInjectionLibraryFromPogo();
        if (runSuccess) {
            Logger.warning("PogoEnhancerJ", "Successfully injected.");
            this.lastPogoPidInjected = pogoPid;
            long unixTime = System.currentTimeMillis() / 1000L;
            SharedPreferences.Editor edit = this._sharedPreferences.edit();
            edit.putLong(Constants.SHAREDPERFERENCES_KEYS.LAST_TIME_INJECTED, unixTime);
            edit.putInt(Constants.SHAREDPERFERENCES_KEYS.LAST_PID_INJECTED, pogoPid);
            edit.apply();
        }
        return runSuccess;
    }

    public synchronized boolean isSetup() {
        return this._sharedPreferences != null && this._applicationContext != null;
    }

    public synchronized boolean inject(Context applicationContext) {
        int pogoPid = this.getPogoPid();
        return this.inject(pogoPid, false, applicationContext);
    }

    private long getTimeStarted(int pid) {
        ShellHelper shellHelper = new ShellHelper();
        ArrayList<String> strings = shellHelper.runCommand(Runlevel.su, "stat -c%X /proc/" + pid);
        if (strings == null || strings.size() != 1 || strings.get(0).isEmpty()) {
            return -1;
        } else {
            try {
                return Long.parseLong(strings.get(0));
            } catch (NumberFormatException ex) {
                Logger.error("PogoEnhancerJ", "Could not retrieve duration pogo has been running");
                return -1;
            }
        }
    }

    public synchronized void setupInstance(Context _applicationContext, SharedPreferences _sharedPreferences) {
        if (this.isSetup()) {
            return;
        }
        //TODO: check caller is Main
        this._sharedPreferences = _sharedPreferences;
        this._applicationContext = _applicationContext;

        long lastTimeInjected = _sharedPreferences.getLong(
                Constants.SHAREDPERFERENCES_KEYS.LAST_TIME_INJECTED,
                Constants.DEFAULT_VALUES.LAST_TIME_INJECTED
        );

        long unixTime = System.currentTimeMillis() / 1000L;
        //check if last injection was within the last 4hours, else just ignore this crap entirely...
        if (unixTime - lastTimeInjected < 4 * 60 * 60) {
            this.lastPogoPidInjected = _sharedPreferences.getInt(
                    Constants.SHAREDPERFERENCES_KEYS.LAST_PID_INJECTED,
                    Constants.DEFAULT_VALUES.LAST_PID_INJECTED
            );
        }
        //TODO: check pogo injected and kill if necessary...
        if (getPogoPid() == this.lastPogoPidInjected) {
            Logger.warning("PogoEnhancerJ", "Killing pogo as a precaution");
            ShellHelper shellHelper = new ShellHelper();
            shellHelper.killAppByPackageName("com.nianticlabs.pokemongo");
        }
    }

    private synchronized boolean runInjector(int pid, String deviceId,
                                             boolean again, Context applicationContext) {
        if (this._applicationContext == null || this._sharedPreferences == null) {
            Logger.error("PogoEnhancerJ", "AppContext or sharedprefs null");
            return false;
        } else if (this.getPogoPid() != pid) {
            Logger.warning("PogoEnhancerJ", "Pogo PID has changed, trying again...");
            if (!again) {
                return injectWithDelay(true, applicationContext);
            } else {
                Logger.error("PogoEnhancerJ", "Failed injecting again");
                return false;
            }
        }

        NotificationManager notificationManager =
                (NotificationManager) applicationContext.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );
        NotificationCompat.Builder notificationBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(applicationContext, HEADS_UP_CHANNEL_ID)
                    .setContentTitle("Injection")
                    .setContentText("Started...")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_autorenew_black_24dp);
        } else {
            //noinspection deprecation
            notificationBuilder = new NotificationCompat.Builder(applicationContext)
                    .setSmallIcon(R.drawable.ic_autorenew_black_24dp)
                    .setContentTitle("Injection")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentText("Started...");
        }


        String injector = "libe.so";
        //deprecated since we do not need 64bit injection
        if (_sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.USE_ARM64_INJECTOR,
                Constants.DEFAULT_VALUES.USE_ARM64_INJECTOR)) {
            injector = "liba.so";
        }

        //TODO: move copyLib call down here and check + get path from it...
        notificationBuilder.setContentText("Preparing environment");
        String pogoLibFolder = getPogoDataAppLibPath();
        if (pogoLibFolder == null) {
            Logger.error("PogoEnhancerJ", "Could not find Pogo lib path");
            notificationBuilder.setContentTitle("Failed");
            notificationBuilder.setContentText("Preparing environment failed :(");
            notificationManager.notify(35432, notificationBuilder.build());
            return false;
        }
        String libname = _sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.LIBFILENAME,
                Constants.DEFAULT_VALUES.LIBFILENAME);
        String derp = this._applicationContext.getPackageCodePath().replace("base.apk", "lib/arm/" + injector);


        boolean externalCommunicationDisabled = _sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.DISABLE_EXTERNAL_COMMUNICATION,
                Constants.DEFAULT_VALUES.DISABLE_EXTERNAL_COMMUNICATION);

        String base64Topass = "";
        String origin = "";
        String destination = "";
        if (!externalCommunicationDisabled) {
            origin = _sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.POST_ORIGIN,
                    Constants.DEFAULT_VALUES.POST_ORIGIN);
            boolean authEnabled = _sharedPreferences.getBoolean(
                    Constants.SHAREDPERFERENCES_KEYS.AUTH_ENABLED,
                    Constants.DEFAULT_VALUES.AUTH_ENABLED);
            String authUsername = _sharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.AUTH_USERNAME,
                    Constants.DEFAULT_VALUES.AUTH_USERNAME);
            String authPassword = _sharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.AUTH_PASSWORD,
                    Constants.DEFAULT_VALUES.AUTH_PASSWORD);
            destination = _sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.POST_DESTINATION,
                    Constants.DEFAULT_VALUES.DEFAULT_POST_DESTINATION);
            destination = android.util.Base64.encodeToString(destination.getBytes(), Base64.DEFAULT).trim();

            if (authEnabled) {
                String authBase = authUsername + ":" + authPassword;
                base64Topass = android.util.Base64.encodeToString(authBase.getBytes(), Base64.DEFAULT).trim();
            }
        }

        int additionalSettings = 0;
//        boolean rawProtosEnabled = _sharedPreferences.getBoolean(
//                Constants.SHAREDPERFERENCES_KEYS.SEND_RAW_PROTOS,
//                Constants.DEFAULT_VALUES.SEND_RAW_PROTOS
//        );
        boolean rawProtosEnabled = false;
        if (rawProtosEnabled) {
            additionalSettings |= RAW_PROTOS_ENABLED | 1;
        }

        if (libname == null || origin == null) {
            Logger.warning("PogoEnhancerJ", "Injection failed due to missing input, " +
                    "libname or origin");
            //pogo likely froze to death, force stop it
            notificationBuilder.setContentTitle("Failed");
            notificationBuilder.setContentText("Failed injecting due to invalid data.");
            notificationManager.notify(35432, notificationBuilder.build());
            return false;
        }

        String userIdEncoded = null;
        String deviceIdEncoded = null;
        try {
            deviceIdEncoded = Constants.hexadecimal(deviceId.trim());
        } catch (UnsupportedEncodingException e) {
            Logger.fatal("PogoEnhancerJ", "Failed converting to hex when starting the injection");
            return false;
        }

        String command = derp.trim() + " "
                + pid + " "
                + "foo" + "," + "bar"
                + "," + deviceIdEncoded + "," + origin.trim() + ","
                + base64Topass.trim() + "," + destination.trim() + "," + additionalSettings
                + " " + pogoLibFolder.trim() + libname.trim();
        ArrayList<String> commands = new ArrayList<>();
        boolean success = false;
        ShellHelper shellHelper = new ShellHelper();
        commands.add("rm -rf /data/local/tmp/frida-*");
        if (_sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.CALL_SETENFORCE,
                Constants.DEFAULT_VALUES.CALL_SETENFORCE)) {
            commands.add("setenforce 0");
        }

        commands.add(command);
        commands.add("echo $?");
        ArrayList<String> output = shellHelper.runCommands(Runlevel.su, commands);
        Logger.debug("PogoEnhancerJ", "Injection resulted in: "
                + output.toString().replaceAll("frida", ""));
        if (output.size() > 0 && output.get(output.size() - 1).equals("0")) {
            notificationBuilder.setContentTitle("Success");
            notificationBuilder.setContentText("Injection successful, have fun.");
            success = true;
        } else {
            Logger.warning("PogoEnhancerJ", "Injection failed with (I): "
                    + output.toString().replaceAll("frida", ""));
            //pogo likely froze to death, force stop it
            notificationBuilder.setContentTitle("Failed");
            notificationBuilder.setContentText("Failed injecting. Killing pogo and trying again.");
            shellHelper.killAppByPackageName("com.nianticlabs.pokemongo");
        }
        notificationManager.notify(35432, notificationBuilder.build());

        return success;
    }

    private synchronized boolean copyInjectionLib() {
        Logger.debug("PogoEnhancerJ", "Copying lib");
        ShellHelper shellHelper = new ShellHelper();
        String randomLibFilename = "libd.so";
        String pogoLibFolder = getPogoDataAppLibPath();
        if (pogoLibFolder == null) {
            Logger.error("PogoEnhancerJ", "Could not find Pogo lib path");
            return false;
        }
        if (this._sharedPreferences != null) {
            randomLibFilename = this._sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.LIBFILENAME,
                    Constants.DEFAULT_VALUES.LIBFILENAME);

            if (randomLibFilename.equals(Constants.DEFAULT_VALUES.LIBFILENAME)) {
                //apparently we are still running default name, let's check for presence in pogo folder, delete if needed
                //and copy new random name in...
                randomLibFilename = Constants.randomString(7) + ".so";
                //TODO: catch rare case where it may collide with pogo libs
                //just try to delete libd.so from pogo
                SharedPreferences.Editor editor = this._sharedPreferences.edit();
                editor.putString(Constants.SHAREDPERFERENCES_KEYS.LIBFILENAME, randomLibFilename);
                editor.apply();

                ArrayList<String> lines = shellHelper.runCommand(Runlevel.su, "for file in " + pogoLibFolder + "* ;do echo $file ;done");
                if (lines.isEmpty()) {
                    return false;
                } else {
                    for (String line : lines) {
                        if (line.contains("libd.so")) {
                            //delete the old default and stop
                            shellHelper.runCommand(Runlevel.su, "rm " + pogoLibFolder + "libd.so");
                        }
                    }
                }
            }
        }
//        String dataDir = this._applicationContext.getApplicationInfo().dataDir;
        String dataDir = this._applicationContext.getPackageCodePath().replace("base.apk", "lib/arm/");
        shellHelper.runCommand(Runlevel.su, "cp " + dataDir + "/libd.so " + pogoLibFolder + randomLibFilename);
        return true; //check output...
    }

    private synchronized boolean deleteInjectionLibraryFromPogo() {
        ShellHelper shellHelper = new ShellHelper();
        String randomLibFilename = "libd.so";
        if (this._sharedPreferences != null) {
            randomLibFilename = this._sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.LIBFILENAME,
                    Constants.DEFAULT_VALUES.LIBFILENAME);
        }
        if (randomLibFilename == null) {
            Logger.fatal("PogoEnhancerJ", "Could not grab filename");
            return false;
        } else if (randomLibFilename.length() > 0) {
            shellHelper.runCommand(Runlevel.su, "rm /data/data/com.nianticlabs.pokemongo/lib/" + randomLibFilename);
            return true;
        } else {
            return false;
        }
    }

    private synchronized int getPogoPid() {
        ShellHelper shellHelper = new ShellHelper();
        ArrayList<String> processIdsOfPackage = shellHelper.getProcessIdsOfPackage(Runlevel.su, "com.nianticlabs.pokemongo");
        if (processIdsOfPackage.size() != 1 || processIdsOfPackage.get(0).isEmpty()) {
            Logger.debug("PogoEnhancerJ", "Returned PIDs empty or multiple returned: " + processIdsOfPackage.toString());
            return -1;
        } else {
            try {
                return Integer.parseInt(processIdsOfPackage.get(0));
            } catch (NumberFormatException ex) {
                Logger.error("PogoEnhancerJ", "Could not retrieve Pogo PID");
                return -1;
            }
        }
    }

    private synchronized String getPogoDataAppLibPath() {
        ShellHelper shellHelper = new ShellHelper();
        ArrayList<String> results = shellHelper.runCommand(Runlevel.su, "for folder in /data/app/* ;do echo $folder ;done");
        for (String folder : results) {
            if (folder.contains("com.nianticlabs.pokemongo")) {
                return folder + "/lib/arm/";
            }
        }
        return null;
    }
}
