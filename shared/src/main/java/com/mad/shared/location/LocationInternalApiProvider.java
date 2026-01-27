package com.mad.shared.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;


public class LocationInternalApiProvider implements ILocationProvider {
    private static final String TAG = "MAD";
    private final SpoofingConfiguration _spoofingConfiguration;
    private ILocationManager locMan;
    private Context _Context;
    private SharedPreferences mSharedPreferences;

    LocationInternalApiProvider(Context context, LocationManager locationManager,
                                SpoofingConfiguration spoofingConfiguration) {
        this._Context = context;
        this.locMan = getLocationManager(locationManager);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this._Context);
        this._spoofingConfiguration = spoofingConfiguration;
    }

    private ILocationManager getLocationManager(LocationManager locationManager) {
        if (locationManager == null) {
            return null;
        }
        Field[] fld = locationManager.getClass().getDeclaredFields();  // for axis the fields
        // Loop for get all the Field in class
        Object serviceMan = null;
        for (Field fld1 : fld) {
            fld1.setAccessible(true);
            if (fld1.getName().toLowerCase().contains("service")
                    || fld1.getType().toString().toLowerCase().contains("locationmanager")) {
                try {
                    serviceMan = fld1.get(locationManager);
                    return (ILocationManager) serviceMan;
                } catch (IllegalAccessException e) {
//                    Logger.warningtf("RGC", "Could not fetch locationmanager due to illegalaccess");
                    Log.w(TAG, "Could not fetch location-manager");
                    return null;
                }
            }
        }
        return null;
    }

    public synchronized void destruct() {
        this.locMan = null;
    }

    @Override
    public void sendLocation(Location location) {
        if (this.locMan == null) {
            this.locMan = getLocationManager((LocationManager) this._Context.getSystemService(Context.LOCATION_SERVICE));
            if (this.locMan == null) {
                Log.w(TAG, "LocationManager is null, aborting reportLocation");
                return;
            }
        }
        try {
            location.setProvider("gps");
            this.locMan.reportLocation(location, false);
            if (this._spoofingConfiguration.overwriteMode == LocationOverwriteMode.COMMON
                    || this._spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL) {
                if (_spoofingConfiguration.overwriteMode != LocationOverwriteMode.INDIRECT) {
                    location.setProvider(LocationManager.NETWORK_PROVIDER);
                }
                this.locMan.reportLocation(location, false);
            }
            if (this._spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL) {
                if (_spoofingConfiguration.overwriteMode != LocationOverwriteMode.INDIRECT) {
                    location.setProvider(LocationManager.PASSIVE_PROVIDER);
                }
                this.locMan.reportLocation(location, true);
            }
            if (this._spoofingConfiguration.overwriteFused) {
                location.setProvider(LocationManager.FUSED_PROVIDER);
                this.locMan.reportLocation(location, false);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private double getAltitudeFromOpenElevationApi(double lat, double lng) {
        String url = "https://api.open-elevation.com/api/v1/lookup?locations="
                + lat + "," + lng;
        String result = "";
        double elevation = 0.0;

        GoogleMapsGet getRequest = new GoogleMapsGet();

        try {
            result = getRequest.execute(url).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return elevation;
        }
        if (result == null || result.length() == 0) {
            return elevation;
        }
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(result);
        } catch (JSONException e) {
            Log.e(TAG, "Failed converting " +
                    "response from Google to JSON");
            return elevation;
        }

        //we got a valid json
        JSONObject firstResult = null;
        try {
            firstResult = jsonObj.getJSONArray("results").getJSONObject(0);
        } catch (JSONException e) {
            Log.e(TAG, "JSON response does not contain results");
            return elevation;
        }
        if (firstResult == null) {
            Log.e(TAG, "First item in results is null");
            return elevation;
        }

        try {
            elevation = firstResult.getDouble("elevation");
        } catch (JSONException e) {
            e.printStackTrace();
            //shouldn't occur with the previous check...
            Log.e(TAG, "No elevation in result");
            return elevation;
        }

        return elevation;
    }

}
