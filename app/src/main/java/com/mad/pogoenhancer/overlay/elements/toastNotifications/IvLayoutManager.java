package com.mad.pogoenhancer.overlay.elements.toastNotifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.pogoenhancer.overlay.elements.OverlayToast;

import org.joda.time.DateTime;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class IvLayoutManager extends OverlayToast {
    private static final int[] weather_array_day = {0, R.drawable.ic_sun_solid,
            R.drawable.ic_umbrella_solid, R.drawable.ic_cloud_sun_solid,
            R.drawable.ic_cloud_solid, R.drawable.ic_wind_solid, R.drawable.ic_snowflake_solid,
            R.drawable.ic_smog_solid};
    private static final int[] weather_array_night = {0, R.drawable.ic_moon_solid,
            R.drawable.ic_umbrella_solid, R.drawable.ic_cloud_moon_solid,
            R.drawable.ic_cloud_solid, R.drawable.ic_wind_solid, R.drawable.ic_snowflake_solid,
            R.drawable.ic_smog_solid};
    private static final String[] gender_array = {"", "⚦", "♀", "⚤"};


    public IvLayoutManager(OverlayManager overlayManager, Context context) {
        super(overlayManager, context);
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, getLayoutFlag(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_IV,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_X_IV
        );
        params.y = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_IV,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_Y_IV
        );
        return params;
    }


    @Override
    protected void storeLocation(int offsetX, int offsetY) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_IV, offsetX
        );
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_IV, offsetY
        );
        edit.apply();
    }

    public void setIv(int attack, int defence, int stamina, double cpMultiplier,
                      double additionalCpMultiplier, int monLvl,
                      int shiny, int typ, int gender, int weather,
                      String weightXSXL, String heightXSXL, int ditto) {
        String dittotext = "";

        int shinyImage = 0;
        if (shiny > 0) {
            shinyImage = R.drawable.ic_shiny_white;
        }

        if (ditto > 0) {
            dittotext = " DITTO";
        }

        int genderPassed = gender;
        if (gender < 0 || gender >= gender_array.length) {
            genderPassed = 0;
        }

        int weatherImage = 0;
        if (weather >= 1 && weather < weather_array_night.length) {
            weatherImage = weather_array_day[weather];

            int hour = new DateTime().getHourOfDay();
            if (hour >= 20 || hour < 7) {
                weatherImage = weather_array_night[weather];
            }
        }

        double ivPercentage = 100 * (attack + defence + stamina) / 45.0;
        ivPercentage = Math.floor(ivPercentage * 100.0) / 100.0;
        int pokemonLevelToDisplay = monLvl == 0 ? Constants.getLevelByMultiplier(cpMultiplier + additionalCpMultiplier) : monLvl;
        String ivText = "IV " + (int) ivPercentage + " " + gender_array[genderPassed] + dittotext + "\n" +
                "A" + attack + " / D" + defence + " / S" + stamina + "  L" + pokemonLevelToDisplay;
        if (weightXSXL.length() > 0 || heightXSXL.length() > 0) {
            ivText += "\n" + weightXSXL + " " + heightXSXL;
        }


        if (typ == 1) {
            this.setText(ivText, shinyImage, weatherImage);
        }
    }

    @Override
    protected void specificSetup() {
        super.specificSetup();
        this._viewContent.setCardBackgroundColor(null);
    }
}
