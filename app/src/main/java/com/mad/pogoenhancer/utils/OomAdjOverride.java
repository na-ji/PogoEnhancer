package com.mad.pogoenhancer.utils;

import android.os.FileObserver;

import androidx.annotation.Nullable;

import com.mad.pogoenhancer.Logger;
import com.mad.shared.utils.Runlevel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OomAdjOverride {
    static String TAG = "OomAdjOverride";
    /*
        public
     */
    //retrieve our PID
    public int pid = android.os.Process.myPid();
    // /proc/<pid>/oom_adj will hold the OOM value (-17 to 15)
    public String oomAdjPath = String.format("/proc/%d/oom_adj", pid);
    //
    public int oomValue;
    // when we detect it's not possible to write to oom_adj we set this to true
    // and stop bothering trying to write to oom_adj again. it's ok for the user of this
    // class to reset to false and call .start() again when the problem might have been recovered
    public boolean phoneNotRooted = false;
    // it's ok to skip some errors (eg EPIPE) if they happen on a Xth run if 'su' has already
    // executed successfully.
    // EPIPE on Xth run could happen when someone kills our 'su' process in that very short window
    // ... we just log it, ignore it and set oom_adj on the next run,
    // EPIPE fail on the first run means 'su' did not open a shell (but probably exited with an error
    // message)
    public boolean runSuccessfully = false;
    /*
        private
     */
    private List<FileObserver> observers = new ArrayList<>();

    /*
        Constructors
     */

    public OomAdjOverride() {
        this(-17);
    }

    /**
     * Note that overriding does not start on creation but you must call start()
     *
     * @param oomValue Override /proc/PID/oom_adj to this value (default -13)
     */
    public OomAdjOverride(int oomValue) {
        this.oomValue = oomValue;
    }

    /*
        Event listeners
     */
    private void onEvent(int i, String s) {
        if (i != FileObserver.MODIFY) {
            return;
        }
        write();
    }

    /*
        public Methods
     */

    /*
        Start all the file observers
     */
    public void start() {
        stop();
        observers.add(new FileObserver(String.format("/proc/%d/oom_adj", pid)) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                OomAdjOverride.this.onEvent(event, path);
            }
        });
        observers.add(new FileObserver(String.format("/proc/%d/oom_score", pid)) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                OomAdjOverride.this.onEvent(event, path);
            }
        });
        observers.add(new FileObserver(String.format("/proc/%d/oom_score_adj", pid)) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                OomAdjOverride.this.onEvent(event, path);
            }
        });

        for (FileObserver observer : this.observers) {
            observer.startWatching();
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                write();
                int currentOomValue;
                try {
                    currentOomValue = read();
                } catch (IOException e) {
                    Logger.error(TAG, "{OomAdjOverride::start::thread.run}:" + e.toString());
                    return;
                }

                //check the currentOomValue against the desired oomValue
                if (currentOomValue == oomValue) {
                    Logger.info(TAG, String.format("{OomAdjOverride::start::thread.run} Forst wrote check: " +
                            "oom_adj value written successfully: %d", currentOomValue));
                } else {
                    Logger.error(TAG, String.format("First write check: " +
                            "oom_adj value not overriden - oom_adj is %d not %d", currentOomValue, oomValue));
                }
            }
        };
        thread.start();
    }

    /*
        Stop all the file observers
     */
    public void stop() {
        for (FileObserver observer : this.observers) {
            observer.stopWatching();
        }
        this.observers.clear();
    }

    /**
     * This method is triggered when an error occurs when executing su
     * (eg permission denied, su not found, "EPIPE" (su did not open shell))
     * <p>
     * WARNING: this method is executed in the thread that executed su.
     * So be careful, for example, if you want to do something with UI from this method
     * (eg Toast) use activity.runOnUiThread
     */
    synchronized public void suErrorHandler(RunException ex) {
        Logger.error(TAG, ex.toString());
        phoneNotRooted = true;
    }


    /*
        private Methods
     */

    /*
        read /proc/<pid>/oom_adj
     */
    private int read() throws IOException {
        ShellHelper shellHelper = new ShellHelper();
        ArrayList<String> strings = shellHelper.runCommand(Runlevel.su, "cat " + this.oomAdjPath);
        if (strings.size() != 1) {
            throw new IOException("Could not read OOJ value");
        }
        return Integer.parseInt(strings.get(0).trim());
    }

    /*
        write oomValue to /proc/<pid>/oom_adj
     */
    synchronized private void write() {
        if (phoneNotRooted) {
            stop();
            return;
        }

        int currentOomValue;
        try {
            currentOomValue = read();
        } catch (IOException e) {
            Logger.error("PogoEnhancerJ", "{OomAdjOverride::write} " + e.toString());
            return;
        }

        if (currentOomValue == this.oomValue) {
            //value to be set already matches the one set
            return;
        }

        Logger.info("PogoEnhancerJ", String.format("{OomAdjOverride::write} writing %d to %s",
                oomValue, oomAdjPath)
        );

        for (FileObserver observer : observers) {
            observer.stopWatching();
        }

        try {
            // this command works on android versions from Gingerbread up to N
            // (kernel version 3.10.0+) even though it says in the android kernel docs
            // oom_adj is obsolete
            run(String.format("echo %d > /proc/%d/oom_adj", oomValue, pid));
        } catch (RunException e) {
            suErrorHandler(e);
        }

        for (FileObserver observer : observers) {
            observer.startWatching();
        }
    }

    /**
     * Executes single command in "su" shell in the current thread
     *
     * @param command the command to execute
     * @throws RunException If anything goes wrong, this exception is thrown, the RunException
     *                      message is ready to be displayed to the end user
     */
    private void run(String command) throws RunException {
        ShellHelper helper = new ShellHelper();
        helper.runCommand(Runlevel.su, command);

        this.runSuccessfully = true;
    }

}
