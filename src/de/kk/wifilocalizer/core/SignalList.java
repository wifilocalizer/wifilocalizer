package de.kk.wifilocalizer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.annotation.SuppressLint;
import de.kk.wifilocalizer.core.helper.Signal;

/**
 * Class that holds the current Wifi-Signals and the methods to compute an averaging over it etc. class is kind of static, it
 * contains just static members and methods cause there is no need for much instances it
 */
@SuppressLint("UseSparseArrays")
public class SignalList {

    /**
     * Number of Elements in the List of SignalLevels, important for the meaning of the Signals
     */
    private static Integer mQueueSize = 3;

    /**
     * Current Position in the Circular Queues that stores the historic level-values of the signals (limit to mQueueSize)
     */
    private static Integer mRotatingIndex = 0;

    /**
     * contains for every bssid a queue (SparseArray-Implementation) that holds the last mQueueSize Values for
     * mean-Calculation
     */
    private static Map<String, Map<Integer, Integer>> mSignalQueues;

    /**
     * Map for storing the Mean-Value of every bssid (computed from the coresponding SignalQueue in mSignalQueues
     */
    private static Map<String, Integer> mMean;

    /**
     * Map for storing the ssid to the Bssid
     */
    private static Map<String, String> mIDs;

    /**
     * initialiser for empty Map
     */
    public static void init() {

        if (mSignalQueues == null)
            mSignalQueues = new HashMap<String, Map<Integer, Integer>>(30);
        if (mMean == null)
            mMean = new HashMap<String, Integer>(30);
        if (mIDs == null)
            mIDs = new HashMap<String, String>(30);
    }

    // --- Methods ---

    /**
     * Gets a bunch of current Wifi-Signals from the Wifi-Fetcher and stores it in the Queue then with some helper-methods it
     * calculates the Average of the signals, and holds them in the Mean-member
     * 
     * @param sl
     *            List of Signals taken from the WiFi-Fetcher
     */
    public static void put(List<Signal> sl) {

        // 1) increment the rotating Index for the position in the Queues of mSignalQueues
        incrLevelIndex();
        // 2) fill new Signal-Values in its Queues
        fillQueues(sl);
        // 3) take all not currently touched queues in the Queue-Map and delete the current Positon on the Queue or - when
        // its
        // the last one - remove the complete Queue
        updateQueues(sl);

    }

    private static void fillQueues(List<Signal> sl) {

        // takes every Signal and updates the SignlaQueues with its entrys
        for (Signal s : sl) {
            String bssid = s.getBssid();
            if (mSignalQueues.containsKey(bssid)) { // when there is already a queue for that signal-bssid

                // 1) update the Value in the Queue on the Position of mRotatingIndex
                mSignalQueues.get(bssid).put(mRotatingIndex, s.getLevel());

                // 2) calculate the new Mean-Value of that specific queue
                calcMean(bssid);
            } else { // otherwise there has to made a new Entry with bssid and Queue for that new Signal

                // new queue with a definite size (describe over wich periode the mean are calculated , here now ~4 sec.
                Map<Integer, Integer> q = new HashMap<Integer, Integer>(mQueueSize);

                // put the new Value in its queue like above
                q.put(mRotatingIndex, s.getLevel());

                // make a new entry with this queue in the Queue-Map
                mSignalQueues.put(bssid, q);

                // and put this new signal also in the mIDS- and Mean-Map
                mIDs.put(bssid, s.getSsid());
                mMean.put(bssid, s.getLevel());

            }// end of if

        }// end of for

    }// end of method

    // goes in all Queues of the QueueMap that was untouched and delete the current Level or the complete Queue when it was
    // the
    // last Level
    private static void updateQueues(List<Signal> sl) {
        // extract the bssids of off the SignalList sl to now which one was not filled in mSignalQueue before
        Set<String> toDel = new HashSet<String>();
        List<String> ids = new ArrayList<String>();
        for (Signal s : sl) {
            ids.add(s.getBssid());
        }

        // loop the whole queue-Set
        for (String bssid : mSignalQueues.keySet()) {

            // if bssid is NOT in the List of already updated bssids(ids)
            // if current element of mSignalQueues was not updated by the put in signals
            if (!ids.contains(bssid)) {

                // delete Value of the Queue on the current Position (= Position RotatingIndex)
                mSignalQueues.get(bssid).remove(mRotatingIndex);

                // if this was the last value and the Queue is now empty delete it complete
                if (mSignalQueues.get(bssid).isEmpty()) {
                    // remember to delete the queue
                    toDel.add(bssid);
                }
            }
        }

        // delete all bssids that was found old
        for (String bssid : toDel) {
            // 1) delete the Queue in mSignalQueues
            mSignalQueues.remove(bssid);
            // 2) delete the entry in the Mean-Map
            mMean.remove(bssid);
            // 3) delete entry in th ID-Map
            mIDs.remove(bssid);
        }
        //

    }

    // intern HelperMethod for calculating the new Mean-Value each time a new Signal-Level was put in its Queue
    // (mSignalQueues)
    private static void calcMean(String bssid) {
        // get the Queue of the given bssid
        Map<Integer, Integer> signalQueue = mSignalQueues.get(bssid);
        float mean = 0; // meanValue, yet in float;

        // get the sum of all Level-Values in the queue
        for (Integer level : signalQueue.values()) {
            mean += level;
        }
        // get the mean-Value
        mean = mean / signalQueue.size();
        // update the mean-List
        mMean.put(bssid, Math.round(mean));
    }

    //
    /**
     * returns the newest Signals of the List (older will be stored for averaging)
     * 
     * @return List of Signals that where come in with the latest put.
     */
    public static List<Signal> getCurrentSignals() {

        List<Signal> signals = new ArrayList<Signal>();

        // go through the whole queue-Map and get just that values in the signal-List that HAS a valid value on the current
        // position
        for (String bssid : mSignalQueues.keySet()) {
            if (mSignalQueues.get(bssid).get(mRotatingIndex) != null) {
                int level = mSignalQueues.get(bssid).get(mRotatingIndex);
                Signal s = new Signal();
                s.setLevel(level);
                s.setBssid(bssid);
                s.setSsid(mIDs.get(bssid));
                signals.add(s);
            }
        }

        return signals;
    }

    /**
     * helper method. builds a HashMap with a bssid of each signal as key for easier use of other outerclass methods
     * 
     * @return a Map with the MeanSignals
     */
    public static Map<String, Signal> getMeanSignalMap() {
        Map<String, Signal> sm = new HashMap<String, Signal>();
        for (String bssid : mMean.keySet()) {
            Signal s = new Signal();
            s.setBssid(bssid);
            s.setLevel(mMean.get(bssid));
            s.setSsid(mIDs.get(bssid));

            sm.put(bssid, s);
        }

        return sm;
    }

    /**
     * Averaged Signals stored in a List for outer use
     * 
     * @return the List with the MeanSignals
     */
    public static List<Signal> getMeanSignals() {
        List<Signal> sl = new ArrayList<Signal>();
        for (String bssid : mMean.keySet()) {
            Signal s = new Signal();
            s.setBssid(bssid);
            s.setLevel(mMean.get(bssid));
            s.setSsid(mIDs.get(bssid));

            sl.add(s);
        }

        return sl;
    }

    // increase the LevelIndex in a circle, so start with zero when arrived by mLevelNumber
    private static void incrLevelIndex() {
        if (mRotatingIndex == mQueueSize) {
            mRotatingIndex = 0;
        } else {
            mRotatingIndex++;
        }
    }
}
