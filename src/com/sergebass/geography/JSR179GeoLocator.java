/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

import javax.microedition.location.*;

/**
 *
 * @author Serge Perinsky
 */
public class JSR179GeoLocator
        implements GeoLocator {

    static LocationProvider locationProvider = null;

    static {
        try {
            Class.forName("javax.microedition.location.LocationProvider");
        } catch (ClassNotFoundException eee) { // no JSR-179 support on this device?
            locationProvider = null;
        }
    }

    public static boolean isLocationAPISupported() {
        return (locationProvider != null);
    }

    public JSR179GeoLocator() {
        
        if (locationProvider == null) {
            return; // nothing to do here, the Location API is not supported
        }
        
        Criteria criteria = new Criteria();
//        criteria.setSpeedAndCourseRequired(true);
//        criteria.setAltitudeRequired(true);
//        criteria.setCostAllowed(true);

        try {
            locationProvider = LocationProvider.getInstance(criteria);
        } catch (LocationException e) {
            locationProvider = null;
        }
    }

    public void close() {
/// just do nothing?
    }

    public void setLocationListener(GeoLocationListener listener) {
///
    }

    public GeoLocation getLocation() {
///
return null;
///
    }

    private Location getLAPILocation() {
        if (locationProvider == null) {
            return null;
        }

        try {
            return locationProvider.getLocation(-1); // default timeout
        } catch (Exception e) {
            return null;
        }
    }

}
