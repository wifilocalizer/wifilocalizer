package de.kk.wifilocalizer.ui.models;

import java.io.Serializable;

/**
 * Model for saving map-relevant data like id, name and filepath
 */
public class MapImage implements Serializable {
    private static final long serialVersionUID = 100L;
    private String mId;
    private String mFilepath;
    private String mFilename;

    public MapImage(String id, String filepath, String filename) {
        mId = id;
        mFilepath = filepath;
        mFilename = filename;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setFilepath(String filepath) {
        mFilepath = filepath;
    }

    public String getFilepath() {
        return mFilepath;
    }

    public void setFilename(String filename) {
        mFilename = filename;
    }

    @Override
    public String toString() {
        return mFilename;
    }
}
