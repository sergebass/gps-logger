/*
 * MapTile.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import javax.microedition.lcdui.Image;

/**
 * The base class for map tiles.
 * 
 * @author Serge Perinsky
 */
public abstract class MapTile {
    public abstract Image getImage();
    public abstract double getMinLatitude();
    public abstract double getMaxLatitude();
    public abstract double getMinLongitude();
    public abstract double getMaxLongitude();
}
