/*
 * (C) Serge Perinsky,  2008
 */

package com.sergebass.geography;

import java.io.*;
import javax.microedition.io.*;

/**
 * BluetoothGeoLocator.
 *
 * @author Serge Perinsky
 */
public class BluetoothGeoLocator
        implements GeoLocator {

    StreamConnection streamConnection = null;
    InputStream stream = null;

    NMEA0183Parser nmeaParser = null;

    public BluetoothGeoLocator(String connectionURLString)
            throws IOException {
        System.out.print("Creating GPS receiver connection to "
                         + connectionURLString + "...");
        
        try {
            streamConnection = (StreamConnection)Connector.open(connectionURLString);
        } catch (Exception e) {
            // strip the device URL string of parameters,
            // some devices have problems with this... (e.g. some Nokias)
            int parameterStart = connectionURLString.indexOf(";");
            if (parameterStart > 0) {
                connectionURLString = connectionURLString.substring(0, parameterStart);
            }
            streamConnection = (StreamConnection)Connector.open(connectionURLString);
        }
        
        stream = streamConnection.openInputStream();

        nmeaParser = new NMEA0183Parser(stream);
        nmeaParser.start();
        
        System.out.println(" Done.");
    }
    
    public void setLocationListener(GeoLocationListener listener) {
        if (nmeaParser != null) {
            nmeaParser.setLocationListener(listener);
        }
    }

    public synchronized void close()
            throws IOException {

        System.out.print("Closing GPS receiver connection...");
        
        if (nmeaParser != null) {
            nmeaParser.stop();
        }

        if (stream != null) {
            stream.close();
            stream = null;
        }
        
        if (streamConnection != null) {
            streamConnection.close();
            streamConnection = null;
        }
        
        System.out.println(" Done.");
    }

    public GeoLocation getLocation() {
        return (nmeaParser != null)? nmeaParser.getLocation() : null;
    }
}
