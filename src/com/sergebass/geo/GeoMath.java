/*
 * GeoMath.java (C) Serge Perinsky, 2008-2010
 */

package com.sergebass.geo;

import com.sergebass.math.Calculator;

/**
 * GeoMath.
 *
 * @author Serge Perinsky
 */
public class GeoMath {
    
    /**
     * Returns distance between two points on the surface of the Earth.
     * 
     * @param latitude1
     * @param longitude1
     * @param latitude2
     * @param longitude2
     * 
     * @return distance between two points on the surface of the Earth, in meters
     */
    public static double computeDistance(double latitude1, double longitude1,
                                         double latitude2, double longitude2) {
        return computeDistanceWithHaversineFormula(latitude1, longitude1, latitude2, longitude2);
    }

    /**
     * Computes distance between two points on the surface of the Earth.
     *
     * @param latitude1
     * @param longitude1
     * @param latitude2
     * @param longitude2
     *
     * @return distance between two points on the surface of the Earth, in meters
     */
    public static double computeDistanceWithHaversineFormula
                                        (double latitude1, double longitude1,
                                         double latitude2, double longitude2) {

        // first convert lat/lon values into radians:
        latitude1 = Math.toRadians(latitude1);
        longitude1 = Math.toRadians(longitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude2 = Math.toRadians(longitude2);

        double latitudeDelta = latitude2 - latitude1;
        double longitudeDelta = longitude2 - longitude1;

        double earthRadius = 6371000.0; // mean radius of the Earth, m

        double halfLatitudeDeltaSine = Math.sin(latitudeDelta / 2);
        double halfLongitudeDeltaSine = Math.sin(longitudeDelta / 2);
        
        double a = halfLatitudeDeltaSine * halfLatitudeDeltaSine
                + Math.cos(latitude1) * Math.cos(latitude2)
                    * halfLongitudeDeltaSine * halfLongitudeDeltaSine;
        
        double c = 2 * Calculator.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return earthRadius * c;
    }

    /**
     * Computes initial bearing (azimuth) angle from point 1 to point2.
     * The bearing angle will change until the destination point is reached but this one is a starting angle.
     * 
     * @param latitude1
     * @param longitude1
     * @param latitude2
     * @param longitude2
     *
     * @return initial bearing (azimuth) angle from point 1 to point2, in degrees
     */
    public static double computeInitialBearing(double latitude1, double longitude1,
                                        double latitude2, double longitude2) {

        // first convert lat/lon values into radians:
        latitude1 = Math.toRadians(latitude1);
        longitude1 = Math.toRadians(longitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude2 = Math.toRadians(longitude2);

        double longitudeDelta = longitude2 - longitude1;

        double y = Math.sin(longitudeDelta) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2)
                - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longitudeDelta);
        double initialBearing = Math.toDegrees(Calculator.atan2(y, x));
        
        return initialBearing >= 0.0? initialBearing : 360.0 + initialBearing;
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
