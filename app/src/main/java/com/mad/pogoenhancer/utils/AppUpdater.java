package com.mad.pogoenhancer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;

import com.mad.pogoenhancer.Logger;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class AppUpdater {

    public static void updatePogoEnhancer(Activity activity, Context ctx) {
        ProgressDialog dialog = ProgressDialog.show(activity,
                "Updating",
                "Downloading latest version, repackaging if needed and installing app, this may take some time!");

        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {

            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File apkToStoreTo = new File(file, "pe.apk");
            try {
                downloadApk();
            } catch (IOException e) {
                Logger.fatal("PogoEnhancerJ", e.toString());
                activity.runOnUiThread(() -> {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            ctx);

                    alertDialogBuilder.setTitle("Failed downloading");

                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Could not download the APK, sorry.")
                            .setCancelable(false)
                            .setNegativeButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                    dialog.cancel();
                });
                return;

            }

            // Move the downloaded apk to /data/local/tmp as installing only works from there...
            SuFile apkToInstall = new SuFile("/data/local/tmp/pe.apk");
            try {
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new FileInputStream(apkToStoreTo);
                    os = new SuFileOutputStream(apkToInstall);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    is.close();
                    os.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            activity.runOnUiThread(() -> dialog.setMessage("Done downloading. Repackaging if needed and installing..."));

            RepackageApk.hidePogoEnhancer(activity, ctx, dialog, Uri.fromFile(apkToInstall));

            file.delete();
        });
    }

    private static void downloadApk() throws IOException {
        URL website = new URL("https://some-invalid-domain-foo.bar/.apk");
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File apkToStoreTo = new File(file, "pe.apk");

        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(String.valueOf(apkToStoreTo));
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}
