/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.gps;

import java.io.*;
import javax.microedition.location.*;

/**
 *
 * @author Serge Perinsky
 */
public class LAPIGPSReceiver
        extends InputStream {

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

    public LAPIGPSReceiver() {
        
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

    public InputStreamReader getInputStreamReader() {
        return new InputStreamReader(this);
    }
    
    public int read()
        throws IOException {
/// actually "read" a byte here
return -1;
///
    }

    private Location getLocation() {
        if (locationProvider == null) {
            return null;
        }

        try {
            return locationProvider.getLocation(-1); // default timeout
        } catch (Exception e) {
            return null;
        }
    }

    private String getLocationDataAsNMEAString() {
/*
$GPRMC,081726.000,A,4634.5483,N,03048.0251,E,0.35,44.44,191108,,,A*54
$GPVTG,44.44,T,,M,0.35,N,0.66,K,A*0B
$GPGGA,081727.000,4634.5484,N,03048.0252,E,1,7,1.18,62.7,M,31.0,M,,*6B
$GPGSA,A,3,05,09,29,14,04,30,12,,,,,,1.47,1.18,0.88*0E
$GPGSV,3,1,11,12,80,017,21,05,72,310,20,02,47,120,,30,45,295,24*7D
$GPGSV,3,2,11,09,44,174,16,39,35,188,,04,33,061,23,29,28,235,17*78
$GPGSV,3,3,11,14,22,289,17,24,02,185,,31,02,322,18*4B
$GPRMC,081727.000,A,4634.5484,N,03048.0252,E,0.27,35.33,191108,,,A*54
$GPVTG,35.33,T,,M,0.27,N,0.50,K,A*0B
$GPGGA,081728.000,4634.5485,N,03048.0251,E,1,7,1.20,62.7,M,31.0,M,,*6D
$GPGSA,A,3,05,09,29,14,04,30,12,,,,,,2.23,1.20,1.87*0A
$GPGSV,3,1,11,12,80,017,21,05,72,310,20,02,47,120,,30,45,295,24*7D
$GPGSV,3,2,11,09,44,174,16,39,35,188,,04,33,061,23,29,28,235,17*78
$GPGSV,3,3,11,14,22,289,17,24,02,185,,31,02,322,18*4B
 */
    Location location = getLocation();

    if (location != null) {
///compute it here
    }

/// generate string from the current location data
return "$GPRMC,081726.000,A,4634.5483,N,03048.0251,E,0.35,44.44,191108,,,A*54";
///
    }
}
