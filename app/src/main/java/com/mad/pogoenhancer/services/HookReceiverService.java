package com.mad.pogoenhancer.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.mad.pogoenhancer.BackendStorage;
import com.mad.pogoenhancer.ConnectionHandler;
import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.LoginActivity;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.StartMyActivityAtBootReceiver;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.pogoenhancer.utils.IvToast;
import com.mad.pogoenhancer.utils.InventoryBuild;
import com.mad.pogoenhancer.utils.NameReplaceBuild;
import com.mad.pogoenhancer.utils.WildmonBuild;
import com.mad.pogoenhancer.utils.OomAdjOverride;
import com.mad.pogoenhancer.utils.PogoPatcher;
import com.mad.pogoenhancer.utils.RunException;
import com.mad.pogoenhancer.utils.ShellHelper;
import com.mad.pogoenhancer.utils.SystemServerPatcher;
import com.mad.shared.gpx.LatLon;
import com.mad.shared.location.LocationCache;
import com.mad.shared.location.SpoofingConfiguration;
import com.mad.shared.location.SpoofingMethod;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;


import static com.mad.pogoenhancer.App.CHANNEL_ID;

import POGOProtos.Rpc.CatchPokemonOutProto;
import POGOProtos.Rpc.GetMapObjectsOutProto;

//import com.google.android.gms.location.FusedLocationProviderClient;

public class HookReceiverService extends Service {
    private static final String TOGGLE_ALL_OVERLAY_VISIBILITY = "TOGGLE_ALL_OVERLAY_VISIBILITY";
    private long lastSuccessfulCheck = -1;

    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        //Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    final String TAG = "HookReceiverService";

    //private FusedLocationProviderClient fusedLocationClient = null;

    private final IBinder binder = new LocalBinder();
    private UnixServerSocketThread mThread = null;
    private SharedPreferences _sharedPreferences;
    private Handler mHandler;
    private SystemServerPatcher mSystemServerPatcher = null;
    private PogoPatcher _pogoPatcher = null;
    private UserLoginRunnable mUserLoginRunnable = null;
    private OverlayManager overlayManager;
    private LatLon _SharedLatLon;
    private LocationCache _LocationCache;
    private FusedLocationProviderClient _FusedLocationClient;
    private LocationCallback _LocationCallback;
    private final ShellHelper _shellHelper = new ShellHelper();
    private volatile boolean _sendOncePerInjection = false;
    private InventoryBuild _inventoryManagementSender;
    private NameReplaceBuild _namereplacebuild;
    private WildmonBuild _wildmonbuild;

    public LatLon getSharedLatLon() {
        return _SharedLatLon;
    }

    private final OomAdjOverride oomAdjOverrider = new OomAdjOverride(-17) {
        @Override
        public synchronized void suErrorHandler(RunException ex) {
            phoneNotRooted = true;
        }
    };
    private final StartMyActivityAtBootReceiver mServiceRestarter = new StartMyActivityAtBootReceiver();
    private boolean overlayEnabled;

    public void setInjected(boolean injected) {
        _sendOncePerInjection = injected;
    }

