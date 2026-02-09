package com.mad.pogoenhancer.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;

import androidx.core.app.NotificationCompat;

import com.mad.pogoenhancer.BackendStorage;
import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.services.HookReceiverService;
import com.mad.pogoenhancer.services.InjectionSettingSender;
import com.mad.shared.utils.Runlevel;
import com.topjohnwu.superuser.Shell;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mad.pogoenhancer.App.HEADS_UP_CHANNEL_ID;

public class PogoPatcher {
    private final SharedPreferences _sharedPreferences;
    private String _processNameBeingWatched = null;
    private boolean _running = false;
    private PogoPatchingThread _pogoPatchingThread = null;
    private final ShellHelper helper = new ShellHelper();
    private final HookReceiverService _hookReceiverService;

    private final ExecutorService _executorService = Executors.newCachedThreadPool();
    private static final int RAW_PROTOS_ENABLED = 0B1;
    private static final int REPLACE_CP_WITH_IV_PERCENTAGE = 0B10;
    private static final int ENHANCE_THROW_GREAT_EXCELLENT_RANDOM = 0B100;
    private static final int ENHANCE_THROW_EXCELLENT = 0B1000;
    private static final int FAST_CATCH_SPEEDUP = 0B10000;
    private static final int ENABLE_AUTOSPIN = 0B100000;
    private static final int FAST_CATCH_QUICK = 0B1000000;
    private static final int SKIP_ENCOUNTER_INTO = 0B10000000;
    private static final int EASY_CATCH_PACIFIST = 0B100000000;
    private static final int EASY_CATCH_IMMOBILIZED =  0B1000000000;
    private static final int AUTORUN_MIN_IV =          0B11111110000000000;
    private static final int REPLACE_ENCOUNTER_NAMES = 0B100000000000000000;
    private static final int OVERLAY_SHOW_XSXL =       0B1000000000000000000;
    private static final int UNLOCK_FPS =                  0B10000000000000000000;
    private static final int SKIP_EVOLVE_ANIMATION =       0B100000000000000000000;
    private static final int SPEEDUP_GIFT_OPENING =        0B1000000000000000000000;
    private static final int MASSTRANSFER =        0B10000000000000000000000;
    private static final int KEEP_ENC_UI =        0B100000000000000000000000;
    private static final int DISABLE_GRUNTS =    0B1000000000000000000000000;
    private static final int ENABLE_AUTORUN =   0B10000000000000000000000000;
    private static final int INCREASE_VISIBILITY = 0B100000000000000000000000000;
    private static final int SAVE_LAST_USED_BALL = 0B1000000000000000000000000000;
    private static final int PINAP_MODE = 0B10000000000000000000000000000;
    private static final int USE_NANNY = 0B100000000000000000000000000000;
    private static final int ENABLE_AUTOTRANSFER = 0B1000000000000000000000000000000;
    private static final int ENABLE_AUTOENCOUNTER = 0B10000000000000000000000000000000;

    private static long _lastVersionInstalledFetch = 0;
    private static String _versionInstalled = "N/A";

    private static long _lastReadOfLibFolders = 0;
    private static List<String> libFolders = null;

    public static String getPogoVersionInstalled(Context appContext) {
        if (_lastVersionInstalledFetch + 1000 * 600 > System.currentTimeMillis()) { // Only check the version every 10 minutes...
            Logger.pdebug("ProtoHookJ", "Cached pogo version '" + PogoPatcher._versionInstalled + "'");
            return PogoPatcher._versionInstalled;
        }
        String installedVersion = "N/A";
        try {
            PackageInfo pinfo = null;
            pinfo = appContext.getPackageManager().getPackageInfo("com.nianticlabs.pokemongo", 0);
            int verCode = pinfo.versionCode;
            installedVersion = pinfo.versionName;
            Logger.pdebug("ProtoHookJ", "VersionName: " + pinfo.versionName +
                    " Version code: " + verCode);
            if (isArm64LibFolderPresent(appContext)) {
                installedVersion += "_64";
            } else {
                installedVersion += "_32";
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.warning("ProtoHookJ", "Could not find Pogo version.");
        }
        // Set the _versionInstalled as a cache. The function should be called multiple times anyway
        PogoPatcher._versionInstalled = installedVersion;
        PogoPatcher._lastVersionInstalledFetch = System.currentTimeMillis();
        Logger.pdebug("ProtoHookJ", "Pogo version '" + PogoPatcher._versionInstalled + "'");
        return installedVersion;
    }

    public PogoPatcher(HookReceiverService hookReceiverService) {
        this._processNameBeingWatched = "com.nianticlabs.pokemongo";
        this._hookReceiverService = hookReceiverService;
        this._sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_hookReceiverService);
    }

