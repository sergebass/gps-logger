/*
 * (C) 2008 by Serge Perinsky
 */

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

/**
 * GPSLogFile.
 *
 * @author Serge Perinsky
 */
public class GPSLogFile {
    
    String logFilePath;
    FileConnection gpsLogFileConnection = null;
    OutputStream outputStream = null;
    
    public GPSLogFile(String logFilePath)
            throws IOException {
        this.logFilePath = logFilePath;
        System.out.println("Opening a connection to " + logFilePath);

        gpsLogFileConnection = (FileConnection)Connector.open(logFilePath);
            
        if (!gpsLogFileConnection.exists()) {
            System.out.print("This is a new file. Creating...");
            gpsLogFileConnection.create();
            System.out.println(" Done.");
        }
    }
            
    public String getPath() {
        return logFilePath;
    }
    
    public OutputStream getOutputStream()
            throws IOException {

        if (outputStream == null) {
            if (gpsLogFileConnection.canWrite()) {
                outputStream = gpsLogFileConnection.openOutputStream();
            }
        }
        
        return outputStream;
    }
            
    public void close()
            throws IOException {

        System.out.print("Closing GPS log file...");
        
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
        
        if (gpsLogFileConnection != null) {
            gpsLogFileConnection.close();
            gpsLogFileConnection = null;
        }
        
        System.out.println(" Done.");
    }
}
