package com.mad.pogoenhancer.services;


import org.json.JSONException;
import org.json.JSONObject;

public class InjectionSettingSender {
    public static void sendBoolSetting(String key, boolean value) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(key, value);

        InjectionSettingSender.sendSetting(obj);
    }

    public static void sendStringSetting(String key, String value) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(key, value);

        InjectionSettingSender.sendSetting(obj);
    }

    public static void sendSetting(JSONObject settings) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.put("settings", settings);
        UnixSender.sendMessage(settingObj.toString());
    }

    public static void sendInventory(JSONObject settings) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.putOpt("inventory", settings);
        UnixSender.sendMessage(settingObj.toString());
    }

    public static void sendCooldown(JSONObject settings) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.putOpt("cooldown", settings);
        UnixSender.sendMessage(settingObj.toString());
    }

    public static void sendWildMonTypes(JSONObject settings) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.putOpt("wildmonTypes", settings);
        UnixSender.sendMessage(settingObj.toString());
    }

    public static void sendNameReplace(JSONObject settings) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.putOpt("namerepl", settings);
        UnixSender.sendMessage(settingObj.toString());
    }

    public static void sendMonMoves(JSONObject settings) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.putOpt("moves", settings);
        UnixSender.sendMessage(settingObj.toString());
    }

    public static void sendAutotransferMons(JSONObject settings) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.putOpt("autotransfer", settings);
        UnixSender.sendMessage(settingObj.toString());
    }

    public static void sendAutoencounterMons(JSONObject settings) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.putOpt("autoencounter", settings);
        UnixSender.sendMessage(settingObj.toString());
    }

    public static void sendCredentials(JSONObject credentials) throws JSONException {
        JSONObject settingObj = new JSONObject();
        settingObj.putOpt("credentials", credentials);
        UnixSender.sendMessage(settingObj.toString());
    }
}
