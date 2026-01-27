package com.mad.pogoenhancer.overlay.elements.mapSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Switch;

import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.utils.WildmonBuild;

/**
 * @attr ref android.R.styleable#InjectionSettingSwitch_PreferenceKey
 */
public class MapSettingSwitch extends Switch {
    protected final SharedPreferences _PreferenceManager;
    protected final String _SettingsKey;
    private final WildmonBuild _wildmonBuild;

    public MapSettingSwitch(Context context) throws Exception {
        super(context);
        this._wildmonBuild = new WildmonBuild(context);
        throw new Exception("Missing settings key");
    }

    public MapSettingSwitch(Context context, AttributeSet attrs) throws Exception {
        super(context, attrs);
        _PreferenceManager = PreferenceManager.
                getDefaultSharedPreferences(context);
        this._wildmonBuild = new WildmonBuild(context);

        // TODO: Actually... throw exception since we can only use this with the key given
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MapSettingSwitch);
        CharSequence title = a.getString(R.styleable.MapSettingSwitch_map_preference_key);
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
            _wildmonBuild.sendWildmonJson();
        });

    }

    public MapSettingSwitch(Context context, AttributeSet attrs, int defStyleAttr) throws Exception {
        this(context, attrs);
    }

    public MapSettingSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) throws Exception {
        this(context, attrs);
    }
}
