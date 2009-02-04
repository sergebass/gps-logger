/*
 * GeoMath.java (C) Serge Perinsky, 2008
 */

package com.sergebass.geography;

/**
 * GeoMath.
 *
 * @author Serge Perinsky
 */
public class GeoMath {
    
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

    public static String convertSecondsToString(long seconds) {
        return convertMillisToString(seconds * 1000L);
    }

    public static String convertMillisToString(long millis) {
        long hours = millis / 3600000L;
        long remainderMillis = millis % 3600000L;
        long minutes = remainderMillis / 60000L;
        String minutesString = minutes < 10? "0" + minutes : String.valueOf(minutes);
        remainderMillis = remainderMillis % 60000L;
        long seconds = remainderMillis / 1000L;
        String secondsString = seconds < 10? "0" + seconds : String.valueOf(seconds);
        return hours + ":" + minutesString + ":" + secondsString;
    }
}
