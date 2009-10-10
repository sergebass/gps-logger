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

    private GeoLocation location = null;

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public abstract boolean render(Graphics g,
            int clipX, int clipY, int clipWidth, int clipHeight,
            int markerX, int markerY);
}
