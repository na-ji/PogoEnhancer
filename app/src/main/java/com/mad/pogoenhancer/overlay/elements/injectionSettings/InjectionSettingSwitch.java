package com.mad.pogoenhancer.overlay.elements.injectionSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Switch;

import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.services.InjectionSettingSender;

import org.json.JSONException;

/**
 * @attr ref android.R.styleable#InjectionSettingSwitch_PreferenceKey
 */
public class InjectionSettingSwitch extends Switch {
    protected final SharedPreferences _PreferenceManager;
    protected final String _SettingsKey;

    public InjectionSettingSwitch(Context context) throws Exception {
        super(context);
        throw new Exception("Missing settings key");
    }

    public InjectionSettingSwitch(Context context, AttributeSet attrs) throws Exception {
        super(context, attrs);
        _PreferenceManager = PreferenceManager.
                getDefaultSharedPreferences(context);
        // TODO: Actually... throw exception since we can only use this with the key given
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InjectionSettingSwitch);
        CharSequence title = a.getString(R.styleable.InjectionSettingSwitch_preference_key);
        if (title != null) {
            this._SettingsKey = title.toString();
        } else {
            throw new Exception("Missing settings key");
        }
        a.recycle();

        boolean currentValueForKey = _PreferenceManager.getBoolean(this._SettingsKey, false);
        super.setChecked(currentValueForKey);
        this.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle Preference
            SharedPreferences.Editor editor = _PreferenceManager.edit();
            editor.putBoolean(_SettingsKey, isChecked);
            editor.apply();

            // Also toggle the setting in an active injection (if there is one...)
            try {
                InjectionSettingSender.sendBoolSetting(_SettingsKey, isChecked);
            } catch (JSONException e) {
                Logger.debug("ProtoHookJ", "Failed setting " + _SettingsKey);
            }
        });
    }

    public InjectionSettingSwitch(Context context, AttributeSet attrs, int defStyleAttr) throws Exception {
        this(context, attrs);
    }

    public InjectionSettingSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) throws Exception {
        this(context, attrs);
    }
}
