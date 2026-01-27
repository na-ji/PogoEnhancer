package com.mad.shared.location;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.mad.shared.Constants;
import com.mad.shared.gpx.LatLon;
import com.mad.shared.utils.RandomGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;


public class LocationCache implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MAD";
    private Context _Context;
    private Location currentLocation;
    private LatLon _SharedLatLon;
    private Handler _Handler;
    private LocationManager mLocationManager;

    private ILocationProvider mLocationProvider = null;
    private SharedPreferences mSharedPreferences = null;

    private volatile long lastLocationReport = 0;
    private LocationReporterThread _locationReporterThread = null;

    private Random randomGen = new Random();
    private RandomGenerator randomGenerator = new RandomGenerator();
    private Method locationMakeCompleteMethod;
    private Bundle satelliteBundleExtras;
    private long lastAltUpdate;
    private boolean deleteCalledOnce = false;

    private FusedLocationProviderClient _FusedLocationClient = null;
    private LocationCallback _LocationCallback;
    private GoogleApiClient _GoogleApiClient;
    private LocationRequest _LocationRequest;
    private SpoofingConfiguration _spoofingConfiguration = null;

    public LocationCache(Context context, LatLon sharedLatLon, SpoofingConfiguration spoofingConfiguration) {
        this._Context = context;
        this._SharedLatLon = sharedLatLon;
        this._spoofingConfiguration = spoofingConfiguration;
        try {
            //noinspection RedundantArrayCreation
            this.locationMakeCompleteMethod = Location.class.getMethod("makeComplete", new Class[0]);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Could not execute makeComplete");
            e.printStackTrace();
        }
        this.satelliteBundleExtras = new Bundle();
        this.satelliteBundleExtras.putInt("satellites", 10);
        this.lastAltUpdate = System.currentTimeMillis() / 1000L;
        currentLocation = new Location(LocationManager.GPS_PROVIDER);

        this.setup();
    }

    private synchronized void setup() {
        if (this.mLocationProvider != null) {
            Log.w(TAG, "LocationCache.setup called twice, invalidating old setup");
            this.destruct();
        }

        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this._Context);
        this.mLocationManager = (LocationManager) this._Context.getSystemService(Context.LOCATION_SERVICE);

        if (this._spoofingConfiguration.enableSuspendedMocking) {
            _FusedLocationClient = LocationServices.getFusedLocationProviderClient(this._Context);
            //this.connectFusedLocationClient();
            this.buildGoogleApiClient();
        }

        if (this._spoofingConfiguration.spoofingEnabled) {
            if (this._spoofingConfiguration.spoofingMethod == SpoofingMethod.MOCK) {
                Log.i(TAG, "Using mock location provider");
                this.mLocationProvider = new LocationMockProvider(this._Context,
                        this.mLocationManager, this._spoofingConfiguration);
            } else {
                Log.i(TAG, "Using legacy spoofing");
                this.mLocationProvider = new LocationInternalApiProvider(this._Context,
                        this.mLocationManager, this._spoofingConfiguration);

//        }
            }
            this.restoreLocationFromSharedLatLon();
            this.reportLocation();

            if (this._locationReporterThread == null) {
                this._locationReporterThread = new LocationReporterThread(this,
                        this.mSharedPreferences, this._spoofingConfiguration);
                this._locationReporterThread.setName("Location reporter thread");
                this._locationReporterThread.start();
            }


        }
    }

    public synchronized void destruct() {
        //stop reporter thread
        if (this._locationReporterThread != null) {
            this._locationReporterThread.interrupt();
            this._locationReporterThread = null;
        }

        if (this._FusedLocationClient != null) {
            this.disconnectFusedLocationClient();
        }
        this.storeLocationInSharedPref();
        this.mLocationProvider.destruct();
    }

    public synchronized long getLastLocationReport() {
        return this.lastLocationReport;
    }


    private synchronized void reportLocation() {
        // TODO: make coarse or sth?

        if (this._spoofingConfiguration.resetAgpsContinuoursly) {
            this.deleteAgpsData();
        } else if (!this.deleteCalledOnce && this._spoofingConfiguration.resetAgpsOnce) {
            this.deleteAgpsData();
            this.deleteCalledOnce = true;
        }

        this.setLatLngAltSat();

        Location location = this.getCoarseLocation();
        location.setAccuracy(getCoarseAccuracyMeters());

        makeLocationComplete(location);

        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setTime(System.currentTimeMillis());
        this.mLocationProvider.sendLocation(location);
        this.lastLocationReport = System.currentTimeMillis();
    }

    public synchronized void setLocation(Location location) {
        if (location != null) {
            this.currentLocation = location;
            this._SharedLatLon.setLat(location.getLatitude());
            this._SharedLatLon.setLon(location.getLongitude());
            // TODO: alt?
        }
    }

    private synchronized void deleteAgpsData() {
        if (this.mLocationManager == null) {
            Log.w(TAG, "LocationManager is null, aborting AGPS reset");
            return;
        }
        Bundle bundle = new Bundle();
        this.mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", null);
        this.mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_xtra_injection", bundle);
        this.mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_time_injection", bundle);

        if (this._spoofingConfiguration.overwriteMode == LocationOverwriteMode.COMMON
                || this._spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL) {
            this.mLocationManager.sendExtraCommand(LocationManager.NETWORK_PROVIDER, "delete_aiding_data", null);
            this.mLocationManager.sendExtraCommand(LocationManager.NETWORK_PROVIDER, "force_xtra_injection", bundle);
            this.mLocationManager.sendExtraCommand(LocationManager.NETWORK_PROVIDER, "force_time_injection", bundle);
        }
        if (this._spoofingConfiguration.overwriteMode == LocationOverwriteMode.OVERKILL) {
            this.mLocationManager.sendExtraCommand(LocationManager.PASSIVE_PROVIDER, "delete_aiding_data", null);
            this.mLocationManager.sendExtraCommand(LocationManager.PASSIVE_PROVIDER, "force_xtra_injection", bundle);
            this.mLocationManager.sendExtraCommand(LocationManager.PASSIVE_PROVIDER, "force_time_injection", bundle);
        }

        if (this._spoofingConfiguration.overwriteFused) {
            this.mLocationManager.sendExtraCommand(LocationManager.FUSED_PROVIDER, "delete_aiding_data", null);
            this.mLocationManager.sendExtraCommand(LocationManager.FUSED_PROVIDER, "force_xtra_injection", bundle);
            this.mLocationManager.sendExtraCommand(LocationManager.FUSED_PROVIDER, "force_time_injection", bundle);
        }
    }

    private void makeLocationComplete(Location location) {
        if (this.locationMakeCompleteMethod != null) {
            try {
                //noinspection RedundantArrayCreation
                this.locationMakeCompleteMethod.invoke(location, new Object[0]);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void setLatLngAltSat() {
        double newLat = this._SharedLatLon.getLat();
        double newLng = this._SharedLatLon.getLon();
        Double newAlt = this._SharedLatLon.getAltitude();
        Integer newSatellites = this._SharedLatLon.getSatellites();
        float newSpeed = this._SharedLatLon.getSpeed();

        //calculate bearing of old lat/lng to new lat/lng
        double oldLat = this.currentLocation.getLatitude();
        double oldLng = this.currentLocation.getLongitude();
        double y = Math.sin(newLng - oldLng) * Math.cos(newLat);
        double x = Math.cos(oldLat) * Math.sin(newLat)
                - Math.sin(oldLat) * Math.cos(newLat) * Math.cos(newLng - oldLng);
        float bearing = (float) Math.atan2(y, x);

        float bearingDegrees = bearing * (180.0f / (float) Math.PI); // convert to degrees
        bearingDegrees = (bearingDegrees > 0.0f ? bearingDegrees : (360.0f + bearingDegrees)); // correct discontinuity
//        Logger.debug("ProtoEnhancer", "Bearing degress: " + bearingDegrees);
        this.currentLocation.setLatitude(newLat);
        this.currentLocation.setLongitude(newLng);
        this.currentLocation.setBearing(bearingDegrees);
        this.currentLocation.setSpeed(newSpeed);

        //float derp = (float) (System.currentTimeMillis() / 1000L) - lastAltUpdate;
//        Logger.debug("ProtoEnhancer", "ms since last alt update: " + derp);

        if (newAlt != null) {
            this.currentLocation.setAltitude(newAlt);
        } else if (System.currentTimeMillis() / 1000L - lastAltUpdate > 300) {
            //alt = getAltitudeFromOpenElevationApi(lat, lng);
            double curAlt = this.currentLocation.getAltitude();
            double ranDouble = randomGenerator.getDoubleInRange(-50.0000d, 50.0000d);
            if (randomGen.nextBoolean()) {
                curAlt += ranDouble;
            } else {
                curAlt -= ranDouble;
            }
            //check for valid latitude
            if (curAlt < -30) {
                curAlt = 0.0d;
            } else if (curAlt > 6000) {
                curAlt = 2500.0d;
            }
            this.currentLocation.setAltitude(curAlt);
            lastAltUpdate = System.currentTimeMillis() / 1000L;
        }

        this.satelliteBundleExtras = new Bundle();

        if (newSatellites != null) {
            this.satelliteBundleExtras.putInt("satellites", newSatellites);
        } else {
            // random generate amount of satellites
            this.satelliteBundleExtras.putInt("satellites", randomGenerator.getIntInRange(9, 15));
        }
//        long lastLocationDelta = System.currentTimeMillis() - this.lastLocationReport;
//        if(this.mSharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.USE_MOCK_LOCATION,
//                Constants.DEFAULT_VALUES.USE_MOCK_LOCATION)
//            && 11000 - lastLocationDelta > 0
//            && teleport) {
//            //we may need to wait a little...
//            try {
//                Thread.sleep(11000 - lastLocationDelta);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
//        this.reportLocation();
    }

    public void setSpeed(float speed) {
        this._SharedLatLon.setSpeed(speed);
    }

    public void resetLastSpeed() {
        this._SharedLatLon.setSpeed(0.0f);
    }

    public synchronized Location getPreciseLocation(String providerName) {
        Location preciseLocation = new Location(providerName);
        preciseLocation.set(this.currentLocation);
        return preciseLocation;
    }

    private synchronized Location getCoarseLocation() {
        Location coarseLocation = new Location(this.currentLocation);
        coarseLocation.setSpeed(getCoarseSpeed());
        coarseLocation.setBearing(getCoarseBearing());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            coarseLocation.setBearingAccuracyDegrees(this.currentLocation.getBearingAccuracyDegrees());
            coarseLocation.setVerticalAccuracyMeters(getCoarseVerticalAccuracyMeters());
        }
        coarseLocation.setLatitude(getCoarseLatitude());
        coarseLocation.setLongitude(getCoarseLongitude());
        coarseLocation.setAltitude(getCoarseAltitude());
        coarseLocation.setExtras(this.satelliteBundleExtras);
        return coarseLocation;
    }

    private synchronized void storeLocationInSharedPref() {
        if (this.mLocationManager == null) {
            Log.e(TAG, "LocationManager is null");
            return;
        }
        double latitude = this.currentLocation.getLatitude();
        double longitude = this.currentLocation.getLongitude();
        double altitude = this.currentLocation.getAltitude();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this._Context).edit();
        editor.putLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LATITUDE, Double.doubleToRawLongBits(latitude));
        editor.putLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LONGITUDE, Double.doubleToRawLongBits(longitude));
        editor.putLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_ALTITUDE, Double.doubleToRawLongBits(altitude));
        editor.apply();
    }

    private synchronized void restoreLocationFromSharedLatLon() {
        this.currentLocation.setLatitude(this._SharedLatLon.getLat());
        this.currentLocation.setLongitude(this._SharedLatLon.getLon());
    }


    private float getCoarseSpeed() {
        float ranFloat = randomGenerator.getFloatInRange(0.0f, 1.5f);
        float curSpeed = this.currentLocation.getSpeed();
        if (randomGen.nextBoolean()) {
            //random true, add random float to speed in range of 1-2
            curSpeed += ranFloat;
        } else {
            curSpeed -= ranFloat;
        }
        if (curSpeed < 0) {
            curSpeed = 0;
        }
        return curSpeed;
    }

    private float getCoarseBearing() {
        float curBearing = this.currentLocation.getBearing();
        float ranFloat = randomGenerator.getFloatInRange(0.0f, 2.0f);
        if (randomGen.nextBoolean()) {
            curBearing += ranFloat;
        } else {
            curBearing -= ranFloat;
        }
        if (curBearing <= 0.0f) {
            curBearing = 0.0001f;
        } else if (curBearing > 360.0f) {
            curBearing = 360f;
        }
        return curBearing;
    }

    private double getCoarseAltitude() {
        //between -90.0 and 90.0
        double curAlt = this.currentLocation.getAltitude();
        double ranDouble = randomGenerator.getDoubleInRange(-10.0000d, 10.0000d);
        if (randomGen.nextBoolean()) {
            curAlt += ranDouble;
        } else {
            curAlt -= ranDouble;
        }
        //check for valid latitude
        return curAlt;
    }

    private double getCoarseLatitude() {
        //between -90.0 and 90.0
        double curLat = this.currentLocation.getLatitude();
        double ranDouble = randomGenerator.getDoubleInRange(0.000001d, 0.000003d);
        if (randomGen.nextBoolean()) {
            curLat += ranDouble;
        } else {
            curLat -= ranDouble;
        }
        //check for valid latitude
        if (curLat < -90.0d) {
            curLat = -90.0d;
        } else if (curLat > 90.0d) {
            curLat = 90.0d;
        }
        return curLat;
    }

    private double getCoarseLongitude() {
        //between -180.0 and 180.0
        double curLng = this.currentLocation.getLongitude();
        double ranDouble = randomGenerator.getDoubleInRange(0.000001d, 0.000003d);
        if (randomGen.nextBoolean()) {
            curLng += ranDouble;
        } else {
            curLng -= ranDouble;
        }
        //check for valid longitude
        if (curLng < -180.0d) {
            curLng = -180.0d;
        } else if (curLng > 180.0d) {
            curLng = 180.0d;
        }
        return curLng;
    }

    private float getCoarseVerticalAccuracyMeters() {
        double ranDouble = randomGenerator.getDoubleInRange(1d, 6.0d);

        return (float) ranDouble;
    }

    private float getCoarseAccuracyMeters() {
        double ranDouble = randomGenerator.getDoubleInRange(1.0d, 6.0d);

        return (float) ranDouble;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        this.connectFusedLocationClient();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private synchronized void disconnectFusedLocationClient() {
        if (this._FusedLocationClient != null && this._LocationCallback != null) {
            Log.w(TAG, "Disconnecting fused client");
            _FusedLocationClient.removeLocationUpdates(this._LocationCallback);
        }
    }

    private synchronized void buildGoogleApiClient() {
        _GoogleApiClient = new GoogleApiClient.Builder(this._Context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        _GoogleApiClient.connect();
    }

    private synchronized void connectFusedLocationClient() {
        if (this._FusedLocationClient != null) {
            Log.w(TAG, "Connecting fused client");

            this._LocationRequest = LocationRequest.create();
            this._LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            this._LocationRequest.setInterval(0);
            this._LocationRequest.setFastestInterval(0);


            _LocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        // compare the location, if the delta to the current outer location is too big, report a new location immediately
                        if (location != null) {
                            double currentLat = Math.round(_SharedLatLon.getLat() * 1000.0) / 1000.0;
                            double currentLng = Math.round(_SharedLatLon.getLon() * 1000.0) / 1000.0;
                            double newLat = Math.round(location.getLatitude() * 1000.0) / 1000.0;
                            double newLng = Math.round(_SharedLatLon.getLon() * 1000.0) / 1000.0;

                            //Logger.debug("PE", "CurrentLat: " + currentLat + " CurrentLng: " + currentLng + " NewLat: " + newLat + " NewLng: " + newLng);
                            if (currentLat != newLat || currentLng != newLng) {
                                Log.d(TAG, "Overwriting location");
                                reportLocation();
                            }
                            /*double distance = _SharedLatLon.distance(new LatLon(location.getLatitude(), location.getLongitude()));
                            if (distance > 50) { // TODO: calc max distance that could've been travelled?
                                reportLocation(false);
                            }*/
                            break;
                        }
                    }
                }

                ;
            };
            if (ActivityCompat.checkSelfPermission(this._Context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this._Context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this._Context,
                        "Missing permissions to access fine location. Please set permissions.",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Unable to start fused location mocking. Lacking permissions ACCESS_FINE_LOCATION");
                return;
            }

            HandlerThread handlerThread = new HandlerThread("LocationCallback");
            if (!handlerThread.isAlive()) {
                handlerThread.start();
            }

            _FusedLocationClient.requestLocationUpdates(this._LocationRequest,
                    _LocationCallback, handlerThread.getLooper());
        }
    }

    private void reconnectGoogleApiClient() {
        if (this._GoogleApiClient != null && !this._GoogleApiClient.isConnected() && !this._GoogleApiClient.isConnecting()) {
                this._GoogleApiClient.reconnect();
        }
    }

    private static class LocationReporterThread extends Thread {
        private final SpoofingConfiguration _spoofingConfiguration;
        private LocationCache _locationCache = null;
        private SharedPreferences _sharedPreferences = null;

        LocationReporterThread(LocationCache locationCache, SharedPreferences sharedPreferences,
                               SpoofingConfiguration spoofingConfiguration) {
            this._locationCache = locationCache;
            this._sharedPreferences = sharedPreferences;
            this._spoofingConfiguration = spoofingConfiguration;
        }

        @Override
        public void run() {
            this._locationCache.reportLocation();

            //this._locationCache.connectFusedLocationClient();
            while (!this.isInterrupted()) {
                try {
                    this._locationCache.reconnectGoogleApiClient();
                    //Logger.debug("PE", "Reporting...");
                    this._locationCache.reportLocation();
                    if (_spoofingConfiguration.spoofingMethod == SpoofingMethod.MOCK) {
                        sleep(500);
                    } else {
                        sleep(300);
                    }

                } catch (InterruptedException ex) {
                    Log.i(TAG, "Interrupted while waiting for next update");
                    break;
                }
            }
            //this._locationCache.disconnectFusedLocationClient();

        }
    }
}