    public synchronized void start(Context context) {
        Logger.info("PogoEnhancerJ", "Starting pogo patcher");
        this.stop();
        if (this._pogoPatchingThread == null) {
            this._pogoPatchingThread = new PogoPatchingThread(this,
                    context);
        }
        this._pogoPatchingThread.start();
        Logger.info("PogoEnhancerJ", "Started pogo patcher");
    }

    public synchronized void stop() {
        if (this._pogoPatchingThread != null) {
            this._pogoPatchingThread.interrupt();
        }
    }

    private int getInitialSettingsBitmask() {
        int additionalSettings = 0;
        boolean rawProtosEnabled = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SEND_RAW_PROTOS,
                Constants.DEFAULT_VALUES.SEND_RAW_PROTOS
        );
        if (rawProtosEnabled) {
            additionalSettings |= RAW_PROTOS_ENABLED;
        }
        boolean replaceCpWithIvPercentage = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.REPLACE_CP_WITH_IV_PERCENTAGE,
                Constants.DEFAULT_VALUES.REPLACE_CP_WITH_IV_PERCENTAGE
        );
        if (replaceCpWithIvPercentage) {
            additionalSettings |= REPLACE_CP_WITH_IV_PERCENTAGE;
        }
        boolean replaceEncounterNames = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.REPLACE_ENCOUNTER_NAMES,
                Constants.DEFAULT_VALUES.REPLACE_ENCOUNTER_NAMES
        );
        if (replaceEncounterNames) {
            additionalSettings |= REPLACE_ENCOUNTER_NAMES;
        }

        boolean showheightweightvalue = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_XSXL,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_XSXL
        );
        if (showheightweightvalue) {
            additionalSettings |= OVERLAY_SHOW_XSXL;
        }

        String enhancedThrowMethod = _sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.ENHANCED_CAPTURE,
                Constants.DEFAULT_VALUES.ENHANCED_CAPTURE
        );
        if (!enhancedThrowMethod.toLowerCase().equals("disabled")) {
            if (enhancedThrowMethod.equals("Great and better (random)")) {
                additionalSettings |= ENHANCE_THROW_GREAT_EXCELLENT_RANDOM;
            } else {
                additionalSettings |= ENHANCE_THROW_EXCELLENT;
            }
        }

        String fastCatchOption = _sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.FAST_CATCH_OPTION,
                Constants.DEFAULT_VALUES.FAST_CATCH_OPTION);
        if (!fastCatchOption.toLowerCase().equals("none")) {
            if (fastCatchOption.toLowerCase().equals("speedup")) {
                additionalSettings |= FAST_CATCH_SPEEDUP;
            } else {
                additionalSettings |= FAST_CATCH_QUICK;
            }
        }

        String easyCatchOption = _sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.EASY_CATCH_OPTION,
                Constants.DEFAULT_VALUES.EASY_CATCH_OPTION);
        if (!easyCatchOption.toLowerCase().equals("none")) {
            if (easyCatchOption.toLowerCase().equals("pacifist")) {
                additionalSettings |= EASY_CATCH_PACIFIST;
            } else {
                additionalSettings |= EASY_CATCH_IMMOBILIZED;
            }
        }

        boolean unlockFps = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.UNLOCK_FPS,
                Constants.DEFAULT_VALUES.UNLOCK_FPS
        );
        if (unlockFps) {
            additionalSettings |= UNLOCK_FPS;
        }

        boolean skipEvolveAnimation = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SKIP_EVOLVE_ANIMATION,
                Constants.DEFAULT_VALUES.SKIP_EVOLVE_ANIMATION
        );
        if (skipEvolveAnimation) {
            additionalSettings |= SKIP_EVOLVE_ANIMATION;
        }

        boolean speedupGiftOpen = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SPEEDUP_GIFT_OPENING,
                Constants.DEFAULT_VALUES.SPEEDUP_GIFT_OPENING
        );
        if (speedupGiftOpen) {
            additionalSettings |= SPEEDUP_GIFT_OPENING;
        }

        boolean masstransfer = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.ENABLE_MASSTRANSFER,
                Constants.DEFAULT_VALUES.ENABLE_MASSTRANSFER
        );
        if (masstransfer) {
            additionalSettings |= MASSTRANSFER;
        }

        boolean keepEncounterUi = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.ENABLE_KEEP_ENCOUNTER_UI,
                Constants.DEFAULT_VALUES.ENABLE_KEEP_ENCOUNTER_UI
        );
        if (keepEncounterUi) {
            additionalSettings |= KEEP_ENC_UI;
        }

        boolean disableGrunts = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.DISABLE_GRUNTS,
                Constants.DEFAULT_VALUES.DISABLE_GRUNTS
        );
        if (disableGrunts) {
            additionalSettings |= DISABLE_GRUNTS;
        }

        boolean increaseVisibility = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.INCREASE_VISIBILITY,
                Constants.DEFAULT_VALUES.INCREASE_VISIBILITY
        );
        if (increaseVisibility) {
            additionalSettings |= INCREASE_VISIBILITY;
        }

            /*boolean skipEncounterIntro = _sharedPreferences.getBoolean(
                    Constants.SHAREDPERFERENCES_KEYS.ENABLE_SKIP_ENCOUNTER_INTRO,
                    Constants.DEFAULT_VALUES.ENABLE_SKIP_ENCOUNTER_INTRO
            );
            if (skipEncounterIntro) {
                additionalSettings |= SKIP_ENCOUNTER_INTO;
            }*/

        if (_sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.ENABLE_AUTOSPIN,
                Constants.DEFAULT_VALUES.ENABLE_AUTOSPIN)) {
            additionalSettings |= ENABLE_AUTOSPIN;
        }

        if (_sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.SAVE_LAST_USED_BALL,
                Constants.DEFAULT_VALUES.SAVE_LAST_USED_BALL)) {
            additionalSettings |= SAVE_LAST_USED_BALL;
        }

        if (_sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.PINAP_MODE,
                Constants.DEFAULT_VALUES.PINAP_MODE)) {
            additionalSettings |= PINAP_MODE;
        }

        if (_sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.USE_NANNY,
                Constants.DEFAULT_VALUES.USE_NANNY)) {
            additionalSettings |= USE_NANNY;
        }

        if (_sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.ENABLE_AUTOTRANSFER,
                Constants.DEFAULT_VALUES.ENABLE_AUTOTRANSFER)) {
            additionalSettings |= ENABLE_AUTOTRANSFER;
        }

        if (_sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.ENABLE_AUTOENCOUNTER,
                Constants.DEFAULT_VALUES.ENABLE_AUTOENCOUNTER)) {
            additionalSettings |= ENABLE_AUTOENCOUNTER;
        }

        if (_sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.ENABLE_AUTORUN,
                Constants.DEFAULT_VALUES.ENABLE_AUTORUN)) {
            additionalSettings |= ENABLE_AUTORUN;
        }

        String minIvString = _sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.AUTORUN_MIN_IV,
                Constants.DEFAULT_VALUES.AUTORUN_MIN_IV);
        int minIv = Integer.parseInt(minIvString);
        minIv = (minIv << 10) & AUTORUN_MIN_IV;
        additionalSettings |= minIv;
        return additionalSettings;
    }

    public void sendCredentials() throws JSONException {
        String deviceId = BackendStorage.getInstance().getDeviceId();

        if (deviceId == null || deviceId.isEmpty()) {
            Logger.warning("ProtoHookJ", "Could not read deviceID");
            return;
        }
        JSONObject credentials = new JSONObject();
        String deviceIdEncoded = null;
        try {
            deviceIdEncoded = Constants.hexadecimal(deviceId.trim());
        } catch (UnsupportedEncodingException e) {
            Logger.fatal("ProtoHookJ", "Failed converting to hex when starting the pogo patch");
            return;
        }
        boolean externalCommunicationDisabled = this._sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.DISABLE_EXTERNAL_COMMUNICATION,
                Constants.DEFAULT_VALUES.DISABLE_EXTERNAL_COMMUNICATION);
        String base64Topass = "";
        String origin = "";
        String destination = "";
        if(!externalCommunicationDisabled) {
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

            if(authEnabled) {
                String authBase = authUsername + ":" + authPassword;
                base64Topass = android.util.Base64.encodeToString(authBase.getBytes(), Base64.DEFAULT).trim();
            }
        }

        int specialSettings = getInitialSettingsBitmask();
        String versionInstalled = PogoPatcher.getPogoVersionInstalled(this._pogoPatchingThread._appContext);
        String additionalSettings = String.valueOf(specialSettings) + "," + versionInstalled;

        try {
            additionalSettings = Constants.hexadecimal(additionalSettings);
        } catch (UnsupportedEncodingException e) {
            Logger.fatal("ProtoHookJ", "Failed converting to hex when starting the pogo patch");
            return;
        }

        credentials.put("deviceId", deviceIdEncoded);
        credentials.put("sessionId", "");
        credentials.put("authHeader", base64Topass);
        credentials.put("destination", destination);
        credentials.put("origin", origin);
        credentials.put("settings", additionalSettings);

        InjectionSettingSender.sendCredentials(credentials);
    }

    public static List<String> getAllLibFolderPathsOfPogo(Context context, boolean useCachedPath) {
        if (useCachedPath && libFolders != null && !libFolders.isEmpty()
                &&_lastReadOfLibFolders + 1000 * 600 > System.currentTimeMillis()) { // Only check the version every 10 minutes...
            Logger.pdebug("ProtoHookJ", "Cached lib folders '" + libFolders.toString() + "'");
            return libFolders;
        }
        ShellHelper helper = new ShellHelper();
        List<String> armFolders = helper.runCommand(Runlevel.su, "find /data/app | grep -E \"com\\.nianticlabs\\.pokemongo\" | grep -E \"lib/arm\" | grep -vE \"\\.so$|\\.ares\"");
        if (!armFolders.isEmpty()) {
            libFolders = armFolders;
            PogoPatcher._lastReadOfLibFolders = System.currentTimeMillis();
            return armFolders;
        }

        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String appSubstring = "/app/";
        String sourceDir = applicationInfo.sourceDir;
        int indexToCutTo = sourceDir.indexOf(appSubstring) + appSubstring.length();
        String dataAppDir = sourceDir.substring(0, indexToCutTo);
        List<String> armExtendedSearchFolder = helper.runCommand(Runlevel.su, "find " + dataAppDir + " | grep -E \"com\\.nianticlabs\\.pokemongo\" | grep -E \"lib/arm\" | grep -vE \"\\.so$\"");
        libFolders = armExtendedSearchFolder;
        PogoPatcher._lastReadOfLibFolders = System.currentTimeMillis();
        return armExtendedSearchFolder;
    }

    public static boolean isArm64LibFolderPresent(Context context) {
        List<String> results = getAllLibFolderPathsOfPogo(context, true);
        boolean allArm64 = true;
        for (String folder : results) {
            allArm64 = allArm64 && folder.contains("arm64");
        }
        return allArm64;
    }

    private static class PogoPatchingThread extends Thread {


        private PogoPatcher _pogoPatcher = null;
        private ShellHelper _shellHelper = null;
        private SharedPreferences _sharedPreferences = null;
        private String _injectorPath = null;
        private Context _appContext = null;
        private boolean _seLinuxAlreadyPatched = false;

        PogoPatchingThread(PogoPatcher owningPatcher, Context context) {
            this._pogoPatcher = owningPatcher;
            this._shellHelper = new ShellHelper();
            this._appContext = context;
            this._sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            
            String injector = "libe.so";
            //deprecated since we do not need 64bit injection
            if (PogoPatcher.isArm64LibFolderPresent(context)) {
                injector = "liba.so";
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                    // Android api > 28 (Android 9) -> Use latest frida
                    injector = "libh.so";
                }
                this._injectorPath = context.getPackageCodePath().replace("base.apk",
                        "lib/arm64/" + injector);
            } else {
                this._injectorPath = context.getPackageCodePath().replace("base.apk",
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
                    Logger.error("PogoEnhancerJ", "Could not retrieve duration pogo has been running");
                    return -1;
                }
            }
        }

        private int getLatestPogoPid() {
            ArrayList<String> pogoPids = _shellHelper.getProcessIdsOfPackage(
                    Runlevel.su, this._pogoPatcher._processNameBeingWatched);
            if (pogoPids.isEmpty()) {
                return -1;
            } else if (pogoPids.size() > 1) {
                Logger.fatal("PogoEnhancerJ", "Got more than one process...");
                return -1;
            } else {
                try {
                    int pid = Integer.parseInt(pogoPids.get(0));
                    Logger.debug("PogoEnhancerJ", "Got PID " + pid);
                    return pid;
                } catch (NumberFormatException ex) {
                    Logger.error("PogoEnhancerJ", "Could not retrieve PID of pogo");
                    return -1;
                }
            }
        }

        private synchronized boolean copyInjectionLib() {
            Logger.debug("PogoEnhancerJ", "Copying lib");
            List<String> allLibFolders = getAllLibFolderPathsOfPogo(this._appContext, false);

            String randomLibFilename = "libd.so";
            if (allLibFolders == null || allLibFolders.isEmpty()) {
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


                    for (String libFolder : allLibFolders) {
                        ArrayList<String> lines = _shellHelper.runCommand(Runlevel.su, "for file in " + libFolder + "* ;do echo $file ;done");
                        if (lines.isEmpty()) {
                            return false;
                        } else {
                            for (String line : lines) {
                                if (line.contains("libd.so")) {
                                    //delete the old default and stop
                                    _shellHelper.runCommand(Runlevel.su, "rm " + libFolder + "/libd.so");
                                }
                            }
                        }
                    }
                }
            }
