/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.gps;

import java.util.Hashtable;

/**
 *
 * @author Serge Perinsky
 */
public class GPSPoint {

    double latitude = 0.0; // degrees in the WGS84 datum
    double longitude = 0.0; // degrees in the WGS84 datum
    float altitude = 0.0f; // meters above the reference ellipsoid

    float course = 0.0f; // degrees from the North
    float speed = 0.0f; // meters per second

    long timeStamp = -1;

    int satelliteCount = 0;

    Hashtable otherData;

    public GPSPoint() {

        otherData = new Hashtable();
    }

    public String toGPXTrackPointString() {
///
return "<zzz>";
///
    }
}
