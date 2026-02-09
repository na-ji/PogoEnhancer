package com.mad.pogoenhancer.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.services.HookReceiverService;
import com.mad.pogoenhancer.utils.IvToast;
import com.mad.pogoenhancer.utils.PogoPatcher;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Locale;

public class HomeViewModel extends ViewModel {
    static {
//        System.loadLibrary("b");
    }

    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        //Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    private MutableLiveData<String> _cooldownText;
    private MutableLiveData<String> _currentVersionInstalled;
    private MutableLiveData<String> _latestVersion;
    private MutableLiveData<String> _pogoVersionSupported;
    private MutableLiveData<String> _pogoVersionInstalled;

//    private String latest;

    public HomeViewModel() {
        _cooldownText = new MutableLiveData<>();
        _cooldownText.setValue("No CD");

        _currentVersionInstalled = new MutableLiveData<>();
        _currentVersionInstalled.setValue("...");

        _latestVersion = new MutableLiveData<>();
        // TODO: update latest version
        _latestVersion.setValue("-");

        _pogoVersionSupported = new MutableLiveData<>();
        _pogoVersionSupported.setValue("0.395.1");

        _pogoVersionInstalled = new MutableLiveData<>();
        // _pogoVersionInstalled.setValue(getPogoVersionInstalled());
    }

    LiveData<String> getCurrentVersionInstalledText() {
        return this._currentVersionInstalled;
    }

    LiveData<String> getLatestVersionText() {
        // TODO update latest version
        this._latestVersion.setValue("31.59.0");
        return this._latestVersion;
    }

    LiveData<String> getPogoVersionSupportedText() {
        return this._pogoVersionSupported;
    }

    LiveData<String> getCooldownText(SharedPreferences sharedPreferences) {
        long teleportedAt = sharedPreferences.getLong(
                Constants.SHAREDPERFERENCES_KEYS.TELEPORTED_AT,
                Constants.DEFAULT_VALUES.TELEPORTED_AT);
        double distanceTravelled = sharedPreferences.getFloat(
                Constants.SHAREDPERFERENCES_KEYS.DISTANCE_TRAVELLED,
                Constants.DEFAULT_VALUES.DISTANCE_TRAVELLED
        );
        long remainingCooldownSeconds = Constants.calculateRemainingCooldown(teleportedAt, distanceTravelled);

        String text = "";
        if (remainingCooldownSeconds == 0) {
            text = "No CD";
        } else {
            text = String.format(Locale.ENGLISH, "%d mins", remainingCooldownSeconds / 60);
            long overAtSeconds = System.currentTimeMillis() / 1000 + remainingCooldownSeconds;

            text +=  "(" + Constants.unixSecondsToTimeString(overAtSeconds) + " )";
        }
        _cooldownText.setValue(text);
        return this._cooldownText;
    }

    LiveData<String> getPogoVersionInstalledText() {
        return this._pogoVersionInstalled;
    }

    private String getPogoVersionInstalled(Context ctx) {
        String installedVersion = "N/A";
        try {
            PackageInfo pinfo = null;
            pinfo = ctx.getPackageManager().getPackageInfo("com.nianticlabs.pokemongo", 0);
            if (pinfo == null) {
                return "N/A";
            }
            String installedPogoVersion = PogoPatcher.getPogoVersionInstalled(ctx);
            Log.i("ProtoHookJ", "Pogo installed: " + installedPogoVersion);
            installedVersion = pinfo.versionName;
/*
            int verCode = pinfo.versionCode;
            installedVersion = pinfo.versionName + "_" + verCode;
            String versionCode = "";
            if (Constants.is64BitPogoenhancerInstalled(ctx)) {
                versionCode = "2022122101";
            } else {
                versionCode = "2022122100";
            }
            String supportedBase = getPogoVersionSupportedText().getValue();
            if (!supportedBase.contains("_")) {
                supportedBase = supportedBase + "_" + versionCode;
                this._pogoVersionSupported.setValue(supportedBase);
            }

 */
        } catch (PackageManager.NameNotFoundException e) {
            Logger.error("PogoEnhancerJ", "RemoteException trying to fetch pogo version installed");
            e.printStackTrace();
        }
        return installedVersion;
    }

    private boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    void stopInjection(@NonNull Context context) {
        Logger.debug("PogoEnhancerJ", "Trying to stop service (userinput)");
        Intent hookReceiverIntent = new Intent(context, HookReceiverService.class);
        context.stopService(hookReceiverIntent);
    }

