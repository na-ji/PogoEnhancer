package com.mad.pogoenhancer.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.services.HookReceiverService;
import com.mad.pogoenhancer.utils.AppUpdater;
import com.mad.pogoenhancer.utils.Injector;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment {
    private FusedLocationProviderClient _FusedLocationClient;

    private HomeViewModel _homeViewModel;

    private View root = null;
    private final Handler textUpdater = new Handler(Looper.getMainLooper());

    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (_homeViewModel != null && _sharedPreferences != null) {
                    _homeViewModel.getCooldownText(_sharedPreferences);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
            textUpdater.postDelayed(textRunnable, 5000);
        }
    };

    private SharedPreferences _sharedPreferences;

    @Override
    public void onPause() {
        Logger.debug("PogoEnhancerJ", "Pausing main activity");
        super.onPause();
        textUpdater.removeCallbacks(textRunnable);
    }

    @Override
    public void onDestroy() {
        Logger.debug("PogoEnhancerJ", "Destroying main activity");
        super.onDestroy();
        textUpdater.removeCallbacks(textRunnable);
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this._sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        _homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        this.setup();
        return root;
    }

    private void setup() {
        this._homeViewModel.updateShownPogoVersion(this.getContext());
        setDeviceIdText();
        Injector.getInstance().setupInstance(this.getContext(), this._sharedPreferences);
        setCurrentVersionText();
        setLatestKnownVersionText();
        setPogoVersionInstalledText();
        setCurrentPogoVersionSupportedText();
        setCooldownText();
        setupButtons();
        setupRadioButtons();

        //Disable PE+ features
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putBoolean(Constants.SHAREDPERFERENCES_KEYS.ENABLE_PEPLUS_AUTOSPIN, false);
        edit.putBoolean(Constants.SHAREDPERFERENCES_KEYS.ENABLE_PEPLUS_AUTOCATCH, false);
        edit.putBoolean(Constants.SHAREDPERFERENCES_KEYS.ENABLE_PEPLUS_AUTOFEED, false);
        edit.apply();

        // TODO: functionality for start/stop buttons as well as location/spoofing related buttons
    }

    private void setLatestKnownVersionText() {
        final TextView latestVersionText = root.findViewById(R.id.text_latest_version_insert);
        final TextView currentlyInstalledVersion = root.findViewById(R.id.text_current_version_insert);
        _homeViewModel.getLatestVersionText().observe(getViewLifecycleOwner(), s -> {
            if (s == null) {
                return;
            }
            s = s.trim();
            latestVersionText.setText(s);
            if (!s.equals(currentlyInstalledVersion.getText().toString().trim())) {
                latestVersionText.setTextColor(getResources().getColor(R.color.colorRedVersionMismatch));
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this.getContext());

                alertDialogBuilder.setTitle("Update available");

                // set dialog message
                alertDialogBuilder
                        .setMessage("New version available. Do you want to update PogoEnhancer? " +
                                "Please check Discord for the changes and if your installed " +
                                "version of pogo is supported.")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> AppUpdater.updatePogoEnhancer(this.getActivity(), this.getContext()))
                        .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            } else {
                latestVersionText.setTextColor(Color.BLACK);
            }
        });
    }

    private void setCooldownText() {
        final TextView cooldownText = root.findViewById(R.id.cooldown_remaining_minutes);
        _homeViewModel.getCooldownText(_sharedPreferences).observe(getViewLifecycleOwner(), cooldownText::setText);
    }

    private void setCurrentVersionText() {
        final TextView currentVersionText = root.findViewById(R.id.text_current_version_insert);
        _homeViewModel.getCurrentVersionInstalledText().observe(getViewLifecycleOwner(), currentVersionText::setText);
    }

    private void setDeviceIdText() {
        String deviceId = this._sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID,
                Constants.DEFAULT_VALUES.DEFAULT_DEVICE_ID);
        if (deviceId != null) {
            ((TextView) root.findViewById(R.id.text_device_id_insert)).setText(deviceId);
        }
    }

    private void setPogoVersionInstalledText() {
        final TextView pogoVersionInstalledText = root.findViewById(R.id.text_pogo_version_installed_insert);
        _homeViewModel.getPogoVersionInstalledText().observe(getViewLifecycleOwner(), pogoVersionInstalledText::setText);
    }

    private void setCurrentPogoVersionSupportedText() {
        final TextView currentPogoVersionSupportedText = root.findViewById(R.id.text_current_pogo_supported_insert);
        final TextView currentlyInstalledVersion = root.findViewById(R.id.text_pogo_version_installed_insert);
        _homeViewModel.getPogoVersionSupportedText().observe(getViewLifecycleOwner(), s -> {
            if (s == null) {
                return;
            }
            s = s.trim();
            currentPogoVersionSupportedText.setText(s);
            if (!s.equals(currentlyInstalledVersion.getText().toString().trim())) {
                currentPogoVersionSupportedText.setTextColor(getResources().getColor(R.color.colorRedVersionMismatch));
            } else {
                currentPogoVersionSupportedText.setTextColor(Color.BLACK);
            }
        });
    }

    private void setupButtons() {
        final Button startButton = this.root.findViewById(R.id.start_listener_button);
        startButton.setOnClickListener(this::startAction);

        final Button stopButton = this.root.findViewById(R.id.stop_listener_button);
        stopButton.setOnClickListener(this::stopAction);

        final Button setLocation = this.root.findViewById(R.id.button_set_location);
        setLocation.setOnClickListener(this::setLocation);
    }

    @Override
    public void onResume() {
        super.onResume();
        textUpdater.post(textRunnable);
        checkLocationIntentContent();
    }

    private void startAction(View v) {
        if (!this._homeViewModel.startInjection(v.getContext())) {
            return;
        }

        SharedPreferences.Editor editor = this._sharedPreferences.edit();
        editor.putBoolean(
                Constants.SHAREDPERFERENCES_KEYS.INTENTIONAL_STOP,
                false
        );
        editor.apply();

    }

    private void stopAction(View v) {
        SharedPreferences.Editor editor = this._sharedPreferences.edit();
        editor.putBoolean(
                Constants.SHAREDPERFERENCES_KEYS.INTENTIONAL_STOP,
                true
        );
        editor.apply();
        this._homeViewModel.stopInjection(v.getContext());
    }

    private void setLocation(View v) {
        if (this.getContext() == null || this.getActivity() == null) {
            return;
        }
        Context context = this.getContext();

        EditText inputField = this.root.findViewById(R.id.new_lat_lon_input);
        String userInput = inputField.getText().toString().trim();
        Pattern userInputValidationPattern = Pattern.compile("^(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+\\.\\d+)(.|\\s)*");
        Matcher matcher = userInputValidationPattern.matcher(userInput);
        Logger.info("PogoEnhancerJ", "Set location called with " + userInput);
        if (!matcher.matches()) {
            // invalid input
            invalidCoordInputInformationDialog(context);
            return;
        }
        String latStr = matcher.group(1);
        String lonStr = matcher.group(2);


        Intent serviceIntent = new Intent(this.getContext(), HookReceiverService.class);

        try {
            serviceIntent.putExtra("latitude", Float.valueOf(latStr));
            serviceIntent.putExtra("longitude", Float.valueOf(lonStr));
        } catch (NumberFormatException e) {
            invalidCoordInputInformationDialog(context);
            return;
        }

        serviceIntent.putExtra("DONT_START", true);
        context.startService(serviceIntent);
    }

    private void invalidCoordInputInformationDialog(Context context) {
        if (this.getActivity() != null) {
            this.getActivity().runOnUiThread(() -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("Invalid input");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Invalid input/format. " +
                                "Please enter a location such as 48.858631, 2.293021")
                        .setCancelable(false)
                        .setNegativeButton("OK", (dialog, id) -> dialog.dismiss());
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            });
        }
    }


    @SuppressLint("MissingPermission")
    private void setupRadioButtons() {
        Context ctx = this.getContext();
        if (ctx != null) {
            _FusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);
        }

        RadioGroup derp = root.findViewById(R.id.radio_group_location_setting);
        ((RadioButton) root.findViewById(R.id.radio_button_last_location_used)).setChecked(true);
        derp.setOnCheckedChangeListener((group, checkedId) -> {
            // first check if the custom location has been selected and show/hide the userinput
            // of it accordingly
            if (checkedId == R.id.radio_button_new_location) {
                // TODO: set last used location
                root.findViewById(R.id.custom_location_input_layout).setVisibility(View.VISIBLE);
                double latitude, longitude;

                latitude = Double.longBitsToDouble(
                        _sharedPreferences.getLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LATITUDE,
                                Constants.DEFAULT_VALUES.LAST_LOCATION_LATITUDE
                        )
                );
                longitude = Double.longBitsToDouble(
                        _sharedPreferences.getLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LONGITUDE,
                                Constants.DEFAULT_VALUES.LAST_LOCATION_LONGITUDE
                        )
                );


                String inputText = String.format(Locale.ENGLISH, "%.5g, %.5g", latitude,
                        longitude);
                ((EditText) root.findViewById(R.id.new_lat_lon_input)).setText(inputText);
            } else if (checkedId == R.id.radio_button_current_location) {
                if (_FusedLocationClient == null) {
                    String inputText = "Unable to fetch location.";
                    ((EditText) root.findViewById(R.id.new_lat_lon_input)).setText(inputText);
                } else {
                    _FusedLocationClient.getLastLocation()
                            .addOnSuccessListener(location -> {
                                if (location != null) {
                                    root.findViewById(R.id.custom_location_input_layout).setVisibility(View.VISIBLE);
                                    double latitude, longitude;

                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    String inputText = String.format(Locale.ENGLISH, "%.5g, %.5g", latitude,
                                            longitude);
                                    ((EditText) root.findViewById(R.id.new_lat_lon_input)).setText(inputText);
                                }
                            });
                }
            } else {
                root.findViewById(R.id.custom_location_input_layout).setVisibility(View.GONE);
            }
        });
    }

    private void checkLocationIntentContent() {
        // TODO: needs fixing... not working at all
        Bundle bundle = this.getArguments();
        if (bundle == null) {
            Logger.info("PogoEnhancerJ", "No lat,lon in bundle");
            return;
        }

        float latRcv = bundle.getFloat("lat", 0.0f);
        float lngRcv = bundle.getFloat("lon", 0.0f);

        if (latRcv != 0.0 || lngRcv != 0.0) {
            Logger.info("PogoEnhancerJ", "New lat/lon via intent");
            ((RadioButton) root.findViewById(R.id.radio_button_new_location)).setChecked(true);
            root.findViewById(R.id.custom_location_input_layout).setVisibility(View.VISIBLE);

            String inputText = String.format(Locale.ENGLISH, "%.5g, %.5g", latRcv,
                    lngRcv);
            ((EditText) root.findViewById(R.id.new_lat_lon_input)).setText(inputText);
        } else {
            Logger.info("PogoEnhancerJ", "No data via intent");
            ((RadioButton) root.findViewById(R.id.radio_button_last_location_used)).setChecked(true);
            root.findViewById(R.id.custom_location_input_layout).setVisibility(View.GONE);
        }

    }

}