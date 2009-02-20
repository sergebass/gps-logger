/*
 * (C) Serge Perinsky
 */

package com.sergebass.geography;

/**
 *
 * @author Serge Perinsky
 */
public interface NMEA0183ParserListener {
    public void handleParsingComplete(boolean isUserRequested);
}
