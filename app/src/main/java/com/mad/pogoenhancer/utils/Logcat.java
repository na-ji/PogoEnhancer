package com.mad.pogoenhancer.utils;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.core.content.FileProvider;

import com.mad.pogoenhancer.BuildConfig;
import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Logcat extends AsyncTask<Void, Void, Boolean> {
    private static final int BUFFER_SIZE = 4096;
    private static final String LOGAT_ZIP_NAME = "/logcat.zip";
    private static final String LOGCAT_FILE_NAME = "logcat.txt";
    private static final String LOGCAT_COMMAND = "logcat -d\n";

    private final AtomicReference<Context> _context = new AtomicReference<>();
    private File _cacheDirectory;
    private AlertDialog _infoDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public Logcat(Context context) {
        this._context.set(context);
        this._cacheDirectory = context.getCacheDir();
    }

    private boolean readLogcatBufferToFile() {
        File logcatFile = new File(_cacheDirectory, "logcat.txt");

        Process process;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            Logger.error("PogoEnhancerJ", "Failed reading logcat");
            return false;
        }
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        File externalStorage = Environment.getExternalStorageDirectory();
        OutputStream stdin = process.getOutputStream();

        mHandler.post(() -> _infoDialog.setMessage("Writing logcat to temporary file"));
        String lineEnding = "\n";
        PrintWriter p;
        try {
            p = new PrintWriter(new FileOutputStream(logcatFile, false));
        } catch (FileNotFoundException e) {
            Logger.error("PogoEnhancerJ", "Failed creating writer for logcat file");
            return false;
        }

        try {
            stdin.write(LOGCAT_COMMAND.getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();   //flush stream
            stdin.close(); //close stream
        } catch (IOException e) {
            Logger.error("PogoEnhancerJ", "Failed reading logcat");
            return false;
        }
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                p.write(line + lineEnding);
            }
            bufferedReader.close();
        } catch (IOException e) {
            Logger.error("PogoEnhancerJ", "Failed writing logcat");
            return false;
        }
        p.close();

        mHandler.post(() -> _infoDialog.setMessage("Zipping logcat"));
        File zipFile = new File(externalStorage.getAbsolutePath() + LOGAT_ZIP_NAME);
        this.zip(logcatFile, zipFile);
        mHandler.post(() -> _infoDialog.setMessage("Report is ready to be sent"));
        this.sendEmail(zipFile);
        return logcatFile.delete();
    }

    private void sendEmail(File zipFile) {
        Logger.info("PogoEnhancerJ", "Try to send support email");
        String filePath = Environment.getExternalStorageDirectory() + LOGAT_ZIP_NAME;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                this._context.get()
        );
        String deviceID = sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID,
                Constants.DEFAULT_VALUES.DEFAULT_DEVICE_ID);

        String phoneModel = android.os.Build.MODEL;
        String androidVersion = android.os.Build.VERSION.RELEASE;

        String version = "";
        String packageName = "";
        try {
            packageName = this._context.get().getPackageName();
            PackageInfo pInfo = this._context.get().getPackageManager().getPackageInfo(packageName, 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException | NoSuchMethodError e) {
            e.printStackTrace();
            Logger.error("PogoEnhanerJ", "Failed fetching app version");
            packageName = BuildConfig.APPLICATION_ID;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{""}); // Recipient...
        emailIntent.putExtra("android.intent.extra.SUBJECT",
                "PE Support");
        emailIntent.putExtra("android.intent.extra.TEXT",
                "Please describe the issue:\n\n" +
                        "Device: " + deviceID + "\n"
                        + "Phone: " + phoneModel + "\n"
                        + "Android: " + androidVersion + "\n"
                        + "PE version: " + version);

        Uri logcatZipUri = FileProvider.getUriForFile(
                this._context.get(),
                packageName + ".provider",
                //"com.example.homefolder.example.provider", //(use your app signature + ".provider" )
                zipFile);
        emailIntent.putExtra(Intent.EXTRA_STREAM, logcatZipUri);

        try {
            this._context.get().startActivity(Intent.createChooser(emailIntent,
                    "Send support information"));
        } catch (ActivityNotFoundException e) {
            Logger.error("PogoEnhancerJ", "Failed sending logcat via email. No mail application found");
        }
    }


    private void zip(File logcatFile, File zipFileName) {
        try {
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte[] data = new byte[BUFFER_SIZE];

            FileInputStream fi = new FileInputStream(logcatFile);
            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE);

            ZipEntry entry = new ZipEntry(LOGCAT_FILE_NAME);
            out.putNextEntry(entry);
            int count;

            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return readLogcatBufferToFile();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // show dialog informing about logcat being prepared
        AlertDialog.Builder builder = new AlertDialog.Builder(this._context.get());
        builder.setTitle("Grabbing logcat");
        builder.setMessage("Initializing logcat report via E-mail");
        builder.setCancelable(false);

        _infoDialog = builder.create();
        _infoDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        //dismiss dialog
        _infoDialog.dismiss();
    }
}