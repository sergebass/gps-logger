/*
 * MapRenderer.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import com.sergebass.geo.GeoLocation;
import javax.microedition.lcdui.Graphics;

/**
 * MapRenderer.
 * 
 * @author Serge Perinsky
 */
public abstract class MapRenderer {

    public static MapRenderer newInstance(MapConfiguration configuration) {
        if (configuration instanceof RasterMapConfiguration) {
            return new RasterMapRenderer((RasterMapConfiguration)configuration);
        } else {
            return null;
        }
    }

    private GeoLocation fixLocation = null;
    private GeoLocation targetLocation = null;

    public void setFixLocation(GeoLocation location) {
        this.fixLocation = location;
    }

    public GeoLocation getFixLocation() {
        return fixLocation;
    }

    public void setTargetLocation(GeoLocation location) {
        this.targetLocation = location;
    }

    public GeoLocation getTargetLocation() {
        return targetLocation;
    }

    public abstract boolean render(Graphics g,
            int clipX, int clipY, int clipWidth, int clipHeight,
            int markerX, int markerY);

    public abstract GeoLocation getShiftedLocation(GeoLocation location, int hOffsetPixels, int vOffsetPixels);

    public abstract int getHLocationShift(GeoLocation originalLocation, GeoLocation shiftedLocation);
    public abstract int getVLocationShift(GeoLocation originalLocation, GeoLocation shiftedLocation);
}