    public void showIvOverlay(int attack, int defence, int stamina, double cpMultiplier,
                              double additionalCpMultiplier, int monLvl, int shiny, int typ, int gender, int weather,
                              String weightXSXL, String heightXSXL, int ditto) {
        boolean ivOVerlayEnabled = this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.IV_OVERLAY_ENABLED,
                Constants.DEFAULT_VALUES.IV_OVERLAY_ENABLED
        );
        if (!ivOVerlayEnabled || Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && !Settings.canDrawOverlays(getBaseContext())) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && !Settings.canDrawOverlays(getBaseContext())) {
                Logger.fatal("PogoEnhancerJ", "IV overlay enabled but missing permissions...");
            }
            return; //user does not want IVs to be shown or overlay permissions not granted
        }


        if (this.overlayManager != null) {
            this.mHandler.post(() -> overlayManager.setIv(attack, defence, stamina, cpMultiplier, additionalCpMultiplier, monLvl, shiny, typ, gender, weather, weightXSXL, heightXSXL, ditto));
        }
    }

    public void sendToast(String msg) {

        this.mHandler.post(() -> IvToast.showToast(getApplicationContext(), msg.toString(), Gravity.TOP | Gravity.LEFT, 100, 200, R.drawable.ic_grin_beam_solid, 0));

    }

    public void saveCooldown(String lat, String lng, long timestamp) {
        SharedPreferences.Editor edit = this._sharedPreferences.edit();
        edit.putString(Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_LAT, lat);
        edit.putString(Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_LNG, lng);
        edit.putLong(Constants.SHAREDPERFERENCES_KEYS.COOLDOWN_TIME, timestamp);
        edit.apply();
    }

    public void notifyCaptureResult(CatchPokemonOutProto.Status catchStatus) {

//        Handler handler = new Handler(Looper.getMainLooper());

        this.mHandler.post(new Runnable() {

            @Override
            public void run() {
                switch (catchStatus) {
                    case CATCH_ERROR:
                        IvToast.showToast(getApplicationContext(), "Error while capturing", Gravity.BOTTOM | Gravity.CENTER, 0, 200, 0, 0);
                        break;
                    case CATCH_SUCCESS:
                        IvToast.showToast(getApplicationContext(), "Successful capture!", Gravity.BOTTOM | Gravity.CENTER, 0, 200, R.drawable.ic_grin_beam_solid, 0);

                        // Toast.makeText(getApplicationContext(), "Successful capture!", Toast.LENGTH_LONG).show();
                        break;
                    case CATCH_ESCAPE:
                        IvToast.showToast(getApplicationContext(), "Mon escaped", Gravity.BOTTOM | Gravity.CENTER, 0, 200, R.drawable.ic_frown_solid, 0);

                        // Toast.makeText(getApplicationContext(), "Mon escaped :(", Toast.LENGTH_LONG).show();
                        break;
                    case CATCH_FLEE:
                        IvToast.showToast(getApplicationContext(), "Mon fled", Gravity.BOTTOM | Gravity.CENTER, 0, 200, R.drawable.ic_dizzy_solid, 0);

                        // Toast.makeText(getApplicationContext(), "Mon fled :(", Toast.LENGTH_LONG).show();
                        break;
                    case CATCH_MISSED:
                        IvToast.showToast(getApplicationContext(), "You missed", Gravity.BOTTOM | Gravity.CENTER, 0, 200, R.drawable.ic_frown_solid, 0);

                        // Toast.makeText(getApplicationContext(), "You missed :(", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });


    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.debug("PogoEnhancerJ", "Creating HookService");
        this.setup();
    }

    private void setup() {
        if (_sharedPreferences == null) {
            _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        if (_inventoryManagementSender == null) {
            _inventoryManagementSender = new InventoryBuild(this);
        }

        if (_namereplacebuild == null) {
            _namereplacebuild = new NameReplaceBuild(this);
        }

        if (_wildmonbuild == null) {
            _wildmonbuild = new WildmonBuild(this);
        }

        if (this.mSystemServerPatcher == null) {
            this.mSystemServerPatcher = new SystemServerPatcher();
        }

        if (this._pogoPatcher == null) {
            this._pogoPatcher = new PogoPatcher(this);
        }

        if (this._SharedLatLon == null) {
            double latitude = Double.longBitsToDouble(
                    this._sharedPreferences.getLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LATITUDE,
                            Constants.DEFAULT_VALUES.LAST_LOCATION_LATITUDE
                    )
            );
            double longitude = Double.longBitsToDouble(
                    this._sharedPreferences.getLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LONGITUDE,
                            Constants.DEFAULT_VALUES.LAST_LOCATION_LONGITUDE
                    )
            );
            this._SharedLatLon = new LatLon(latitude, longitude);
        }


        this.mHandler = new Handler(Looper.getMainLooper());
        this.mUserLoginRunnable = new UserLoginRunnable();

        this.mHandler.post(this.mUserLoginRunnable);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Logger.fatal("PogoEnhancerJ", "Uncaught exception: "
                    + e.getMessage() + " with: " + e.toString() + " stacktrace: "
                    + Arrays.toString(e.getStackTrace()));
            Context ctx = getApplicationContext();
            Intent startServiceIntent = new Intent(ctx, HookReceiverService.class);
            ctx.startService(startServiceIntent);
            int mPendingIntentId = 11111111;
            PendingIntent mPendingIntent = PendingIntent.getService(ctx, mPendingIntentId, startServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, mPendingIntent);
        });
    }

    public void updateNearby(GetMapObjectsOutProto gmo) {
        this.mHandler.post(() -> {
            if (overlayEnabled && overlayManager != null) {
                overlayManager.updateDataset(gmo);
                if (_sendOncePerInjection) {
                    _inventoryManagementSender.sendInventoryJson();
                    _namereplacebuild.sendNameReplaceJson();
                    // TODO: Also send current injectionSettings?...
                    _sendOncePerInjection = false;
                    IvToast.showToast(getApplicationContext(), "Successfully transmitted settings", Gravity.BOTTOM | Gravity.CENTER, 0, 200, R.drawable.ic_grin_beam_solid, 0);
                }
                _inventoryManagementSender.sendCooldown();
                _wildmonbuild.sendWildmonJson();
                overlayManager.showInjectionSettings();
                _inventoryManagementSender.sendAutotransferJson();
                _inventoryManagementSender.sendAutoencounterJson();
                _inventoryManagementSender.sendNotAutrunMonJson();
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: consider adding a break in the locationCache.reportLocation
        if (intent != null && intent.getAction() != null && intent.getAction().equals(TOGGLE_ALL_OVERLAY_VISIBILITY)) {
            if (this.overlayManager != null) {
                this.overlayManager.toggleVisibilityOfAllElements();
            }
            return START_NOT_STICKY;
        }
        if (this._sharedPreferences == null) {
            this.setup();
        }

        if (intent != null && intent.hasExtra("latitude")) {
            float latitude = intent.getFloatExtra("latitude", 0.0f) % 90;
            this._SharedLatLon.setLat(latitude);
            SharedPreferences.Editor edit = this._sharedPreferences.edit();
            edit.putLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LATITUDE, Double.doubleToRawLongBits(latitude));
            edit.apply();
            // put into preferences
        }
        if (intent != null && intent.hasExtra("longitude")) {
            float longitude = intent.getFloatExtra("longitude", 0.0f) % 180;

            this._SharedLatLon.setLon(longitude);
            SharedPreferences.Editor edit = this._sharedPreferences.edit();
            edit.putLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LONGITUDE, Double.doubleToRawLongBits(longitude));
            edit.apply();
        }

        if (this.overlayManager != null || intent != null && intent.hasExtra("DONT_START")) {
            return START_NOT_STICKY;
        }

        if (this._LocationCache == null && this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SPOOFING_ENABLED,
                Constants.DEFAULT_VALUES.SPOOFING_ENABLED
        )) {
            // TODO: check if systemized and method all set up for

            SpoofingConfiguration spoofingConfiguration = getSpoofingConfiguration();
            // TODO: gms reset?
            this._LocationCache = new LocationCache(this,
                    this._SharedLatLon, spoofingConfiguration);
        } else {
            // TODO: start thread updating sharedLatLon...
            _FusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Logger.fatal("PogoEnhancerJ", "Location permissions not granted");
                return START_NOT_STICKY;
            }
            _FusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            _SharedLatLon.setLat(location.getLatitude());
                            _SharedLatLon.setLon(location.getLongitude());
                        }
                    });

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);

            _LocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            _SharedLatLon.setLat(location.getLatitude());
                            _SharedLatLon.setLon(location.getLongitude());
                            break;
                        }
                    }
                }

                ;
            };

            _FusedLocationClient.requestLocationUpdates(locationRequest,
                    _LocationCallback,
                    null /* Looper */);
        }

        Intent notificationIntent = new Intent(this, LoginActivity.class);
        notificationIntent.putExtra("classFrom", HookReceiverService.class.toString());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Intent toggleVisibilityIntent = new Intent(this, HookReceiverService.class);
        toggleVisibilityIntent.setAction(TOGGLE_ALL_OVERLAY_VISIBILITY);
        PendingIntent toggleVisibilityPendingIntent =
                PendingIntent.getService(this, 0,
                        toggleVisibilityIntent, 0);

        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Pogo information processing")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_autorenew_black_24dp)
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.ic_remove_red_eye_red_24dp, "Toggle overlay visibility",
                            toggleVisibilityPendingIntent)
                    .build();
        } else {
            //noinspection deprecation
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_autorenew_black_24dp)
                    .setContentTitle("Pogo information processing")
                    .setContentText("Running...")
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.ic_remove_red_eye_red_24dp, "Toggle overlay visibility",
                            toggleVisibilityPendingIntent)
                    .build();
        }

        startForeground(1, notification);

        if (this.mThread == null) {
            Logger.debug("PogoEnhancerJ", "Starting internal listener");
            this.mThread = new UnixServerSocketThread(null, this, this._sharedPreferences);
            mThread.setName("Unix server thread");
            mThread.start();
        }

        if (this._sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.OOM_ADJ_ENABLED,
                Constants.DEFAULT_VALUES.OOM_ADJ_ENABLED)
        ) {
            // creating new OomAdjOverrider instance everytime the worker has started
            // means phoneNotRooted attribute is reset - but that's what we want - someone might
            // have changed "Root Access" setting in cyanogenmod for example

            this.oomAdjOverrider.start();
        }

        if (intent != null && !intent.hasExtra("latitude")
                || intent != null && !intent.hasExtra("longitude")) {
            // restore the location from preferences...
            double latitude = Double.longBitsToDouble(
                    this._sharedPreferences.getLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LATITUDE,
                            Constants.DEFAULT_VALUES.LAST_LOCATION_LATITUDE
                    )
            );
            double longitude = Double.longBitsToDouble(
                    this._sharedPreferences.getLong(Constants.SHAREDPERFERENCES_KEYS.LAST_LOCATION_LONGITUDE,
                            Constants.DEFAULT_VALUES.LAST_LOCATION_LONGITUDE
                    )
            );
            this._SharedLatLon.setLat(latitude);
            this._SharedLatLon.setLon(longitude);
        }

        HookReceiverService parent = this;
        overlayEnabled = this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.IV_OVERLAY_ENABLED,
                Constants.DEFAULT_VALUES.IV_OVERLAY_ENABLED
        );
        if (overlayEnabled) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && !Settings.canDrawOverlays(getBaseContext())) {
                Logger.fatal(Constants.LOGTAG, "IV overlay enabled but missing permissions...");
                Toast.makeText(this, "IV overlay enabled but missing permissions...", Toast.LENGTH_LONG).show();
            } else {
                this.mHandler.post(() -> {
                    overlayManager = new OverlayManager(parent, _SharedLatLon, _LocationCache != null);
                    overlayManager.setup();
                });
            }
        }

        if (_sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.ENABLE_SYSTEM_PATCHING,
                Constants.DEFAULT_VALUES.ENABLE_SYSTEM_PATCHING)) {
            this.mSystemServerPatcher.start(getApplicationContext());
        }
        String injectAfterSecondsString = _sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.INJECT_AFTER_SECONDS,
                Constants.DEFAULT_VALUES.INJECT_AFTER_SECONDS);
        if (injectAfterSecondsString != null && Long.parseLong(injectAfterSecondsString) > 0) {
            this._pogoPatcher.start(getApplicationContext());
        } else {
            Logger.pdebug("PogoEnhancerJ", "No injection delay, disabling injection");
            IvToast.showToast(this.getApplicationContext(), "Injection disabled", Gravity.BOTTOM|Gravity.CENTER, 0, 200, R.drawable.ic_frown_solid, 0);
        }


        return START_STICKY;
    }

    @NotNull
    private SpoofingConfiguration getSpoofingConfiguration() {
        SpoofingConfiguration spoofingConfiguration = new SpoofingConfiguration();
        spoofingConfiguration.spoofingEnabled = true; // TODO: kinda not needed at all..
        spoofingConfiguration.overwriteFused = this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.LOCATION_OVERWRITE_FUSED,
                Constants.DEFAULT_VALUES.LOCATION_OVERWRITE_FUSED
        );
        spoofingConfiguration.overwriteMode = Constants.getLocationOverwriteMode(this._sharedPreferences);
        String spoofingMethod = this._sharedPreferences.getString(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_METHOD,
                Constants.DEFAULT_VALUES.SPOOFING_METHOD).toLowerCase();

        if (spoofingMethod.equals("mock")) {
            spoofingConfiguration.spoofingMethod = SpoofingMethod.MOCK;
        } else {
            spoofingConfiguration.spoofingMethod = SpoofingMethod.LEGACY;
        }
        spoofingConfiguration.resetAgpsOnce = this._sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.RESET_AGPS_ONCE,
                Constants.DEFAULT_VALUES.RESET_AGPS_ONCE);
        spoofingConfiguration.resetAgpsContinuoursly = this._sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.RESET_AGPS_CONTINUOUSLY,
                Constants.DEFAULT_VALUES.RESET_AGPS_CONTINUOUSLY);
        spoofingConfiguration.resetGplayServices = this._sharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.RESET_GOOGLE_PLAY_SERVICES,
                Constants.DEFAULT_VALUES.RESET_GOOGLE_PLAY_SERVICES);
        spoofingConfiguration.enableSuspendedMocking = this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SUSPENDED_MOCKING,
                Constants.DEFAULT_VALUES.SUSPENDED_MOCKING
        );
        return spoofingConfiguration;
    }


    @Override
    public void onDestroy() {
        Logger.info("PogoEnhancerJ", "Trying to stop service");
        super.onDestroy();
        if (this.mSystemServerPatcher != null) {
            Logger.debug("PogoEnhancerJ", "Stopping system patcher");
            this.mSystemServerPatcher.stop();
        }

        if (this._pogoPatcher != null) {
            this._pogoPatcher.stop();
        }

        Logger.debug("PogoEnhancerJ", "Stopping socket");
        if (this.mThread != null) {
            this.mThread.interrupt();
        }

        sendMessageToServerSocketRunning();
        this.mThread = null;

        this.oomAdjOverrider.stop();
        try {
            unregisterReceiver(mServiceRestarter);
        } catch (IllegalArgumentException ex) {
            Logger.info("PogoEnhancerJ", "Service not registered");
        }

        if (this._LocationCache != null) {
            this._LocationCache.destruct();
            this._LocationCache = null;
        }

        if (this.overlayManager != null) {
            this.overlayManager.cleanup();
            this.overlayManager = null;
        }

        if (this._FusedLocationClient != null) {
            this._FusedLocationClient.removeLocationUpdates(this._LocationCallback);

        }

        if (this._sharedPreferences != null && !this._sharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.INTENTIONAL_STOP,
                Constants.DEFAULT_VALUES.INTENTIONAL_STOP)
        ) {
            //no intentional stop, send broadcast to restart the service
            Logger.warning("PogoEnhancerJ", "Unintentional stop of service");
            Context ctx = getApplicationContext();

            Intent startServiceIntent = new Intent(ctx, HookReceiverService.class);
            ctx.startService(startServiceIntent);
            int mPendingIntentId = 11111111;
            PendingIntent mPendingIntent = PendingIntent.getService(ctx, mPendingIntentId, startServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + Long.valueOf(2) * 1000, mPendingIntent);

        } else {
            Logger.info("PogoEnhancerJ", "Service stopped intentionally");
        }
    }

    private void sendMessageToServerSocketRunning() {
        //to unblock the .accept() in the thread
        if (this._sharedPreferences == null) {
            return;
        }
        String deviceId = this._sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID,
                Constants.DEFAULT_VALUES.DEFAULT_DEVICE_ID);
        LocalSocketAddress socketAddress = null;
        try {
            socketAddress = new LocalSocketAddress(Constants.hexadecimal(deviceId.trim()).substring(0, 5));
        } catch (UnsupportedEncodingException e) {
            Logger.fatal("PogoEnhancerJ", "Failed sending message");
            return;
        }
        LocalSocket localSocket = new LocalSocket();
        try {
            localSocket.connect(socketAddress);
        } catch (IOException e) {
            Logger.error("PogoEnhancerJ", "Failed connecting to socket to call exit: " + e.getMessage());
            return;
        }
        OutputStream outputStream = null;
        try {
            outputStream = localSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                localSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
        try {
            outputStream.write("exit".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            localSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyIdAndMoveset(int monId, int move_1, int move_2) {

        String localizedMonName = (String) this.getResources().getText(
                this.getResources().getIdentifier(
                        "pokemon_" + monId,
                        "string", "com.mad.pogoenhancer"
                ));

        String localizedMove1Name = (String) this.getResources().getText(
                this.getResources().getIdentifier(
                        "move_" + move_1,
                        "string", "com.mad.pogoenhancer"
                ));

        String localizedMove2Name = (String) this.getResources().getText(
                this.getResources().getIdentifier(
                        "move_" + move_2,
                        "string", "com.mad.pogoenhancer"
                ));

        String toBeShown = localizedMonName + " with moves:\n" + localizedMove1Name + "\n" + localizedMove2Name;
        int monicon = getResources().getIdentifier("mon_" + String.format(Locale.ENGLISH, "%03d", monId), "drawable", "com.mad.pogoenhancer");
        this.mHandler.post(() -> IvToast.showToast(getApplicationContext(), toBeShown, Gravity.BOTTOM | Gravity.CENTER, 0, 200, monicon, R.drawable.ic_sword));
    }

    public void addEncountered(long encounterId) {
        if (this.overlayManager != null) {
            this.overlayManager.addAlreadyEncountered(encounterId);
        }
    }

    public PogoPatcher getPogoPatcher() {
        return this._pogoPatcher;
    }

    private static class WorkerThread extends Thread {
        final String TAG = "HookReceiverService";

        private final LocalSocket socket;
        private final ConnectionHandler socketHandler;
        private SharedPreferences _sharedPreferences;
        private HookReceiverService _hookService;

        WorkerThread(LocalSocket socket, ConnectionHandler socketHandler,
                     SharedPreferences _sharedPreferences,
                     HookReceiverService mHookReceiverService) {
            this.socket = socket;
            this._hookService = mHookReceiverService;
            try {
                socket.setReceiveBufferSize(4096);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            this._sharedPreferences = _sharedPreferences;
            this.socketHandler = socketHandler;
        }

        @Override
        public void run() {
            try {
                socketHandler.handle(socket.getInputStream(), socket.getOutputStream(), this._sharedPreferences, this._hookService);
            } catch (IOException e) {
                Logger.error("PogoEnhancerJ", "Worker thread error " + e.toString());
            } catch (NumberFormatException e) {
                Logger.error("PogoEnhancerJ", "Number format exception: " + e.toString());
            } catch (Exception e) {
                Logger.error("PogoEnhancerJ", "Unknown exception: " + e.toString());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    Logger.warning("PogoEnhancerJ", "Unable to close socket");
                }
            }
        }
    }

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public HookReceiverService getService() {
            // Return this instance of MyService so clients can call public methods
            return HookReceiverService.this;
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        UserLoginTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String _deviceId = _shellHelper.getDeviceId();
            if (_deviceId == null || _deviceId.isEmpty()) {
                // start main activity
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("classFrom", HookReceiverService.class.toString());
                i.putExtra("bootup", true);
                getApplicationContext().startActivity(i);
                return false;
            } else {
                Logger.warning("PogoEnhancerJ", "Auth successful, setting deviceID");
                BackendStorage.getInstance().setDeviceId(_deviceId);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                        getApplicationContext()
                ).edit();
                editor.putString(Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID, _deviceId);
                editor.apply();
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                Logger.info("PogoEnhancerJ", "Service: Auth success");
            } else {
                Logger.warning("PogoEnhancerJ", "Failed logging in starting the service...");
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("classFrom", HookReceiverService.class.toString());
                i.putExtra("bootup", true);
                getApplicationContext().startActivity(i);
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    class UserLoginRunnable implements Runnable {

        UserLoginRunnable() {
        }

        public void run() {
            new UserLoginTask().execute();
        }
    }


    private class UnixServerSocketThread extends Thread {
        private final AtomicInteger threadId;
        protected boolean mStopAndRestart = false;
        protected HookReceiverService mHookReceiverService;
        String TAG = "ReceiverThread";
        private LocalServerSocket localServerSocket = null;
        private ConnectionHandler socketHandler = null;
        private SharedPreferences _sharedPreferences;

        public UnixServerSocketThread(Context context, HookReceiverService service, SharedPreferences _sharedPreferences) {
            this.mHookReceiverService = service;
            this._sharedPreferences = _sharedPreferences;
            String deviceId = this._sharedPreferences.getString(
                    Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID,
                    Constants.DEFAULT_VALUES.DEFAULT_DEVICE_ID);
            this.threadId = new AtomicInteger(0);
            try {
                Logger.debug("PogoEnhancerJ", "Creating serversocket " + Constants.hexadecimal(deviceId.trim()).substring(0, 5));
                this.localServerSocket = new LocalServerSocket(Constants.hexadecimal(deviceId.trim()).substring(0, 5));
                this.socketHandler = new ConnectionHandler();
            } catch (Exception e) {
                Logger.error("PogoEnhancerJ", "Failed opening up socket to receive data");
                e.printStackTrace();
            }

        }

        //TODO: consider trying to open socket again on certain exceptions...
        @Override
        public void run() {
            super.run();

            while (!Thread.currentThread().isInterrupted()) {
                if (Thread.currentThread().isInterrupted()) {
                    try {
                        localServerSocket.close();
                    } catch (IOException e) {
                        Logger.error("PogoEnhancerJ", "Receiving informat" +
                                "ion got interrupted");
                        e.printStackTrace();
                    }
                    return;
                }
                try {
                    if (localServerSocket == null) {
                        break;
                    }
                    final LocalSocket socket = localServerSocket.accept();

                    final WorkerThread workerThread = new WorkerThread(socket, socketHandler, this._sharedPreferences, mHookReceiverService);
                    workerThread.setName("SuasUnixSocket " + threadId.incrementAndGet());
                    workerThread.setDaemon(true);
                    workerThread.start();

                } catch (Exception e) {
                    Logger.error("PogoEnhancerJ", "Server thread error " + e.toString());
                    e.printStackTrace();
                    break;
                }
            }
            try {
                if (localServerSocket != null) {
                    localServerSocket.close();
                }
            } catch (IOException e) {
                Logger.error("PogoEnhancerJ", "Could not close socket");
                e.printStackTrace();
            }
        }
    }


}