    boolean startInjection(Context context) {
        updateShownPogoVersion(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String injectAfterSecondsString = sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.INJECT_AFTER_SECONDS,
                Constants.DEFAULT_VALUES.INJECT_AFTER_SECONDS);
        if (injectAfterSecondsString == null || Long.parseLong(injectAfterSecondsString) == 0) {
            Logger.pdebug("PogoEnhancerJ", "No injection delay, disabling injection");
            IvToast.showToast(context, "Injection disabled", Gravity.BOTTOM|Gravity.CENTER, 0, 200, R.drawable.ic_frown_solid, 0);
        } else {
            String installedPogoVersion = getPogoVersionInstalled(context);
            /*
            String versionCode = "";

            if (Constants.is64BitPogoenhancerInstalled(context)) {
                versionCode = "2022122101";
            } else {
                versionCode = "2022122100";
            }
            String supportedBase = getPogoVersionSupportedText().getValue();
            if (!supportedBase.contains("_")) {
                supportedBase = supportedBase + "_" + versionCode;
                this._pogoVersionSupported.setValue(supportedBase);
            }
            */
                // 32 bit 2022110300
                // 64 bit 2022110301
            if (installedPogoVersion == null || !installedPogoVersion.equals(getPogoVersionSupportedText().getValue())) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("Pogo version unsupported");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Pogo version does not match the supported version. Please downgrade/upgrade.")
                        .setCancelable(false)
                        .setNegativeButton("OK", (dialog, id) -> dialog.dismiss());
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return false;
            }

            boolean arm64LibFolderPresent = PogoPatcher.isArm64LibFolderPresent(context);
            boolean arm64PogoenhancerInstalled = Constants.is64BitPogoenhancerInstalled(context);
            if (arm64PogoenhancerInstalled ^ arm64LibFolderPresent) {
                // ARM64 system but 32bit pogo installed
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);
                alertDialogBuilder.setTitle("Pogo version unsupported");
                List<String> results = PogoPatcher.getAllLibFolderPathsOfPogo(context, false);
                String foldersFound = results.toString();
                // set dialog message
                alertDialogBuilder
                        .setMessage("You appear to be running ARM64 PogoEnhancer while having installed ARM32 (ARMEABI) pogo, please install ARM64 version of pogo or ARMEABI version of PogoEnhancer.\nFolders found: " + foldersFound)
                        .setCancelable(false)
                        .setNegativeButton("OK", (dialog, id) -> dialog.dismiss());
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return false;
            }
        }

        if (!startListener(context)) {
            return false;
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        IvToast.showToast(context, "Starting pogo in just a second", Gravity.BOTTOM|Gravity.CENTER, 0, 200, 0, 0);
        handler.postDelayed(() -> {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.nianticlabs.pokemongo");
            if (launchIntent != null) {
                context.startActivity(launchIntent);//null pointer check in case package name was not found
            }
        }, 1500);

        return true;
    }



    private boolean startListener(@NonNull Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String injectAfterSecondsString = sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.INJECT_AFTER_SECONDS,
                Constants.DEFAULT_VALUES.INJECT_AFTER_SECONDS);
        if (injectAfterSecondsString == null || Long.parseLong(injectAfterSecondsString) == 0) {
            Logger.pdebug("PogoEnhancerJ", "No injection delay, disabling injection");
            IvToast.showToast(context, "Injection disabled", Gravity.BOTTOM|Gravity.CENTER, 0, 200, R.drawable.ic_frown_solid, 0);
        } else {
            String installedPogoVersion = getPogoVersionInstalled(context);
            if (installedPogoVersion == null || !installedPogoVersion.equals(this.getPogoVersionSupportedText().getValue())) {
                return false;
            }
        }
        if (sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_ENABLED,
                Constants.DEFAULT_VALUES.SPOOFING_ENABLED)) {
            String spoofingMethod = sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_METHOD,
                    Constants.DEFAULT_VALUES.SPOOFING_METHOD).toLowerCase();

            if (spoofingMethod.equals("mock")) {
                // we just need to check if we are set as a mock provider in the settings...
                Logger.debug("PogoEnhancerJ", "Mocking mode");
            } else {
                if (Build.VERSION.SDK_INT >= 23) {
                    boolean weAreSystemized = isSystemPackage(context.getApplicationInfo());
                    if (!weAreSystemized) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setTitle("Not systemized");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage("PogoEnhancer has not been systemized yet, please systemize PogoEnhancer..")
                                .setCancelable(false)
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // TODO
                                    }
                                });
                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
                        return false;
                    }
                }
            }
        }
        boolean overlayEnabled = sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.IV_OVERLAY_ENABLED,
                Constants.DEFAULT_VALUES.IV_OVERLAY_ENABLED
        );
        if (overlayEnabled) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
                    && !Settings.canDrawOverlays(context)) {
                Logger.fatal("PogoEnhancerJ", "IV overlay enabled but missing permissions...");
                IvToast.showToast(context, "IV overlay missing permissions", Gravity.BOTTOM|Gravity.CENTER, 0, 200, R.drawable.ic_frown_solid, 0);
            }
        } else {
            IvToast.showToast(context, "IV overlay disabled", Gravity.BOTTOM|Gravity.CENTER, 0, 200, R.drawable.ic_frown_solid, 0);
        }
        Intent serviceIntent = new Intent(context, HookReceiverService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        return true;
    }

    public void updateShownPogoVersion(Context context) {
        _pogoVersionInstalled.setValue(getPogoVersionInstalled(context));
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            _currentVersionInstalled.setValue(version);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}