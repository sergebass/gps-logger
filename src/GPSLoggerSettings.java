/*
 * GPSLoggerSettings.java (C) Serge Perinsky, 2008
 */

import com.sergebass.util.Settings;
import javax.microedition.rms.RecordStoreException;

/**
 * GPSLoggerSettings.
 *
 * @author Serge Perinsky
 */
public class GPSLoggerSettings
            extends Settings {

    final static String RECORD_STORE_NAME = "GPSLogger";

    public final static String GPS_DEVICE_URL = "GPSDeviceURL";
    
    public final static String LOG_FOLDER_PATH = "logFolderPath";
    
    public final static String COORDINATES_MODE = "coordinatesMode";
    public final static int COORDINATES_MODE_D = 0;
    public final static int COORDINATES_MODE_DM = 1;
    public final static int COORDINATES_MODE_DMS = 2;

    public final static String ALTITUDE_UNITS = "altitudeUnits";
    public final static int ALTITUDE_UNITS_METERS = 0;
    public final static int ALTITUDE_UNITS_FEET = 1;

    public final static String SPEED_UNITS = "speedUnits";
    public final static int SPEED_UNITS_KPH = 0;
    public final static int SPEED_UNITS_MPH = 1;
    public final static int SPEED_UNITS_KNOTS = 2;
    public final static int SPEED_UNITS_MPS = 3;

    public GPSLoggerSettings(GPSLogger loggerMidlet) {
        super(loggerMidlet, RECORD_STORE_NAME);
        setListener(loggerMidlet);
    }
    
    public void load()
            throws RecordStoreException {
        super.load();
    }
    
    public void save()
            throws RecordStoreException {
        super.save();
    }
    
    public String getGPSDeviceURL() {
        return (String)get(GPS_DEVICE_URL);
    }

    public void setGPSDeviceURL(String url) {
        put(GPS_DEVICE_URL, url);
    }
    
    public String getLogFolder() {
        return (String)get(LOG_FOLDER_PATH);
    }

    public void setLogFolder(String path) {
        put(LOG_FOLDER_PATH, path);
    }

    public int getCoordinatesMode() {
        String modeIndexString = (String)get(COORDINATES_MODE);
        return  modeIndexString != null?
                  Integer.parseInt(modeIndexString)
                : COORDINATES_MODE_D;
    }

    public void setCoordinatesMode(int modeIndex) {
        put(COORDINATES_MODE, "" + modeIndex);
    }

    public int getAltitudeUnits() {
        String unitsIndexString = (String)get(ALTITUDE_UNITS);
        return unitsIndexString != null?
                  Integer.parseInt(unitsIndexString)
                : ALTITUDE_UNITS_METERS;
    }

    public void setAltitudeUnits(int unitsIndex) {
        put(ALTITUDE_UNITS, "" + unitsIndex);
    }

    public int getSpeedUnits() {
        String unitsIndexString = (String)get(SPEED_UNITS);
        return unitsIndexString != null?
                  Integer.parseInt(unitsIndexString)
                : SPEED_UNITS_KPH;
    }

    public void setSpeedUnits(int unitsIndex) {
        put(SPEED_UNITS, "" + unitsIndex);
    }
}
