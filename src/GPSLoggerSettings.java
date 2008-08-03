/*
 * GPSLoggerSettings.java (C) Serge Perinsky, 2008
 */

import com.sergebass.util.Settings;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

/**
 * GPSLoggerSettings.
 *
 * @author Serge Perinsky
 */
public class GPSLoggerSettings
            extends Settings {

    final static String RECORD_STORE_NAME = "GPSLogger";
    
    public GPSLoggerSettings(MIDlet midlet) {
        
        super(midlet, RECORD_STORE_NAME);
    }
    
    public void load()
            throws RecordStoreException {
        
        super.load();
    }
    
    public void save()
            throws RecordStoreException {
        
        super.save();
    }
    
    public void setGPSDeviceURL(String url) {
        
        put("GPSDeviceURL", url);
    }
    
    public String getGPSDeviceURL() {
        
        return (String)get("GPSDeviceURL");
    }
    
    public void setLogFolder(String path) {

        put("logFolderPath", path);
    }

    public String getLogFolder() {
        
        return (String)get("logFolderPath");
    }
}
