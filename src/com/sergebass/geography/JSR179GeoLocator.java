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

    public static boolean isLocationAPISupported() {
        try {
            Class.forName("javax.microedition.location.LocationProvider");
        } catch (ClassNotFoundException e) { // no JSR-179 support on this device?
            return false;
        }
        return true;
    }

    LocationProvider locationProvider = null;
    GeoLocationListener listener = null;

    boolean isStarted = true;

    public JSR179GeoLocator() {
        this(new Criteria()); // use defaults
    }

    public JSR179GeoLocator(Criteria criteria) {
        System.out.println("Creating JSR179 Location API locator connection...");
        try {
            locationProvider = LocationProvider.getInstance(criteria);
        } catch (LocationException e) {
            locationProvider = null;
        }

        if (locationProvider == null) {
            return; // nothing to do here anymore
        }

        // let's start a location polling thread
        isStarted = true;
        new Thread() {
            public void run() {
                String oldDateString = "";
                String oldTimeString = "";
                do {
                    GeoLocation location = getLocation();
                   
                    if (location == null) { // avoid bad data
                        continue;
                    }
                    // make sure to only pass new data (check the timestamp),
                    // avoid clogging the bandwidth with duplicate points
                    
                    String dateString = location.getDateString();
                    String timeString = location.getTimeString();
                    
                    if (!dateString.equals(oldDateString)
                     || !timeString.equals(oldTimeString)) {
                        if (listener != null) {
                            listener.locationChanged(location);
                        }
                        
                        oldDateString = dateString;
                        oldTimeString = timeString;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                } while (isStarted);
            }
        }.start();
    }

    public void close() {
        isStarted = false;
    }

    public void setLocationListener(GeoLocationListener listener) {
        this.listener = listener;
    }

    public GeoLocation getLocation() {

        if (locationProvider == null) {
            return null;
        }

        Location lapiLocation;

        try {
            lapiLocation = locationProvider.getLocation(-1); // use default timeout
        } catch (Exception e) {
            lapiLocation = null;
        }

        if (lapiLocation == null) {
            return null;
        }

        QualifiedCoordinates coordinates = lapiLocation.getQualifiedCoordinates();
        GeoLocation location = new GeoLocation
                    (coordinates.getLatitude(), coordinates.getLongitude());

        float altitude = coordinates.getAltitude();
        if (!Float.isNaN(altitude)) {
            location.setAltitude(coordinates.getAltitude());
        }

        float course = lapiLocation.getCourse();
        if (!Float.isNaN(course)) {
            location.setCourse(course);
        }

        float speed = lapiLocation.getSpeed();
        if (!Float.isNaN(speed)) {
            location.setSpeed(speed);
        }

        location.setTimestamp(lapiLocation.getTimestamp());
        location.setValid(lapiLocation.isValid());
        
///... add more fields?
///lapiLocation.getExtraInfo("application/X-jsr179-location-nmea"); /// - save NMEA data
///lapiLocation.get...()

        return location;
    }
}
