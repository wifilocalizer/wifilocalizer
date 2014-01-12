package de.kk.wifilocalizer.core;

import java.util.List;
import java.util.Map;
import de.kk.wifilocalizer.core.helper.MapBase;
import de.kk.wifilocalizer.core.helper.Position;
import de.kk.wifilocalizer.core.helper.Signal;
import android.content.Context;

/**
 * Core Manager is the main-Interface between UI an Core. It contains just static Members and Methods The UI uses this
 * "static" class to trigger the calibration as well as the positioning etc.
 */
public final class CoreManager {

    /**
     * Holds all Fingerprints of a Location (Fingerprint is a pair of Position and SignalList)
     */
    private static FingerprintMap mCurrentMap;

    /**
     * Instance of the Fetcher-Class, that receives and computes the Wifi-Signals
     */
    private static WifiSignalFetcher mFetcher;

    /**
     * @param c
     *            Context of the Application
     */
    public static void init(Context c) {
        Localizer.init(c);
        MapBase.init(c);
        SignalList.init();
        mFetcher = new WifiSignalFetcher(c);

    }

    /**
     * main Function for the Positioning, triggered by the UI Continuously predicts and returns the current Position
     * (delegate it to the localizer)
     * 
     * @return Position-Object (Float x,y 0..1), is the relative Position on the Mobile-Screen
     */
    public static Position getPosition() {

        if ((mCurrentMap != null) && (mCurrentMap.isFilled())) {
            Map<String, Signal> meanSigMap = SignalList.getMeanSignalMap();

            Position p = Localizer.getPosition(meanSigMap, mCurrentMap);
            if (p == null) {
                return null;
            } else {
                return p;
            }
        } else
            return null;
    }

    /**
     * main Function of the calibration-Mode, triggert by the UI gives an Position Object and the Method get the current
     * Wifi-Signals and stores both together as a Fingerprint in the Map
     * 
     * @param pos
     *            Position-Object(float x,y 0..1)
     */
    public static void setPosition(Position pos) {

        // get the mean of the WiFi-Signals
        List<Signal> signals = SignalList.getMeanSignals(); // mean of all bssids that are now(shortly) found

        // store this and the given position together in the FingerprintMap, but just when there were valid signals
        if (!signals.isEmpty()) {
            mCurrentMap.add(pos, signals);
        }

    }

    /**
     * Triggered by the UI, asks the Map-Database if an specific Map (FingerprintMap) exists
     * 
     * @param mapName
     *            Name of the MapPicture (set by user or default Map1 .. MapN) this name also identifies the Fingerprint-Map
     *            in the whole Map-Database
     * @return true, when the Fingerprint-Map exist in the Map-Database
     */
    public static boolean hasMap(String mapName) {
        return MapBase.hasMap(mapName);
    }

    /**
     * Sets a Map as that to use now. Normally triggered by the UI by change or first choose a Map
     * 
     * @param mapName
     *            Name of the Map to set as now active
     */
    public static void setMap(String mapName) {
        mCurrentMap = MapBase.setMap(mapName);
    }

    /**
     * UI triggers the MapBase to store the current Points. Normally used before Destroying the Main-Activity or similiar
     * app-changes
     */
    public static void persist() {
        MapBase.store();
    }

    /**
     * With this the UI asks for all Positions that where stored in the active map
     * 
     * @param mapName
     *            Name of the Map
     * @return (Array)-List of all Positions of the active Map
     */
    public static List<Position> getMeasuredPositions(String mapName) {
        return MapBase.getMeasuredPoints(mapName);
    }

    /**
     * Deletes all previously measured Positions in the active Map
     */
    public static void removeMeasuredPositions() {
        mCurrentMap.removeMeasuredPositions();
        MapBase.store();
    }

    /**
     * With that, the UI asks the static SignalList for the current received WiFi-Signals
     * 
     * @return List of Signals, Signal contains of bssid, ssid, level (strength)
     */
    public static List<Signal> getSignalList() {
        return SignalList.getCurrentSignals();
    }

    /**
     * UI tells the WiFi-Fetcher to start receiving Signals from the build in Wifi-Module
     */
    public static void startFetching() {
        CoreManager.mFetcher.startFetching();
    }

    /**
     * UI tells the WiFi-Fetcher to stop receiving Signals from the build in Wifi-Module
     */
    public static void stopFetching() {
        CoreManager.mFetcher.stopFetching();

    }

    /**
     * UI asks the Fetcher if he is active now or not
     * 
     * @return true if the Fetcher is active and receives WiFi-Signals from the WiFi-Module
     */
    public static boolean isFetcherActive() {
        return CoreManager.mFetcher.isFetcherActive();
    }

    /**
     * let the fetcher register its BroadcastReceiver
     */
    public static void registerReceiver() {
        mFetcher.registerReceiver();
    }

    /**
     * let the fetcher unregister its BroadcastReceiver
     */
    public static void unregisterReceiver() {
        mFetcher.unregisterReceiver();
    }
}
