package de.kk.wifilocalizer.core.helper;

import java.io.Serializable;

/**
 * Helper-Class that defines a Position object. Its just like a pointF from graphics. But it usefull for later enhancements
 * to give more attributes to the Position ( e. how likely the Position is correct, or in which Order they come in, etc. not
 * implemented yet)
 */
public class Position implements Serializable {

    private static final long serialVersionUID = 1L;
    private float x;
    private float y;

    public Position(float x, float y) {
        super();
        this.x = x;
        this.y = y;
    }

    public Position() {
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
