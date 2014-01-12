package de.kk.wifilocalizer.core.helper;

import java.io.Serializable;

/**
 * Helper-Class that holds an unique WiFi-Signal with 3 Parameter<br />
 * BSSID: Mac-Adress of the Adapter, this is for unique identification<br />
 * SSID: Name for filtering and showing<br />
 * Level: Signal strength in db , e. -36db very strong signal, -80db weak signal
 */
public class Signal implements Serializable {
    private static final long serialVersionUID = 0L;

    private String mSsid;
    private String mBssid;
    private int mLevel;

    public Signal(String ssid, String bssid, int level) {
        mSsid = ssid;
        mBssid = bssid;
        mLevel = level;
    }

    public Signal() {

    }

    public String getSsid() {
        return mSsid;
    }

    public String getBssid() {
        return mBssid;
    }

    public int getLevel() {
        return mLevel;
    }

    public void set(String ssid, String bssid, int level) {
        this.mSsid = ssid;
        this.mBssid = bssid;
        this.mLevel = level;
    }

    public void setSsid(String mSsid) {
        this.mSsid = mSsid;
    }

    public void setBssid(String mBssid) {
        this.mBssid = mBssid;
    }

    public void setLevel(int mLevel) {
        this.mLevel = mLevel;
    }

    @Override
    public String toString() {
        return mSsid + "\n" + mBssid + "     |||   " + mLevel;
    }
}
