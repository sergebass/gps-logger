/*
 * GPSProcessor.java (C) 2008 by Serge Perinsky
 */

import java.util.Vector;
import java.io.OutputStreamWriter;
import java.io.IOException;
import com.sergebass.gps.*;
import com.sergebass.util.Instant;

/**
 * GPSProcessor.
 *
 * @author Serge Perinsky
 */
public class GPSProcessor
        extends Thread {
    
    final static double MIN_DISTANCE_DELTA = 0.5; // meters (each second)
    final static double MIN_GROUND_DISTANCE_DELTA = 10.0; // meters

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

    // current GPS data cache
    boolean isValidGPSData = false;
    int satelliteCount = 0;
    String fixQuality = "";
            
    String gpsTimeString = "";
    String gpsDateString = "";
        
    double latitude = 0.0; // minutes
    double longitude = 0.0; // minutes
    double altitude = Double.MIN_VALUE; // also as a flag
    
    double directionAngle = 0.0;
    String headingSymbol = "";
    String headingAngleString = "";

    double groundSpeedKPH = -1.0; // km/h (kilometers per hour)
    double groundSpeedKnots = -1.0; // knots (sea miles per hour)
    
    double speed = -1.0; // km/h
    double maxSpeed = 0.0; // km/h
            
    // old (previous point) data
    boolean isOldDataSet = false;
    double oldLatitude = 0.0; // minutes
    double oldLongitude = 0.0; // minutes
    int oldFixTimeSeconds = 0;
    
    // odometer data cache
    long startTimeMillis = 0;
    long tripTimeMillis = 0L;
    double distance = 0.0; // accumulative, meters
    
    double totalElevation = 0.0; // accumulative, meters
    
    // slope calculator data cache
    long slopeStartTimeMillis = -1L; // as well as a flag (-1 = invalid data)
    double slopeStartAltitude = 0.0; // meters above sea level
    double groundDistanceFromSlopeStart = 0.0; // meters
    double slopePercent = 0.0; // %

    // other important fields
    GPSLogger midlet = null;
    
    boolean isStarted = false;
    final Object sentenceLock = new Object();
    String sentence;
    
    public GPSProcessor(GPSLogger midlet) {
        this.midlet = midlet;
        
        resetOdometer();
        resetSlopeCalculator();
    }
    
    public void setSentence(String newSentence) {
        synchronized(sentenceLock) {
            this.sentence = newSentence.trim(); // copy
            sentenceLock.notifyAll();
        }
    }
    
    public void writeLogHeader(OutputStreamWriter writer) {

        // let's use GPX 1.0 as of now, then we'll see...
        
        try {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<gpx version=\"1.0\" creator=\"GPSLogger (http://code.google.com/p/gps-logger)\"\n");
            writer.write(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            writer.write(" xmlns=\"http://www.topografix.com/GPX/1/0\"\n");
            writer.write(" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
            writer.write("\n<name>GPSLogger waypoints</name>\n");
        } catch (IOException e) {
            // ignore?
        }
    }
    
    public void writeLogFooter(OutputStreamWriter writer) {
        try {
            writer.write("\n</gpx>\n");
        } catch (IOException e) {
            // ignore?
        }
    }
    
    public void writeCurrentPoint(OutputStreamWriter writer, String comment) {
        System.out.print("Writing a mark \"" + comment + "\"...");
        
        try {
            Instant now = new Instant();

            // <wpt> tag with latitude and longitude of the point. Decimal degrees, WGS84 datum.
            writer.write("\n<wpt lat=\""
                    + String.valueOf(latitude / 60.0) // convert to degrees
                    + "\" lon=\""
                    + String.valueOf(longitude / 60.0) // convert to degrees
                    + "\">\n");
            
            // <name> (if present) The GPS name of the waypoint.
            if (comment != null) {
                if (!comment.trim().equals("")) {
                    writer.write(" <name>" + comment + "</name>\n");
                }
            }
            
            // <time> Creation/modification timestamp for element. UTC, ISO 8601.
            writer.write(" <time>"
                    + gpsDateString
                    + "T"
                    + gpsTimeString
                    + "Z</time>\n");
            
            // <ele> altitude/elevation (in meters) of the point.
            writer.write(" <ele>" + String.valueOf(altitude) + "</ele>\n");
            
            // <course> (degrees, true), actually, missing in GPX 1.1!
            writer.write(" <course>" + String.valueOf(directionAngle) + "</course>\n");
            
            // <speed> (meters per second), actually, missing in GPX 1.1!
            writer.write(" <speed>" + String.valueOf(speed / 3.6) + "</speed>\n");

            // <sat> satellite count
            writer.write(" <sat>" + String.valueOf(satelliteCount) + "</sat>\n");

///TODO: <fix>
///TODO: <hdop>
///TODO: <vdop>
///TODO: <pdop>
           
            // </wpt> waypoint closing tag
            writer.write("</wpt>\n");

            midlet.getDisplay().vibrate(200);
        } catch (IOException e) {
            // ignore?
        }
        
        System.out.println(" Done.");
    }
    
    public void stop() {
        isStarted = false;
        synchronized(sentenceLock) {
            sentenceLock.notifyAll();
        }
    }
    
    public void wakeUp() {
        synchronized(sentenceLock) {
            sentenceLock.notifyAll();
        }
    }
    
    public void run() {
        isStarted = true;
        do {
            try {
                synchronized (sentenceLock) {
                    sentenceLock.wait();
                }
            } catch (InterruptedException e) {
                // ignore?
            } finally {
                try {
                    processSentence(sentence);
                } catch (Throwable e) {
                    e.printStackTrace();
                    midlet.handleException(e, midlet.getMainForm());
                }
            }
        } while (isStarted);
    }

    public void resetOdometer() {
        // reset the odometer
        startTimeMillis = System.currentTimeMillis();
        tripTimeMillis = 0L;
        distance = 0.0;
        maxSpeed = 0.0;
    }
    
    void resetSlopeCalculator() {
        slopeStartTimeMillis = System.currentTimeMillis();
        slopeStartAltitude = altitude;
        groundDistanceFromSlopeStart = 0.0; // reset the counter
    }
    
    void processSentence(String sentence) {

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
            midlet.getCanvas().repaint(); // GPRMC sentence is the key one for display update
        } else if (sentenceHeader.equals("$GPGGA,")) {
            parseGPGGA(convertToArray(strippedSentence));
//        } else if (sentenceHeader.equals("$GPGSA,")) {
//            parseGPGSA(convertToArray(strippedSentence));
//        } else if (sentenceHeader.equals("$GPGSV,")) {
//            parseGPGSV(convertToArray(strippedSentence));
        } else if (sentenceHeader.equals("$GPVTG,")) {
            parseGPVTG(convertToArray(strippedSentence));
        }
    }
    
    /**
     * Our values are separated by commas, convert the whole sentence
     * into an array of separate string values
     * 
     * @param sentence
     * @return
     */
    String[] convertToArray(String sentence) {
        
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
    void parseGPRMC(String[] values) {

        int fixTimeSeconds = Integer.MAX_VALUE;
        
        if (values.length > 0) {
            String originalTimeString = values[0];
            if (originalTimeString.length() > 5) { // HHMMSS must be present
                String hourString = originalTimeString.substring(0, 2);
                String minuteString = originalTimeString.substring(2, 4);
                String secondString = originalTimeString.substring(4, 6);
                gpsTimeString =  hourString + ":" + minuteString + ":" + secondString;
                
                try {
                    fixTimeSeconds = Integer.parseInt(hourString) * 3600
                                   + Integer.parseInt(minuteString) * 60
                                   + Integer.parseInt(secondString);
                            
                } catch (Exception e) {
                    // ignore?
                }
            }
        }

        String activeString = "";
        
        if (values.length > 1) {
            activeString = values[1];
            isValidGPSData = activeString.equalsIgnoreCase("A");
        }

        double latitudeDegrees = 0.0;
        double latitudeMinutes = 0.0;
        
        if (values.length > 3) { // latitude
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
                // ignore wrong values
            }
            
            try {
                latitudeMinutes = Double.valueOf(latitudeMinutesString).doubleValue();
            } catch (Exception e) {
                // ignore wrong values
            }
            
            if (latitudeHemisphere.equalsIgnoreCase("S")) { // southern hemisphere?
                latitudeDegrees = -latitudeDegrees; // invert sign for southern hemisphere;
            }
            
            latitude = 60.0 * latitudeDegrees + latitudeMinutes;
            double fractionalDegrees = latitudeMinutes / 60.0;
            String fractionalDegreesString = "." + (long)(fractionalDegrees * 1000000.0);
            //midlet.getLatitudeStringItem().setLabel(latitudeHemisphere + " "); // 'N' or 'S'
            String latitudeLabel = latitudeHemisphere + " "; // 'N' or 'S'
            midlet.setText(midlet.getLatitudeStringItem(),
                           latitudeLabel + String.valueOf((int)latitudeDegrees) // degrees
                                    + "\u00B0"
                                    + latitudeMinutesString // minutes, fractional
                                    + "\' ("
                                    + fractionalDegreesString
                                    + ")");
///
midlet.getCanvas().setLatitude(latitudeLabel + String.valueOf((int)latitudeDegrees) // degrees
                                    + "\u00B0"
                                    + latitudeMinutesString // minutes, fractional
                                    + "\' ("
                                    + fractionalDegreesString
                                    + ")");
///
        }
        
        double longitudeDegrees = 0.0;
        double longitudeMinutes = 0.0;

        if (values.length > 5) { // longitude
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
                // ignore wrong values
            }
            
            try {
                longitudeMinutes = Double.valueOf(longitudeMinutesString).doubleValue();
            } catch (Exception e) {
                // ignore wrong values
            }
            
            if (longitudeHemisphere.equalsIgnoreCase("W")) { // western hemisphere?
                longitudeDegrees = -longitudeDegrees; // invert sign for western hemisphere;
            }
            
            longitude = 60.0 * longitudeDegrees + longitudeMinutes;
            double fractionalDegrees = longitudeMinutes / 60.0;
            String fractionalDegreesString = "." + (long)(fractionalDegrees * 1000000.0);
            //midlet.getLongitudeStringItem().setLabel(longitudeHemisphere + " "); // 'W' or 'E'
            String longitudeLabel = longitudeHemisphere + " "; // 'W' or 'E'
            midlet.setText(midlet.getLongitudeStringItem(),
                           longitudeLabel + String.valueOf((int)longitudeDegrees) // degrees
                                    + "\u00B0"
                                    + longitudeMinutesString // minutes, fractional
                                    + "\' ("
                                    + fractionalDegreesString
                                    + ")");
///
midlet.getCanvas().setLongitude(longitudeLabel + String.valueOf((int)longitudeDegrees) // degrees
                                    + "\u00B0"
                                    + longitudeMinutesString // minutes, fractional
                                    + "\' ("
                                    + fractionalDegreesString
                                    + ")");
///
        }
        
        if (values.length > 7) { // heading
            headingAngleString = values[7];
            try {
                directionAngle = Double.parseDouble(headingAngleString);
            } catch (Exception e) {
                directionAngle = 0.0;
            }
            if (directionAngle >= 22.5 && directionAngle < 67.5) {
                headingSymbol = "NE"; // north-east
            } else if (directionAngle >= 67.5 && directionAngle < 112.5) {
                headingSymbol = "E"; // east
            } else if (directionAngle >= 112.5 && directionAngle < 157.5) {
                headingSymbol = "SE"; // south-east
            } else if (directionAngle >= 157.5 && directionAngle < 202.5) {
                headingSymbol = "S"; // south
            } else if (directionAngle >= 202.5 && directionAngle < 247.5) {
                headingSymbol = "SW"; // south-west
            } else if (directionAngle >= 247.5 && directionAngle < 292.5) {
                headingSymbol = "W"; // west
            } else if (directionAngle >= 292.5 && directionAngle < 337.5) {
                headingSymbol = "NW"; // north-west
            } else {
                headingSymbol = "N"; // north
            }
        }
        
        if (values.length > 8) { // date
            String originalDateString = values[8];
            if (originalDateString.length() > 5) { // DDMMYY must be present
                gpsDateString = "20"
                       + originalDateString.substring(4, 6) // year
                       + "-"
                       + originalDateString.substring(2, 4) // month
                       + "-"
                       + originalDateString.substring(0, 2); // day
            }
        }

///
midlet.getCanvas().setTime(gpsTimeString);
midlet.getCanvas().setDate(gpsDateString);
///

        midlet.setText(midlet.getDateTimeStringItem(),
                       "UT "+ gpsTimeString + ", " + gpsDateString);
        
        double speedMS = 0.0;
        
        // compute motion data out of our current and previous position:
        if (isValidGPSData && isOldDataSet) {
            double stepDistanceDelta = GPSMath.computeDistance
                    (latitude, longitude, oldLatitude, oldLongitude);
            
            int stepTimeDeltaSeconds = 0;
            if (fixTimeSeconds != Integer.MAX_VALUE) {
                stepTimeDeltaSeconds = Math.abs(fixTimeSeconds - oldFixTimeSeconds);
            }
            
            if (stepTimeDeltaSeconds > 0) { // avoid division by zero
                speedMS = stepDistanceDelta / stepTimeDeltaSeconds;
                speed = speedMS * 3.6;
                if (speed > maxSpeed) {
                    maxSpeed = speed;
                }
            }
            
            // only execute this when moving faster than a certain speed
            if (stepDistanceDelta >= MIN_DISTANCE_DELTA) {
                distance += stepDistanceDelta;
                tripTimeMillis += stepTimeDeltaSeconds * 1000;
            }
        }

        if (speed > 0.0) { // we need speed to be present in order to calculate slopes
            if (slopeStartTimeMillis < 0) {
                resetSlopeCalculator();
            } else { // there is something already, we can start calculation
                long thisTimeMillis = System.currentTimeMillis();
                double timeDeltaSeconds = ((double)thisTimeMillis - (double)slopeStartTimeMillis) / 1000.0;
                double groundDistanceDelta = timeDeltaSeconds * speedMS;
                double altitudeDelta = altitude - slopeStartAltitude;
                if (groundDistanceDelta >= MIN_GROUND_DISTANCE_DELTA) {
                    slopePercent = 100.0 * altitudeDelta / groundDistanceDelta;

//                    double distanceDeltaMeters = Math.sqrt
//                            ((groundDistanceDelta * groundDistanceDelta)
//                            + (altitudeDelta * altitudeDelta));
//                    distance += distanceDeltaMeters;
 
                    resetSlopeCalculator();
                }
            }
        }
        
        String slopeString = String.valueOf(((int)(slopePercent * 10.0)) / 10.0);
        
        String speedString = String.valueOf(((int)(speed * 10)) / 10.0);
        String maxSpeedString = String.valueOf(((int)(maxSpeed * 10)) / 10.0);
//        midlet.getSpeedStringItem().setText("" + speedString + " < " + maxSpeedString + " km/h");
//        String groundSpeedKPHString = String.valueOf(((int)(groundSpeedKPH * 10)) / 10.0);
        String speedMSString = String.valueOf(((int)(speedMS * 10)) / 10.0);
        midlet.setText(midlet.getSpeedStringItem(),
                       "v " + speedString + " km/h ("
                       + speedMSString + " m/s)");
        
///
midlet.getCanvas().setSpeed("v " + speedString + " km/h ("
                       + speedMSString + " m/s)");
///
        String distanceString = String.valueOf(((long)(distance)) / 1000.0);
        midlet.setText(midlet.getOdometerStringItem(),
                       "s " + distanceString + " km");
        
///
midlet.getCanvas().setDistance("s " + distanceString + " km");
///
        // trip time & average speed
        String tripTimeString = convertMillisToString(tripTimeMillis);
        
        if (tripTimeMillis > 0) { // avoid division by zero
            double tripAverageSpeed = distance * 3600 / tripTimeMillis;
            String tripAverageSpeedString = String.valueOf(((int)(tripAverageSpeed * 10)) / 10.0);

            midlet.setText(midlet.getTripTimeAndSpeedStringItem(),
                    "t " + tripTimeString
                    + " (" + tripAverageSpeedString + " km/h)");
///
midlet.getCanvas().setTripTime("t " + tripTimeString
                    + " (" + tripAverageSpeedString + " km/h)");
///
        }
        
        // total time & average speed
        long totalTimeMillis = System.currentTimeMillis() - startTimeMillis;
        String totalTimeString = convertMillisToString(totalTimeMillis);
        
        if (totalTimeMillis > 0) { // avoid division by zero
            double totalAverageSpeed = distance * 3600 / totalTimeMillis;
            String totalAverageSpeedString = String.valueOf(((int)(totalAverageSpeed * 10)) / 10.0);

            midlet.setText(midlet.getTotalTimeAndSpeedStringItem(),
                    "T " + totalTimeString
                    + " (" + totalAverageSpeedString + " km/h)");
///
midlet.getCanvas().setTotalTime("T " + totalTimeString
                    + " (" + totalAverageSpeedString + " km/h)");
///
        }

        if (isValidGPSData) {
            // update the values for next time use
            oldLatitude = latitude;
            oldLongitude = longitude;
            oldFixTimeSeconds = fixTimeSeconds;
            isOldDataSet = true;
        }
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
     */
    void parseGPGGA(String[] values) {
        
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
        
        midlet.getCanvas().setTitle("" + satelliteCount
                  +  " sat. (" + fixQuality + ")");
        
        if (values.length > 11) {
            try {
                altitude = Double.valueOf(values[8]).doubleValue();
            } catch (Exception e) {
                // ignore wrong values
            }
            
            String altitudeUnits = values[9].equals("M")? " m" : " ?";
            
            midlet.setText(midlet.getAltitudeStringItem(),
                    "A " + values[8] + altitudeUnits
                    + ", ^" +  headingAngleString + "\u00B0" + headingSymbol);
///
midlet.getCanvas().setAltitude("A " + values[8] + altitudeUnits);
midlet.getCanvas().setDirection(directionAngle);
midlet.getCanvas().setDirection("^" +  headingAngleString + "\u00B0" + headingSymbol);
///
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
    void parseGPGSA(String[] values) {
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
    void parseGPGSV(String[] values) {
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
    void parseGPVTG(String[] values) {
        if (isValidGPSData) {
            if (values.length > 6) {
                try {
                    groundSpeedKnots = Double.valueOf(values[4]).doubleValue();
                    groundSpeedKPH = Double.valueOf(values[6]).doubleValue();
                } catch (Exception e) {
                }
            }
        
//            if (speed > maxSpeed) {
//                maxSpeed = speed;
//            }
        }
}
    
    String convertSecondsToString(long seconds) {
        return convertMillisToString(seconds * 1000L);
    }
    
    String convertMillisToString(long millis) {
        long hours = millis / 3600000L;
        long remainderMillis = millis % 3600000L;
        long minutes = remainderMillis / 60000L;
        String minutesString = minutes < 10? "0" + minutes : String.valueOf(minutes);
        remainderMillis = remainderMillis % 60000L;
        long seconds = remainderMillis / 1000L;
        String secondsString = seconds < 10? "0" + seconds : String.valueOf(seconds);
        return hours + ":" + minutesString + ":" + secondsString;
    }
}
