package com.mad.pogoenhancer.overlay.elements.nearby;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.OverlayFragmentManager;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.shared.gpx.LatLon;

import POGOProtos.Rpc.GetMapObjectsOutProto;

public class NearbyListingManager extends OverlayFragmentManager {
    private RecyclerView _NearbyRecyclerView;
    private NearbyMonAdapter _NearbyMonAdapter;
    private RecyclerView.LayoutManager _RecyclerViewLayoutManager;
    private final LatLon _sharedLatLon;

    public NearbyListingManager(Context context, LatLon sharedLatLon,
                                OverlayManager supervisingManager) {
        super(supervisingManager, context);
        this._sharedLatLon = sharedLatLon;
    }


    public void updateDataset(GetMapObjectsOutProto gmo) {
        if (this._NearbyMonAdapter != null) {
            this._NearbyMonAdapter.updateDataset(gmo);
        }
    }

    @Override
    protected void specificSetup() {
        super.specificSetup();

        this._NearbyRecyclerView = this._enclosingLayout.findViewById(R.id.nearby_recycler_view);


        this._NearbyRecyclerView.setHasFixedSize(true);
        // use a linear layout_old manager
        this._RecyclerViewLayoutManager = new LinearLayoutManager(this._context);
        this._NearbyRecyclerView.setLayoutManager(this._RecyclerViewLayoutManager);
        // specify an adapter
        this._NearbyMonAdapter = new NearbyMonAdapter(this._sharedLatLon, this._context,
                this._overlayManager);
        this._NearbyRecyclerView.setAdapter(this._NearbyMonAdapter);

    }

    @Override
    protected int getBaseWidth() {
        return 200;
    }

    @Override
    protected void storeVisibility(boolean visible) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_NEARBY_PREOPEN, visible
        );
        edit.apply();
    }

    @Override
    protected boolean fragmentPreviouslyShown() {
        return this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_NEARBY_PREOPEN,
                Constants.DEFAULT_VALUES.OVERLAY_NEARBY_PREOPEN);
    }

    @Override
    protected void specificCleanup() {

    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getLayoutFlag(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        // TODO: restore from last run...
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_NEARBY,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_X_NEARBY
        );
        params.y = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_NEARBY,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_Y_NEARBY
        );
        return params;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.overlay_nearby_listing;
    }

    @Override
    protected void storeLocation(int offsetX, int offsetY) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_NEARBY, offsetX
        );
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_NEARBY, offsetY
        );
        edit.apply();
    }
}
