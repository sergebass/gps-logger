/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author sperinskiy
 */
public class GPXWriter
        extends OutputStreamWriter {

    public GPXWriter(OutputStream stream)
            throws UnsupportedEncodingException {
        super(stream, "UTF-8");
    }

    public void writeHeader(String name)
            throws IOException {

        // let's use GPX 1.0 as of now, then we'll see...
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
///FIX HEADER TEXT (pass a parameter?)
        write("<gpx version=\"1.0\" creator=\"GPSLogger (http://code.google.com/p/gps-logger)\"\n");
        write(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        write(" xmlns=\"http://www.topografix.com/GPX/1/0\"\n");
        write(" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
        if (name != null) {
            write(" <name>" + name + "</name>\n");
        }
    }

    public void writeFooter()
            throws IOException {

        write("</gpx>\n");
    }

    public void writeTrackHeader(String name)
            throws IOException {

        write("<trk>\n");
        
        if (name != null) {
            write(" <name>" + name + "</name>\n");
        }
    }

    public void writeTrackFooter()
            throws IOException {

        write("</trk>\n");
    }

    public void writeTrackSegmentHeader()
            throws IOException {

        write("<trkseg>\n");
    }

    public void writeTrackSegmentFooter()
            throws IOException {

        write("</trkseg>\n");
    }

    public void writeLocationAsTrackpoint(GeoLocation location)
            throws IOException {

        write(location.toGPXTrackpointString());
    }

    public void writeLocationAsWaypoint(GeoLocation location)
            throws IOException {

        write(location.toGPXWaypointString());
    }
}
