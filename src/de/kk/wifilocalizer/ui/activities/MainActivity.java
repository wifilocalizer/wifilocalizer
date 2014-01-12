package de.kk.wifilocalizer.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;

import de.kk.wifilocalizer.R;
import de.kk.wifilocalizer.core.CoreManager;
import de.kk.wifilocalizer.ui.fragments.LocalizationFragment;
import de.kk.wifilocalizer.ui.fragments.MapsFragment;
import de.kk.wifilocalizer.ui.fragments.NavDrawerFragment;
import de.kk.wifilocalizer.ui.fragments.SignalsFragment;

/**
 * Starting point off WifiLocalizer app
 */
public class MainActivity extends ActionBarActivity implements NavDrawerFragment.NavDrawerCallbacks,
        MapsFragment.MapsCallbacks {
    // SharedPreferences strings and strings for saving InstanceStates
    public static final String NAVDRAWER_STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    public static final String NAVDRAWER_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    public static final String WIFI_FETCHING_TOGGLE_ON = "wifi_fetching_toggle_on";
    public static final String PREF_USE_GEOMETRIC_MEAN = "pref_use_geometric_mean";
    public static final String PREF_BSSID_FILTER = "pref_bssid_filter";
    public static final String PREF_WIFI_FETCH_INTERVAL = "pref_extended_wifi_fetching_interval";
    public static final String PREF_POSITION_REFRESH_INTERVAL = "pref_position_refresh_fetching_interval";
    public static final String PREF_SIGNALLIST_REFRESH_INTERVAL = "pref_extended_signallist_refresh_interval";
    public static final String MAPS_LIST_STORED = "maps_list_stored";

    private static final String INIT_STRING = "main_initstring";
    private static final String ARG_MAPNAME = "main_mapname";
    private static final String ARG_FILEPATH = "main_filepath";

    private String mInitString;
    private String mMapname;
    private String mFilepath;
    private NavDrawerFragment mNavDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mInitString = savedInstanceState.getString(INIT_STRING);
            mMapname = savedInstanceState.getString(ARG_MAPNAME);
            mFilepath = savedInstanceState.getString(ARG_FILEPATH);
        }

        CoreManager.init(this);
        CoreManager.persist();
        setContentView(R.layout.activity_main);

        mNavDrawerFragment = (NavDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // set up the drawer
        mNavDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();
        CoreManager.registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CoreManager.unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CoreManager.persist();
        CoreManager.stopFetching();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putBoolean(MainActivity.WIFI_FETCHING_TOGGLE_ON, false).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INIT_STRING, mInitString);
        outState.putString(ARG_MAPNAME, mMapname);
        outState.putString(ARG_FILEPATH, mFilepath);
    }

    // Callback method of NavDrawerFragment
    @Override
    public void onNavDrawerItemSelected(int position) {
        // Update the main content by replacing fragments
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        switch (position) {
        case 0:
            fragmentTransaction.replace(R.id.container, LocalizationFragment.newInstance(mInitString, mMapname, mFilepath));
            break;
        case 1:
            fragmentTransaction.replace(R.id.container, SignalsFragment.newInstance(mInitString));
            break;
        case 2:
            fragmentTransaction.replace(R.id.container, MapsFragment.newInstance(mInitString));
            break;
        }

        fragmentTransaction.commit();
    }

    // Callback method of MapsFragment
    @Override
    public void onMapSelected(String mapname, String filepath) {
        mMapname = mapname;
        mFilepath = filepath;
        CoreManager.setMap(mMapname);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, LocalizationFragment.newInstance(mInitString, mMapname, mFilepath)).commit();
        mNavDrawerFragment.updateNavDrawer(0);
    }
}
