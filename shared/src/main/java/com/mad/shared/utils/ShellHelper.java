package com.mad.shared.utils;

import android.os.Build;
import android.util.Log;

import com.topjohnwu.superuser.NoShellException;
import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellHelper {

    private static final String TAG = "MAD";
    public ArrayList<String> runCommand(Runlevel level, String command) {
        ArrayList<String> singleList = new ArrayList<String>();
        singleList.add(command);
        return runCommands(level, singleList);
    }

    private ArrayList<String> runCommandsSu(ArrayList<String> commands) {
        ArrayList<String> output = new ArrayList<>();
        for (String command : commands) {
            Log.d(TAG, "[Input]: " + command);
            output.addAll(Shell.su(command).exec().getOut());
        }
        Log.d(TAG, "[Output]: " + output.toString());
        return output;
    }

    public ArrayList<String> runCommands(Runlevel level, ArrayList<String> commands) {
        Process process = null;
        ArrayList<String> processLines = new ArrayList<>();
        String line;
        //god knows why we need it...

        try {
            if (level == Runlevel.sh) {
                process = Runtime.getRuntime().exec("sh");
            } else {
                return runCommandsSu(commands);
            }
        } catch (IOException e) {
            Log.e(TAG, "{ShellHelper::runCommand} Could not execute su/sh");
            return null;
        } catch (NoShellException e) {
            Log.e(TAG, "Failed executing command as SU.");
            return null;
        }

        if (process == null) {
            Log.e(TAG, "{ShellHelper::runCommand} Could not start process");
            return null;
        }

        OutputStream stdin = process.getOutputStream();
        InputStream stdout = process.getInputStream();

        try {
            for (String command : commands) {
//                Logger.debug("[Input]", command);
                Log.d(TAG, "[Input]: " + command);
                stdin.write((command + " 2>&1\n").getBytes());
            }
//            Logger.debug(TAG, "Sending exit");
            stdin.write("exit\n".getBytes());
            stdin.flush();   //flush stream
            stdin.close(); //close stream

//            Logger.debug(TAG, "Starting to read output");
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            while ((line = br.readLine()) != null) {
                processLines.add(line);
                Log.d(TAG, "[Output]: " + line);
            }
            br.close();
        } catch (IOException ex) {
            Log.e(TAG, "{ShellHelper::runCommand} Could not write to or read buffer");
            return null;
        }
        try {
            Log.d(TAG, "Waiting for child-process to terminate");
            process.waitFor();//wait for process to finish*/
        } catch (InterruptedException e) {
            Log.e(TAG, "{ShellHelper::runCommand} Interrupt waiting for process");
            return null;
        }
        Log.d(TAG, "Done with command");
        return processLines;
    }

    public ArrayList<String> getProcessIdsOfPackage(Runlevel level, String packageName) {
        ArrayList<String> processIds = new ArrayList<>();

        String psCommand = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? "ps -A" : "ps";
        ArrayList<String> processLines = runCommand(level, psCommand + " | grep -E \""
                + packageName + "\" | grep -vE \"root|shell|grep|pokemongo:\""
        );
        Log.d(TAG, "Checking the PIDs returned via regex");
        if (processLines.size() > 0) {
            String idRegex = "^\\w*\\s+(\\d+).*";
            Pattern pattern = Pattern.compile(idRegex);

            for (String lineWithId : processLines) {
                Matcher matcher = pattern.matcher(lineWithId);
                if (matcher.matches()) {
                    Log.d(TAG, "Found PID to add to list");
                    processIds.add(matcher.group(1));
                }
            }
        }
        Log.d(TAG, "Returning output of ps");
        return processIds;
    }

    public boolean killAppByPackageName(String packageName) {
        ArrayList<String> pids = getProcessIdsOfPackage(Runlevel.su, packageName);

        if (pids == null) {
            Log.e(TAG, "Requested kill of " + packageName + " failed");
            return false;
        } else if (pids.size() == 0) {
            //nothing to kill
            Log.i(TAG, "Requested kill of " + packageName + " but package not running");
            return true;
        }

        for (int i = 0; i < pids.size(); i++) {
            pids.set(i, "kill " + pids.get(i));
        }
        runCommands(Runlevel.su, pids);

        return true;
    }

    public boolean clearDataOfApp(String packageName) {
        return runCommand(Runlevel.su, "pm clear " + packageName).contains("Success");
    }

    public boolean clearCacheOfApp(String packageName) {
        ArrayList<String> result =
                runCommand(Runlevel.su, "rm -rf /data/data/" + packageName + "/cache/*");
        return result.size() == 0 || result.contains("Success");

    }

    public String topmostAppRunning() {
        ArrayList<String> result = runCommand(Runlevel.sh,
                "dumpsys activity activities | grep -E ResumedActivity | head -n1");

        if (result.size() == 0) {
            Log.i(TAG, "Empty result trying to read topmost app. Screen likely off.");
            return "";
        } else if (result.size() > 1) {
            Log.e(TAG, "More than one line trying to receive topmost app");
            return null;
        }
        String topmost = result.get(0).trim();
        String regex = ".*\\{.*\\s(.+)\\/.*\\}.*";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(topmost);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            Log.e(TAG, "Could not find package name in " + topmost);
            return "";
        }
    }

    public String screenState() {
        ArrayList<String> result = runCommand(Runlevel.sh,
                "dumpsys power | grep mHolding");

        if (result == null) {
            Log.e(TAG, "Could not retrieve screenstate");
            return null;
        } else if (result.size() == 0) {
            Log.e(TAG, "Screen state result is empty");
            return "";
        }

        boolean mHoldingDisplaySuspendBlocker = false;
        for (String line : result) {
            String[] keysValues = line.trim().split("=");
            //if more than 2 elements -> nothing we wanna deal with
            if (keysValues.length > 2) {
                continue;
            }
            boolean value = false;
            if (keysValues[1].trim().equals("true")) {
                value = true;
            }
//            if (keysValues[0].equals("mHoldingWakeLockSuspendBlocker")) {
//                mHoldingWakeLockSuspendBlocker = value;
//            } else
            if (keysValues[0].trim().equals("mHoldingDisplaySuspendBlocker")) {
                mHoldingDisplaySuspendBlocker = value;
            }
        }

        if (mHoldingDisplaySuspendBlocker) {
            return "on";
        } else {
            return "off";
        }
    }

    public void resetGms() {
        if (killAppByPackageName("com.google.android.gms")) {
            clearDataOfApp("com.google.android.gms");
        }
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
                Log.e(TAG, "Could not retrieve duration pogo has been running");
                return -1;
            }
        }
    }
}
