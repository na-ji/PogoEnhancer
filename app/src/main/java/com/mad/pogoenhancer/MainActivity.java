package com.mad.pogoenhancer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.mad.pogoenhancer.services.HookReceiverService;
import com.topjohnwu.superuser.Shell;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Bundle extras = getIntent().getExtras();
//        if (!checkAuthenticated(extras)) {
//            return;
//        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gpx, R.id.nav_slideshow,
                R.id.nav_tools)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        /*navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(destination.getId() == R.id.nav_home && arguments != null) {
                NavArgument arg = new NavArgument.Builder().setType(NavType.FloatType).setDefaultValue(123.013f).build();
                /*destination.addArgument("lat", arg);
                controller.getGraph().addArgument("lat", arg);//
                destination.addArgument("lat", arg);
                arguments.putFloat("lat", 133.004f);
            }
        });*/
        /*Bundle derp = new Bundle();
        derp.putFloat("lat", 100.999f);
        navController.setGraph(navController.getGraph(), derp);*/
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    protected void onResume() {
        if (this.getIntent() != null) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            Intent intent = getIntent();
            if (intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null && extras.containsKey("lat") && extras.containsKey("lon")) {
                    //Logger.info("PogoEnhancerJ", "Adding lat/lon");
                    float lat = extras.getFloat("lat");
                    float lon = extras.getFloat("lon");
                    lat = lat % 90; // java returns negative remainders :)
                    lon = lon % 180;

                    Bundle args = new Bundle();
                    args.putFloat("lat", lat);
                    args.putFloat("lon", lon);
                    navController.setGraph(navController.getGraph(), args);
                }
            }
        }

        super.onResume();
    }

    private boolean checkAuthenticated(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Intent myIntent = new Intent(this, LoginActivity.class);
            this.startActivity(myIntent);
            Logger.fatal("PogoEnhancerJ", "Not authenticated");
            return false;
        }
        String from = savedInstanceState.getString("classFrom");
        if (from == null
                || (!from.equals(LoginActivity.class.toString())
                && !from.equals(HookReceiverService.class.toString()))
        ) {
            android.os.Process.killProcess(android.os.Process.myPid());
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Context context = this.getApplicationContext();
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setOnMenuItemClickListener(item1 -> {
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            startActivityIfNeeded(settingsIntent, 0);
            return true;
        });
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}
