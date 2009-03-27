package com.sergebass.geography;

/*
 * NMEA0183Parser.java (C) 2008 by Serge Perinsky
 */

import java.util.Vector;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * NMEA0183Parser.
 *
 * @author Serge Perinsky
 */
public class NMEA0183Parser
        extends Thread {
    
    final static long WATCHDOG_TIMEOUT_MILLIS = 10000L; // 10 seconds

    long lastSentenceTimeMillis = System.currentTimeMillis();
    String lastSentencePack = null;
    StringBuffer sentenceBuffer = null;

    InputStream inputStream = null;

    boolean isValidGPSData = false;
    
    String gpxTimeString = "";
    String gpxDateString = "";
    long timestamp = -1;

    double latitude = 0.0; // degrees, [-90..90]
    double longitude = 0.0; // degrees, [-180..180)
    float altitude = Float.NaN; // meters above WGS84 reference ellipsoid

    float course = Float.NaN; // the terminal's course made good in degrees relative to true north or Float.NaN if the course is not known

    int satelliteCount = 0;
    
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

    int fixQualityIndex = -1;
    String fixQuality = "";

    int fixTypeIndex = -1;
    String fixType = "";

    float speed = Float.NaN; // the current ground speed in m/s for the terminal or Float.NaN if the speed is not known
            
    float pdop = Float.NaN; // Position dillution of precision
    float hdop = Float.NaN; // Horizontal dillution of precision
    float vdop = Float.NaN; // Vertical dillution of precision

    // other important fields
    boolean isStarted = false;
    final Object sentenceLock = new Object();
    
    NMEA0183ParserListener parserListener = null;
    GeoLocationListener locationListener = null;

    NMEA0183ParserWatchdog watchdog = null;

    public NMEA0183Parser(NMEA0183ParserListener parserListener,
                          InputStream inputStream) {
        this.parserListener = parserListener;
        this.inputStream = inputStream;
    }
    
    public void setLocationListener(GeoLocationListener listener) {
        this.locationListener = listener;
    }

    public GeoLocation getLocation() {

        GeoLocation location = new GeoLocation(latitude, longitude);

        if (!Float.isNaN(altitude)) {
            location.setAltitude(altitude);
        }

        if (!Float.isNaN(course)) {
            location.setCourse(course);
        }

        if (!Float.isNaN(speed)) {
            location.setSpeed(speed);
        }
        
        location.setDateString(gpxDateString);
        location.setTimeString(gpxTimeString);

        location.setSatelliteCount(satelliteCount);
        location.setFixType(fixType);

        if (!Float.isNaN(pdop)) {
            location.setPDOP(pdop);
        }

        if (!Float.isNaN(hdop)) {
            location.setHDOP(hdop);
        }

        if (!Float.isNaN(vdop)) {
            location.setVDOP(vdop);
        }
        
        location.setValid(isValidGPSData);

        // save NMEA 0183 data in the location object
        if (lastSentencePack != null) {
            location.setNMEASentences(lastSentencePack);
        }
        
        return location;
    }

    public long getLastSentenceTime() {
        return lastSentenceTimeMillis;
    }

    public void stop() {
        isStarted = false;
        watchdog.stop();
    }
    
    public void run() {
        isStarted = true;

        // add a watchdog thread to handle GPS receiver hang-ups!
        watchdog = new NMEA0183ParserWatchdog(this, WATCHDOG_TIMEOUT_MILLIS);
        watchdog.start();

        InputStreamReader reader = new InputStreamReader(inputStream);

        try {
            do {
                StringBuffer buffer = new StringBuffer();
                char c;
                int aWord;

                do { // first, read a sentence (ending with a linefeed character)

                    // let's do an additional check in order
                    // to react more quickly to a stop request
                    if (!isStarted) {
                        return;
                    }

                    aWord = reader.read();

                    if (aWord == -1) { // end-of-file?
                        System.out.println("The end of NMEA stream has been reached\n");
                        isStarted = false;
                        onParsingComplete(false); // not requested by user
                        return; // Ok, let's quit
                    }

                    c = (char)aWord;
                    buffer.append(c);

/// $GP - GPS
/// $GL - GLONASS
/// $GN - GLONASS+GPS


                } while (c != '\n');
///                } while (c != '$'); // $GP is the NMEA-0183 prefix for GPS data

                String sentence = splitSentences(buffer.toString()).trim();

/// some chips may generate several sentences in a single text line (really?);
/// make sure $GPxxx,$GPyyy are split into different lines
                
                // avoid broken input sentences
                if (sentence.startsWith("$")) {
                    processSentence(sentence);
                }
            } while (isStarted);

            onParsingComplete(true); // this stopping was requested by user
            
        } catch (Exception e) {
            e.printStackTrace();
            if (locationListener != null) {
                locationListener.onLocatorException(e);
            }
        }
    }

    public void onParsingComplete(boolean isUserRequested) {
        System.out.println("The NMEA stream parsing is finished");
        watchdog.stop();
        if (parserListener != null) {
            parserListener.handleParsingComplete(isUserRequested);

            // this was the last notification
            parserListener = null;
        }
    }

    public static String splitSentences(String originalSentences) {
        // let's quickly check if we need to split anything at all:
        // if this is the only sentence, leave it as is
        int sentenceStartIndex = originalSentences.lastIndexOf('$');
        if (sentenceStartIndex == 0) {
            return originalSentences;
        }

/// TODO: splitSentencesInSeparateLines()
        
/*
        int startIndex = 0;

        do {
            sentenceStartIndex = originalSentences.indexOf("$GP", startIndex);
        } while (true);
        
        originalSentences.replace('$', '\n');
*/
return originalSentences;
///
    }

    public void processSentence(String sentence) {

        lastSentenceTimeMillis = System.currentTimeMillis(); // update timestamp

        if (sentenceBuffer == null) {
            sentenceBuffer = new StringBuffer();
        }

        sentenceBuffer.append(sentence + "\n"); // save for later, this may be recorded

        String strippedSentence = "";
        String sentenceHeader = "";

        // by stripping we are also _copying_ the values at the same time,
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
            
            lastSentencePack = sentenceBuffer.toString().trim(); // make a copy
            
            GeoLocation currentLocation = getLocation();

            // mark for deletion, this will be recreated during the next iteration
            sentenceBuffer = null;

            if (locationListener != null) {
                locationListener.onLocationUpdated(currentLocation);
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
        
        // first, strip the sentence of the checksum
        // (starts with asterisk at the end of the sentence)
        int checkSumPosition = sentence.indexOf('*');

        if (checkSumPosition != -1) {
            sentence = sentence.substring(0, checkSumPosition);
        }

        Vector stringVector = new Vector/* <String> */();
        
        int fromPosition = 0;
        int commaPosition;
        
        do {
            commaPosition = sentence.indexOf(',', fromPosition);
        
            if (commaPosition > 0) {
                String element = sentence.substring(fromPosition, commaPosition).trim();
                stringVector.addElement(element);
            } else { // no more commas in the sentence
                String element = sentence.substring(fromPosition).trim();
                stringVector.addElement(element);
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
        
        if (values.length > 6) { // satellite information
            try {
                fixQualityIndex = Integer.parseInt(values[5]);
                if (fixQualityIndex < fixQualityNames.length) {
                    fixQuality = fixQualityNames[fixQualityIndex];
                }
                satelliteCount = Integer.parseInt(values[6]);
            } catch (Exception e) {
                satelliteCount = 0;
                fixQualityIndex = -1;
            }
        }
        
        if (values.length > 8) {
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
        
        fixType = ""; // reset
        
        if (values.length > 1) {
            try {
                fixTypeIndex = Integer.parseInt(values[1]);
            } catch (Exception e) {
                fixTypeIndex = -1;
            }

            if (fixQualityIndex == 1) { // normal GPS
                if (fixTypeIndex == 2) {
                    fixType = "2d";
                } else if (fixTypeIndex == 3) {
                    fixType = "3d";
                }
            } else if (fixQualityIndex == 2) { // DGPS
                fixType = "dgps";
            } else if (fixQualityIndex == 3) { // PPS
                fixType = "pps";
            } else if (fixQualityIndex == 0) { // invalid
                fixType = "none";
            }
        }

        if (values.length > 14) {
            try {
                pdop = Float.valueOf(values[14]).floatValue();
            } catch (Exception e) {
                pdop = Float.NaN;
            }
        }

        if (values.length > 15) {
            try {
                hdop = Float.valueOf(values[15]).floatValue();
            } catch (Exception e) {
                hdop = Float.NaN;
            }
        }

        if (values.length > 16) {
            try {
                vdop = Float.valueOf(values[16]).floatValue();
            } catch (Exception e) {
                vdop = Float.NaN;
            }
        }
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
