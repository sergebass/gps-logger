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
        implements GeoLocator,
                   NMEA0183ParserListener {

    StreamConnection streamConnection = null;
    InputStream stream = null;

    GeoLocationListener locationListener = null;

    NMEA0183Parser nmeaParser = null;

    final Object connectionLock = new Object();

    public BluetoothGeoLocator(String connectionURLString)
            throws IOException {

        synchronized (connectionLock) {
            System.out.print("Creating GPS receiver connection to "
                             + connectionURLString + "...");

            try {
                streamConnection = (StreamConnection)Connector.open
                                    (connectionURLString, Connector.READ, true);
            } catch (Exception e) {
                // strip the device URL string of parameters,
                // some devices have problems with this... (e.g. some Nokias)
                int parameterStart = connectionURLString.indexOf(";");
                if (parameterStart > 0) {
                    connectionURLString = connectionURLString.substring(0, parameterStart);
                }

                // try one more time...
                streamConnection = (StreamConnection)Connector.open
                                    (connectionURLString, Connector.READ, true);
            }

            stream = streamConnection.openInputStream();
        } // synchronized (connectionLock)

        nmeaParser = new NMEA0183Parser(this, stream);
        nmeaParser.start();
        
        System.out.println(" Connected.");
    }
    
    public void setLocationListener(GeoLocationListener locationListener) {
        this.locationListener = locationListener;
        if (nmeaParser != null) {
            nmeaParser.setLocationListener(locationListener);
        }
    }

    public synchronized void close()
            throws IOException {

        System.out.print("Closing GPS receiver connection...");
        
        if (nmeaParser != null) {
            nmeaParser.stop();
            nmeaParser = null;
        }

        synchronized (connectionLock) {
            if (stream != null) {
                stream.close();
                stream = null;
            }

            if (streamConnection != null) {
                streamConnection.close();
                streamConnection = null;
            }
        }
        
        System.out.println(" Disconnected.");
    }

    public GeoLocation getLocation() {
        return (nmeaParser != null)? nmeaParser.getLocation() : null;
    }

    public void handleParsingComplete() {

        try {
            close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // end-of-file for bluetooth connection means
        // that the connection was lost/broken; notify the interested party

        if (locationListener != null) {
            locationListener.onLocatorException(new GeoLocatorException("Lost Bluetooth GPS connection"));
        }
    }
}
