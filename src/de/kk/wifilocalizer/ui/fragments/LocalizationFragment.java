package de.kk.wifilocalizer.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.kk.wifilocalizer.R;
import de.kk.wifilocalizer.core.CoreManager;
import de.kk.wifilocalizer.ui.activities.MainActivity;
import de.kk.wifilocalizer.ui.views.MapView;

/**
 * Handles Localization mode
 */
public class LocalizationFragment extends Fragment {
    private static final String INIT_STRING = "localization_initstring";
    private static final String ARG_MAPNAME = "localization_mapname";
    private static final String ARG_FILEPATH = "localization_filepath";

    @SuppressWarnings("unused")
    private String mInitString;
    private String mMapname;
    private String mFilepath;

    private int mRefreshInterval;
    private boolean mLocalizingOn = false;
    private boolean mMapIsReady = false;
    private Handler mHandler;
    private Runnable mRunnable;

    private MapView mMapView;

    public LocalizationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters
     * 
     * @param mapname
     *            unique name either passed by MapsFragment or null if no map available
     * @param filepath
     *            passed by MapsFragment
     * @return A new instance of fragment LocalizationFragment
     */
    public static LocalizationFragment newInstance(String initString, String mapname, String filepath) {
        LocalizationFragment fragment = new LocalizationFragment();
        Bundle args = new Bundle();
        args.putString(INIT_STRING, initString);
        args.putString(ARG_MAPNAME, mapname);
        args.putString(ARG_FILEPATH, filepath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mInitString = getArguments().getString(INIT_STRING);
            mMapname = getArguments().getString(ARG_MAPNAME);
            mFilepath = getArguments().getString(ARG_FILEPATH);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRefreshInterval = Integer.parseInt(sharedPref.getString(MainActivity.PREF_POSITION_REFRESH_INTERVAL, "1000"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_localization, container, false);

        mMapView = (MapView) view.findViewById(R.id.mapview);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mLocalizingOn) {
                    Log.d("LocalFragment", "interval:" + String.valueOf(mRefreshInterval)); // LOG
                    if (CoreManager.getPosition() != null)
                        mMapView.drawEgoPosition(CoreManager.getPosition());
                    mHandler.postDelayed(this, mRefreshInterval);
                }
            }
        };

        if (mMapname != null && mFilepath != null) {
            mMapIsReady = true;
        }

        getActivity().setTitle(getString(R.string.localization_fragment));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMapIsReady) {
            mMapView.setUpMap(getActivity(), mMapname, mFilepath);
            startLocalizing();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocalizing();
    }

    /**
     * Starts localizing thread
     */
    public void startLocalizing() {
        mLocalizingOn = true;
        mHandler.post(mRunnable);
    }

    /**
     * Stops localizing thread
     */
    public void stopLocalizing() {
        mLocalizingOn = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
        case R.id.action_showHidePoints:
            mMapView.toggleShowHidePoints();
            return true;
        case R.id.action_removeMeasuredPoints:
            if (mMapname != null) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.localizationfragment_alert_dialog_remove_points_title)
                        .setMessage(R.string.localizationfragment_alert_dialog_remove_points_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CoreManager.removeMeasuredPositions();
                                mMapView.invalidate();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Close dialog
                            }
                        }).show();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
