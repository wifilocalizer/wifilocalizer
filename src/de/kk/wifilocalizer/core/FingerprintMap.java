package de.kk.wifilocalizer.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import de.kk.wifilocalizer.core.helper.Fingerprint;
import de.kk.wifilocalizer.core.helper.Position;
import de.kk.wifilocalizer.core.helper.Signal;

/**
 * The Class FingerprintMap contains all Fingerprints of a Map.<br />
 * It stores all Pairs of Positions and its related List of Wifi-Signals<br />
 * { Fingerprint1 [Position1, [Sig1, Sig2..Sig n] ; Fingerprint2 [Position2, [Sig1, Sig2..Sig n]; . . . }
 */
public class FingerprintMap implements Serializable {
    // -- Instance Variables
    private static final long serialVersionUID = 0L;

    // List of Fingerprints
    private List<Fingerprint> mFingerprints;

    // --- constructors ---

    /**
     * default Constructor, each Map-Picture is related to one of these Fingerprint-Maps
     */
    public FingerprintMap() {

        mFingerprints = new ArrayList<Fingerprint>();
    }

    /**
     * @return the whole member-List of Fingerprints
     */
    List<Fingerprint> getFingerprints() {
        return mFingerprints;
    }

    /**
     * Answer the question if the class is currently instantiated with a Fingerprint and if she is not empty Needed from the
     * MapBase in initialize- or first-use-situations
     * 
     * @return true, if instantiated and not empty
     */
    public boolean isFilled() {
        if ((mFingerprints == null) || mFingerprints.isEmpty())
            return false;
        return true;
    }

    /**
     * @return the size of the Member-List of Fingerprints
     */
    public int size() {
        return mFingerprints.size();
    }

    /**
     * Add a new Position and a List of related WiFi-Signals as a Fingerprint to the List
     * 
     * @param pos
     *            Position(float x,y 0..1)
     * @param signals
     *            List of Signal's signal(bssid, ssid, strength-level)
     */
    public void add(Position pos, List<Signal> signals) {
        Fingerprint fingerprint = new Fingerprint(pos, signals);
        if (mFingerprints == null) { // !!!! nur zum debuggen
            mFingerprints = new ArrayList<Fingerprint>();
        }

        mFingerprints.add(fingerprint);
    }

    /**
     * @return a List of all Positions in the member-List of Fingerprints
     */
    public List<Position> getPositions() {
        if ((mFingerprints != null) && (!mFingerprints.isEmpty())) {
            List<Position> posList = new ArrayList<Position>();
            for (Fingerprint fp : mFingerprints) {
                posList.add(fp.getPosition());
            }
            return posList;
        } else {
            return new ArrayList<Position>();
        }
    }

    /**
     * delete all Positions in this Map, for example when they where wrongly measured or outdated
     */
    public void removeMeasuredPositions() {
        mFingerprints.clear();
    }
}
