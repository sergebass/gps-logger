/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

import java.util.Hashtable;

/**
 *
 * @author Serge Perinsky
 */
public class GeoLocation {

    String name = null;
    String description = null;

    double latitude = 0.0; // degrees in the WGS84 datum
    double longitude = 0.0; // degrees in the WGS84 datum
    float altitude = Float.NaN; // meters above the reference ellipsoid

    float course = Float.NaN; // degrees from the North
    float speed = Float.NaN; // meters per second

    long timeStamp = -1;
    String dateString = "";
    String timeString = "";

    int satelliteCount = 0;
    String fixTypeString = null;

    Hashtable otherData;

    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;

        otherData = new Hashtable();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAltitude() {
        return altitude;
    }
    
    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public float getCourse() {
        return course;
    }
    
    public void setCourse(float course) {
        this.course = course;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public long getTimestamp() {
        return timeStamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timeStamp = timestamp;
    }

    public int getSatelliteCount() {
        return satelliteCount;
    }

    public void setSatelliteCount(int satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    public String toString() {
        return toGPXTrackpointString(); // let this be default
    }

    public String toGPXTrackpointString() {
        return toGPXPointString("trkpt");
    }

    public String toGPXWaypointString() {
        return toGPXPointString("wpt");
    }

    public String toGPXPointString(String pointTypeTag) {

        StringBuffer stringBuffer = new StringBuffer();

        // opening tag with latitude and longitude of the point. Decimal degrees, WGS84 datum.
        stringBuffer.append("<" + pointTypeTag + " lat=\""
                    + String.valueOf(latitude)
                    + "\" lon=\""
                    + String.valueOf(longitude)
                    + "\">\n");
        
        // <name> the name of the waypoint.
        if (name != null) {
            if (!name.trim().equals("")) {
                stringBuffer.append(" <name>" + name + "</name>\n");
            }
        }

        // <desc> the description of the point.
        if (description != null) {
            if (!description.trim().equals("")) {
                stringBuffer.append(" <desc>" + description + "</desc>\n");
            }
        }

        // <time> Creation/modification timestamp for element. UTC, ISO 8601.
        if (dateString != null && timeString != null) {
            if ((!dateString.trim().equals("")) && (!dateString.trim().equals(""))) {
                stringBuffer.append(" <time>"
                        + dateString
                        + "T"
                        + timeString
                        + "Z</time>\n");
            }
        }

        // <ele> altitude/elevation (in meters) of the point.
        stringBuffer.append(" <ele>" + String.valueOf(altitude) + "</ele>\n");

        // <course> (degrees, true), actually, missing in GPX 1.1!
        stringBuffer.append(" <course>" + String.valueOf(course) + "</course>\n");

        // <speed> (meters per second), actually, missing in GPX 1.1!
        stringBuffer.append(" <speed>" + String.valueOf(speed / 3.6) + "</speed>\n");

        // <sat> satellite count
        stringBuffer.append(" <sat>" + String.valueOf(satelliteCount) + "</sat>\n");

        if (fixTypeString != null) {
            stringBuffer.append(" <fix>" + String.valueOf(fixTypeString) + "</fix>\n");
        }

///TODO: <hdop>
///TODO: <vdop>
///TODO: <pdop>

        // point closing tag
        stringBuffer.append("</" + pointTypeTag + ">\n");

        return stringBuffer.toString();
    }

    public String toKMLPlacemarkString() {

        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("<Placemark>\n");
        
        if (name != null) {
            if (!name.trim().equals("")) {
                stringBuffer.append(" <name>" + name + "</name>\n");
            }
        }

        if (description != null) {
            if (!description.trim().equals("")) {
                stringBuffer.append(" <description>" + description + "</description>\n");
            }
        }

        stringBuffer.append(" <Point>\n");

        stringBuffer.append("  <coordinates>"
                + longitude
                + ","
                + latitude
                + ","
                + altitude
                + "</coordinates>\n");
        
        stringBuffer.append(" </Point>\n");
        stringBuffer.append("</Placemark>\n");

/// add all of the other necessary fields here...

        return stringBuffer.toString();
    }
}
