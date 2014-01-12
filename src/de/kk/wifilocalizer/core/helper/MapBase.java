package de.kk.wifilocalizer.core.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import de.kk.wifilocalizer.core.FingerprintMap;

/**
 * Database-Class of the App, contains several FingerprintMaps, one Map contains all Fingerprints(Position-Signals-Pairs) of
 * an Map While the UI stores the Picture-Maps, this class stores the Values(Fingerprints) of it. Later implementation of a
 * sqlite database is recommended coded as static, so just once available in the app
 */
public final class MapBase {
    // Members
    /**
     * Context of the app
     */
    private static Context context;
    private static final String fileName = "MapBase.db";

    /**
     * THE Pseudo-Database-Object, Map of FingerprintMaps and its names
     */
    private static Map<String, FingerprintMap> mMaps;
    /**
     * Name of the Map that is active in the App now
     */
    private static String mCurrentMapName;
    private static File file;

    /**
     * init rebuild the MapBase instead of building a new instance
     * 
     * @param c
     *            Context of the app
     */
    public static void init(Context c) {

        context = c;
        if (mMaps == null) {
            if (file == null)
                file = new File(context.getFilesDir(), fileName);
            if (file.exists()) {
                load(c);
            } else {
                mMaps = new HashMap<String, FingerprintMap>();
                store();
            }

        }

    }

    /**
     * store the whole MapBase to File
     */
    public static void store() {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(mMaps);
            os.close();
            // SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            // sp.edit().putBoolean(MainActivity.CORE_MAPBASE_STORED, true).commit();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * loades the Database-Object "mMaps" from a File in the mobile-device to the RAM
     * 
     * @param context
     *            of the app
     */
    @SuppressWarnings("unchecked")
    public static void load(Context context) {
        // SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        // if (sp.getBoolean(MainActivity.CORE_MAPBASE_STORED, false)) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fis);
            mMaps = (Map<String, FingerprintMap>) in.readObject();
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * @return the Current used Map with all its Fingerprints inside
     */
    public static FingerprintMap getCurrentMap() {
        return mMaps.get(mCurrentMapName);
    }

    /**
     * sets the map with the given Name to currently in use and return it
     * 
     * @param mapName
     *            Name of the Map (defined by user or default Map1, ...)
     * @return the Fingerprint-Map with all its Fingerprints of one Map
     */
    public static FingerprintMap setMap(String mapName) {

        // proof if Container mMaps is empty an make a new one if so
        if ((mMaps.isEmpty()) || (!mMaps.containsKey(mapName))) {
            FingerprintMap fpm = new FingerprintMap();
            mMaps.put(mapName, fpm);
        }
        store();

        return mMaps.get(mapName);
    }

    /**
     * Asks if the Map exists in the Map-Database-Object
     * 
     * @param mapName
     *            Name of the Map
     * @return true, if it is found
     */
    public static boolean hasMap(String mapName) {
        if (mMaps.containsKey(mapName)) {
            return true;
        } else
            return false;
    }

    /**
     * @param mapName
     *            Name of the Map
     * @return a List of Point of the given map
     */
    public static List<Position> getMeasuredPoints(String mapName) {

        if ((!mMaps.isEmpty()) && (mMaps.containsKey(mapName))) {
            List<Position> pf = mMaps.get(mapName).getPositions();
            return pf;
        } else
            return new ArrayList<Position>();
    }
}
