package com.mad.pogoenhancer;

import com.mad.pogoenhancer.utils.ShellHelper;
import com.mad.shared.utils.Runlevel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class BackendStorage {
    private static BackendStorage ourInstance = null;
    private String deviceId = "";

    protected boolean isExLog() {
        return exLog;
    }

    public void setExLog(boolean exLog) {
        this.exLog = exLog;
    }

    private boolean exLog = false;

    private BackendStorage() {
    }

    public static BackendStorage getInstance() {
        if (ourInstance == null) {
            ourInstance = new BackendStorage();
        }
        return ourInstance;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
