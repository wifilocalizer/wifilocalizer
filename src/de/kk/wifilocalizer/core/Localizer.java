package de.kk.wifilocalizer.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.kk.wifilocalizer.core.helper.Fingerprint;
import de.kk.wifilocalizer.core.helper.Position;
import de.kk.wifilocalizer.core.helper.Signal;
import de.kk.wifilocalizer.ui.activities.MainActivity;

/**
 * This Class is the Calculation-Core of the app. Its a static Container for different Calculation-Methods Will be triggered
 * by the UI by means of the CoreManager
 */
public class Localizer {

    /**
     * if true the geometric average Position will be calculated otherwise just the one likeliest Position will be returned
     */
    private static boolean useGeoMean;

    /**
     * Defines with how many Positions the averaging will be computed Note, when you just measure these number of Positions
     * the predicted one will always be in the geometric middle so, the higher geoMeanNumber , the more measure-Positions you
     * should take
     */
    private final static int geoMeanNumber = 3;

    /**
     * Context of the APP, needed for the shared Preferences
     */
    private static Context context;

    /**
     * Due to this class is static, it just need to get the Context, the first time Will be triggered from CoreManager
     * 
     * @param c
     *            Context of the App
     */
    public static void init(Context c) {
        if (context == null)
            context = c;
    }

    //
    /**
     * main Function of this Class and the Positioning Continuously predict the current Position and gives it back For that
     * it compares the current Wifi-Signals with the previously measured ones and calculates a value(distance) that describes
     * how similiar the signal-Lists are
     * 
     * @param meanSigMap
     *            a Map with the bssids as key and related signal as Value, Currently received Signals from the WiFi-Module
     * @param fpm
     *            FingerprintMap, the Map of all Fingerprints previously stored, to
     * @return Position-Object (float x,y 0..1) that describes the prediction of the most likeliest position now
     */
    public static Position getPosition(Map<String, Signal> meanSigMap, FingerprintMap fpm) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        useGeoMean = sp.getBoolean(MainActivity.PREF_USE_GEOMETRIC_MEAN, false);

        // get the distance if something is in the meanSigMap
        if (meanSigMap.isEmpty()) { // if the fetcher dont transmit signals (wifi is off, etc) return null as positioin ( gui
                                    // catch that)
            return null;
        } else { // otherwise calculate the distance betwenn the current wifi-signal and all stored signals(fingerprints)
            TreeMap<Float, Position> distanceVector = getDistanceVector(meanSigMap, fpm.getFingerprints());
            if (distanceVector.isEmpty()) {
                return null; // if there was nothing to compare due to complete different wifi's between calibrating and now
                             // tell that the gui by null
            } else // otherwise get the point which is most likely that you are now, caused by the likeliness of the measured
                   // wifi-signals and the current one.
            {
                if (useGeoMean) { // deside if you want exact that point on the measured position or a meaning between the 3
                                  // or
                                  // sth most likeliest
                    int idx = 0;

                    Map<Float, Position> candidates = new HashMap<Float, Position>(geoMeanNumber);

                    // take the N likeliest positions for calculating the mean geometric mean of their x/y positions (N=
                    // geoMeanNumber)
                    for (Float key : distanceVector.keySet()) {
                        if (idx == geoMeanNumber)
                            break;
                        candidates.put(key, distanceVector.get(key));
                        idx++;
                    }

                    return geometricMean(candidates);
                }
                return distanceVector.get(distanceVector.firstKey());
            }

        }

    }

    /**
     * This method compares a given Wifi-Signal-List with many stored Signal-Lists, computes the distance of them and stores
     * it together with the position in the distance-Vector Principal of KNN (K Nearest Neighborhood ) of Neural Networks
     * 
     * @param meanSigMap
     * @param fpl
     * @return
     */
    private static TreeMap<Float, Position> getDistanceVector(Map<String, Signal> meanSigMap, List<Fingerprint> fpl) {
        // Vector with distances between the given signal and the fingerprints
        // key: distance / Value: Position
        TreeMap<Float, Position> distanceVector = new TreeMap<Float, Position>();

        // got through all fingerprints and compare to the given meanSignal and compute the distance between them
        for (Fingerprint fp : fpl) { // for every single fingerprint do

            // List<Signal> fpSignals = fp.getSignals(); //get every Signal in the current fingerprint as List
            float distance = 0;

            // traverse through the FingerprintSignals and compare with the currents
            for (Signal fpSignal : fp.getSignals()) {
                if (meanSigMap.keySet().contains(fpSignal.getBssid())) { // is the signal one to compare (in mean-list and
                                                                         // fingerprint-list

                    // add up the distances of each Level whos bssid is in both lists

                    float currentLevel = meanSigMap.get(fpSignal.getBssid()).getLevel();
                    float fpLevel = fpSignal.getLevel();

                    // absolute difference between the compared signals
                    float tmpDist = Math.abs(currentLevel - fpLevel);

                    // -- Use of Wifi-Level in -db as integer (-95db <-80db) --

                    // 4 Versions: (uncomment one of these for use)
                    // 1) Weaker Signals will more badly influence the distance
                    // tmpDist = tmpDist * ((currentLevel+fpLevel) / 10);

                    // 2) Stronger Signals will more badly influence the distance
                    tmpDist = tmpDist / ((currentLevel + fpLevel) / 10);

                    // 3) euclidean distance,computation is theoreticaly more exact, but much more time-consuming so its not
                    // used
                    tmpDist = tmpDist * tmpDist;

                    // 4) raw, no weighting of the db-strength
                    // comment all of the weight-formulas

                    // compute the sum of all distances of one Fingerprint
                    distance += tmpDist;
                    // 3b) square-root of the vector-sum
                    distance = (float) Math.sqrt(distance);

                }
            }
            distanceVector.put(distance, fp.getPosition());
        }

        return distanceVector;

    }// end of getDistanceVector

    /**
     * takes the given Positions and its distances(value of likelihood to be the true position) and calculates a new more
     * like position between them weighted geometric calculation, new Position is nearest to the Point with the lowest
     * Distance
     * 
     * @param candidates
     *            Map of the 3 or what distances and its Positions
     * @return the new predicted Position
     */
    private static Position geometricMean(Map<Float, Position> candidates) {
        float x = 0, y = 0, w = 1, wsum = 0;

        // Math: newX = oldX1*dist1 +oldX2*dist2 + . . . + old3*dist3 --> Arithmetic Mean
        for (Entry<Float, Position> c : candidates.entrySet()) {
            if (c.getKey() < 1) {
                w = 1;
            } else {
                w = c.getKey();
            }

            wsum += 1 / w;

            x += c.getValue().getX() / w;
            y += c.getValue().getY() / w;
        }

        x = x / wsum;
        y = y / wsum;

        return new Position(x, y);
    }
}