//        String dataDir = this._applicationContext.getApplicationInfo().dataDir;
            String dataDir;

            if (PogoPatcher.isArm64LibFolderPresent(this._appContext)) {
                dataDir = this._appContext.getPackageCodePath().replace("base.apk", "lib/arm64/");
            } else {
                dataDir = this._appContext.getPackageCodePath().replace("base.apk", "lib/arm/");
            }

            for (String folder : allLibFolders) {
                _shellHelper.runCommand(Runlevel.su, "cp " + dataDir + "/libd.so " + folder + "/" + randomLibFilename);
            }
            return true; //check output...
        }

        private String getSELinuxContext(String packageName) {
            String psCommand = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? "ps -AZ" : "ps -Z";
            psCommand += " | grep -E \"" + packageName + "\" | grep -vE \"root|shell|grep|pokemongo:\"";
            ArrayList<String> psLines = this._shellHelper.runCommand(Runlevel.su, psCommand);
            if (psLines == null || psLines.isEmpty()) {
                Logger.pdebug("PogoEnhancerJ", "Could not determine context");
                return getDefaultSELinuxContext(packageName);
            }

            // parse the first line of psLines and check the context...
            String psLineOfPackage = psLines.get(0);
            String[] cols = psLineOfPackage.split("\\s");
            if (cols.length == 0) {
                return getDefaultSELinuxContext(packageName);
            }
            String[] split = cols[0].split(":");
            if (split.length < 4) {
                Logger.error("PogoEnhancerJ", "Could not determine context of " + psLines);
                return getDefaultSELinuxContext(packageName);
            }

            return split[2];
        }

        private String getDefaultSELinuxContext(String packageName) {
            if (packageName.equals(this._appContext.getPackageName())) {
                // we are referring to ourselves, we can determine the context with ps -Z OR just assume
                if (((this._appContext.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0)) {
                    return "priv_app";
                } else {
                    return "untrusted_app";
                }
            }

            return "untrusted_app";
        }

        private boolean inject(int pid) throws InterruptedException {
//            String deviceId = this._sharedPreferences.getString(
//                    Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID,
//                    Constants.DEFAULT_VALUES.DEFAULT_DEVICE_ID);
            String deviceId = BackendStorage.getInstance().getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                Logger.warning("PogoEnhancerJ", "Could not read deviceID");
                return false;
            }

            NotificationManager notificationManager =
                    (NotificationManager) this._appContext.getSystemService(
                            Context.NOTIFICATION_SERVICE
                    );
            NotificationCompat.Builder notificationBuilder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationBuilder = new NotificationCompat.Builder(this._appContext, HEADS_UP_CHANNEL_ID)
                        .setContentTitle("Injection")
                        .setContentText("Started...")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSmallIcon(R.drawable.ic_autorenew_black_24dp);
            } else {
                //noinspection deprecation
                notificationBuilder = new NotificationCompat.Builder(this._appContext)
                        .setSmallIcon(R.drawable.ic_autorenew_black_24dp)
                        .setContentTitle("Injection")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentText("Started...");
            }

            String deviceIdEncoded = null;
            try {
                deviceIdEncoded = Constants.hexadecimal(deviceId.trim());
            } catch (UnsupportedEncodingException e) {
                Logger.fatal("PogoEnhancerJ", "Failed converting to hex when starting the pogo patch");
                return false;
            }

            List<String> pogoLibFolder = getAllLibFolderPathsOfPogo(this._appContext, true);
            if (pogoLibFolder == null || pogoLibFolder.isEmpty()) {
                Logger.error("PogoEnhancerJ", "Could not find Pogo lib path");
                notificationBuilder.setContentTitle("Failed");
                notificationBuilder.setContentText("Preparing environment failed :(");
                notificationManager.notify(35432, notificationBuilder.build());
                return false;
            }
            String libname = _sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.LIBFILENAME,
                    Constants.DEFAULT_VALUES.LIBFILENAME);

            if (libname == null || libname.isEmpty()) {
                Logger.warning("PogoEnhancerJ", "Injection failed due to missing " +
                        "libname or origin");
                //pogo likely froze to death, force stop it
                notificationBuilder.setContentTitle("Failed");
                notificationBuilder.setContentText("Failed injecting due to invalid data.");
                notificationManager.notify(35432, notificationBuilder.build());
                return false;
            }

            // TODO: Support multiple pogo instances...
            String command = this._injectorPath.trim() + " "
                    + pid + " "
                    + deviceIdEncoded
                    + " " + pogoLibFolder.get(0).trim() + "/" + libname.trim();

            ArrayList<String> commands = new ArrayList<>();
            boolean success = false;
            boolean explicitIssueFound = false;
            commands.add("rm -rf /data/local/tmp/frida-*");
            commands.add("chmod +x " + this._injectorPath.trim());
            commands.add(command);
            commands.add("echo $?");

            Callable<Object> commandsExecutionTask = () -> _shellHelper.runCommands(Runlevel.su, commands);
            Future<Object> future = this._pogoPatcher._executorService.submit(commandsExecutionTask);
            ArrayList<String> output = null;
            try {
                Object result = future.get(15, TimeUnit.SECONDS);
                output = (ArrayList<String>) result;
            } catch (TimeoutException ex) {
                // handle the timeout of calling the injection. This usually happens at the very last step...
                Logger.warning("ProtoHookJ", "Timeout during the execution of injection. Assuming a success");
                notificationBuilder.setContentTitle("Likely Success");
                notificationBuilder.setContentText("Injection likely successful, please check and have fun.");
                notificationManager.notify(1333337, notificationBuilder.build());
                return true;
            } catch (InterruptedException e) {
                // handle the interrupts
            } catch (ExecutionException e) {
                // handle other exceptions
            } finally {
                future.cancel(true); // may or may not desire this
            }
            if (output == null) return false;
            Logger.debug("PogoEnhancerJ", "Injection resulted in: "
                    + output.toString().replaceAll("frida", ""));
            ArrayList<String> filtered = new ArrayList<>();
            for (String part : output) {
                if (part == null) {
                    continue;
                }
                else if (part.contains("does not match my version range 15-30")) {
                    continue;
                } else if (part.trim().toLowerCase().startsWith("warning")) {
                    continue;
                } else if (part.contains("Unable to find process")) {
                    explicitIssueFound = true;
                } else if (part.contains("Unable to open library")) {
                    explicitIssueFound = true;
                } else {
                    filtered.add(part);
                }
            }
            if (!explicitIssueFound
                    && (filtered.size() == 1 || filtered.get(filtered.size() - 1).equals("0"))) {
                notificationBuilder.setContentTitle("Success");
                notificationBuilder.setContentText("Injection successful, have fun.");
                success = true;
            } else {
                Logger.warning("PogoEnhancerJ", "Injection failed with: "
                        + output.toString().replaceAll("frida", ""));
                //pogo likely froze to death, force stop it
                notificationBuilder.setContentTitle("Failed");
                notificationBuilder.setContentText("Failed injecting. Killing pogo and trying again.");

            }
            notificationManager.notify(1333337, notificationBuilder.build());

            try {
                PackageInfo pInfo = this._appContext.getPackageManager().getPackageInfo(this._appContext.getPackageName(), 0);
                String version = pInfo.versionName;
                Logger.info("ProtoHookJ", "Current PogoEnhancer version: " + version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        public void run() {
            this._pogoPatcher._running = true;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Logger.info("PogoEnhancerJ", "Interrupted pogo patcher");
            }
            while (!this.isInterrupted()) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                Logger.debug("PogoEnhancerJ", "Checking pogo PID");
                int latestPogoPid = getLatestPogoPid();
                if (latestPogoPid == -1) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
                int lastInjectedPid = this._sharedPreferences.getInt(
                        Constants.SHAREDPERFERENCES_KEYS.LAST_PID_INJECTED,
                        Constants.DEFAULT_VALUES.LAST_PID_INJECTED);

                Logger.debug("PogoEnhancerJ", "Checking pogo started timestamp");
                long timeStarted = this.getTimeStarted(latestPogoPid);
                long lastTimeInjected = this._sharedPreferences.getLong(
                        Constants.SHAREDPERFERENCES_KEYS.LAST_TIME_INJECTED,
                        Constants.DEFAULT_VALUES.LAST_TIME_INJECTED
                );

                if (lastInjectedPid != latestPogoPid) {
                    Logger.info("PogoEnhancerJ", "PID mismatch");
                }
                if (timeStarted > lastTimeInjected) {
                    Logger.info("PogoEnhancerJ", "Time older");
                }

                boolean toggleDetectionMethodToAND = this._sharedPreferences.getBoolean(
                        Constants.SHAREDPERFERENCES_KEYS.TOGGLE_INJECTION_DETECTION,
                        Constants.DEFAULT_VALUES.TOGGLE_INJECTION_DETECTION
                );

                if((toggleDetectionMethodToAND && lastInjectedPid != latestPogoPid && timeStarted > lastTimeInjected
                        || (!toggleDetectionMethodToAND && (lastInjectedPid != latestPogoPid || timeStarted > lastTimeInjected)))) {
                    //we can now inject...

                    Logger.info("ProtoHookJ", "Killing Pogo");

                    // Now KILL Pogo, start it and inject immediately... Which may take too long! Need to reduce time it takes
                    if (!_seLinuxAlreadyPatched && this._sharedPreferences.getBoolean(
                            Constants.SHAREDPERFERENCES_KEYS.CALL_SETENFORCE,
                            Constants.DEFAULT_VALUES.CALL_SETENFORCE)) {
                        String pogoContext = getSELinuxContext("com.nianticlabs.pokemongo");
                        String pogoEnhancerContext = getSELinuxContext(this._appContext.getPackageName());

                        ArrayList<String> commands = new ArrayList<>();
                        commands.add("supolicy --live \"allow " + pogoContext + " " + pogoEnhancerContext + " unix_stream_socket connectto\"");
                        commands.add("supolicy --live \"allow " + pogoEnhancerContext + " " + pogoContext + " unix_stream_socket connectto\"");
                        //commands.add("supolicy --live \"allow untrusted_app untrusted_app unix_stream_socket connectto\"");
                        commands.add("supolicy --live \"attradd " + pogoContext + " mlstrustedsubject\"");
                        commands.add("supolicy --live \"typeattribute " + pogoContext + " mlstrustedsubject\"");
                        commands.add("supolicy --live \"allow " + pogoEnhancerContext + " zygote:unix_stream_socket getopt\"");
                        _shellHelper.runCommands(Runlevel.su, commands);
                        commands.clear();

                        _seLinuxAlreadyPatched = true;
                    }
                    latestPogoPid = getLatestPogoPid();

                    int attempts = 0;
                    while (latestPogoPid != -1 && attempts <= 3) {
                        _shellHelper.killAppByPackageName("com.nianticlabs.pokemongo");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            //
                        }
                        latestPogoPid = getLatestPogoPid();
                        attempts += 1;
                    }
                    if (attempts > 3) {
                        Logger.fatal("ProtoHookJ", "Failed stopping pogo without it opening right away");
                    }
                    String installedPogoVersion = PogoPatcher.getPogoVersionInstalled(this._appContext);
                    Logger.info("ProtoHookJ", "Injecting into " + installedPogoVersion);

                    _shellHelper.runCommand(Runlevel.su, "am start com.nianticlabs.pokemongo/com.nianticproject.holoholo.libholoholo.unity.UnityMainActivity");
                    Intent launchIntent = _appContext.getPackageManager().getLaunchIntentForPackage("com.nianticlabs.pokemongo");
                    if (launchIntent != null) {
                        Logger.pdebug("PogoEnhancerJ", "Launching pogo");
                        _appContext.startActivity(launchIntent);//null pointer check in case package name was not found
                    } else {
                        Logger.fatal("PogoEnhancerJ", "Unable to start pogo");
                    }
                    /*try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }*/

                    long curTime = System.currentTimeMillis();
                    boolean timeout = true;
                    int newPogoPid = getLatestPogoPid();
                    do {
                        String topmostApp = _shellHelper.topmostAppRunning();
                        if (topmostApp != null && topmostApp.trim().startsWith("com.nianticlabs.pokemongo") && newPogoPid != -1) {
                            timeout = false;
                            break;
                        }

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            return;
                        }

                        newPogoPid = getLatestPogoPid();
                        if (newPogoPid != -1) {
                            timeout = false;
                            break;
                        }
                    } while (curTime + 5000 > System.currentTimeMillis());

                    if (timeout) {
                        // Failed finding pogo..
                        continue;
                    }

                    Logger.info("PogoEnhancerJ", "Copying injection lib");

                    this.copyInjectionLib();
                    //latestPogoPid = getLatestPogoPid();
                    if (newPogoPid == -1) {
                        NotificationManager notificationManager =
                                (NotificationManager) this._appContext.getSystemService(
                                        Context.NOTIFICATION_SERVICE
                                );
                        NotificationCompat.Builder notificationBuilder = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationBuilder = new NotificationCompat.Builder(this._appContext, HEADS_UP_CHANNEL_ID)
                                    .setContentTitle("Injection")
                                    .setContentText("Started...")
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setSmallIcon(R.drawable.ic_autorenew_black_24dp);
                        } else {
                            //noinspection deprecation
                            notificationBuilder = new NotificationCompat.Builder(this._appContext)
                                    .setSmallIcon(R.drawable.ic_autorenew_black_24dp)
                                    .setContentTitle("Injection")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setContentText("Started...");
                        }
                        notificationBuilder.setContentTitle("Failed");
                        notificationBuilder.setContentText("Failed starting pogo, try again please");
                        Logger.warning("PogoEnhancerJ", "Pogo failed starting up apparently...");
                        notificationManager.notify(1333337, notificationBuilder.build());
                        continue;
                    }
                    Logger.info("PogoEnhancerJ", "Injecting " + newPogoPid);
                    boolean injected = false;
                    try {
                        injected = this.inject(newPogoPid);
                    } catch (InterruptedException e) {
                        Logger.warning("PogoEnhancerJ", "Interrupted while waiting for injection. Aborting");
                        break;
                    }

                    if (injected) {
                        Logger.info("PogoEnhancerJ", "Pogo patched successfully");
                        SharedPreferences.Editor editor = this._sharedPreferences.edit();
                        editor.putInt(Constants.SHAREDPERFERENCES_KEYS.LAST_PID_INJECTED,
                                newPogoPid);
                        long now = System.currentTimeMillis() / 1000L;
                        editor.putLong(Constants.SHAREDPERFERENCES_KEYS.LAST_TIME_INJECTED,
                                now);
                        editor.apply();
                        this._pogoPatcher._hookReceiverService.setInjected(true);
                    } else {
                        _shellHelper.killAppByPackageName("com.nianticlabs.pokemongo");
                        ArrayList<String> meminfo = _shellHelper.runCommand(Runlevel.su, "cat /proc/meminfo");
                        Logger.pdebug("PogoEnhancerJ", meminfo.toString());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            break;
                        }
                        _shellHelper.runCommand(Runlevel.su, "am start com.nianticlabs.pokemongo/com.nianticproject.holoholo.libholoholo.unity.UnityMainActivity");
                        continue;
                    }
                } else {
                    Logger.debug("PogoEnhancerJ", "Pogo patched previously");
                }

                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            this._pogoPatcher._running = false;
        }
    }
}
