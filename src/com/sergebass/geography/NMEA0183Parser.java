package com.sergebass.geography;

/*
 * NMEA0183Parser.java (C) 2008 by Serge Perinsky
 */

import java.util.Vector;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * NMEA0183Parser.
 *
 * @author Serge Perinsky
 */
public class NMEA0183Parser
        extends Thread {
    
    InputStream inputStream = null;
    
    boolean isValidGPSData = false;
    
    String gpxTimeString = "";
    String gpxDateString = "";
    long timestamp = -1;

    double latitude = 0.0; // degrees, [-90..90]
    double longitude = 0.0; // degrees, [-180..180)
    float altitude = Float.NaN; // meters above WGS84 reference ellipsoid

    float course = Float.NaN; // the terminal's course made good in degrees relative to true north or Float.NaN if the course is not known

    final static String fixQualityNames[] = {
                                            "invalid",
                                            "GPS/SPS",
                                            "DGPS",
                                            "PPS",
                                            "RTK",
                                            "FRTK",
                                            "estimate",
                                            "manual",
                                            "simulation"
    };

    int satelliteCount = 0;
    String fixQuality = "";

    float speed = Float.NaN; // the current ground speed in m/s for the terminal or Float.NaN if the speed is not known
            
    // other important fields
    boolean isStarted = false;
    final Object sentenceLock = new Object();
    
    GeoLocationListener listener = null;

    public NMEA0183Parser(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    public void setLocationListener(GeoLocationListener listener) {
        this.listener = listener;
    }

    public GeoLocation getLocation() {

        GeoLocation location = new GeoLocation(latitude, longitude);
        location.setAltitude(altitude);
        location.setCourse(course);
        location.setSpeed(speed);
        location.setDateString(gpxDateString);
        location.setTimeString(gpxTimeString);
        location.setSatelliteCount(satelliteCount);

///add the other fields ASAP!!!
        
        return location;
    }

    public void stop() {
        isStarted = false;
    }
    
    public void run() {
        isStarted = true;

        InputStreamReader reader = new InputStreamReader(inputStream);

        try {
            do {
                StringBuffer buffer = new StringBuffer();
                char c;
                int aWord;

                do { // first, read a sentence, ended by a LF character
                    aWord = reader.read();

                    if (aWord == -1) { // end-of-file?
        ///???                mustReconnectToGPS = true;
        ///???                getDisplay().vibrate(500);
                        break;
                    }

                    c = (char)aWord;
                    buffer.append(c);

                } while (c != '\n');

                String sentence = buffer.toString().trim();

                // avoid broken input sentences
                if (sentence.startsWith("$")) {
                    processSentence(sentence);
                }
            } while (isStarted);

        } catch (IOException e) {
/// HOW DO WE REPORT EXCEPTIONS??
            if (listener != null) {
                listener.handleLocatorException(e);
            }
        }
    }

    public void processSentence(String sentence) {

        String strippedSentence = "";
        String sentenceHeader = "";

        // by stripping we also _copying_ the values at the same time,
        // to release this resource as soon as possible
        // (to be used by the logging thread again, if necessary)
        synchronized (sentenceLock) {
            if (sentence.length() > 8) {
                strippedSentence = sentence.substring(7);
                sentenceHeader = sentence.substring(0, 7);
            }
        }
        
        if (sentenceHeader.equals("$GPRMC,")) {
            parseGPRMC(convertToArray(strippedSentence));

            // let the GPRMC be the callback trigger sentence
            // as it is the most important one
            // (and supposedly supported by all NMEA-0183 compliant devices)
            if (listener != null) {
                listener.locationChanged(getLocation());
            }
            
        } else if (sentenceHeader.equals("$GPGGA,")) {
            parseGPGGA(convertToArray(strippedSentence));
        } else if (sentenceHeader.equals("$GPGSA,")) {
            parseGPGSA(convertToArray(strippedSentence));
//?        } else if (sentenceHeader.equals("$GPGSV,")) {
//?            parseGPGSV(convertToArray(strippedSentence));
//?        } else if (sentenceHeader.equals("$GPVTG,")) {
//?            parseGPVTG(convertToArray(strippedSentence));
        }
    }
    
    /**
     * Our input values are separated by commas, convert the whole sentence
     * into an array of separate string values
     * 
     * @param sentence
     * @return
     */
    public String[] convertToArray(String sentence) {
        
        Vector stringVector = new Vector/* <String> */();
        
        int fromPosition = 0;
        int commaPosition;
        
        do {
            commaPosition = sentence.indexOf(',', fromPosition);
        
            if (commaPosition > 0) {
                stringVector.addElement(sentence.substring(fromPosition, commaPosition).trim());
            } else { // no more commas in the sentence
                stringVector.addElement(sentence.substring(fromPosition).trim());
                break;
            }
            
            fromPosition = commaPosition + 1;

        } while (true);
        
        // convert the vector into an array
        int stringCount = stringVector.size();
        String[] strings = new String[stringCount];

        for (int i = 0; i < stringCount; i++) {
            strings[i] = (String)stringVector.elementAt(i);
        }
        
        return strings;
    }
    
    /**
     * RMC - NMEA has its own version of essential gps pvt (position, velocity, time) data. It is called RMC, The Recommended Minimum, which will look similar to:
     *
     * $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A
     *
     * Where:
     * RMC          Recommended Minimum sentence C
     * 123519       Fix taken at 12:35:19 UTC
     * A            Status A=active or V=Void.
     * 4807.038,N   Latitude 48 deg 07.038' N
     * 01131.000,E  Longitude 11 deg 31.000' E
     * 022.4        Speed over the ground in knots
     * 084.4        Track angle in degrees True
     * 230394       Date - 23rd of March 1994
     * 003.1,W      Magnetic Variation
     * *6A          The checksum data, always begins with *
     *
     * @param values
     */
    public void parseGPRMC(String[] values) {

        if (values.length > 0) {
            String originalTimeString = values[0];
            gpxTimeString = ""; // reset
            if (originalTimeString.length() > 5) { // HHMMSS must be present
                String hourString = originalTimeString.substring(0, 2);
                String minuteString = originalTimeString.substring(2, 4);
                String secondString = originalTimeString.substring(4, 6);
                gpxTimeString =  hourString + ":" + minuteString + ":" + secondString;
            }
        }

        if (values.length > 1) {
            isValidGPSData = values[1].equalsIgnoreCase("A");
        }

        if (values.length > 3) { // latitude

            double latitudeDegrees = 0.0;
            double latitudeMinutes = 0.0;

            String latitudeHemisphere = values[3]; // 'N' or 'S'
            String latitudeString = values[2];
            String latitudeDegreesString = "0.0";
            String latitudeMinutesString = "0.0";
            
            if (latitudeString.length() > 2) {
                latitudeDegreesString = latitudeString.substring(0, 2); // degrees
                latitudeMinutesString = latitudeString.substring(2); // minutes, fractional
            }
            
            try {
                latitudeDegrees = Double.valueOf(latitudeDegreesString).doubleValue();
            } catch (Exception e) {
                latitudeDegrees = 0.0;
            }
            
            try {
                latitudeMinutes = Double.valueOf(latitudeMinutesString).doubleValue();
            } catch (Exception e) {
                latitudeMinutes = 0.0;
            }
            
            latitude = latitudeDegrees + latitudeMinutes / 60.0;
            
            if (latitudeHemisphere.equalsIgnoreCase("S")) { // southern hemisphere?
                latitude = -latitude; // invert sign for southern hemisphere;
            }
        }
        
        if (values.length > 5) { // longitude

            double longitudeDegrees = 0.0;
            double longitudeMinutes = 0.0;

            String longitudeHemisphere = values[5]; // 'W' or 'E'
            String longitudeString = values[4];
            String longitudeDegreesString = "0.0";
            String longitudeMinutesString = "0.0";
            
            if (longitudeString.length() > 3) {
                longitudeDegreesString = longitudeString.substring(0, 3); // degrees
                longitudeMinutesString = longitudeString.substring(3); // minutes, fractional
            }

            try {
                longitudeDegrees = Double.valueOf(longitudeDegreesString).doubleValue();
            } catch (Exception e) {
                longitudeDegrees = 0.0;
            }
            
            try {
                longitudeMinutes = Double.valueOf(longitudeMinutesString).doubleValue();
            } catch (Exception e) {
                longitudeMinutes = 0.0;
            }
            
            longitude = longitudeDegrees + longitudeMinutes / 60.0;

            if (longitudeHemisphere.equalsIgnoreCase("W")) { // western hemisphere?
                longitude = -longitude; // invert sign for western hemisphere;
            }
        }

        if (values.length > 6) { // ground speed in knots
            try {
                speed = Float.parseFloat(values[6]) * 0.5144444f; // knots -> m/sec
            } catch (Exception e) {
                speed = Float.NaN;
            }
        }
        
        if (values.length > 7) { // course/heading
            try {
                course = Float.parseFloat(values[7]);
            } catch (Exception e) {
                course = 0.0f;
            }
        }
        
        if (values.length > 8) { // date
            gpxDateString = ""; // reset
            String originalDateString = values[8];
            if (originalDateString.length() > 5) { // DDMMYY must be present
                gpxDateString = "20"
                       + originalDateString.substring(4, 6) // year
                       + "-"
                       + originalDateString.substring(2, 4) // month
                       + "-"
                       + originalDateString.substring(0, 2); // day
            }
        }

/// compute timestamp from gpxDate & gpxTime?
        timestamp = System.currentTimeMillis();
    }

    /**
     * The most important NMEA sentences include the GGA which provides the current Fix data, the RMC which provides the minimum gps sentences information, and the GSA which provides the Satellite status data.
     *
     * GGA - essential fix data which provide 3D location and accuracy data.
     *
     * $GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47
     * 
     * Where:
     * GGA          Global Positioning System Fix Data
     * 123519       Fix taken at 12:35:19 UTC
     * 4807.038,N   Latitude 48 deg 07.038' N
     * 01131.000,E  Longitude 11 deg 31.000' E
     * 1            Fix quality: 0 = invalid
     *                           1 = GPS fix (SPS)
     *                           2 = DGPS fix
     *                           3 = PPS fix
     * 			         4 = Real Time Kinematic
     *			         5 = Float RTK
     *                           6 = estimated (dead reckoning) (2.3 feature)
     *                           7 = Manual input mode
     *                           8 = Simulation mode
     * 08           Number of satellites being tracked
     * 0.9          Horizontal dilution of position
     * 545.4,M      Altitude, Meters, above mean sea level
     * 46.9,M       Height of geoid (mean sea level) above WGS84
     *              ellipsoid
     * (empty field) time in seconds since last DGPS update
     * (empty field) DGPS station ID number
     * 47           the checksum data, always begins with *
     *
     * @param values
     */
    public void parseGPGGA(String[] values) {
        
        // (the date/time and latitude/longitude values are currently set by the GPRMC sentence)
        
        if (values.length > 7) { // satellite information
            try {
                int fixQualityIndex = Integer.parseInt(values[5]);
                if (fixQualityIndex < fixQualityNames.length) {
                    fixQuality = fixQualityNames[fixQualityIndex];
                }
                        
                satelliteCount = Integer.parseInt(values[6]);
            } catch (Exception e) {
                satelliteCount = 0;
            }
        }
        
        if (values.length > 11) {
            try {
                altitude = Float.valueOf(values[8]).floatValue();
            } catch (Exception e) {
                altitude = Float.NaN;
            }
            
            //? String altitudeUnits = values[9].equals("M")? " m" : " ?";
        }
    }

    /**
     * GSA - GPS DOP and active satellites.
     * 
     * 1    = Mode:
     *        M=Manual, forced to operate in 2D or 3D
     *        A=Automatic, 3D/2D
     * 2    = Mode:
     *        1=Fix not available
     *        2=2D
     *        3=3D
     * 3-14 = PRN's of Satellite Vechicles (SV's) used in position fix (null for unused fields)
     * 15   = Position Dilution of Precision (PDOP)
     * 16   = Horizontal Dilution of Precision (HDOP)
     * 17   = Vertical Dilution of Precision (VDOP)
     * 
     * @param values
     */
    public void parseGPGSA(String[] values) {
        
///!! process this ASAP: supported by GPX format, needs to be saved
///e.g. $GPGSA,A,3,21,24,06,29,16,30,31,,,,,,1.46,1.16,0.89*08

    }

    /**
     * GSV - Satellites in view.
     * 
     * 1    = Total number of messages of this type in this cycle
     * 2    = Message number
     * 3    = Total number of SVs in view
     * 4    = SV PRN number
     * 5    = Elevation in degrees, 90 maximum
     * 6    = Azimuth, degrees from true north, 000 to 359
     * 7    = SNR, 00-99 dB (null when not tracking)
     * 8-11 = Information about second SV, same as field 4-7
     * 12-15= Information about third SV, same as field 4-7
     * 16-19= Information about fourth SV, same as field 4-7
     * 
     * @param values
     */
    public void parseGPGSV(String[] values) {
    }

    /**
     * VTG - Velocity made good. The gps receiver may use the LC prefix instead of GP if it is emulating Loran output.
     *
     * $GPVTG,054.7,T,034.4,M,005.5,N,010.2,K*33
     *
     * where:
     *   VTG          Track made good and ground speed
     *   054.7,T      True track made good (degrees)
     *   034.4,M      Magnetic track made good
     *   005.5,N      Ground speed, knots
     *   010.2,K      Ground speed, Kilometers per hour
     *   *33          Checksum
     *
     * Note that, as of the 2.3 release of NMEA, there is a new field in the VTG sentence at the end just prior to the checksum. For more information on this field see here.
     * 
     * Receivers that don't have a magnetic deviation (variation) table built in will null out the Magnetic track made good.
     * 
     * @param values
     */
    public void parseGPVTG(String[] values) {
        if (isValidGPSData) {
            if (values.length > 6) {
                try {
                    double groundSpeedKnots = Double.valueOf(values[4]).doubleValue();
                    double groundSpeedKPH = Double.valueOf(values[6]).doubleValue();
                } catch (Exception e) {
                }
            }
        }
    }
}