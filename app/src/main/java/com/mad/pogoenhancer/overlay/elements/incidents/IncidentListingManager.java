package com.mad.pogoenhancer.overlay.elements.incidents;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.OverlayFragmentManager;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.shared.gpx.LatLon;

import POGOProtos.Rpc.GetMapObjectsOutProto;

public class IncidentListingManager extends OverlayFragmentManager {
    private RecyclerView _IncidentRecyclerView;
    private IncidentAdapter _IncidentAdapter;
    private RecyclerView.LayoutManager _RecyclerViewLayoutManager;
    private final LatLon _sharedLatLon;
    private GetMapObjectsOutProto _latestGmo = null;

    public IncidentListingManager(Context context, LatLon sharedLatLon,
                                  OverlayManager supervisingManager) {
        super(supervisingManager, context);
        this._sharedLatLon = sharedLatLon;
    }


    public void updateDataset(GetMapObjectsOutProto gmo) {
        if (this._IncidentAdapter != null) {
            this._IncidentAdapter.updateDataset(gmo);
        }
        this._latestGmo = gmo;
    }

    @Override
    protected void specificSetup() {
        super.specificSetup();

        this._IncidentRecyclerView = this._enclosingLayout.findViewById(R.id.incident_recycler_view);

        // Setup toggle buttons for grunt visibility
        // TODO
        setupGruntDisplayButton(R.id.grunt, Constants.SHAREDPERFERENCES_KEYS.INCIDENT_DISPLAY_GRUNTS, Constants.DEFAULT_VALUES.INCIDENT_DISPLAY_GRUNTS);
        setupGruntDisplayButton(R.id.grunt_leader, Constants.SHAREDPERFERENCES_KEYS.INCIDENT_DISPLAY_LEADERS, Constants.DEFAULT_VALUES.INCIDENT_DISPLAY_LEADERS);
        setupGruntDisplayButton(R.id.grunt_giovanni, Constants.SHAREDPERFERENCES_KEYS.INCIDENT_DISPLAY_GIOVANNI, Constants.DEFAULT_VALUES.INCIDENT_DISPLAY_GIOVANNI);

        this._IncidentRecyclerView.setHasFixedSize(true);
        // use a linear layout_old manager
        this._RecyclerViewLayoutManager = new LinearLayoutManager(this._context);
        this._IncidentRecyclerView.setLayoutManager(this._RecyclerViewLayoutManager);
        // specify an adapter
        this._IncidentAdapter = new IncidentAdapter(this._sharedLatLon, this._context,
                this._overlayManager);
        this._IncidentRecyclerView.setAdapter(this._IncidentAdapter);

    }

    private void setupGruntDisplayButton(int p, String incidentDisplayGrunts, boolean incidentDisplayGrunts2) {
        ImageView gruntToggle = this._enclosingLayout.findViewById(p);
        gruntToggle.setOnClickListener(l -> {
            boolean gruntDisplayEnabled = this._sharedPreferences.getBoolean(
                    incidentDisplayGrunts,
                    incidentDisplayGrunts2);
            SharedPreferences.Editor editor = this._sharedPreferences.edit();
            editor.putBoolean(incidentDisplayGrunts, !gruntDisplayEnabled);
            editor.apply();
            if (gruntDisplayEnabled) {
                gruntToggle.setBackgroundColor(Color.RED);
            } else {
                gruntToggle.setBackgroundColor(Color.GREEN);
            }
            this._IncidentAdapter.updateDataset(this._latestGmo);
        });

        boolean gruntDisplayEnabled = this._sharedPreferences.getBoolean(
                incidentDisplayGrunts,
                incidentDisplayGrunts2);
        SharedPreferences.Editor editor = this._sharedPreferences.edit();
        editor.putBoolean(incidentDisplayGrunts, gruntDisplayEnabled);
        editor.apply();
        if (!gruntDisplayEnabled) {
            gruntToggle.setBackgroundColor(Color.RED);
        } else {
            gruntToggle.setBackgroundColor(Color.GREEN);
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
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_INCIDENT_PREOPEN, visible
        );
        edit.apply();
    }

    @Override
    protected boolean fragmentPreviouslyShown() {
        return this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_INCIDENT_PREOPEN,
                Constants.DEFAULT_VALUES.OVERLAY_INCIDENT_PREOPEN);
    }

    @Override
    protected void specificCleanup() {

    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getLayoutFlag(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_INCIDENT,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_X_INCIDENT
        );
        params.y = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_INCIDENT,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_Y_INCIDENT
        );
        return params;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.overlay_incident_listing;
    }

    @Override
    protected void storeLocation(int offsetX, int offsetY) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_INCIDENT, offsetX
        );
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_INCIDENT, offsetY
        );
        edit.apply();
    }
}
