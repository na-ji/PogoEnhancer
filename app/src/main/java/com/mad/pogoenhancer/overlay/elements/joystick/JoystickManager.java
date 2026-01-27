package com.mad.pogoenhancer.overlay.elements.joystick;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.OverlayFragmentManager;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.shared.gpx.CourseDistance;
import com.mad.shared.gpx.LatLon;

import java.util.concurrent.TimeUnit;


public class JoystickManager extends OverlayFragmentManager {
    private final GpxRouteHandler _gpxRouteHandler;
    private OverlayJoystickListener _JoystickListener;
    public OverlayManager _parentManager;
    private volatile WalkTask _WalkTask;
    private final LatLon _SharedLatLon;


    public JoystickManager(OverlayManager parentManager, Context context, LatLon sharedLatLon) {
        super(parentManager, context);

        this._SharedLatLon = sharedLatLon;
        this._parentManager = parentManager;
        this._gpxRouteHandler = new GpxRouteHandler(this, this._context, this._SharedLatLon);
    }

    @Override
    protected void specificSetup() {
        super.specificSetup();
        this._JoystickListener = new OverlayJoystickListener(this._overlayManager, this._context,
                this._SharedLatLon, this._enclosingLayout,
                this, _gpxRouteHandler);
    }

    @Override
    protected void specificCleanup() {
        this.cancelRoute();
        this.cancelWalk();
        try {
            this._JoystickListener.destroy();
        } catch (InterruptedException e) {
            Logger.fatal("PogoEnhancerJ", "Failed cleaning up joystick properly");
        }
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                getLayoutFlag(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        // TODO: restore from last run...
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_JOYSTICK,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_X_JOYSTICK
        );
        params.y = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_JOYSTICK,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_Y_JOYSTICK
        );
        return params;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.overlay_joystick;
    }

    @Override
    protected View getMoveButton() {
        return this._enclosingLayout.findViewById(R.id.overlay_position_lock);
    }

    @Override
    protected void storeLocation(int offsetX, int offsetY) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_JOYSTICK, offsetX
        );
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_JOYSTICK, offsetY
        );
        edit.apply();
    }

    @Override
    protected int getBaseWidth() {
        return 90;
    }

    @Override
    protected void storeVisibility(boolean visible) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_JOYSTICK_PREOPEN, visible
        );
        edit.apply();
    }

    @Override
    protected boolean fragmentPreviouslyShown() {
        return this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_JOYSTICK_PREOPEN,
                Constants.DEFAULT_VALUES.OVERLAY_JOYSTICK_PREOPEN);
    }

    private static class WalkTask extends AsyncTask<Void, Boolean, Boolean> {

        private final double UPDATE_INTERVAL = 0.5;
        private final LatLon destination;
        private final LatLon _SharedLatLon;
        private final SharedPreferences _SharedPreferences;
        private final OverlayJoystickListener _JoystickListener;
        private final JoystickManager _Parent;
        private final boolean hideAutowalkButtonAfterWalk;

        WalkTask(LatLon sharedLatLon, LatLon destination, SharedPreferences sharedPreferences,
                 OverlayJoystickListener joystickListener, JoystickManager parent,
                 boolean hideAutowalkButtonAfterWalk) {
            this.destination = destination;
            this._SharedLatLon = sharedLatLon;
            this._SharedPreferences = sharedPreferences;
            this._JoystickListener = joystickListener;
            this._Parent = parent;
            this.hideAutowalkButtonAfterWalk = hideAutowalkButtonAfterWalk;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            publishProgress(true);

            String speedInKmph = this._SharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.SPOOF_SPEED_RUN_KMPH,
                    Constants.DEFAULT_VALUES.SPOOF_SPEED_RUN_KMPH
            );

            double metersPerSecond = 10.0;
            try {
                metersPerSecond = Float.parseFloat(speedInKmph) / 3.6f;
            } catch (NumberFormatException e) {
                Logger.fatal("PogoEnhancerJ", "Invalid speed set");
            }


            // let's get this moving...
            // total distance
            double distance = _SharedLatLon.distance(this.destination);
            double travelTimeSeconds = distance / metersPerSecond;

            if (travelTimeSeconds <= UPDATE_INTERVAL) {
                try {
                    Thread.sleep((long) (UPDATE_INTERVAL * 1000));
                } catch (InterruptedException e) {
                    Logger.warning("PogoEnhancerJ", "Interrupted while walking...");
                    return false;
                }
            }

            distance = UPDATE_INTERVAL * metersPerSecond;
            double bearing = _SharedLatLon.bearing(this.destination);
            CourseDistance courseDistance = new CourseDistance(bearing, distance);
            while (travelTimeSeconds > UPDATE_INTERVAL && !this.isCancelled()) {
                this._JoystickListener.showAutowalkAbortButton();
                try {
                    TimeUnit.MILLISECONDS.sleep((long) (UPDATE_INTERVAL * 1000));
                } catch (InterruptedException e) {
                    Logger.warning(Constants.LOGTAG, "GeoWalk got interrupted.");
                    return true;
                }
                travelTimeSeconds -= UPDATE_INTERVAL;
                bearing = _SharedLatLon.bearing(this.destination);
                courseDistance.setCourse(bearing);
                _SharedLatLon.add(courseDistance);
                _SharedLatLon.setSpeed((float) metersPerSecond);
                this.setSpeedAndLatLonText((int) Math.round(metersPerSecond * 3.6),
                        _SharedLatLon.getLat(), _SharedLatLon.getLon());
            }
            _SharedLatLon.setSpeed(0.0f);
            this.setSpeedAndLatLonText(0,
                    _SharedLatLon.getLat(), _SharedLatLon.getLon());
            publishProgress(false);
            return true;
        }

        private void setSpeedAndLatLonText(int speed, double lat, double lon) {
            this._JoystickListener.setCurrentLocationText(lat, lon);
            this._JoystickListener.setCurrentSpeedText(speed);
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this._JoystickListener.showAutowalkAbortButton();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            cleanup();
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            cleanup();
            super.onCancelled(aBoolean);
        }

        @Override
        protected void onCancelled() {
            cleanup();
            super.onCancelled();
        }

        private void cleanup() {
            synchronized (this._Parent) {
                if (this._Parent._WalkTask == this) {
                    this._Parent._WalkTask = null;
                }
            }

            if (this.hideAutowalkButtonAfterWalk) {
                this._JoystickListener.hideAutowalkAbortButton();
            }
        }
    }

    public void cancelWalk() {
        if (this._WalkTask != null) {
            this._WalkTask.cancel(true);
            //this._WalkTask = null;
            /*if (cancelRouteHandler) {
                this._JoystickListener.hideAutowalkAbortButton();
            }*/
        }
    }

    public void cancelRoute() {
        if (this._gpxRouteHandler != null) {
            this._gpxRouteHandler.stopRoute();
        }
    }

    public boolean isWalkOngoing() {
        return this._WalkTask != null;
    }

    public void walkToDest(LatLon dest, boolean hideButtonAfterWalk) {
        this.cancelWalk();
        this._WalkTask = new WalkTask(this._SharedLatLon, dest,
                this._sharedPreferences, this._JoystickListener, this,
                hideButtonAfterWalk);
        this._WalkTask.execute();
    }

    void teleportTo(LatLon dest) {
        this.cancelWalk();
        this._SharedLatLon.setLocation(dest);
    }

    synchronized void showAutowalkAbortButton() {
        this._JoystickListener.showAutowalkAbortButton();
        this._overlayManager.showStopIcon(true);
    }

    synchronized void hideAutowalkAbortButton() {
        this._JoystickListener.hideAutowalkAbortButton();
        this._overlayManager.showStopIcon(false);
    }

    public void showTeleportToDialog() {
        this._JoystickListener.showTeleportToDialog();
    }

    public void showGpxSelectDialog() {
        this._JoystickListener.onClickGpxSelectionButton();
    }

    public void showSpeedAdjustmentDialog() {
        this._JoystickListener.showSpeedAdjustmentDialog();
    }
}
