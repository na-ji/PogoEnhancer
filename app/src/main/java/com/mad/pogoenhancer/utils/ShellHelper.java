package com.mad.pogoenhancer.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.mad.pogoenhancer.Logger;
import com.mad.shared.utils.Runlevel;
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
    private final String TAG = "ShellHelper";
    public ArrayList<String> runCommand(Runlevel level, String command) {
        ArrayList<String> singleList = new ArrayList<String>();
        singleList.add(command);
        return runCommands(level, singleList);
    }

    private ArrayList<String> runCommandsSu(ArrayList<String> commands) {
        ArrayList<String> output = new ArrayList<>();
        for (String command : commands) {
            Logger.debug("ProtoHookJ", "[Input]: " + command);
            output.addAll(Shell.su(command).exec().getOut());
        }
        Logger.debug("ProtoHookJ", "[Output]: " + output.toString());
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
            Logger.error("ProtoHookJ", "{ShellHelper::runCommand} Could not execute su/sh");
            return null;
        } catch (NoShellException e) {
            Logger.error("ProtoHookJ", "Failed executing command as SU.");
            return null;
        }

        if (process == null) {
            Logger.error("ProtoHookJ", "{ShellHelper::runCommand} Could not start process");
            return null;
        }

        OutputStream stdin = process.getOutputStream();
        InputStream stdout = process.getInputStream();

        try {
            for (String command : commands) {
//                Logger.d("[Input]", command);
                Logger.debug("ProtoHookJ", "[Input]: " + command);
                stdin.write((command + " 2>&1\n").getBytes());
            }
            Logger.debug("ProtoHookJ", "Sending exit");
            stdin.write("exit\n".getBytes());
            Logger.debug("ProtoHookJ", "Flushing");
            stdin.flush();   //flush stream
            Logger.debug("ProtoHookJ", "Closing");
            stdin.close(); //close stream

            Logger.debug("ProtoHookJ", "Starting to read output");
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            while ((line = br.readLine()) != null) {
                processLines.add(line);
                Logger.pdebug("ProtoHookJ", "[Output]: " + line);
            }
            br.close();
        } catch (IOException ex) {
            Logger.error("ProtoHookJ","{ShellHelper::runCommand} Could not write to or read buffer");
            return null;
        }
        try {
            Logger.debug("ProtoHookJ", "Waiting for child-process to terminate");
            process.waitFor();//wait for process to finish*/
        } catch (InterruptedException e) {
            Logger.error("ProtoHookJ","{ShellHelper::runCommand} Interrupt waiting for process");
            return null;
        }
        Logger.debug("ProtoHookJ", "Done with command");
        return processLines;
    }

    public ArrayList<String> runCommandSuDirect(String command) {
        Process process = null;
        ArrayList<String> processLines = new ArrayList<>();
        String line;
        //god knows why we need it...

        try {
            process = Runtime.getRuntime().exec("su");

        } catch (IOException e) {
            Logger.error("ProtoHookJ", "{ShellHelper::runCommand} Could not execute su/sh");
            return null;
        } catch (NoShellException e) {
            Logger.error("ProtoHookJ", "Failed executing command as SU.");
            return null;
        }

        if (process == null) {
            Logger.error("ProtoHookJ", "{ShellHelper::runCommand} Could not start process");
            return null;
        }

        OutputStream stdin = process.getOutputStream();
        InputStream stdout = process.getInputStream();

        try {
            stdin.write((command + " 2>&1\n").getBytes());
//            Logger.debug("ProtoHookJ", "Sending exit");
            stdin.write("exit\n".getBytes());
            stdin.flush();   //flush stream
            stdin.close(); //close stream

//            Logger.debug("ProtoHookJ", "Starting to read output");
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            while ((line = br.readLine()) != null) {
                processLines.add(line);
                Logger.pdebug("ProtoHookJ", "[Output]: " + line);
            }
            br.close();
        } catch (IOException ex) {
            Logger.error("ProtoHookJ","{ShellHelper::runCommand} Could not write to or read buffer");
            return null;
        }
        try {
            Logger.debug("ProtoHookJ", "Waiting for child-process to terminate");
            process.waitFor();//wait for process to finish*/
        } catch (InterruptedException e) {
            Logger.error("ProtoHookJ","{ShellHelper::runCommand} Interrupt waiting for process");
            return null;
        }
        Logger.debug("ProtoHookJ", "Done with command");
        return processLines;
    }

    public ArrayList<String> getProcessIdsOfPackage(Runlevel level, String packageName) {
        ArrayList<String> processIds = new ArrayList<>();

//        ArrayList<String> processLines = runCommand(level,"top -n 1 | grep -E \""
//                + packageName + "\" | grep -vE \"root|shell|grep\" | cut -d \\  -f 1,2,3");
//        ArrayList<String> processLines = runCommand(level,"top -n 1");
//        ArrayList<String> processLines = runCommand(level,"ps | grep -E \""
//                + packageName + "\" | grep -vE \"root|shell|grep\""
//        );
        String psCommand = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? "ps -A" : "ps";
        ArrayList<String> processLines = runCommand(level,psCommand + " | grep -E \""
                + packageName + "\" | grep -vE \"root|shell|grep|pokemongo:|\\.ares\""
        );
        Logger.debug("ProtoHookJ", "Checking the PIDs returned via regex");
        if (processLines.size() > 0) {
            String idRegex = "^\\w*\\s+(\\d+).*";
            Pattern pattern = Pattern.compile(idRegex);

            for (String lineWithId : processLines) {
                Matcher matcher = pattern.matcher(lineWithId);
                if (matcher.matches()) {
                    Logger.debug("ProtoHookJ", "Found PID to add to list");
                    processIds.add(matcher.group(1));
                }
            }
        }
        Logger.debug("ProtoHookJ", "Returning output of ps");
        return processIds;
    }

    public boolean killAppByPackageName(String packageName) {
        ArrayList<String> pids = getProcessIdsOfPackage(Runlevel.su, packageName);

        if (pids == null) {
            Logger.error("ProtoHookJ","Requested kill of " + packageName + " failed");
            return false;
        } else if (pids.size() == 0) {
            //nothing to kill
            Logger.info("ProtoHookJ","Requested kill of " + packageName + " but package not running");
            return true;
        }

        for (int i = 0; i < pids.size(); i++) {
            pids.set(i, "kill -9 " + pids.get(i));
        }
        runCommands(Runlevel.su, pids);

        return true;
    }

    public boolean clearDataOfApp(String packageName) {
        return runCommand(Runlevel.su, "pm clear " + packageName).contains("Success");
    }

    public boolean clearCacheOfApp(String packageName) {
        ArrayList<String> result =
                runCommand(Runlevel.su,"rm -rf /data/data/" + packageName + "/cache/*");
        return result.size() == 0 || result.contains("Success");

    }

    public String topmostAppRunning() {
        ArrayList<String> result = runCommand(Runlevel.su,
                "dumpsys activity activities | grep -E ResumedActivity | head -n1");

        if (result.size() == 0) {
            Logger.info("ProtoHookJ", "Empty result trying to read topmost app. Screen likely off.");
            return null;
        }
        String topmost = result.toString();
        String regex = ".*\\{.*\\s(.+)\\s.*\\}.*";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(topmost);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            Logger.error("ProtoHookJ", "Could not find package name in " + topmost);
            return "";
        }
    }

    public String screenState() {
        ArrayList<String> result = runCommand(Runlevel.sh,
                "dumpsys power | grep mHolding");

        if (result == null) {
            Logger.error("ProtoHookJ","Could not retrieve screenstate");
            return null;
        } else if (result.size() == 0) {
            Logger.error("ProtoHookJ","Screen state result is empty");
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
        if (false && killAppByPackageName("com.google.android.gms")) {
            clearDataOfApp("com.google.android.gms");
        }
    }

    private long getTimeStarted(int pid) {
        ShellHelper shellHelper = new ShellHelper();
        ArrayList<String> strings = shellHelper.runCommand(Runlevel.su, "stat -c%X /proc/" + pid);
        if(strings == null || strings.size() != 1 || strings.get(0).isEmpty()) {
            return -1;
        } else {
            try {
                return Long.parseLong(strings.get(0));
            } catch (NumberFormatException ex) {
                Logger.error("ProtoHookJ", "Could not retrieve duration pogo has been running");
                return -1;
            }
        }
    }

    public void clickX(Context contex) {
        WindowManager wm = (WindowManager) contex.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return;
        }

        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        //display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);

        int x = size.x / 2;
        int y = size.y - (int)(size.x / 7.57);
        int offset = getSoftButtonsBarHeight(wm);
        if (offset > 0) {
            y = y + offset - 55;
        }

        this.runCommandSuDirect("input tap " + x + " " + y);
    }

    private int getSoftButtonsBarHeight(WindowManager wm) {
        // getRealMetrics is only available with API 17 and +
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        wm.getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }

    public String getDeviceId() {
        ShellHelper helper = new ShellHelper();
        ArrayList<String> strings = helper.runCommand(Runlevel.su, "getprop ro.serialno");
        if (strings == null || strings.size() != 1) {
            Logger.pdebug("PogoEnhancerJ", "Error retrieving device-information. Is root enabled?");
            return null;
        }
        String potentialDeviceId = strings.get(0).trim();
        if (potentialDeviceId.isEmpty() || potentialDeviceId.equals("1234567890")
                || potentialDeviceId.equals("0000000000") || potentialDeviceId.equals("0123456789abcdef")) {
            strings = helper.runCommand(Runlevel.su, "cat /sys/class/net/wlan0/address");
            if (strings == null || strings.isEmpty()) {
                return readEthMac();
            }
            potentialDeviceId = strings.get(0).trim();
            if (potentialDeviceId.isEmpty() || potentialDeviceId.contains(" ")) {
                return readEthMac();
            }
        }
        return potentialDeviceId;
    }

    private String readEthMac() {
        ShellHelper helper = new ShellHelper();
        ArrayList<String> strings =  helper.runCommand(Runlevel.su, "cat /sys/class/net/eth0/address");
        if (strings == null || strings.isEmpty()) {
            return null;
        }
        String ethMac = strings.get(0).trim();
        if (ethMac.isEmpty() || ethMac.contains(" ")) {
            return null;
        } else {
            return ethMac;
        }
    }
}
