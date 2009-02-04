/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

/**
 *
 * @author Serge Perinsky
 */
public interface GeoLocationListener {
    public void locationChanged(GeoLocation location);
    public void handleLocatorException(Exception e);
}
