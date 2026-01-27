package com.mad.pogoenhancer.overlay.elements.joystick;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.gpx.GpxManager;
import com.mad.pogoenhancer.gpx.GpxUtil;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.shared.gpx.CourseDistance;
import com.mad.shared.gpx.LatLon;

import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import io.ticofab.androidgpxparser.parser.domain.Gpx;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class OverlayJoystickListener implements JoystickView.OnMoveListener, View.OnClickListener {

    private final Context _Context;
    private final CourseDistance _CourseDistance;
    private final LatLon _SharedLatLon;
    private final SharedPreferences _SharedPreferences;
    private final TextView _CurrentLocationTextView;
    private final TextView _CurrentSpeedTextView;
    private ImageView _TeleportToButton;
    private final ImageView _AbortAutowalkButton;
    private final JoystickManager _JoystickManager;
    private final Handler _MainHandler;
    private ImageView _gpxSelectionButton;

    private final RecyclerView _gpxSelectionListingView;
    private final GpxListingAdapter _gpxSelectionListingAdapter;
    private final RecyclerView.LayoutManager _layoutManager;
    private final OverlayManager _OverlayManagerParent;
    private AlertDialog _gpxSelectionDialog = null;
    private final JoystickView _joystick;
    private final ImageView _DisableCenteringLock;

    private final LayoutInflater li;

    public OverlayManager getOverlayManager() {
        return this._OverlayManagerParent;
    }

    private class LockedMovingTask extends Thread {
        private int angle = 0;
        private int strength = 0;

        LockedMovingTask() {

        }

        public synchronized void setAngleAndStrength(int angle, int strength) {
            this.angle = angle;
            this.strength = strength;
        }

        @Override
        public void run() {
            super.run();
            while (!this.isInterrupted()) {
                synchronized (this) {
                    while (_joystick.isAutoReCenterButton()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    moveInDirection(angle, strength);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private final LockedMovingTask movingTask = new LockedMovingTask();

    OverlayJoystickListener(OverlayManager parent, Context context, LatLon sharedLatLon, View wrappingContainer,
                            JoystickManager supervisingManager, GpxRouteHandler _gpxRouteHandler) {
        this._Context = context;
        this._SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this._OverlayManagerParent = parent;
        this._SharedLatLon = sharedLatLon;
        this._CourseDistance = new CourseDistance(0.0, 0);
        this._JoystickManager = supervisingManager;
        this._MainHandler = new Handler(Looper.getMainLooper());

        this._CurrentLocationTextView = wrappingContainer.findViewById(R.id.current_location_text);
        this._CurrentSpeedTextView = wrappingContainer.findViewById(R.id.current_speed_text);

        float dpFactor = context.getResources().getDisplayMetrics().density;
        int overlayScaling = this._SharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SCALING,
                Constants.DEFAULT_VALUES.OVERLAY_SCALING
        );

        _joystick = wrappingContainer.findViewById(R.id.joystick);
        this._AbortAutowalkButton = wrappingContainer.findViewById(R.id.stop_autowalk_image);

        _joystick.setOnMoveListener(this);
        this._AbortAutowalkButton.setOnClickListener(v -> {
            _JoystickManager.cancelRoute();
            _JoystickManager.cancelWalk();
        });

        _DisableCenteringLock = wrappingContainer.findViewById(R.id.lock_centering);
        _DisableCenteringLock.setOnClickListener(this);

        li = (LayoutInflater) this._Context.getSystemService(LAYOUT_INFLATER_SERVICE);
        _gpxSelectionListingView = (RecyclerView) li.inflate(R.layout.gpx_selection_listing, null);
        _gpxSelectionListingView.setHasFixedSize(true);
        _layoutManager = new LinearLayoutManager(context);
        _gpxSelectionListingView.setLayoutManager(_layoutManager);

        // specify an adapter (see also next example)
        _gpxSelectionListingAdapter = new GpxListingAdapter(context, _SharedLatLon, _gpxRouteHandler, this);
        _gpxSelectionListingView.setAdapter(_gpxSelectionListingAdapter);
        this.movingTask.start();
    }

    public void destroy() throws InterruptedException {
        this.movingTask.interrupt();
        this.movingTask.join();
    }

    public void showSpeedAdjustmentDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this._Context);
        alert.setTitle("Set speed in km/h");
        final EditText input = new EditText(this._Context);
        String currentSpeedSetting = this._SharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.SPOOF_SPEED_RUN_KMPH,
                Constants.DEFAULT_VALUES.SPOOF_SPEED_RUN_KMPH
        );
        input.setText(currentSpeedSetting);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);

        alert.setView(input);
        alert.setPositiveButton("Ok", (dialog, which) -> {
            String s = input.getText().toString();
            double speed = 0.0d;
            try {
                speed = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                Toast.makeText(this._Context,
                        "Invalid number for speed setting",
                        Toast.LENGTH_LONG).show();
                dialog.dismiss();
                return;
            }
            SharedPreferences.Editor edit = this._SharedPreferences.edit();
            edit.putString(Constants.SHAREDPERFERENCES_KEYS.SPOOF_SPEED_RUN_KMPH,
                    String.valueOf(speed));
            edit.apply();
            dialog.dismiss();
        });
        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = alert.create();
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        alertDialog.getWindow().setType(LAYOUT_FLAG);
        alertDialog.show();
    }

    void closeGpxSelectionDialog() {
        if (this._gpxSelectionDialog != null && this._gpxSelectionDialog.isShowing()) {
            this._gpxSelectionDialog.dismiss();
        }
    }

    public void onClickGpxSelectionButton() {
        _gpxSelectionListingAdapter.updateDataset();
        if (_gpxSelectionDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this._Context);
            builder.setTitle("Select a route / location");

            builder.setView(_gpxSelectionListingView);
            builder.setPositiveButton("Add current location", (dialog, which) -> {
                showAddCurrentLocationAsFavouriteDialog();
            });
            builder.setNegativeButton("Close", (dialog, which) -> dialog.cancel());

            _gpxSelectionDialog = builder.create();
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            _gpxSelectionDialog.getWindow().setType(LAYOUT_FLAG);
        }

        _gpxSelectionDialog.show();
    }

    private AlertDialog addCurrentLocatioNAsFavouriteDialog = null;

    private void showAddCurrentLocationAsFavouriteDialog() {
        if (addCurrentLocatioNAsFavouriteDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this._Context);
            builder.setTitle("Add favourite location");

            LinearLayout addLocationDialogInput = (LinearLayout) li.inflate(R.layout.overlay_add_current_location_dialog, null);

            EditText nameOfNewLocation = addLocationDialogInput.findViewById(R.id.overlay_add_current_location_dialog_name_input);

            builder.setView(addLocationDialogInput);

            builder.setPositiveButton("Save", (dialog, which) -> {
                this.addCurrentLocationAsFavourite(nameOfNewLocation.getText().toString());
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            addCurrentLocatioNAsFavouriteDialog = builder.create();
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            addCurrentLocatioNAsFavouriteDialog.getWindow().setType(LAYOUT_FLAG);
        }


        addCurrentLocatioNAsFavouriteDialog.show();
    }

    private void addCurrentLocationAsFavourite(String name) {
        if (name == null || name.isEmpty()) {
            Toast.makeText(this._Context,
                    "No Location Name is set",
                    Toast.LENGTH_LONG).show();
            return;
        }

        LatLon location = new LatLon(_SharedLatLon.getLat(), _SharedLatLon.getLon());

        File externalStorage = Environment.getExternalStorageDirectory();
        String tmpFile = externalStorage.getAbsolutePath() + "/tmpgpx.gpx";
        Uri tmpFileUri = Uri.parse("file:///" + tmpFile);
        File tmpGpxFile = new File(tmpFile);
        GpxManager gpxManager = GpxManager.getInstance(this._Context);
        if (GpxUtil.generateGfx(tmpGpxFile, name, location)) {
            Gpx gpxRead = GpxUtil.readToGpx(this._Context, tmpFileUri);
            gpxManager.addRoute(name, gpxRead);
            this._gpxSelectionListingAdapter.updateDataset();
            Toast.makeText(this._Context,
                    "Location added",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this._Context,
                    "Something went wrong",
                    Toast.LENGTH_LONG).show();
        }
        tmpGpxFile.delete();
    }

    @Override
    public void onMove(int angle, int strength) {
        // TODO: If locked (no centering) we need to continue this until unlocked/moved...
        if (!_joystick.isAutoReCenterButton()) {
            // Remove old refreshing and/or setup refreshing of the same command in a loop...
            this.movingTask.setAngleAndStrength(angle, strength);
        } else {
            moveInDirection(angle, strength);
        }
    }

    private void moveInDirection(int angle, int strength) {
        if (strength == 0) {
            return;
        }
        int newAngle = rotateAngleToBearing(angle);
        this._CourseDistance.setCourse(newAngle);
        // we run 20 updates per second currently
        String speedInKmph = this._SharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.SPOOF_SPEED_RUN_KMPH,
                Constants.DEFAULT_VALUES.SPOOF_SPEED_RUN_KMPH
        );

        double metersPerSecond = Float.valueOf(speedInKmph) / 3.6f;
        metersPerSecond = strength / 100.0f * metersPerSecond;

        this.setCurrentSpeedText((int) Math.round(metersPerSecond * 3.6));

        double distanceTravel = 1.0 / 20.0 * metersPerSecond;
        this._CourseDistance.setDistance(distanceTravel);

        this._SharedLatLon.add(this._CourseDistance);
        this._SharedLatLon.setSpeed((float) metersPerSecond);

        if (this._CurrentLocationTextView != null) {
            double lat = this._SharedLatLon.getLat();
            double lon = this._SharedLatLon.getLon();

            setCurrentLocationText(lat, lon);
        }
    }

    void setCurrentLocationText(double lat, double lon) {
        if (this._CurrentLocationTextView == null) {
            return;
        }
        String newLocationText = String.format(Locale.ENGLISH, "%.5g, %.5g", lat, lon);

        this._MainHandler.post(() -> _CurrentLocationTextView.setText(newLocationText));
    }

    void setCurrentSpeedText(int speed_kmph) {
        if (speed_kmph < 0 || this._CurrentSpeedTextView == null) {
            return;
        }
        this._MainHandler.post(() -> _CurrentSpeedTextView.setText(String.format(Locale.ENGLISH, "%d km/h", speed_kmph)));
    }


    private int rotateAngleToBearing(int angle) {
        // the angle given has 90degrees when it's north (0 degrees)
        int bearing = angle - 90;
        if (bearing < 180) {
            bearing = -bearing;
        } else if (bearing > 180) {
            // get difference to 180 and invert it
//            int diff = bearing - 180;
            bearing = 180 - (bearing - 180);

        }
        return bearing;
    }

    @Override
    public void onClick(View v) {
        if (this._TeleportToButton != null && v == this._TeleportToButton) {
            this.showTeleportToDialog();
        } else if (this._DisableCenteringLock != null && v == this._DisableCenteringLock) {
            _joystick.setAutoReCenterButton(!_joystick.isAutoReCenterButton());
            if (_joystick.isAutoReCenterButton()) {
                // currently unlocked, locking
                this._DisableCenteringLock.setImageDrawable(this._Context.getResources().getDrawable(R.drawable.ic_lock_white_24dp));
                //this.movingTask.interrupt();
            } else {
                this._DisableCenteringLock.setImageDrawable(this._Context.getResources().getDrawable(R.drawable.ic_lock_open_white_24dp));
                /*if (this.movingTask.isAlive()) {
                    this.movingTask.interrupt();
                }
                try {
                    this.movingTask.join(1000);
                } catch (InterruptedException e) {
                    Logger.fatal("PogoEnhancerJ", "Failed joining moving task");
                }
                this.movingTask = new LockedMovingTask();
                this.movingTask.start();*/
                synchronized (this.movingTask) {
                    this.movingTask.notifyAll();
                }
            }
        }
    }

    private AlertDialog teleportDialog = null;

    public void showTeleportToDialog() {
        if (teleportDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this._Context);
            builder.setTitle("Change Location");

            LinearLayout teleportOverlayDialogInput = (LinearLayout) li.inflate(R.layout.overlay_teleport_inputs, null);

            EditText teleportOverlayDialogInputText = teleportOverlayDialogInput.findViewById(R.id.overlay_teleport_dialog_lat_lon_input);

            final Button pasteButton = teleportOverlayDialogInput.findViewById(R.id.overlay_teleport_dialog_paste);
            pasteButton.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) _Context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.getPrimaryClipDescription() != null
                        && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)
                        && clipboard.getPrimaryClip() != null) {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    String yourText = item.getText().toString();
                    teleportOverlayDialogInputText.setText(yourText);
                }
            });
