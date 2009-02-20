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
        implements GeoLocator,
                   LocationListener {

    public static boolean isLocationAPISupported() {
        try {
            Class.forName("javax.microedition.location.LocationProvider");
        } catch (ClassNotFoundException e) { // no JSR-179 support on this device?
            return false;
        }
        return true;
    }

    LocationProvider locationProvider = null;
    GeoLocationListener locationListener = null;

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

        locationProvider.setLocationListener(this,
                                             1,  // polling interval, seconds
                                             -1,  // default timeout, seconds
                                             -1); // default maximum age
    }

    public void close() {
        isStarted = false;
    }

    public void setLocationListener(GeoLocationListener listener) {
        this.locationListener = listener;
    }

    public GeoLocation getLocation(Location lapiLocation) {

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

        String nmeaSentences = lapiLocation.getExtraInfo("application/X-jsr179-location-nmea");
        if (nmeaSentences != null) {
            location.setNMEASentences(nmeaSentences);
        }

        return location;
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

        return getLocation(lapiLocation);
    }

    public void locationUpdated(LocationProvider locationProvider, Location lapiLocation) {

///avoid duplicate timestamps/date/time values!!!

        if (locationListener != null) {
            locationListener.onLocationUpdated(getLocation(lapiLocation));
        }
    }

    public void providerStateChanged(LocationProvider locationProvider, int newState) {
        if (locationListener != null) {
            locationListener.onLocatorStateChanged(newState);
        }
    }
}
