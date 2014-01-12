package de.kk.wifilocalizer.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;

import de.kk.wifilocalizer.R;
import de.kk.wifilocalizer.core.CoreManager;
import de.kk.wifilocalizer.ui.activities.MainActivity;
import de.kk.wifilocalizer.ui.activities.SettingsActivity;

/**
 * Responsible for NavigationDrawer
 */
public class NavDrawerFragment extends Fragment {
    private NavDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private CharSequence mTitle, mDrawerTitle;
    private String[] mFragmentTitles;
    private CompoundButton toggle;

    public NavDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get info whether user has used navigation drawer in the past
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(MainActivity.NAVDRAWER_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(MainActivity.NAVDRAWER_STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        mTitle = mDrawerTitle = getString(R.string.app_name);

        // Select either the default item (0) or the last selected item
        selectItem(mCurrentSelectedPosition);
        mFragmentTitles = getResources().getStringArray(R.array.fragments_array);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navdrawer, container, false);
        mDrawerListView = (ListView) view.findViewById(R.id.navdrawerlist);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentSelectedPosition == position)
                    mDrawerLayout.closeDrawer(mFragmentContainerView);
                else
                    selectItem(position);
            }
        });
        mDrawerListView.setAdapter(new ArrayAdapter<String>(getActionBar().getThemedContext(), R.layout.navdrawer_list_item,
                R.id.list_item, mFragmentTitles));
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        toggle = (CompoundButton) view.findViewById(R.id.togglebutton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (isChecked) {
                    CoreManager.startFetching();
                    sp.edit().putBoolean(MainActivity.WIFI_FETCHING_TOGGLE_ON, true).commit();
                } else {
                    CoreManager.stopFetching();
                    sp.edit().putBoolean(MainActivity.WIFI_FETCHING_TOGGLE_ON, false).commit();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar
        setHasOptionsMenu(true);
    }

    /**
     * Info about navigation drawer
     * 
     * @return state of navigation drawer
     */
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     * 
     * @param fragmentId
     *            The android:id of this fragment in its activity's layout.
     * @param drawerLayout
     *            The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /*
                                                                  * host Activity
                                                                  */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
        R.string.navigation_drawer_open, /*
                                          * "open drawer" description for accessibility
                                          */
        R.string.navigation_drawer_close /*
                                          * "close drawer" description for accessibility
                                          */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActionBar().setTitle(mTitle);
                getActivity().supportInvalidateOptionsMenu(); // calls
                                                              // onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to
                    // prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(MainActivity.NAVDRAWER_USER_LEARNED_DRAWER, true).commit();
                }

                getActionBar().setTitle(mDrawerTitle);
                getActivity().supportInvalidateOptionsMenu(); // calls
                                                              // onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce
        // them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        setTitle(mFragmentTitles[mCurrentSelectedPosition]);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            setTitle(mFragmentTitles[position]);
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavDrawerItemSelected(position);
        }
    }

    // method for updating NavDrawer mainly for MapsFragment-onMapSelected()
    public void updateNavDrawer(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            setTitle(mFragmentTitles[position]);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MainActivity.NAVDRAWER_STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!isDrawerOpen()) {
            if (mTitle.equals(getString(R.string.localization_fragment))) {
                menu.findItem(R.id.action_add).setVisible(false);
                menu.findItem(R.id.action_showHidePoints).setVisible(true);
                menu.findItem(R.id.action_removeMeasuredPoints).setVisible(true);
            } else if (mTitle.equals(getString(R.string.signals_fragment))) {
                menu.findItem(R.id.action_add).setVisible(false);
                menu.findItem(R.id.action_showHidePoints).setVisible(false);
                menu.findItem(R.id.action_removeMeasuredPoints).setVisible(false);
            } else if (mTitle.equals(getString(R.string.maps_fragment))) {
                menu.findItem(R.id.action_add).setVisible(true);
                menu.findItem(R.id.action_showHidePoints).setVisible(false);
                menu.findItem(R.id.action_removeMeasuredPoints).setVisible(false);
            }
        } else {
            menu.findItem(R.id.action_add).setVisible(false);
            menu.findItem(R.id.action_showHidePoints).setVisible(false);
            menu.findItem(R.id.action_removeMeasuredPoints).setVisible(false);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons, rest in responsible fragments
        switch (item.getItemId()) {
        case R.id.action_settings:
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        case R.id.action_help:
            new AlertDialog.Builder(getActivity()).setTitle(R.string.help_dialog_title)
                    .setMessage(R.string.help_dialog_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    }).show();
            return true;
        case R.id.action_about:
            new AlertDialog.Builder(getActivity()).setTitle(R.string.about_dialog_title)
                    .setMessage(R.string.about_dialog_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    }).show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavDrawerItemSelected(int position);
    }
}
