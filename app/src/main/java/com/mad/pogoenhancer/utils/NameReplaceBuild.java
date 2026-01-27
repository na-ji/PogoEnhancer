package com.mad.pogoenhancer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.services.InjectionSettingSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class NameReplaceBuild {

    private SharedPreferences _sharedPreferences = null;
    private Context _context = null;

    public NameReplaceBuild(@NonNull Context context) {
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        _context = context;
    }



    public void sendNameReplaceJson()  {
        JSONObject obj = new JSONObject();
        try {
            obj.put("nameReplace", isNameReplace());
            obj.put("nameReplaceEncounter", isNameReplaceEncounter());
            obj.put("nameValues", getNameValues());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            InjectionSettingSender.sendNameReplace(obj);
            InjectionSettingSender.sendMonMoves(loadMoves());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNameReplace() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.REPLACE_CP_WITH_IV_PERCENTAGE,
                Constants.DEFAULT_VALUES.REPLACE_CP_WITH_IV_PERCENTAGE);
    }

    private boolean isNameReplaceEncounter() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.REPLACE_ENCOUNTER_NAMES,
                Constants.DEFAULT_VALUES.REPLACE_ENCOUNTER_NAMES);
    }

    private JSONObject getNameValues()  throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("IvPercentage", getIvPercentage());
        obj.put("IvValues", getIvValues());
        obj.put("monName", getName());
        obj.put("Lvl", getLvl());
        obj.put("Gender", getGender());
        obj.put("XsXl", getXsXl());
        obj.put("Moveset", getMoveset());
        obj.put("Specialmon", getSpecialMon());

        return obj;
    }

    private boolean getIvPercentage() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_IV_PERCENTAGE,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_IV_PERCENTAGE);
    }

    private boolean getIvValues() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_IV_VALUES,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_IV_VALUES);
    }

    private boolean getName() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_NAME,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_NAME);
    }

    private boolean getLvl() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_LVL,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_LVL);
    }

    private boolean getGender() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_GENDER,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_GENDER);
    }

    private boolean getXsXl() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_XSXL,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_XSXL);
    }

    private boolean getMoveset() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_MOVESET,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_MOVESET);
    }

    private boolean getSpecialMon() {
        return _sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_SPECIALMON,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_SPECIALMON);
    }

    private JSONObject loadMoves() throws IOException, JSONException {
        InputStream is = this._context.getResources().openRawResource(R.raw.moves);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String moveJson = new String(buffer, "UTF-8");
        JSONObject obj = new JSONObject(moveJson);
        return obj;
    }

}
