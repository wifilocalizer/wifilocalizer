package de.kk.wifilocalizer.core.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper-Class. A Fingerprint is a pair of a Position and a List of Wifi-Signals that are related to that position users set
 * these Fingerprints by longpress, and the system stores it in a Map-Database
 * 
 */
public class Fingerprint implements Serializable {
    // -- instance variable --
    private static final long serialVersionUID = 0L;

    Position mPosition;
    List<Signal> mSignals;

    /**
     * default constructor
     */
    public Fingerprint() {
        this.mPosition = new Position();
        this.mSignals = new ArrayList<Signal>();
    }

    /**
     * enhanced Constructor
     * 
     * @param position
     *            (float x,y between 0 und 1) relative position on the mobile-screen
     * @param signals
     *            List with Wifi-Signals containing bssid, ssid and level (signal strength)
     */
    public Fingerprint(Position position, List<Signal> signals) {
        super();
        this.mPosition = position;
        this.mSignals = signals;
    }

    // -- Methods--

    /**
     * Getter
     * 
     * @return the Position of the Fingerprint as Object
     */
    public Position getPosition() {
        return mPosition;
    }

    /**
     * Setter
     * 
     * @param mPosition
     */
    public void setPosition(Position mPosition) {
        this.mPosition = mPosition;
    }

    /**
     * getter for just the Signals
     * 
     * @return List of Signals
     */
    public List<Signal> getSignals() {
        return mSignals;
    }

    /**
     * Setter for just the Signals
     * 
     * @param mSignals
     */
    public void setSignals(List<Signal> mSignals) {
        this.mSignals = mSignals;
    }

    /**
     * @return size, means Number of WIFI-Signals in the Signal-List
     */
    public int size() {
        return mSignals.size();
    }
}
