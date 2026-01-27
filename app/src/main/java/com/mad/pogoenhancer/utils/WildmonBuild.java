package com.mad.pogoenhancer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.services.InjectionSettingSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class WildmonBuild {
    private SharedPreferences _sharedPreferences = null;

    public WildmonBuild(@NonNull Context context) {
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public void sendWildmonJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("types", getMonTypes());
            obj.put("hideMons" , getHideMons());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            InjectionSettingSender.sendWildMonTypes(obj);
            InjectionSettingSender.sendBoolSetting("disable_grunts", getGruntSetting());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean getGruntSetting () {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.DISABLE_GRUNTS,
                Constants.DEFAULT_VALUES.DISABLE_GRUNTS);
    }




    private JSONObject getMonTypes()  throws JSONException {
        JSONObject obj = new JSONObject();

        for (int itemId : Constants.SHAREDPERFERENCES_KEYS.wildmonTyp) {
            String wildMonTypeIdentifier = "type_" + itemId;
            Boolean wildMonType = _sharedPreferences.getBoolean(
                    wildMonTypeIdentifier,
                    Constants.DEFAULT_VALUES.SHOW_WILDMON);
            obj.put(wildMonTypeIdentifier, wildMonType);
        }

        return obj;
    }

    private JSONObject getHideMons()  throws JSONException {
        JSONObject obj = new JSONObject();

        boolean hideOnMap = _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.HIDE_MON_ON_MAP,
                Constants.DEFAULT_VALUES.HIDE_MON_ON_MAP);

        Set<String> stringSet = _sharedPreferences.getStringSet(
                Constants.SHAREDPERFERENCES_KEYS.MON_HIDE_IDS_NEARBY,
                Constants.DEFAULT_VALUES.MON_HIDE_IDS_NEARBY);

        //Set<Integer> retval = new HashSet<>();
        obj.put("hideonmap", hideOnMap);
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
