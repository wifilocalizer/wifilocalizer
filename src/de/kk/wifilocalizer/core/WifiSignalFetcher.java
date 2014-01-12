package de.kk.wifilocalizer.core;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import de.kk.wifilocalizer.core.helper.Signal;
import de.kk.wifilocalizer.ui.activities.MainActivity;

/**
 * Fetches WiFi ScanResult from the device with an interval defined in settings menu and puts it in a SignalList
 */
public class WifiSignalFetcher {
    private Context mContext;
    private WifiManager mWifiManager;
    private ScanResult mScanResult;
    private List<ScanResult> mResultList;
    private List<Signal> mTempSignals;

    private int mFetchInterval;
    private boolean mFetchingOn = false;
    private BroadcastReceiver mReceiver;
    private Handler mHandler;
    private Runnable mRunnable;
    private SharedPreferences sp;

    /**
     * Instantiates variables and defines runnable for fetching ScanResult
     * 
     * @param context
     * @param sigList
     */
    public WifiSignalFetcher(Context context) {
        mContext = context;
        mTempSignals = new ArrayList<Signal>();
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mFetchingOn) {
                    mFetchInterval = Integer.parseInt(sp.getString(MainActivity.PREF_WIFI_FETCH_INTERVAL, "1000"));
                    Log.d("WifiFetcher", "interval:" + String.valueOf(mFetchInterval)); // LOG
                    fetchSignals();
                    mHandler.postDelayed(this, mFetchInterval);
                }
            }
        };
    }

    /**
     * Registers new receiver which listens to IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) onReceive()-method:
     * gets ScanResults and puts it in a SignalList
     */
    public void registerReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mResultList = mWifiManager.getScanResults();
                mTempSignals.clear(); // clear the temporary signallist to fill in new ScanResults
                String filter = sp.getString(MainActivity.PREF_BSSID_FILTER, "");
                for (int i = 0; i < mResultList.size(); i++) {
                    mScanResult = mResultList.get(i);
                    // generate a new Signal to store the result
                    Signal signal = new Signal();
                    // Checks for possible filter, defined in settings
                    if (filter.equals("")) {
                        if (mScanResult.SSID.equals(""))
                            signal.setSsid("---HIDDEN SSID---");
                        else
                            signal.setSsid(mScanResult.SSID);
                        signal.setBssid(mScanResult.BSSID);
                        signal.setLevel(mScanResult.level);
                        mTempSignals.add(signal); // add the Signal to the temporary list
                    } else {
                        if (filter.equals(mScanResult.SSID)) {
                            signal.setSsid(mScanResult.SSID);
                            signal.setBssid(mScanResult.BSSID);
                            signal.setLevel(mScanResult.level);
                            mTempSignals.add(signal); // add the Signal to the temporary list
                        }
                    }
                }
                // put the temporary Signals to the SignalList (where the latest are stored and mean is computed etc.)
                SignalList.put(mTempSignals);
                Log.d("WifiFetcher", "Aufruf put()"); // LOG
            }
        };

        mContext.registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    /**
     * Unregisters receiver with IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
     */
    public void unregisterReceiver() {
        if (mReceiver != null)
            mContext.unregisterReceiver(mReceiver);
    }

    /**
     * Gets Wifi fetching interval from settings and starts WiFi fetching thread
     */
    public void startFetching() {
        mFetchingOn = true;
        mHandler.post(mRunnable);
    }

    /**
     * Stops the WiFi fetching thread
     */
    public void stopFetching() {
        mFetchingOn = false;
    }

    /**
     * Determines if WiFi fetching thread is running
     * 
     * @return current WiFi fetching state
     */
    public boolean isFetcherActive() {
        return mFetchingOn;
    }

    /**
     * Turns WiFi on if it's off and starts WiFi scan
     */
    private void fetchSignals() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.startScan();
        }
    }
}
