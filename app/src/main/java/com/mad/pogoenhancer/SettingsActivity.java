package com.mad.pogoenhancer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.core.app.NavUtils;

import com.mad.pogoenhancer.utils.Logcat;
import com.mad.pogoenhancer.utils.PreferenceSerializer;

import java.util.List;

import static com.mad.pogoenhancer.utils.RepackageApk.hidePogoEnhancer;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    static String TAG = "SettingsActivity";
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        Logger.info("PogoEnhancerJ", "Starting settings");

//        if(!AccessChecker.ivOverlayAllowed()) {
//            findViewById(R.id.iv_overlay_enabled_switch).setEnabled(false);
//        } else {
//            findViewById(R.id.iv_overlay_enabled_switch).setEnabled(false); //TODO/ remove
//        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || OverlayPreferenceFragment.class.getName().equals(fragmentName)
                || AppPreferenceFragment.class.getName().equals(fragmentName)
                || LibrariesUsedFragment.class.getName().equals(fragmentName)
                || LocationPreferenceFragment.class.getName().equals(fragmentName)
                || AdditionalPreferenceFragment.class.getName().equals(fragmentName)
                || SupportPreferenceFragment.class.getName().equals(fragmentName)
                || InventoryCleanFragment.class.getName().equals(fragmentName)
                || NameReplaceFragment.class.getName().equals(fragmentName)
                || WildmonFragment.class.getName().equals(fragmentName);
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AppPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start App Preferences");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_app);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(Constants.SHAREDPERFERENCES_KEYS.BOOT_DELAY));

            Preference button = findPreference(getString(R.string.repackage_button));
            button.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());

                alertDialogBuilder.setTitle("Repackage");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Do you really want to repackage this app with a random name?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (!getActivity().getApplicationContext().getPackageManager().canRequestPackageInstalls()) {
                                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getActivity().getApplicationContext().getPackageName()))), 1234);
                                } else {
                                    ProgressDialog dialog = ProgressDialog.show(this.getContext(),
                                            this.getString(R.string.hide_manager_toast),
                                            this.getString(R.string.hide_manager_toast));
                                    hidePogoEnhancer(getActivity(), getActivity().getApplicationContext(), dialog, null);
                                }
                            } else {
                                ProgressDialog dialog = ProgressDialog.show(this.getContext(),
                                        this.getString(R.string.hide_manager_toast),
                                        this.getString(R.string.hide_manager_toast));
                                hidePogoEnhancer(getActivity(), getActivity().getApplicationContext(), dialog, null);                            }
                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.dismiss());

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                return true;
            });

            Preference exportSettingsButton = findPreference(getString(R.string.export_settings_button));
            exportSettingsButton.setOnPreferenceClickListener(preference -> {
                //TODO: show popup with success true/false
                PreferenceSerializer preferenceSerializer = new PreferenceSerializer(
                        PreferenceManager.getDefaultSharedPreferences(
                                getActivity().getApplicationContext()
                        )
                );
                boolean success = preferenceSerializer.exportSettings();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Settingsexport");
                if (success) {
                    alertDialogBuilder.setMessage("Successfully exported settings.");
                } else {
                    alertDialogBuilder.setMessage("Failed exporting settings.");
                }
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class OverlayPreferenceFragment extends PreferenceFragment {
        private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
        private static final int CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE = 101;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start Overlay Preferences");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_overlay);
            setHasOptionsMenu(true);

            SwitchPreference ivSwitch = (SwitchPreference) findPreference(Constants.SHAREDPERFERENCES_KEYS.IV_OVERLAY_ENABLED);
            ivSwitch.setEnabled(false);

            findPreference(Constants.SHAREDPERFERENCES_KEYS.IV_OVERLAY_ENABLED).setOnPreferenceClickListener(
                    preference -> {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
                                && !Settings.canDrawOverlays(getActivity().getBaseContext())) {
                            //check if we already have the permission to show overlays
                            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getBaseContext().getPackageName()));
                            startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
                        }
                        return true;
                    }
            );
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE) {
                showFloatingView(getActivity(), false, true);
            }
        }

        @SuppressLint("NewApi")
        private void showFloatingView(Context context, boolean isShowOverlayPermission, boolean isCustomFloatingView) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                return;
            }

            if (Settings.canDrawOverlays(context)) {
                return;
            }

            if (isShowOverlayPermission) {
                final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LibrariesUsedFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start LibrariesUsed Preferences");

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_libraries_used);
            setHasOptionsMenu(true);

            findPreference("libraries_frida").setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/frida/frida/blob/master/COPYING"));
                startActivity(browserIntent);
                return false;
            });

            findPreference("libraries_json11").setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dropbox/json11/blob/master/LICENSE.txt"));
                startActivity(browserIntent);
                return false;
            });

            findPreference("libraries_pogoprotos").setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Furtif/POGOProtos/blob/master/LICENSE.md"));
                startActivity(browserIntent);
                return false;
            });

            findPreference("libraries_curl").setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://curl.haxx.se/docs/copyright.html"));
                startActivity(browserIntent);
                return false;
            });

            findPreference("library_smashicons").setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.flaticon.com/authors/smashicons"));
                startActivity(browserIntent);
                return false;
            });

            findPreference("library_gymicon").setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.flaticon.com/de/autoren/roundicons-freebies"));
                startActivity(browserIntent);
                return false;
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LocationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start Location Preferences");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_location);
            setHasOptionsMenu(true);

