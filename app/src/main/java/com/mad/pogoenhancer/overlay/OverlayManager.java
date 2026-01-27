package com.mad.pogoenhancer.overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.cardview.widget.CardView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.elements.cooldown.CooldownLayoutManager;
import com.mad.pogoenhancer.overlay.elements.gyms.GymListingManager;
import com.mad.pogoenhancer.overlay.elements.incidents.IncidentListingManager;
import com.mad.pogoenhancer.overlay.elements.injectionSettings.InjectionSettingsManager;
import com.mad.pogoenhancer.overlay.elements.joystick.JoystickManager;
import com.mad.pogoenhancer.overlay.elements.mapSettings.MapSettingsManager;
import com.mad.pogoenhancer.overlay.elements.nearby.NearbyListingManager;
import com.mad.pogoenhancer.overlay.elements.toastNotifications.IvLayoutManager;
import com.mad.shared.gpx.LatLon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import POGOProtos.Rpc.ClientMapCellProto;
import POGOProtos.Rpc.GetMapObjectsOutProto;


public class OverlayManager extends OverlayView implements View.OnClickListener, View.OnTouchListener {
    private final Handler _mainHandler;
    private ImageView overlayPositionalLock;
    private final boolean spoofingEnabled;
    protected CardView _viewContent;

    private LinearLayout _hidableChildren;
    private final LatLon _sharedLatLon;
    // managers of the fragments of the overlay
    private IvLayoutManager ivLayoutManager;
    private NearbyListingManager _NearbyListingManager;
    private IncidentListingManager _IncidentListingManager;
    private InjectionSettingsManager _InjectionSettingsManager;
    private MapSettingsManager _MapSettingsManager;
    private GymListingManager _GymListingManager;

    private JoystickManager _JoystickManager;
    private CooldownLayoutManager _CooldownLayoutManager;
    // key is encounter ID, value is timestamp of the time encountered
    private final Map<Long, Long> _AlreadyEncountered;


    public OverlayManager(Context context, LatLon sharedLatLon, boolean spoofingEnabled) {
        super(context);
        this.spoofingEnabled = spoofingEnabled;
        this._sharedLatLon = sharedLatLon;
        this._mainHandler = new Handler(Looper.getMainLooper());
        this._AlreadyEncountered = new HashMap<>();
    }

    private static Bitmap getBitmap(Drawable drawable) {
        Bitmap result;
        if (drawable instanceof BitmapDrawable) {
            result = ((BitmapDrawable) drawable).getBitmap();
        } else {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            // Some drawables have no intrinsic width - e.g. solid colours.
            if (width <= 0) {
                width = 1;
            }
            if (height <= 0) {
                height = 1;
            }

            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return result;
    }

    private static boolean areDrawablesIdentical(Drawable drawableA, Drawable drawableB) {
        Drawable.ConstantState stateA = drawableA.getConstantState();
        Drawable.ConstantState stateB = drawableB.getConstantState();
        // If the constant state is identical, they are using the same drawable resource.
        // However, the opposite is not necessarily true.
        return (stateA != null && stateA.equals(stateB))
                || getBitmap(drawableA).sameAs(getBitmap(drawableB));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isOverlayPositionLocked()) {
            return false;
        } else {
            return super.onTouch(v, event);
        }
    }

