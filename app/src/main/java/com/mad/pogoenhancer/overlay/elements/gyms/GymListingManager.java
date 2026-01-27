package com.mad.pogoenhancer.overlay.elements.gyms;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.OverlayFragmentManager;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.shared.gpx.LatLon;

import POGOProtos.Rpc.GetMapObjectsOutProto;

public class GymListingManager extends OverlayFragmentManager {
    private RecyclerView _GymRecyclerView;
    private GymAdapter _GymAdapter;
    private RecyclerView.LayoutManager _RecyclerViewLayoutManager;
    private final LatLon _sharedLatLon;
    private GetMapObjectsOutProto _latestGmo = null;

    public GymListingManager(Context context, LatLon sharedLatLon,
                             OverlayManager supervisingManager) {
        super(supervisingManager, context);
        this._sharedLatLon = sharedLatLon;
    }


    public void updateDataset(GetMapObjectsOutProto gmo) {
        if (this._GymAdapter != null) {
            this._GymAdapter.updateDataset(gmo);
        }
        this._latestGmo = gmo;
    }

    @Override
    protected void specificSetup() {
        super.specificSetup();
        this._GymRecyclerView = this._enclosingLayout.findViewById(R.id.gym_recycler_view);

        // Setup toggle buttons for grunt visibility
        setupGymDisplayButton(R.id.fraction_white, R.id.fraction_white_check, Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_FRACTION_NONE, Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_FRACTION_NONE);
        setupGymDisplayButton(R.id.fraction_red, R.id.fraction_red_check, Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_FRACTION_RED, Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_FRACTION_RED);
        setupGymDisplayButton(R.id.fraction_blue, R.id.fraction_blue_check, Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_FRACTION_BLUE, Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_FRACTION_BLUE);
        setupGymDisplayButton(R.id.fraction_yellow, R.id.fraction_yellow_check, Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_FRACTION_YELLOW, Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_FRACTION_YELLOW);

        setupLevelDisplayButton(R.id.raid_level_filter_none,
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_NONE,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_NONE);
        setupLevelDisplayButton(R.id.raid_level_filter_1,
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_1,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_1);
        setupLevelDisplayButton(R.id.raid_level_filter_2,
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_2,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_2);
        setupLevelDisplayButton(R.id.raid_level_filter_3,
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_3,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_3);
        setupLevelDisplayButton(R.id.raid_level_filter_4,
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_4,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_4);
        setupLevelDisplayButton(R.id.raid_level_filter_5,
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_5,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_5);

        this._GymRecyclerView.setHasFixedSize(true);
        // use a linear layout_old manager
        this._RecyclerViewLayoutManager = new LinearLayoutManager(this._context);
        this._GymRecyclerView.setLayoutManager(this._RecyclerViewLayoutManager);
        // specify an adapter
        this._GymAdapter = new GymAdapter(this._sharedLatLon, this._context,
                this._overlayManager);
        this._GymRecyclerView.setAdapter(this._GymAdapter);
    }

    private void setupGymDisplayButton(int buttonId, int checkedButtonId, String preferenceKey, boolean defaultValue) {
        ImageView checkedButton = this._enclosingLayout.findViewById(checkedButtonId);
        ImageView gymFractionToggle = this._enclosingLayout.findViewById(buttonId);
        gymFractionToggle.setOnClickListener(l -> {
            boolean fractionDisplayEnabled = this._sharedPreferences.getBoolean(
                    preferenceKey,
                    defaultValue);
            SharedPreferences.Editor editor = this._sharedPreferences.edit();
            editor.putBoolean(preferenceKey, !fractionDisplayEnabled);
            editor.apply();
            if (!fractionDisplayEnabled) {
                checkedButton.setVisibility(View.VISIBLE);
            } else {
                checkedButton.setVisibility(View.INVISIBLE);
            }
            this._GymAdapter.updateDataset(this._latestGmo);
        });

        boolean fractionDisplayEnabled = this._sharedPreferences.getBoolean(
                preferenceKey,
                defaultValue);
        if (fractionDisplayEnabled) {
            checkedButton.setVisibility(View.VISIBLE);
        } else {
            checkedButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setupLevelDisplayButton(int p, String preference, boolean defaultValue) {
        TextView levelToggle = this._enclosingLayout.findViewById(p);
        levelToggle.setOnClickListener(l -> {
            boolean levelEnabled = this._sharedPreferences.getBoolean(
                    preference,
                    defaultValue);
            SharedPreferences.Editor editor = this._sharedPreferences.edit();
            editor.putBoolean(preference, !levelEnabled);
            editor.apply();
            if (levelEnabled) {
                levelToggle.setBackgroundColor(Color.RED);
            } else {
                levelToggle.setBackgroundColor(Color.GREEN);
            }
            this._GymAdapter.updateDataset(this._latestGmo);
        });

        boolean levelEnabled = this._sharedPreferences.getBoolean(
                preference,
                defaultValue);
        if (!levelEnabled) {
            levelToggle.setBackgroundColor(Color.RED);
        } else {
            levelToggle.setBackgroundColor(Color.GREEN);
        }
    }

    @Override
    protected int getBaseWidth() {
        return 200;
    }

    @Override
    protected void storeVisibility(boolean visible) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_GYM_PREOPEN, visible
        );
        edit.apply();
    }

    @Override
    protected boolean fragmentPreviouslyShown() {
        return this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_GYM_PREOPEN,
                Constants.DEFAULT_VALUES.OVERLAY_GYM_PREOPEN);
    }

    @Override
    protected void specificCleanup() {

    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getLayoutFlag(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_GYM,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_X_GYM
        );
        params.y = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_GYM,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_Y_GYM
        );
        return params;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.overlay_gym_listing;
    }

    @Override
    protected void storeLocation(int offsetX, int offsetY) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_GYM, offsetX
        );
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_GYM, offsetY
        );
        edit.apply();
    }
}
