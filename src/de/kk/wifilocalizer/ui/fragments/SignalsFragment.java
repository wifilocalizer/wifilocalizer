package de.kk.wifilocalizer.ui.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import de.kk.wifilocalizer.R;
import de.kk.wifilocalizer.core.CoreManager;
import de.kk.wifilocalizer.core.helper.Signal;
import de.kk.wifilocalizer.ui.activities.MainActivity;

/**
 * Shows a screen with fetched WiFi signals
 */
public class SignalsFragment extends Fragment {
    private static final String INIT_STRING = "signals_initstring";

    @SuppressWarnings("unused")
    private String mInitString;
    private int mRefreshInterval;
    private boolean mFetchingOn = false;

    private List<Signal> mSignalList;
    private Handler mHandler;
    private Runnable mRunnable;

    public static List<Signal> WIFI_SIGNALS = new ArrayList<Signal>();

    private AbsListView mListView;
    private ListAdapter mAdapter;

    public SignalsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters
     * 
     * @return A new instance of fragment SignalsFragment
     */
    public static SignalsFragment newInstance(String initString) {
        SignalsFragment fragment = new SignalsFragment();
        Bundle args = new Bundle();
        args.putString(INIT_STRING, initString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mInitString = getArguments().getString(INIT_STRING);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRefreshInterval = Integer.parseInt(sharedPref.getString(MainActivity.PREF_SIGNALLIST_REFRESH_INTERVAL, "1000"));

        mAdapter = new ArrayAdapter<Signal>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1,
                WIFI_SIGNALS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signals, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("SigFragment", "interval:" + String.valueOf(mRefreshInterval)); // LOG
                WIFI_SIGNALS.clear();
                if (mFetchingOn) {
                    // get latest signals
                    mSignalList = CoreManager.getSignalList();
                    for (int i = 0; i < mSignalList.size(); i++) {
                        WIFI_SIGNALS.add(mSignalList.get(i));
                    }
                }
                ((BaseAdapter) mAdapter).notifyDataSetChanged();
                if (mFetchingOn) {
                    mHandler.postDelayed(this, mRefreshInterval);
                }
            }
        };

        getActivity().setTitle(getString(R.string.signals_fragment));
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
        mFetchingOn = true;
        mHandler.post(mRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        mFetchingOn = false;
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
