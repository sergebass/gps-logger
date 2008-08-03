/*
 * GPSMath.java (C) Serge Perinsky, 2008
 */

package com.sergebass.gps;

/**
 * GPSMath.
 *
 * @author Serge Perinsky
 */
public class GPSMath {
    
    public static double computeDistance(double latitude1, double longitude1,
                           double latitude2, double longitude2) {

        double latitudeDelta = Math.abs(latitude1 - latitude2);
        
        // each minute arc of any meridian is equals exactly one nautical mile, 1852 meters
        double vDistanceDelta = latitudeDelta * 1852.0; // meters
        double longitudeDelta = Math.abs(latitude1 - latitude2);
        double averageLatitude = Math.min(latitude1, latitude2) + latitudeDelta / 2;
        double hMinuteLength = Math.cos(averageLatitude) * 1855.324; // meters per minute arc of equator
        double hDistanceDelta = longitudeDelta * hMinuteLength; // meters
        
        // calculate the hypotenuse of our right-angle triangle
        return Math.sqrt((vDistanceDelta * vDistanceDelta)
                       + (hDistanceDelta * hDistanceDelta));
    }
}
