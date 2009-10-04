/*
 * (C) Serge Perinsky
 */

package com.sergebass.geo;

/**
 *
 * @author Serge Perinsky
 */
public interface NMEA0183ParserListener {
    public void handleParsingComplete(boolean isUserRequested);
}