//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences();
            boolean systemized = (getActivity().getApplicationContext()
                    .getApplicationInfo().flags
                    & ApplicationInfo.FLAG_SYSTEM) != 0;
            if (systemized) {
                findPreference(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_ENABLED).setEnabled(true);
            }

            Preference recommendedAndroid10 = findPreference(getString(R.string.android_10_load_settings_button));
            recommendedAndroid10.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Load settings");
                alertDialogBuilder.setMessage("Do you want to load Android 10 recommended location settings? If the settings do not work straight away, do try a reboot." +
                        "Do not forget to reduce device location (Android settings) to GPS only / disable Google Location Accuracy.");
                // set dialog message
                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, id) ->
                        {
                            SharedPreferences.Editor editor = preference.getEditor();
                            findPreference(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_ENABLED).setEnabled(true);

                            //editor.putString(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_METHOD, "Mock");
                            ((ListPreference) findPreference(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_METHOD)).setValue("Mock");

                            //editor.putBoolean(Constants.SHAREDPERFERENCES_KEYS.LOCATION_OVERWRITE_FUSED, false);
                            ((SwitchPreference) findPreference(Constants.SHAREDPERFERENCES_KEYS.LOCATION_OVERWRITE_FUSED)).setChecked(false);

                            //editor.putString(Constants.SHAREDPERFERENCES_KEYS.LOCATION_OVERWRITE_METHOD, "Indirect");
                            ((ListPreference) findPreference(Constants.SHAREDPERFERENCES_KEYS.LOCATION_OVERWRITE_METHOD)).setValue("Indirect");

                            //editor.putBoolean(Constants.SHAREDPERFERENCES_KEYS.SUSPENDED_MOCKING, false);
                            ((SwitchPreference) findPreference(Constants.SHAREDPERFERENCES_KEYS.SUSPENDED_MOCKING)).setChecked(true);

                            //editor.putBoolean(Constants.SHAREDPERFERENCES_KEYS.RESET_AGPS_CONTINUOUSLY, false);
                            ((SwitchPreference) findPreference(Constants.SHAREDPERFERENCES_KEYS.RESET_AGPS_CONTINUOUSLY)).setChecked(false);

                            //editor.putBoolean(Constants.SHAREDPERFERENCES_KEYS.RESET_AGPS_ONCE, false);
                            ((SwitchPreference) findPreference(Constants.SHAREDPERFERENCES_KEYS.RESET_AGPS_ONCE)).setChecked(true);

                            //editor.putBoolean(Constants.SHAREDPERFERENCES_KEYS.RESET_GOOGLE_PLAY_SERVICES, false);
                            ((SwitchPreference) findPreference(Constants.SHAREDPERFERENCES_KEYS.RESET_GOOGLE_PLAY_SERVICES)).setChecked(true);

                            //editor.apply();
                            dialog.dismiss();
                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.dismiss());
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            });

            findPreference(Constants.SHAREDPERFERENCES_KEYS.SPOOF_SPEED_RUN_KMPH)
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            String newVal = (String) newValue;

                            if (newVal.matches("^\\d+(\\.\\d+)?$")) {
                                return true;
                            } else {
                                preference.getEditor().putFloat(Constants.SHAREDPERFERENCES_KEYS.SPOOF_SPEED_RUN_KMPH, 30.0f).apply();
                                Context applicationContext = getActivity().getApplicationContext();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(applicationContext);
                                alertDialogBuilder.setTitle("Invalid input");

                                // set dialog message
                                alertDialogBuilder
                                        .setMessage("Invalid input/format. " +
                                                "Please enter a valid floating point number representing km/h such as 30.0")
                                        .setCancelable(false)
                                        .setNegativeButton("OK", (dialog, id) -> dialog.dismiss());
                                // create alert dialog
                                AlertDialog alertDialog = alertDialogBuilder.create();

                                // show it
                                alertDialog.show();
                            }
                            return false;
                        }
                    });
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AdditionalPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start Additional Preferences");

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_additional);
            setHasOptionsMenu(true);

            Preference pref_nearby_notify_all = findPreference("pref_nearby_notify_all");
            Preference pref_nearby_remove_notify_all = findPreference("pref_nearby_remove_notify_all");
            MonIdPreferenceListPicker notifyNearbyPicker = (MonIdPreferenceListPicker) findPreference(Constants.SHAREDPERFERENCES_KEYS.MON_NOTIFICATION_IDS_NEARBY);
            pref_nearby_notify_all.setOnPreferenceClickListener(preference -> {
                notifyNearbyPicker.selectAll();
                return true;
            });
            pref_nearby_remove_notify_all.setOnPreferenceClickListener(preference -> {
                notifyNearbyPicker.deselectAll();
                return true;
            });

            Preference pref_not_transfered_unhide_all = findPreference("pref_not_transfered_unhide_all");
            MonIdPreferenceListPicker notTransferPicker = (MonIdPreferenceListPicker) findPreference(Constants.SHAREDPERFERENCES_KEYS.MON_NOT_TRANSFERED_IDS);
            pref_not_transfered_unhide_all.setOnPreferenceClickListener(preference -> {
                notTransferPicker.deselectAll();
                return true;
            });

            bindPreferenceSummaryToValue(findPreference(Constants.SHAREDPERFERENCES_KEYS.ENHANCED_CAPTURE));
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class InventoryCleanFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start Inventory Management");

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_inventory_management);
            setHasOptionsMenu(true);

        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NameReplaceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start name replace");

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_name_replace);
            setHasOptionsMenu(true);

        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class WildmonFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start wildmon settings");

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_wildmon);
            setHasOptionsMenu(true);

            Preference pref_nearby_hide_all = findPreference("pref_nearby_hide_all");
            Preference pref_nearby_unhide_all = findPreference("pref_nearby_unhide_all");
            MonIdPreferenceListPicker hideNearbyPicker = (MonIdPreferenceListPicker) findPreference(Constants.SHAREDPERFERENCES_KEYS.MON_HIDE_IDS_NEARBY);
            pref_nearby_hide_all.setOnPreferenceClickListener(preference -> {
                hideNearbyPicker.selectAll();
                return true;
            });
            pref_nearby_unhide_all.setOnPreferenceClickListener(preference -> {
                hideNearbyPicker.deselectAll();
                return true;
            });

        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SupportPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Logger.info("PogoEnhancerJ","Trying to start support Preferences");

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_support);
            setHasOptionsMenu(true);

            findPreference("send_logcat").setOnPreferenceClickListener(preference -> {
                Logcat logcat = new Logcat(preference.getContext());
                logcat.execute();
                return true;
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}

