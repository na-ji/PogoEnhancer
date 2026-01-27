package com.mad.pogoenhancer.overlay.elements.injectionSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.OverlayFragmentManager;
import com.mad.pogoenhancer.overlay.OverlayManager;

public class InjectionSettingsManager extends OverlayFragmentManager {
    public InjectionSettingsManager(OverlayManager overlayManager, Context context) {
        super(overlayManager, context);
    }

    @Override
    protected void storeVisibility(boolean visible) {
        // nothing for now
    }

    @Override
    protected int getBaseWidth() {
        return 200;
    }

    @Override
    protected boolean fragmentPreviouslyShown() {
        return false;
    }

    @Override
    protected void specificCleanup() {

    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getLayoutFlag(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_INJECTION_SETTINGS,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_X_INJECTION_SETTINGS
        );
        params.y = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_INJECTION_SETTINGS,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_Y_INJECTION_SETTINGS
        );
        return params;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.overlay_injection_settings;
    }

    @Override
    protected void storeLocation(int offsetX, int offsetY) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_INJECTION_SETTINGS, offsetX
        );
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_INJECTION_SETTINGS, offsetY
        );
        edit.apply();
    }
}
