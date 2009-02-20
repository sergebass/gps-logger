/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

/**
 *
 * @author Serge Perinsky
 */
public interface GeoLocationListener {
    public void onLocationUpdated(GeoLocation location);
    public void onLocatorStateChanged(int newState);
    public void onLocatorException(Exception e);
}
