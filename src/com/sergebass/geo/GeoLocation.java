/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geo;

import com.sergebass.util.Instant;
/**
 *
 * @author Serge Perinsky
 */
public class GeoLocation {

    boolean isDataValid = true;

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

    int satelliteCount = -1;
    String fixType = null;

    float pdop = Float.NaN; // Position dillution of precision
    float hdop = Float.NaN; // Horizontal dillution of precision
    float vdop = Float.NaN; // Vertical dillution of precision

    String nmeaSentences = null;

///set this flag from user settings
    boolean saveNMEASentences = true;

    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isValid() {
        return isDataValid;
    }

    public void setValid(boolean isDataValid) {
        this.isDataValid = isDataValid;
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

        // make GPX/UTC date/time from the timestamp instant
        Instant instant = new Instant(timestamp);
        setDateString(instant.getISO8601UTCDateId());
        setTimeString(instant.getISO8601UTCTimeId());
    }

    public int getSatelliteCount() {
        return satelliteCount;
    }

    public void setSatelliteCount(int satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    public String getFixType() {
        return fixType;
    }

    public void setFixType(String fixType) {
        this.fixType = fixType;
    }

    public float getPDOP() {
        return pdop;
    }

    public void setPDOP(float pdop) {
        this.pdop = pdop;
    }

    public float getHDOP() {
        return hdop;
    }

    public void setHDOP(float hdop) {
        this.hdop = hdop;
    }

    public float getVDOP() {
        return vdop;
    }

    public void setVDOP(float vdop) {
        this.vdop = vdop;
    }

    public String getNMEASentences() {
        return nmeaSentences;
    }
    
    public void setNMEASentences(String sentences) {
        nmeaSentences = sentences;
    }

    public String toString() {
        return toGPXTrackpointString(); // let this be default
    }

///move this to GPX writer
    public String toGPXTrackpointString() {
        return toGPXPointString("trkpt");
    }

///move this to GPX writer
    public String toGPXWaypointString() {
        return toGPXPointString("wpt");
    }

///move this to GPX writer
    public String toGPXPointString(String pointTypeTag) {

        StringBuffer stringBuffer = new StringBuffer();

        // comment out invalid trackpoints
        // (who knows, maybe someone will need them anyway)
        // this is the opening comment tag:
        if (!isDataValid && pointTypeTag.equalsIgnoreCase("trkpt")) {
            stringBuffer.append("<!-- invalid point\n"); // start comment section
        }

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
        if (!Float.isNaN(altitude)) {
            stringBuffer.append(" <ele>" + String.valueOf(altitude) + "</ele>\n");
        }

        // <course> (degrees, true), actually, missing in GPX 1.1!
        if (!Float.isNaN(course)) {
            stringBuffer.append(" <course>" + String.valueOf(course) + "</course>\n");
        }

        // <speed> (meters per second), actually, missing in GPX 1.1!
        if (!Float.isNaN(speed)) {
            stringBuffer.append(" <speed>" + String.valueOf(speed) + "</speed>\n");
        }

        // <sat> satellite count
        if (satelliteCount >= 0) {
            stringBuffer.append(" <sat>" + String.valueOf(satelliteCount) + "</sat>\n");
        }

        if (fixType != null) {
            if (!fixType.equals("")) {
                stringBuffer.append(" <fix>" + String.valueOf(fixType) + "</fix>\n");
            }
        }

        if (!Float.isNaN(pdop)) {
            stringBuffer.append(" <pdop>" + String.valueOf(pdop) + "</pdop>\n");
        }

        if (!Float.isNaN(hdop)) {
            stringBuffer.append(" <hdop>" + String.valueOf(hdop) + "</hdop>\n");
        }

        if (!Float.isNaN(vdop)) {
            stringBuffer.append(" <vdop>" + String.valueOf(vdop) + "</vdop>\n");
        }

        // point closing tag
        stringBuffer.append("</" + pointTypeTag + ">\n");

        // comment out invalid trackpoints
        // (who knows, maybe someone will need them anyway)
        // this is the closing comment tag:
        if (!isDataValid && pointTypeTag.equalsIgnoreCase("trkpt")) {
            stringBuffer.append("invalid point -->\n"); // finish comment section
        }

        if (saveNMEASentences) {
            if (nmeaSentences != null) {
                stringBuffer.append("<!--\n" + nmeaSentences + "\n-->\n\n");
            }
        }

        return stringBuffer.toString();
    }

///move this to KML writer
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

/// add all of the other necessary KML tags here...

        return stringBuffer.toString();
    }

    public GeoLocation createCopy() {
        GeoLocation newLocation = new GeoLocation(latitude, longitude);
        newLocation.altitude = altitude;
        newLocation.course = course;
        newLocation.speed = speed;
        newLocation.timeStamp = timeStamp;
        newLocation.timeString = timeString;
        newLocation.dateString = dateString;
        newLocation.name = name;
        newLocation.description = description;
        newLocation.satelliteCount = satelliteCount;
        newLocation.fixType = fixType;
        newLocation.isDataValid = isDataValid;
        newLocation.nmeaSentences = nmeaSentences;
        newLocation.saveNMEASentences = saveNMEASentences;
        newLocation.pdop = pdop;
        newLocation.hdop = hdop;
        newLocation.vdop = vdop;
        return newLocation;
    }
}