    @Override
    protected void specificCleanup() {
        // TODO: Subscriber pattern...
        if (this._NearbyListingManager != null) {
            this._NearbyListingManager.cleanup();
        }
        if (this._IncidentListingManager != null) {
            this._IncidentListingManager.cleanup();
        }
        if (this._CooldownLayoutManager != null) {
            this._CooldownLayoutManager.cleanup();
        }
        if (this._JoystickManager != null) {
            this._JoystickManager.cleanup();
        }
        if (this.ivLayoutManager != null) {
            this.ivLayoutManager.cleanup();
        }
        if (this._InjectionSettingsManager != null) {
            this._InjectionSettingsManager.cleanup();
        }
        if (this._MapSettingsManager != null) {
            this._MapSettingsManager.cleanup();
        }
        if (this._GymListingManager != null) {
            this._GymListingManager.cleanup();
        }
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getLayoutFlag(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        // TODO: restore from last run...
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_MAIN,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_X_MAIN
        );
        params.y = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_MAIN,
                Constants.DEFAULT_VALUES.LAST_OVERLAY_Y_MAIN
        );
        return params;
    }

    public LatLon getLocation() {
        return this._sharedLatLon;
    }

    public double getDistance(LatLon startPoint, LatLon endpoint) {
        return this._sharedLatLon.distanceetweenpoints(startPoint, endpoint);
    }

    @Override
    protected void specificSetup() {
        this._hidableChildren = this._enclosingLayout.findViewById(R.id.show_hide_layout_buttons);
        this._viewContent = this._enclosingLayout.findViewById(R.id.main_overlay_card);

        int overlayScaling = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SCALING,
                Constants.DEFAULT_VALUES.OVERLAY_SCALING
        );

        float dpFactor = this._context.getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams layoutParams = this._viewContent.getLayoutParams();
        int baseWidth = this.getBaseWidth();
        if (baseWidth > 0) {
            layoutParams.width = (int) (dpFactor * (this.getBaseWidth() + overlayScaling / 3));
        } else {
            layoutParams.width = baseWidth;
        }
        this._viewContent.setLayoutParams(layoutParams);


        this._moveButton.setOnClickListener(this);
        this._moveButton.setOnTouchListener(this);

        this.overlayPositionalLock = this._enclosingLayout.findViewById(R.id.overlay_position_lock);
        this.overlayPositionalLock.setOnClickListener(this);

        // TODO: DO NOT PASS _hidableChildren
        if (spoofingEnabled) {
            this._JoystickManager = new JoystickManager(this, this._context,
                    this._sharedLatLon);
            this._JoystickManager.setup();
            this._enclosingLayout.findViewById(R.id.overlay_show_joystick)
                    .setOnClickListener(v -> this._JoystickManager.toggleFragmentVisibility());

            this._enclosingLayout.findViewById(R.id.overlay_gpx_dialog)
                    .setOnClickListener(v -> this._JoystickManager.showGpxSelectDialog());

            this._enclosingLayout.findViewById(R.id.overlay_teleport_button)
                    .setOnClickListener(v -> this._JoystickManager.showTeleportToDialog());

            this._enclosingLayout.findViewById(R.id.overlay_speed_dialog)
                    .setOnClickListener(v -> this._JoystickManager.showSpeedAdjustmentDialog());

            this._enclosingLayout.findViewById(R.id.overlay_stop_walking)
                    .setOnClickListener(v -> {
                        _JoystickManager.cancelRoute();
                        _JoystickManager.cancelWalk();
                        this.showStopIcon(false);
                    });

            this._CooldownLayoutManager = new CooldownLayoutManager(this, this._context);
            this._CooldownLayoutManager.setup();
            this._enclosingLayout.findViewById(R.id.cooldown_dialog)
                    .setOnClickListener(v -> this._CooldownLayoutManager.toggleFragmentVisibility());
        } else {
            this._enclosingLayout.findViewById(R.id.overlay_show_joystick).setVisibility(View.GONE);
            this._enclosingLayout.findViewById(R.id.overlay_gpx_dialog).setVisibility(View.GONE);
            this._enclosingLayout.findViewById(R.id.overlay_teleport_button).setVisibility(View.GONE);
            this._enclosingLayout.findViewById(R.id.overlay_speed_dialog).setVisibility(View.GONE);
            this._enclosingLayout.findViewById(R.id.cooldown_dialog).setVisibility(View.GONE);
        }

        if (this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SHOW_IV_ELEMENT,
                Constants.DEFAULT_VALUES.OVERLAY_SHOW_IV_ELEMENT
        )) {
            this.ivLayoutManager = new IvLayoutManager(this, this._context);
            this.ivLayoutManager.setup();
        }

        this._NearbyListingManager = new NearbyListingManager(this._context,
                this._sharedLatLon, this);
        this._NearbyListingManager.setup();
        this._enclosingLayout.findViewById(R.id.overlay_show_nearby)
                .setOnClickListener(v -> this._NearbyListingManager.toggleFragmentVisibility());


        this._IncidentListingManager = new IncidentListingManager(this._context,
                this._sharedLatLon, this);
        this._IncidentListingManager.setup();

        this._enclosingLayout.findViewById(R.id.overlay_show_incident)
                .setOnClickListener(v -> this._IncidentListingManager.toggleFragmentVisibility());

        this._InjectionSettingsManager = new InjectionSettingsManager(this,
                this._context);
        this._InjectionSettingsManager.setup();

        this._MapSettingsManager = new MapSettingsManager(this,
                this._context);
        this._MapSettingsManager.setup();

        this._enclosingLayout.findViewById(R.id.overlay_show_injection_settings)
                .setOnClickListener(v -> this._InjectionSettingsManager.toggleFragmentVisibility());

        this._enclosingLayout.findViewById(R.id.overlay_show_map_settings)
                .setOnClickListener(v -> this._MapSettingsManager.toggleFragmentVisibility());

        this._GymListingManager = new GymListingManager(this._context,
                this._sharedLatLon, this);
        this._GymListingManager.setup();

        this._enclosingLayout.findViewById(R.id.overlay_show_gyms)
                .setOnClickListener(v -> this._GymListingManager.toggleFragmentVisibility());


        this._enclosingLayout.findViewById(R.id.show_hide_entire_overlay_button)
                .setOnClickListener(v -> toggleVisibilityOfView(this._hidableChildren));
        // Hide move elements of children
        toggleMoveButtonsOfFragments();
    }

    public void showInjectionSettings() {
        // Logger.info("PogoEnhancerJ", "Show Injection Settings");
        this._enclosingLayout.findViewById(R.id.overlay_show_injection_settings).setVisibility(View.VISIBLE);
        //this._enclosingLayout.findViewById(R.id.overlay_show_map_settings).setVisibility(View.VISIBLE);

    }

    public void toggleVisibilityOfAllElements() {
        if (this._enclosingLayout != null && this._enclosingLayout.getVisibility() == View.VISIBLE) {
            if (this._NearbyListingManager != null)
                this._NearbyListingManager.hideFragmentEntirely();
            if (this.ivLayoutManager != null) this.ivLayoutManager.hideFragmentEntirely();
            if (this._JoystickManager != null) this._JoystickManager.hideFragmentEntirely();
            if (this._CooldownLayoutManager != null)
                this._CooldownLayoutManager.hideFragmentEntirely();
            this._enclosingLayout.setVisibility(View.GONE);
        } else if (this._enclosingLayout != null) {
            // show them all..
            if (this._NearbyListingManager != null) this._NearbyListingManager.restoreVisibility();
            if (this.ivLayoutManager != null) this.ivLayoutManager.restoreVisibility();
            if (this._JoystickManager != null) this._JoystickManager.restoreVisibility();
            if (this._CooldownLayoutManager != null)
                this._CooldownLayoutManager.restoreVisibility();
            this._enclosingLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected @LayoutRes
    int getContentLayoutRes() {
        return R.layout.main_layout_overlay;
    }

    @Override
    protected int getEnclosingLayoutRes() {
        return R.layout.main_layout_overlay;
    }

    protected int getBaseWidth() {
        return 23;
    }

    @Override
    protected View getMoveButton() {
        return this._enclosingLayout.findViewById(R.id.overlay_toggle_drag);
    }

    @Override
    protected void storeLocation(int offsetX, int offsetY) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_X_MAIN, offsetX
        );
        edit.putInt(
                Constants.SHAREDPERFERENCES_KEYS.LAST_OVERLAY_Y_MAIN, offsetY
        );
        edit.apply();
    }

    private boolean isOverlayPositionLocked() {
        Drawable currentDisplayOfLock = this.overlayPositionalLock.getDrawable();
        Drawable lockedDisplay = this._context.getResources().getDrawable(R.drawable.ic_lock_white_24dp);
        return areDrawablesIdentical(currentDisplayOfLock, lockedDisplay);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.show_hide_entire_overlay_button) {
            this.toggleVisibilityOfView(_hidableChildren);
        } else if (viewId == R.id.overlay_position_lock) {
            if (!isOverlayPositionLocked()) {
                // currently unlocked, locking
                this.overlayPositionalLock.setImageDrawable(this._context.getResources().getDrawable(R.drawable.ic_lock_white_24dp));
            } else {
                this.overlayPositionalLock.setImageDrawable(this._context.getResources().getDrawable(R.drawable.ic_lock_open_white_24dp));
            }
        } else if (v == this._moveButton) {
            toggleMoveButtonsOfFragments();
        }

        // TODO: button clicks
    }

    private void toggleMoveButtonsOfFragments() {
        if (this._NearbyListingManager != null) {
            this._NearbyListingManager.toggleMoveButtonVisibility();
        }
        if (this._IncidentListingManager != null) {
            this._IncidentListingManager.toggleMoveButtonVisibility();
        }
        if (this._CooldownLayoutManager != null) {
            this._CooldownLayoutManager.toggleMoveButtonVisibility();
        }
        if (this._JoystickManager != null) {
            this._JoystickManager.toggleMoveButtonVisibility();
        }
        if (this.ivLayoutManager != null) {
            this.ivLayoutManager.toggleMoveButtonVisibility();
        }
        if (this._InjectionSettingsManager != null) {
            this._InjectionSettingsManager.toggleMoveButtonVisibility();
        }
        if (this._MapSettingsManager != null) {
            this._MapSettingsManager.toggleMoveButtonVisibility();
        }
        if (this._GymListingManager != null) {
            this._GymListingManager.toggleMoveButtonVisibility();
        }
    }

    public void setIv(int attack, int defence, int stamina, double cpMultiplier,
                      double additionalCpMultiplier, int monLvl, int shiny, int typ, int gender, int weather,
                      String weightXSXL, String heightXSXL, int ditto) {
        if (this.ivLayoutManager != null) {
            this.ivLayoutManager.setIv(attack, defence, stamina, cpMultiplier,
                    additionalCpMultiplier, monLvl, shiny, typ, gender, weather, weightXSXL,
                    heightXSXL, ditto);
        }
    }

    public void addAlreadyEncountered(long encounterId) {
        if (this._AlreadyEncountered != null) {
            synchronized (_AlreadyEncountered) {
                this._AlreadyEncountered.put(encounterId, System.currentTimeMillis());
            }
        }
    }

    public Map<Long, Long> getAlreadyEncountered() {
        return this._AlreadyEncountered;
    }

    private void cleanupAlreadyEncountered() {
        // remove all items that have been in there for more than 1800seconds
        synchronized (_AlreadyEncountered) {
            long currentTimestamp = System.currentTimeMillis();
            Iterator it = _AlreadyEncountered.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                long timestamp = (long) pair.getValue() + 3600 * 1000;
                if (currentTimestamp > timestamp) {
                    it.remove(); // avoids a ConcurrentModificationException
                }
                //System.out.println(pair.getKey() + " = " + pair.getValue());
            }
        }
    }

    public void updateDataset(GetMapObjectsOutProto gmo) {
        if (!this.isGmoValid(gmo)) {
            return;
        }
        if (this._NearbyListingManager != null) {
            this.cleanupAlreadyEncountered();
            this._NearbyListingManager.updateDataset(gmo);
        }
        if (this._IncidentListingManager != null) {
            this._IncidentListingManager.updateDataset(gmo);
        }
        if (this._GymListingManager != null) {
            this._GymListingManager.updateDataset(gmo);
        }
    }

    private int lastCellCount = 0;

    private boolean isGmoValid(GetMapObjectsOutProto gmo) {
        if (gmo.getStatus() != GetMapObjectsOutProto.Status.SUCCESS) {
            return false;
        }
        int currentCellCount = gmo.getMapCellCount();
        boolean validityToBeReturned = lastCellCount <= currentCellCount;

        int fortsPresent = 0;
        int nearbyMonsPresent = 0;
        int wildMonsPresent = 0;
        for (ClientMapCellProto mapCell : gmo.getMapCellList()) {
            fortsPresent += mapCell.getFortCount();
            nearbyMonsPresent += mapCell.getNearbyPokemonCount();
            wildMonsPresent += mapCell.getWildPokemonCount();
        }

        //return fortsPresent > 0 && nearbyMonsPresent > 0 || wildMonsPresent > 0;
        //return gmo.getMapCellsList().size() > 6 && (wildMonsPresent > 0 || nearbyMonsPresent > 2);
        lastCellCount = currentCellCount;
        return validityToBeReturned;
    }

    public void walkToDest(LatLon location) {
        if (this._JoystickManager != null) {
            this._JoystickManager.walkToDest(location, true);
        }
        this.showStopIcon(true);
        // TODO: else show dialog with error/copy of location or just direction?
    }

    public void cancelGpxWalk() {
        // call this everytime the walk is supposed to be stopped in order to make sure we aren't running a GPX route...
        this._JoystickManager.cancelRoute();
    }

    private boolean isWalkOngoing() {
        return this._JoystickManager != null && this._JoystickManager.isWalkOngoing();
    }

    public void setCooldown(long timeTeleported, double distanceTravelledKm) {
        if (this._CooldownLayoutManager != null) {
            this._CooldownLayoutManager.updateView(timeTeleported, (float) distanceTravelledKm);
        }
    }

    public void setCooldownText() {
        if (this._CooldownLayoutManager != null) {

            double lat = this._sharedLatLon.getLat();
            double lng = this._sharedLatLon.getLon();

            String location = lat + "/" + lng;

            this._CooldownLayoutManager.updateText(location);
        }
    }

    public void showStopIcon(boolean show) {
        this._mainHandler.post(() -> {
            View stopIcon = this._enclosingLayout.findViewById(R.id.overlay_stop_walking);
            if (show) {
                stopIcon.setVisibility(View.VISIBLE);
            } else {
                stopIcon.setVisibility(View.GONE);
            }
        });
    }
}
