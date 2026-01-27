package com.mad.pogoenhancer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.services.InjectionSettingSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class InventoryBuild {
    private SharedPreferences _sharedPreferences = null;

    public InventoryBuild(@NonNull Context context) {
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void sendInventoryJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("active", isInventoryManagementEnabled());
            obj.put("items", getInventoryItems());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            InjectionSettingSender.sendInventory(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendCooldown() {
        JSONObject obj = new JSONObject();

        String cooldownLat = this._sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_LAT,
                Constants.DEFAULT_VALUES.COOLDOWN_LAT
        );

        String cooldownLng = this._sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_LNG,
                Constants.DEFAULT_VALUES.COOLDOWN_LNG
        );

        Long cooldownTimestamp = this._sharedPreferences.getLong(
                Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_TIME,
                Constants.DEFAULT_VALUES.COOLDOWN_TIME
        );


        try {
            obj.put("lat", Double.parseDouble(cooldownLat));
            obj.put("lng", Double.parseDouble(cooldownLng));
            obj.put("timestamp", cooldownTimestamp);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Logger.debug("Cooldown", obj.toString());

        try {
            InjectionSettingSender.sendCooldown(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isInventoryManagementEnabled() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.ACTIVE_INVENTORY_MANAGEMENT,
                Constants.DEFAULT_VALUES.ACTIVE_INVENTORY_MANAGEMENT);
    }

    private JSONObject getInventoryItems()  throws JSONException {
        JSONObject obj = new JSONObject();

        for (int itemId : Constants.SHAREDPERFERENCES_KEYS.supportedItemIdsForInventoryCleaning) {
            String itemIdentifier = "item_" + Integer.toString(itemId);
            String itemCountToKeepAsString = _sharedPreferences.getString(
                    itemIdentifier,
                    Constants.DEFAULT_VALUES.SEMI_INFINITE_INVENTORY_KEEP_AMOUNT);
            obj.put(itemIdentifier, Integer.parseInt(itemCountToKeepAsString));
        }

        return obj;
    }

    private boolean isAutotransferEnable() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.ENABLE_AUTOTRANSFER,
                Constants.DEFAULT_VALUES.ENABLE_AUTOTRANSFER);
    }

    private boolean isAutoencounterEnable() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.ENABLE_AUTOENCOUNTER,
                Constants.DEFAULT_VALUES.ENABLE_AUTOENCOUNTER);
    }

    private boolean isAutotransferListInverted() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.AUTOTRANSFER_SWITCH_NOT_TRANSFER,
                Constants.DEFAULT_VALUES.AUTOTRANSFER_SWITCH_NOT_TRANSFER);
    }

    private JSONObject getNotTransferIDs()  throws JSONException {
        JSONObject obj = new JSONObject();

        Set<String> stringSet = _sharedPreferences.getStringSet(
                Constants.SHAREDPERFERENCES_KEYS.MON_NOT_TRANSFERED_IDS,
                Constants.DEFAULT_VALUES.MON_NOT_TRANSFERED_IDS);

        //Set<Integer> retval = new HashSet<>();
        if (stringSet == null) {
            return obj;
        }
        for (String id : stringSet) {
            //retval.add(Integer.valueOf(id));
            obj.put("mon_" + id, true);
        }

        return obj;
    }

    public void sendAutotransferJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("active", isAutotransferEnable());
            obj.put("monid", getNotTransferIDs());
            obj.put("invertedList", isAutotransferListInverted());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            InjectionSettingSender.sendAutotransferMons(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendAutoencounterJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("active", isAutoencounterEnable());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            InjectionSettingSender.sendAutoencounterMons(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendNotAutrunMonJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("noautorunmonid", getNotAutorunMons());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            InjectionSettingSender.sendSetting(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getNotAutorunMons()  throws JSONException {
        JSONObject obj = new JSONObject();

        Set<String> stringSet = _sharedPreferences.getStringSet(
                Constants.SHAREDPERFERENCES_KEYS.MON_NOT_RUN_IDS,
                Constants.DEFAULT_VALUES.MON_NOT_RUN_IDS);

        //Set<Integer> retval = new HashSet<>();
        if (stringSet == null) {
            return obj;
        }
        for (String id : stringSet) {
            //retval.add(Integer.valueOf(id));
            obj.put("mon_" + id, true);
        }

        return obj;
    }

}
