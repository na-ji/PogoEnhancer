package com.mad.pogoenhancer.overlay.elements.cooldown;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.OverlayFragmentManager;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.shared.gpx.LatLon;

import java.util.Locale;
import java.util.regex.Pattern;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CooldownLayoutManager extends OverlayFragmentManager implements View.OnClickListener {

    private ImageView reset_cd_button;

    private TextView cooldown_remaining_minutes;

    private TextView cooldown_over_at;

    private long cooldownOverAtUnixSeconds = 0;

    private Handler textUpdater;

    private Runnable textRunnable;

    public CooldownLayoutManager(OverlayManager overlayManager, Context context) {
        super(overlayManager, context);
    }

    @Override
    protected void storeVisibility(boolean visible) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_COOLDOWN_PREOPEN, visible
        );
        edit.apply();
    }

    private void updateRemainingSeconds() {
        long teleportedAt = this._sharedPreferences.getLong(
                Constants.SHAREDPERFERENCES_KEYS.TELEPORTED_AT,
                Constants.DEFAULT_VALUES.TELEPORTED_AT);
        double distanceTravelled = this._sharedPreferences.getFloat(
                Constants.SHAREDPERFERENCES_KEYS.DISTANCE_TRAVELLED,
                Constants.DEFAULT_VALUES.DISTANCE_TRAVELLED
        );

        long remainingCooldownTeleport = Constants.calculateRemainingCooldown(teleportedAt, distanceTravelled);
        if (remainingCooldownTeleport > 0 && cooldownOverAtUnixSeconds > System.currentTimeMillis() / 1000 + 300) {
            // only if remaining cooldown after teleport AND the cd that may have been set by the user is more than 5minutes left...
            cooldownOverAtUnixSeconds = System.currentTimeMillis() / 1000 + Constants.calculateRemainingCooldown(teleportedAt, distanceTravelled);

        }

    }

    public void updateText(String text) {
        this.cooldown_over_at.setText(text);
    }


    public void updateView(long timeTeleported, float distanceTravelledKm) {
        if (timeTeleported < 0) {
            timeTeleported = System.currentTimeMillis() / 1000;
        }
        cooldownOverAtUnixSeconds = System.currentTimeMillis() / 1000 + Constants.calculateRemainingCooldown(timeTeleported, distanceTravelledKm);
        String cooldownOverAt = Constants.cooldownOverAt(timeTeleported, distanceTravelledKm);

        if (this.cooldown_over_at != null) {
            this.cooldown_over_at.setText(cooldownOverAt);
        }

        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putLong(Constants.SHAREDPERFERENCES_KEYS.TELEPORTED_AT, timeTeleported);
        edit.putFloat(Constants.SHAREDPERFERENCES_KEYS.DISTANCE_TRAVELLED, distanceTravelledKm);
        edit.apply();
    }

    private void reset() {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putLong(Constants.SHAREDPERFERENCES_KEYS.TELEPORTED_AT, 0);
        edit.putFloat(Constants.SHAREDPERFERENCES_KEYS.DISTANCE_TRAVELLED, 0);

        edit.putString(Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_LAT, "0.0");
        edit.putString(Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_LNG, "0.0");
        edit.putLong(Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_TIME, 0);

        edit.apply();

        this.cooldownOverAtUnixSeconds = 0;
    }

    public boolean isCooldownActive() {
        return (this.cooldownOverAtUnixSeconds == 0 || cooldownOverAtUnixSeconds < System.currentTimeMillis() / 1000);
    }

    @SuppressLint("SetTextI18n")
    private void updateTexts() {

        long lastActionTimestamp = this._sharedPreferences.getLong(
                Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_TIME,
                Constants.DEFAULT_VALUES.COOLDOWN_TIME);


        if (lastActionTimestamp == 0 || lastActionTimestamp + 2400 < (System.currentTimeMillis() / 1000)) {

            this.cooldown_remaining_minutes.setText("No CD");
            this.cooldown_over_at.setText("");

        } else {

            String lastLat = this._sharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_LAT,
                    Constants.DEFAULT_VALUES.COOLDOWN_LAT);

            String latLng = this._sharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_LNG,
                    Constants.DEFAULT_VALUES.COOLDOWN_LNG);

            LatLon currentLatLng = this._overlayManager.getLocation();
            LatLon lastLatLng = new LatLon(Double.parseDouble(lastLat), Double.parseDouble(latLng));

            double distance = this._overlayManager.getDistance(currentLatLng, lastLatLng) / 1000;
            long remainingCooldownTeleport = System.currentTimeMillis() / 1000 + Constants.calculateRemainingCooldown(lastActionTimestamp, distance);
            long remainingMinutes = remainingCooldownTeleport - System.currentTimeMillis() / 1000;

            if ((remainingMinutes / 60) > 0) {
                this.cooldown_remaining_minutes.setText(String.format(Locale.ENGLISH, "%d mins", remainingMinutes / 60));
                this.cooldown_over_at.setText(Constants.unixSecondsToTimeString(remainingCooldownTeleport));
            } else {
                this.cooldown_remaining_minutes.setText("No CD");
                this.cooldown_over_at.setText("");
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (this.reset_cd_button != null && v == this.reset_cd_button) {
            this.showResetDialog();
        }
    }

    final private Pattern minutesPattern = Pattern.compile("\\d+");

    public void showResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this._context);
        builder.setTitle("Reset cooldown");

        /*
        final EditText input = new EditText(this._context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Custom time (minutes)", (dialog, which) -> {
            String numberEntered = input.getText().toString();
            if (minutesPattern.matcher(numberEntered).matches()) {
                int minutes = Integer.parseInt(numberEntered);
                cooldownOverAtUnixSeconds = System.currentTimeMillis() / 1000 + minutes * 60;
            }
        });
        */

        builder.setPositiveButton("Reset", (dialog, which) -> reset());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        alertDialog.getWindow().setType(LAYOUT_FLAG);

        alertDialog.show();
    }

    @Override
    protected int getBaseWidth() {
        return WRAP_CONTENT;
    }

    @Override
    protected boolean fragmentPreviouslyShown() {
        return this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_COOLDOWN_PREOPEN,
                Constants.DEFAULT_VALUES.OVERLAY_COOLDOWN_PREOPEN);
    }

    @Override
    protected void specificCleanup() {
        this.textUpdater.removeCallbacks(this.textRunnable);
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, getLayoutFlag(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        // TODO: restore from last run...
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_COOLDOWN,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_X_COOLDOWN
        );
        params.y = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_COOLDOWN,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_Y_COOLDOWN
        );
        return params;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.overlay_cooldown;
    }

    @Override
    protected void storeLocation(int offsetX, int offsetY) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_COOLDOWN, offsetX
        );
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_COOLDOWN, offsetY
        );
        edit.apply();
    }

    @Override
    protected void specificSetup() {
        super.specificSetup();
        textUpdater = new Handler(Looper.getMainLooper());

        textRunnable = () -> {
            try {
                //updateRemainingSeconds(diffLastUpdate);
                updateTexts();
            } catch (Exception e) {
                Logger.debug(Constants.LOGTAG, "Failed updating cooldown timer");
            }
            textUpdater.postDelayed(textRunnable, 5000);
        };

        this._sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this._context);

        this.reset_cd_button = this._enclosingLayout.findViewById(R.id.reset_cd_button);
        this.cooldown_remaining_minutes = this._enclosingLayout.findViewById(R.id.cooldown_remaining_minutes);
        this.cooldown_over_at = this._enclosingLayout.findViewById(R.id.cooldown_over_at);

        this.reset_cd_button.setOnClickListener(this);

        //updateRemainingSeconds();

        textUpdater.post(textRunnable);
    }
}
