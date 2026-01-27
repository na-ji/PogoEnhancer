package com.mad.shared.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.mad.shared.utils.RandomGenerator;
import com.mad.shared.utils.Runlevel;
import com.mad.shared.utils.ShellHelper;

import java.util.ArrayList;


public class LocationMockProvider implements ILocationProvider {
    private static final String TAG = "MAD";
    private LocationManager mLocationManager = null;
    private Context _Context;
    private SharedPreferences mSharedPreferences;
    private SpoofingConfiguration _spoofingConfiguration;
    private RandomGenerator _randomGenerator = null;

    LocationMockProvider(Context context, LocationManager locationManager,
                         SpoofingConfiguration spoofingConfiguration) {
        this._Context = context;
        updateMockLocationPermission();
        this.mLocationManager = locationManager;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this._Context);
        this._spoofingConfiguration = spoofingConfiguration;
        this._randomGenerator = new RandomGenerator();
        setup();
    }

    private void setup() {
        if (this.mLocationManager == null) {
            Log.e(TAG,"Could not setup spoofing due to missing location manager");
            Toast.makeText(this._Context, "Error setting up mocking, a reboot may be required", Toast.LENGTH_LONG).show();
            return;
        }
        if (_spoofingConfiguration.overwriteMode != LocationOverwriteMode.INDIRECT) {
            try {
                this.mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, //requiresNetwork,
                        true, // requiresSatellite,
                        false, // requiresCell,
                        false, // hasMonetaryCost,
                        true, // supportsAltitude,s
                        true,
                        false, // supportsBearing,
                        Criteria.POWER_LOW, // powerRequirement
                        Criteria.ACCURACY_FINE); // accuracy
                if (!this.mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    this.mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG,"Could not set GPS provider");
                Toast.makeText(this._Context, "Error setting mock providers (0), a reboot may be required", Toast.LENGTH_LONG).show();
            }
        }

        if (_spoofingConfiguration.overwriteMode == LocationOverwriteMode.COMMON
            || _spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL
            || _spoofingConfiguration.overwriteMode == LocationOverwriteMode.INDIRECT) {
            try {
                int accuracyToUse = Build.VERSION.SDK_INT > Build.VERSION_CODES.P ? Criteria.ACCURACY_FINE : Criteria.ACCURACY_HIGH;
                this.mLocationManager.addTestProvider(LocationManager.NETWORK_PROVIDER, true, //requiresNetwork,
                        false, // requiresSatellite,
                        true, // requiresCell,
                        false, // hasMonetaryCost,
                        true, // supportsAltitude,
                        true,
                        false, // supportsBearing,
                        Criteria.POWER_MEDIUM, // powerRequirement
                        accuracyToUse); // accuracy
                if (!this.mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    this.mLocationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG,"Could not set Network provider: " + e.toString());
                Toast.makeText(this._Context, "Error setting mock providers (1), a reboot may be required", Toast.LENGTH_LONG).show();
            }
        }
        if (_spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL) {

            try {
                this.mLocationManager.addTestProvider(LocationManager.PASSIVE_PROVIDER, false, //requiresNetwork,
                        false, // requiresSatellite,
                        false, // requiresCell,
                        false, // hasMonetaryCost,
                        true, // supportsAltitude,
                        true,
                        true, // supportsBearing,
                        Criteria.POWER_LOW, // powerRequirement
                        Criteria.ACCURACY_LOW); // accuracy
                if (!this.mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                    this.mLocationManager.setTestProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG,"Could not set Passive provider");
                Toast.makeText(this._Context, "Error setting mock providers, a reboot may be required", Toast.LENGTH_LONG).show();
            }

        }
        if (this._spoofingConfiguration.overwriteFused) {
            try {
                this.mLocationManager.addTestProvider(LocationManager.FUSED_PROVIDER, false, //requiresNetwork,
                        false, // requiresSatellite,
                        true, // requiresCell,
                        false, // hasMonetaryCost,
                        true, // supportsAltitude,
                        true,
                        false, // supportsBearing,
                        Criteria.POWER_LOW, // powerRequirement
                        Criteria.ACCURACY_LOW); // accuracy
                if (!this.mLocationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)) {
                    this.mLocationManager.setTestProviderEnabled(LocationManager.FUSED_PROVIDER, true);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG,"Could not set fused provider");
                Toast.makeText(this._Context, "Error setting mock providers, a reboot may be required", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateMockLocationPermission() {
        //adb shell appops set <PACKAGE> android:mock_location deny
        String packageName = this._Context.getPackageName();
        ShellHelper shellHelper = new ShellHelper();

        ArrayList<String> commands = new ArrayList<String>();
        commands.add("appops set " + packageName + " android:mock_location deny");
        commands.add("sleep 1");
        commands.add("appops set " + packageName + " android:mock_location allow");
        ArrayList<String> strings = shellHelper.runCommands(Runlevel.su, commands);
        strings.size();
    }

    @Override
    public void destruct() {
        if (this.mLocationManager == null) {
            return;
        }
        if (_spoofingConfiguration.overwriteMode != LocationOverwriteMode.INDIRECT) {
            try {
                mLocationManager.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
                mLocationManager.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
                mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "Failed destructing gps provider");
            }
        }
        if (_spoofingConfiguration.overwriteMode == LocationOverwriteMode.COMMON
                || _spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL
            || _spoofingConfiguration.overwriteMode == LocationOverwriteMode.INDIRECT) {
            try {
                mLocationManager.clearTestProviderLocation(LocationManager.NETWORK_PROVIDER);
                mLocationManager.clearTestProviderEnabled(LocationManager.NETWORK_PROVIDER);
                mLocationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "Failed destructing network provider");
            }
        }
        if (_spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL) {
            try {
                mLocationManager.clearTestProviderLocation(LocationManager.PASSIVE_PROVIDER);
                mLocationManager.clearTestProviderEnabled(LocationManager.PASSIVE_PROVIDER);
                mLocationManager.removeTestProvider(LocationManager.PASSIVE_PROVIDER);
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "Failed destructing passive provider");
            }
        }
        if (this._spoofingConfiguration.overwriteFused) {
            try {
                mLocationManager.clearTestProviderLocation(LocationManager.FUSED_PROVIDER);
                mLocationManager.clearTestProviderEnabled(LocationManager.FUSED_PROVIDER);
                mLocationManager.removeTestProvider(LocationManager.FUSED_PROVIDER);
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "Failed destructing fused provider");
            }
        }
        this.mLocationManager = null;
    }

    @Override
    public void sendLocation(Location location) {
        if (this.mLocationManager == null) {
            return;
        }
        try {
            location.setProvider(LocationManager.GPS_PROVIDER);

            if (_spoofingConfiguration.overwriteMode != LocationOverwriteMode.INDIRECT) {
                this.mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, 2, null, System.currentTimeMillis());
                this.mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
            }

            if (((_spoofingConfiguration.overwriteMode == LocationOverwriteMode.COMMON
                    || _spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL)
                    && this._randomGenerator.getIntInRange(0, 100) % 2 == 1)
                    || _spoofingConfiguration.overwriteMode == LocationOverwriteMode.INDIRECT) {
                if (_spoofingConfiguration.overwriteMode != LocationOverwriteMode.INDIRECT) {
                    location.setProvider(LocationManager.NETWORK_PROVIDER);
                }
                    this.mLocationManager.setTestProviderStatus(LocationManager.NETWORK_PROVIDER, 2, null, System.currentTimeMillis());
                    this.mLocationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, location);
            }
            try {
                if (_spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL) {
                    if (this._randomGenerator.getIntInRange(0, 100) % 2 == 1) {
                        if (_spoofingConfiguration.overwriteMode != LocationOverwriteMode.INDIRECT) {
                            location.setProvider(LocationManager.PASSIVE_PROVIDER);
                        }
                        this.mLocationManager.setTestProviderStatus(LocationManager.PASSIVE_PROVIDER, 2, null, System.currentTimeMillis());
                        this.mLocationManager.setTestProviderLocation(LocationManager.PASSIVE_PROVIDER, location);
                    }
                }
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Passive provider issue, probably android 10");
            }


            if (this._spoofingConfiguration.overwriteFused) {
                if (this._randomGenerator.getIntInRange(0, 100) % 2 == 1) {
                    if (_spoofingConfiguration.overwriteMode != LocationOverwriteMode.INDIRECT) {
                        location.setProvider(LocationManager.FUSED_PROVIDER);
                    }
                    this.mLocationManager.setTestProviderStatus(LocationManager.FUSED_PROVIDER, 2, null, System.currentTimeMillis());
                    this.mLocationManager.setTestProviderLocation(LocationManager.FUSED_PROVIDER, location);
                }
            }

        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Failed clearing test location and setting new one");
            //TODO: cleanup previous locations, rerun setup
            this.destruct();
            //this.mLocationManager = null;
            this.setup();
        }
    }
}
