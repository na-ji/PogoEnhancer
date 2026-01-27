package com.mad.pogoenhancer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mad.pogoenhancer.services.HookReceiverService;
import com.mad.pogoenhancer.utils.Logcat;
import com.mad.pogoenhancer.utils.PreferenceSerializer;
import com.mad.pogoenhancer.utils.ShellHelper;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mTokenView;
    private View mProgressView;
    private View mLoginFormView;
    private boolean onBootCalled = false;
    private boolean onRestartCalled = false;
    private SharedPreferences _sharedPreferences = null;
    private Handler mHandler;
    private ShellHelper _shellHelper = new ShellHelper();

    private Pattern googleMapsWebUrl = Pattern.compile(".*@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+),.*");
    private Pattern googleMapsWebUrlShort = Pattern.compile("^.*=(-?\\d+\\.\\d+),(-?\\d+.\\d+).*$");
    private Pattern geoFallback = Pattern.compile("^.*:(-?\\d+\\.\\d+),(-?\\d+\\.\\d+).*$");

    private Float latRecv = null;
    private Float lonRecv = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.info("PogoEnhancerJ", "Showing login");
        setContentView(R.layout.activity_login);

        mEmailView = findViewById(R.id.email);
        populateAutoComplete();

        Intent intentReceived = getIntent();
        String action = intentReceived.getAction();
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            // we likely received a geo intent...
            String dataString = intentReceived.getDataString();
            if (dataString != null) {
                extractLatlon(dataString);
            }
            Logger.info("PogoEnhancerJ", "Geo intent");
        } else {
            Logger.info("PogoEnhancerJ", "Other intent");
        }

        onBootCalled = getIntent().getBooleanExtra("bootup", false);
        onRestartCalled = getIntent().getBooleanExtra("restart", false);
        this.mHandler = new Handler();

        mTokenView = findViewById(R.id.token);
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mTokenView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button mTokenSignInButton = findViewById(R.id.token_sign_in_button);
        mTokenSignInButton.setOnClickListener(view -> attemptLogin());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void extractLatlon(String dataString) {
        // now check the dataString with regexes...
        if (dataString == null) {
            return;
        }
        Matcher matcher = googleMapsWebUrl.matcher(dataString);
        if (matcher.matches()) {
            this.latRecv = Float.valueOf(matcher.group(1));
            this.lonRecv = Float.valueOf(matcher.group(2));
        } else {
            matcher = googleMapsWebUrlShort.matcher(dataString);
            if (matcher.matches()) {
                this.latRecv = Float.valueOf(matcher.group(1));
                this.lonRecv = Float.valueOf(matcher.group(2));
            } else {
                matcher = geoFallback.matcher(dataString);
                if (matcher.matches()) {
                    this.latRecv = Float.valueOf(matcher.group(1));
                    this.lonRecv = Float.valueOf(matcher.group(2));
                }
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        File file = new File("/storage/emulated/0/settings_pogodroid.pgdr");

        if (mTokenView.getText().length() > 4) {
            attemptLogin();
        } else if (file.exists() && file.isFile()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Settings backup present");

            // set dialog message
            alertDialogBuilder
                    .setMessage("A backup of settings is present on the filesystem. Do you want to import it?")
                    .setCancelable(false)
                    .setPositiveButton("Import", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            importSettings(null);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    }

    private void populateAutoComplete() {
        // TODO: check root permission

        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mTokenView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String token = mTokenView.getText().toString();

        String deviceId = _shellHelper.getDeviceId();
        if (deviceId == null || deviceId.trim().isEmpty()) {
            mTokenView.setError(getString(R.string.error_login_failed_getting_deviceid));
            return;
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(token)) {
            mTokenView.setError(getString(R.string.error_incorrect_token));
            focusView = mTokenView;
            cancel = true;
        } else if (!isTokenValid(token)) {
            mTokenView.setError(getString(R.string.error_invalid_token));
            focusView = mTokenView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email.trim(), deviceId.trim(), token.trim(), this);
            mAuthTask.execute();
        }
    }

    private boolean isTokenValid(String token) {
        //TODO: Replace this with your own logic
        return token.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return email.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public void importSettings(View v) {
        new ImportTask(this).execute();
    }

    public void showDeviceId(View v) {
        // Store values at the time of the login attempt.
        String deviceId = _shellHelper.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            mTokenView.setError(getString(R.string.error_login_failed_getting_deviceid));
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("DeviceID");

        // set dialog message
        alertDialogBuilder
                .setMessage("Device ID: " + deviceId)
                .setCancelable(false)
                .setNegativeButton("OK", (dialog, id) -> dialog.dismiss());
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void stopService(View v) {
        SharedPreferences.Editor editor = this._sharedPreferences.edit();
        editor.putBoolean(
                Constants.SHAREDPERFERENCES_KEYS.INTENTIONAL_STOP,
                true
        );
        editor.apply();
        Logger.debug("PogoEnhancerJ", "Trying to stop service");
        Intent hookReceiverIntent = new Intent(this, HookReceiverService.class);
        stopService(hookReceiverIntent);
    }

    public void getSupport(View v) {
        Logcat logcat = new Logcat(v.getContext());
        logcat.execute();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String _deviceId;
        private final String _userId;
        private final String _token;
        private final LoginActivity parent;
        private int errorCode = -1;

        UserLoginTask(String userId, String deviceId, String token, LoginActivity activity) {
            this._userId = userId;
            this._deviceId = deviceId;
            this._token = token;
            this.parent = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            //Store the token and device ID in shared prefs
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext()
            ).edit();
            editor.putString(Constants.SHAREDPERFERENCES_KEYS.DEVICE_ID, this._deviceId);
            BackendStorage.getInstance().setDeviceId(this._deviceId);
            editor.apply();


//                editor.apply();
            finish();
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("classFrom", LoginActivity.class.toString());
            if (onBootCalled) {
//                    Logger.fatal("ProtoHook", "Adding onBoot flag to intent");
                myIntent.putExtra("onBoot", true);
            } else if (onRestartCalled) {

            }
            if (latRecv != null) {
                myIntent.putExtra("lat", latRecv);
            }
            if (lonRecv != null) {
                myIntent.putExtra("lon", lonRecv);
            }
            myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityIfNeeded(myIntent, 1211);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private class ImportTask extends AsyncTask<String, Void, Boolean> {
        /**
         * application context.
         */
        private AppCompatActivity activity;
        private ProgressDialog _dialog;

        //initiate vars
        public ImportTask(AppCompatActivity activity) {
            this.activity = activity;
            this._dialog = new ProgressDialog(activity);
        }

        protected void onPreExecute() {
            this._dialog.setMessage("Please wait...");
            this._dialog.show();
        }

        protected Boolean doInBackground(String... params) {
            PreferenceSerializer preferenceSerializer = new PreferenceSerializer(
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
            );
            return preferenceSerializer.importSettings("/storage/emulated/0/pogosettings_pogoenhancer.pgdr");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //do stuff on UI thread
            this._dialog.dismiss();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this.activity);
            if (result) {
                // set title
                alertDialogBuilder.setTitle("Import successful");

                // set dialog message
                alertDialogBuilder
                        .setMessage("The import was successful, logging in automatically")
                        .setCancelable(false)
                        .setNegativeButton("OK", (dialog, id) -> {
                            dialog.dismiss();
                            attemptLogin();
                        });
            } else {
                alertDialogBuilder.setTitle("Import failed");

                // set dialog message
                alertDialogBuilder
                        .setMessage("The import failed. Make sure that the file at '/storage/emulated/0/download/pogosettings_pogodroid.pgdr' exists and is a valid settings file.")
                        .setCancelable(false)
                        .setNegativeButton("OK", (dialog, id) -> dialog.dismiss());
            }
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    }
}

