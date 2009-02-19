/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

/**
 *
 * @author Serge Perinsky
 */
public interface GeoLocationListener {
    public void locationUpdated(GeoLocation location);
    public void locatorStateChanged(int newState);
    public void handleLocatorException(Exception e);
}