//        pasteButton.setGravity(Gravity.END);

            builder.setView(teleportOverlayDialogInput);

            builder.setPositiveButton("Teleport", (dialog, which) -> {
                String toLocation = teleportOverlayDialogInputText.getText().toString();
                setLocationFromOverlayDialog(toLocation);
                dialog.dismiss();
            });
            builder.setNeutralButton("Walk", ((dialog, which) -> {
                String toLocation = teleportOverlayDialogInputText.getText().toString();
                walkToLocationFromOverlay(toLocation);
                dialog.dismiss();
            }));
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            teleportDialog = builder.create();
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            teleportDialog.getWindow().setType(LAYOUT_FLAG);
        }


        teleportDialog.show();
    }

    private void walkToLocationFromOverlay(String toLocation) {
        String userInput = toLocation.trim();
        Pattern userInputValidationPattern = Pattern.compile("(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+.\\d+)(.|\\s)*");
        Matcher matcher = userInputValidationPattern.matcher(userInput);
        if (!matcher.find()) {
            // invalid input
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this._Context);
            alertDialogBuilder.setTitle("Invalid input");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Invalid input/format. " +
                            "Please enter a location such as 48.858631, 2.293021")
                    .setCancelable(false)
                    .setNegativeButton("OK", (dialog, id) -> dialog.dismiss());
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            alertDialog.getWindow().setType(LAYOUT_FLAG);
            // show it
            alertDialog.show();
        } else {
            double lat = Double.parseDouble(matcher.group(1));
            double lon = Double.parseDouble(matcher.group(2));

            long now = System.currentTimeMillis();
            double distance = this._SharedLatLon.distance(new LatLon(lat, lon)) / 1000;

            // TODO: estimate duration of walk?
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this._Context);
            alertDialogBuilder.setTitle("Walk");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Distance (metres): " + distance)
                    .setCancelable(false)
                    .setPositiveButton("Walk", ((dialog, id) -> {
                        _OverlayManagerParent.cancelGpxWalk();
                        _OverlayManagerParent.walkToDest(new LatLon(lat, lon));
                    }))
                    .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            alertDialog.getWindow().setType(LAYOUT_FLAG);
            // show it
            alertDialog.show();
        }
    }

    private void setLocationFromOverlayDialog(String input) {
        String userInput = input.trim();
        Pattern userInputValidationPattern = Pattern.compile("(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+.\\d+)(.|\\s)*");
        Matcher matcher = userInputValidationPattern.matcher(userInput);
        if (!matcher.find()) {
            // invalid input
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this._Context);
            alertDialogBuilder.setTitle("Invalid input");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Invalid input/format. " +
                            "Please enter a location such as 48.858631, 2.293021")
                    .setCancelable(false)
                    .setNegativeButton("OK", (dialog, id) -> dialog.dismiss());
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            alertDialog.getWindow().setType(LAYOUT_FLAG);
            // show it
            alertDialog.show();
        } else {
            double lat = Double.parseDouble(matcher.group(1));
            double lon = Double.parseDouble(matcher.group(2));

            long now = System.currentTimeMillis();
            double distance = this._SharedLatLon.distance(new LatLon(lat, lon)) / 1000;
            long cooldownInMinutes = Constants.calculateRemainingCooldown(now / 1000, distance) / 60;

            String teleport_default_option = _SharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.TELEPORT_DEFAULT_OPTION,
                    Constants.DEFAULT_VALUES.TELEPORT_DEFAULT_OPTION
            );

            if (teleport_default_option.equals("Always with cooldown timer")) {
                teleport(lat, lon);
                this._OverlayManagerParent.setCooldown(-1, distance);
                return;
            } else if (teleport_default_option.equals("Always without cooldown timer")) {
                teleport(lat, lon);
                return;
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this._Context);
            alertDialogBuilder.setTitle("Cooldown");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Estimated cooldown (minutes): "
                            + cooldownInMinutes
                            + " (" + Constants.cooldownOverAt(now / 1000, distance) + ")")
                    .setCancelable(false)
                    .setPositiveButton("Teleport", ((dialog, id) -> {
                        teleport(lat, lon);
                        this._OverlayManagerParent.setCooldown(-1, distance);
                    }))
                    .setNeutralButton("Teleport without timer", ((dialog, id) -> {
                        teleport(lat, lon);
                    }))
                    .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            alertDialog.getWindow().setType(LAYOUT_FLAG);
            // show it
            alertDialog.show();
        }
    }

    private void teleport(double lat, double lng) {
        _SharedLatLon.setLat(lat);
        _SharedLatLon.setLon(lng);
        _JoystickManager.cancelRoute();
        _JoystickManager.cancelWalk();
    }

    synchronized void showAutowalkAbortButton() {
        if (this._AbortAutowalkButton != null
                && this._AbortAutowalkButton.getVisibility() != View.VISIBLE) {
            this._MainHandler.post(() -> this._AbortAutowalkButton.setVisibility(View.VISIBLE));
        }
    }

    synchronized void hideAutowalkAbortButton() {
        if (this._AbortAutowalkButton != null
                && this._AbortAutowalkButton.getVisibility() != View.GONE) {
            this._MainHandler.post(() -> this._AbortAutowalkButton.setVisibility(View.GONE));

        }
    }
}