package com.mad.pogoenhancer;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;


public class FileLoggingTree extends Timber.DebugTree {

    private static final String TAG = "PogoEnhancerJ";

    private Context context;

    public FileLoggingTree(Context context) {
        this.context = context;
    }

    @Override
    protected void log(int priority, String tag, @NotNull String message, Throwable t) {
        try {
            File direct = new File("/storage/emulated/0/download");

            if (!direct.exists()) {
                direct.mkdir();
            }

            String fileNameTimeStamp = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            String logTimeStamp = new SimpleDateFormat("E MMM dd yyyy 'at' hh:mm:ss:SSS aaa", Locale.getDefault()).format(new Date
                    ());

            String fileName = fileNameTimeStamp + "droidpogoplusplus.html";

            File file = new File("/storage/emulated/0/download" + File.separator + fileName);

            file.createNewFile();

            if (file.exists()) {

                OutputStream fileOutputStream = new FileOutputStream(file, true);

                fileOutputStream.write(("<p style=\"background:lightgray;\"><strong style=\"background:lightblue;\">&nbsp&nbsp" + logTimeStamp + " :&nbsp&nbsp</strong>&nbsp&nbsp" + message + "</p>").getBytes());
                fileOutputStream.close();

            }

            //if (context != null)
            //MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);

        } catch (Exception e) {
            Logger.error(TAG, "Error while logging into file : " + e);
        }

    }
}