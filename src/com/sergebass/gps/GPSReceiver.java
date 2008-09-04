/*
 * (C) Serge Perinsky,  2008
 */

package com.sergebass.gps;

import java.io.*;
import javax.microedition.io.*;

/**
 * GPSReceiver.
 *
 * @author Serge Perinsky
 */
public class GPSReceiver {

    StreamConnection streamConnection = null;
    InputStream stream = null;
    InputStreamReader reader = null;
    
    public GPSReceiver(String connectionURLString)
            throws IOException {
        System.out.print("Creating GPS receiver connection to "
                         + connectionURLString + "...");
        
        try {
            streamConnection = (StreamConnection)Connector.open(connectionURLString);
        } catch (Exception e) {
            // strip the device URL string of parameters,
            // some devices have problems with this...
            int parameterStart = connectionURLString.indexOf(";");
            if (parameterStart > 0) {
                connectionURLString = connectionURLString.substring(0, parameterStart);
            }
            streamConnection = (StreamConnection)Connector.open(connectionURLString);
        }
        
        stream = streamConnection.openInputStream();
        reader = new InputStreamReader(stream);
        
        System.out.println(" Done.");
    }
    
    public StreamConnection getStreamConnection() {
        return streamConnection;
    }
    
    public InputStream getInputStream() {
        return stream;
    }
    
    public InputStreamReader getInputStreamReader() {
        return reader;
    }
    
    public void close()
            throws IOException {

        System.out.print("Closing GPS receiver connection...");
        
        if (reader != null) {
            reader.close();
            reader = null;
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
}
