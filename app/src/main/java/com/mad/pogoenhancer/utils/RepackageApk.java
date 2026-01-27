package com.mad.pogoenhancer.utils;

import android.annotation.Nullable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Process;
import android.preference.PreferenceManager;

import com.mad.pogoenhancer.BuildConfig;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.shared.utils.Runlevel;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileOutputStream;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RepackageApk {
    private static String genPackageName(String prefix, int length) {
        StringBuilder builder = new StringBuilder(length);
        builder.append(prefix);
        length -= prefix.length();
        SecureRandom random = new SecureRandom();
        String base = "abcdefghijklmnopqrstuvwxyz";
        String alpha = base + base.toUpperCase();
        String full = alpha + "0123456789..........";
        char next, prev = '\0';
        for (int i = 0; i < length; ++i) {
            if (prev == '.' || i == length - 1 || i == 0) {
                next = alpha.charAt(random.nextInt(alpha.length()));
            } else {
                next = full.charAt(random.nextInt(full.length()));
            }
            builder.append(next);
            prev = next;
        }
        return builder.toString();
    }

    private static boolean findAndPatch(byte[] xml, String from, String to, List<String> namesToExclude) {
        if (from.length() != to.length())
            return false;

        String xmlWrapped = new String(ByteBuffer.wrap(xml).order(ByteOrder.LITTLE_ENDIAN).array(), StandardCharsets.UTF_16LE);
        namesToExclude.add(from + ".App");
        Matcher m = Pattern.compile("("+ from + ")([\\.a-zA-Z0-9-]*)")
                .matcher(xmlWrapped);
        List<Integer> offList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char[] buff = xmlWrapped.toCharArray();
        int currentOffset = 0;
        while (m.find()) {
            if (!namesToExclude.contains(m.group())) {
                sb.append(buff , currentOffset, m.start() - currentOffset);
                // append the replacement
                sb.append(to);
                currentOffset = m.start() + to.length();

                offList.add(m.start());
            }
        }
        sb.append(buff,currentOffset,buff.length - currentOffset);
        xmlWrapped = sb.toString();
        CharBuffer buf = ByteBuffer.wrap(xml).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();

        if (offList.isEmpty())
            return false;
        buf.put(xmlWrapped);
        return true;
    }

    private static boolean findAndPatch(byte[] xml, int a, int b) {
        IntBuffer buf = ByteBuffer.wrap(xml).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        int len = xml.length / 4;
        for (int i = 0; i < len; ++i) {
            if (buf.get(i) == a) {
                buf.put(i, b);
                return true;
            }
        }
        return false;
    }

    private static boolean patchAndHide(Context ctx, @Nullable Uri pathToCustomApk) {
        List<String> namesToExclude = new ArrayList<>();
        try {
            PackageManager packageManager = ctx.getPackageManager();
            ActivityInfo[] activities = packageManager.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES).activities;
            ActivityInfo[] receivers = packageManager.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_RECEIVERS).receivers;
            ServiceInfo[] services = packageManager.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_SERVICES).services;
            for(ActivityInfo info : activities) {
                namesToExclude.add(info.name);
            }
            for(ActivityInfo info : receivers) {
                namesToExclude.add(info.name);
            }
            for(ServiceInfo info : services) {
                namesToExclude.add(info.name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // Generate a new app with random package name
        SuFile repack = new SuFile("/data/local/tmp/repack.apk");
        String newPackagename;
        String packageCodePath;
        if (pathToCustomApk != null) {
            newPackagename = ctx.getPackageName();
            packageCodePath = pathToCustomApk.getPath();
        } else {
            newPackagename = genPackageName("com.", BuildConfig.APPLICATION_ID.length());
            packageCodePath = ctx.getPackageCodePath();
        }

        try {
            JarMap apk = new JarMap(packageCodePath);
            if (!patchPackageID(apk, BuildConfig.APPLICATION_ID, newPackagename, namesToExclude)) {
                Logger.warning("PATCH", "Failed patching");
                return false;
            }
            SignAPK.sign(apk, new SuFileOutputStream(repack));
        } catch (Exception e) {
            Logger.warning("PATCH", "Exception while patching: " + e.toString());
            return false;
        }

        ShellHelper helper = new ShellHelper();
        ArrayList<String> strings = helper.runCommand(Runlevel.su, "pm install -g -t -r " + repack);
        boolean success = false;
        for (String line : strings) {
            if (line.equals("Success")) {
                success = true;
                break;
            }
        }
        if (!success) {
            Logger.warning("PATCH", "Failed installing new APK");
            return false;
        }
        if (pathToCustomApk != null) {
            File apkToDelete = new File(String.valueOf(pathToCustomApk));
            apkToDelete.delete();
        }

        repack.delete();

        ArrayList<String> commands = new ArrayList<>();
        commands.add("db_clean " + Process.myUid() / 100000);
        commands.add("pm uninstall " + BuildConfig.APPLICATION_ID);
        helper.runCommands(Runlevel.su, commands);

        return true;
    }


    public static boolean patchPackageID(JarMap apk, String from, String to, List<String> namesToExclude) {
        try {
            JarEntry je = apk.getJarEntry("AndroidManifest.xml");
            Enumeration<JarEntry> entries = apk.entries();
            if (entries.hasMoreElements()) {
                Logger.debug("D", "A");
            }

            byte[] xml = apk.getRawData(je);
            findAndPatch(xml, from, to, namesToExclude);

            // Write in changes
            apk.getOutputStream(je).write(xml);
            long size = je.getSize();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void updateAppWithoutRepackaging(Context ctx, Uri pathToCustomApk, ProgressDialog dialog) {
        ShellHelper helper = new ShellHelper();
        ArrayList<String> strings = helper.runCommand(Runlevel.su, "pm install -g -t -r " + pathToCustomApk.getPath());
        boolean success = false;
        for (String line : strings) {
            if (line.equals("Success")) {
                success = true;
                break;
            }
        }
        if (!success) {
            dialog.setMessage("Failed to install update");
        }
        File apkToDelete = new File(String.valueOf(pathToCustomApk));
        apkToDelete.delete();
    }

    public static void hidePogoEnhancer(Activity activity, Context ctx, ProgressDialog dialogToUse, @Nullable Uri pathToCustomApk) {
        activity.runOnUiThread(() -> {
            dialogToUse.setTitle(activity.getString(R.string.hide_manager_toast));
            dialogToUse.setMessage(activity.getString(R.string.hide_manager_toast2));
        });
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            PreferenceSerializer preferenceSerializer = new PreferenceSerializer(
                    PreferenceManager.getDefaultSharedPreferences(ctx)
            );
            boolean a = preferenceSerializer.exportSettings();
            if (pathToCustomApk != null && BuildConfig.APPLICATION_ID.equals(ctx.getPackageName())) {
                Logger.fatal("PogoEnhancerJ", "Simple update without repackaging...");
                updateAppWithoutRepackaging(ctx, pathToCustomApk, dialogToUse);
                activity.runOnUiThread(dialogToUse::cancel);
                return;
            }
            Logger.fatal("PogoEnhancerJ", "Done serializing settings");
            boolean b = patchAndHide(ctx, pathToCustomApk);
            Logger.fatal("PogoEnhancerJ", "Done patching");

            activity.runOnUiThread(dialogToUse::cancel);

            if (!(a && b)) {
                //handle with error showing
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        activity);

                alertDialogBuilder.setTitle("Repackage failed");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Failed repackaging. Check log for possible causes.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
    }
}

