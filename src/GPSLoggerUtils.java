/*
 * (C) Serge Perinsky, 2009
 */

/**
 *
 * @author Serge Perinsky
 */
public class GPSLoggerUtils {

    public static String convertLatitudeToString(double latitude, int coordinatesMode) {

        if (!Double.isNaN(latitude)) {
            String hemisphereCode = (latitude >= 0)?
                  GPSLoggerLocalization.getMessage("latitudeN")  // northern hemisphere
                : GPSLoggerLocalization.getMessage("latitudeS"); // southern hemisphere

            double degrees = Math.abs(latitude); // remove negative sign, if present
            int intDegrees = 0;

            double minutes = 0.0;
            int intMinutes = 0;
            
            double seconds = 0.0;

            int dotPosition = 0;
            
            switch (coordinatesMode) {
                case GPSLoggerSettings.COORDINATES_MODE_D:
                    String degreesString = String.valueOf(degrees);
                    dotPosition = degreesString.indexOf('.');
                    if (dotPosition >= 0) {
                        int dataStringLength = degreesString.length();
                        // leave at most 5 digits after decimal point
                        if (dataStringLength - dotPosition > 6) {
                            degreesString = degreesString.substring(0, dotPosition + 6);
                        }
                    }
                    return degreesString + "\u00B0 " + hemisphereCode;
                case GPSLoggerSettings.COORDINATES_MODE_DM:
                    intDegrees = (int)degrees;
                    minutes = (degrees - (double)intDegrees) * 60.0;
                    String minutesString = String.valueOf(minutes);
                    dotPosition = minutesString.indexOf('.');
                    if (dotPosition >= 0) {
                        int dataStringLength = minutesString.length();
                        // leave at most 3 digits after decimal point
                        if (dataStringLength - dotPosition > 4) {
                            minutesString = minutesString.substring(0, dotPosition + 4);
                        }
                    }
                    return intDegrees + "\u00B0" + minutesString + "\' " + hemisphereCode;
                case GPSLoggerSettings.COORDINATES_MODE_DMS:
                    intDegrees = (int)degrees;
                    minutes = (degrees - (double)intDegrees) * 60.0;
                    intMinutes = (int)minutes;
                    seconds = (minutes - (double)intMinutes) * 60.0;
                    String secondsString = String.valueOf(seconds);
                    dotPosition = secondsString.indexOf('.');
                    if (dotPosition >= 0) {
                        int dataStringLength = secondsString.length();
                        // leave at most 2 digits after decimal point
                        if (dataStringLength - dotPosition > 3) {
                            secondsString = secondsString.substring(0, dotPosition + 3);
                        }
                    }
                    return intDegrees + "\u00B0" + intMinutes + "\'" + secondsString + "\" " + hemisphereCode;
                default: // uknown mode
                    return "";
            }
        } else { // NaN
            return ""; // invalid data
        }
    }

    public static String convertLongitudeToString(double longitude, int coordinatesMode) {

        if (!Double.isNaN(longitude)) {
            String hemisphereCode = (longitude >= 0)?
                  GPSLoggerLocalization.getMessage("longitudeE")  // eastern hemisphere
                : GPSLoggerLocalization.getMessage("longitudeW"); // western hemisphere

            double degrees = Math.abs(longitude); // remove negative sign, if present
            int intDegrees = 0;

            double minutes = 0.0;
            int intMinutes = 0;

            double seconds = 0.0;

            int dotPosition = 0;

            switch (coordinatesMode) {
                case GPSLoggerSettings.COORDINATES_MODE_D:
                    String degreesString = String.valueOf(degrees);
                    dotPosition = degreesString.indexOf('.');
                    if (dotPosition >= 0) {
                        int dataStringLength = degreesString.length();
                        // leave at most 5 digits after decimal point
                        if (dataStringLength - dotPosition > 6) {
                            degreesString = degreesString.substring(0, dotPosition + 6);
                        }
                    }
                    return degreesString + "\u00B0 " + hemisphereCode;
                case GPSLoggerSettings.COORDINATES_MODE_DM:
                    intDegrees = (int)degrees;
                    minutes = (degrees - (double)intDegrees) * 60.0;
                    String minutesString = String.valueOf(minutes);
                    dotPosition = minutesString.indexOf('.');
                    if (dotPosition >= 0) {
                        int dataStringLength = minutesString.length();
                        // leave at most 3 digits after decimal point
                        if (dataStringLength - dotPosition > 4) {
                            minutesString = minutesString.substring(0, dotPosition + 4);
                        }
                    }
                    return intDegrees + "\u00B0" + minutesString + "\' " + hemisphereCode;
                case GPSLoggerSettings.COORDINATES_MODE_DMS:
                    intDegrees = (int)degrees;
                    minutes = (degrees - (double)intDegrees) * 60.0;
                    intMinutes = (int)minutes;
                    seconds = (minutes - (double)intMinutes) * 60.0;
                    String secondsString = String.valueOf(seconds);
                    dotPosition = secondsString.indexOf('.');
                    if (dotPosition >= 0) {
                        int dataStringLength = secondsString.length();
                        // leave at most 2 digits after decimal point
                        if (dataStringLength - dotPosition > 3) {
                            secondsString = secondsString.substring(0, dotPosition + 3);
                        }
                    }
                    return intDegrees + "\u00B0" + intMinutes + "\'" + secondsString + "\" " + hemisphereCode;
                default: // uknown mode
                    return "";
            }
        } else { // NaN
            return ""; // invalid data
        }
    }


    public static String convertAltitudeToString(float altitude, int altitudeUnits) {

        if (!Float.isNaN(altitude)) {
            switch (altitudeUnits) {
                case GPSLoggerSettings.ALTITUDE_UNITS_METERS:
                    return ((int)altitude + " m^");
                case GPSLoggerSettings.ALTITUDE_UNITS_FEET:
                    return ((int)(altitude / 0.3048)+ " ft^");
                default: // uknown mode
                    return "";
            }
        } else { // NaN
            return ""; // invalid data
        }
    }

    public static String convertCourseToString(float course) {

/// localize course symbols
        
        if (!Float.isNaN(course)) {
            String directionSymbol = "";
            if (course >= 22.5 && course < 67.5) {
                directionSymbol = "NE"; // north-east
            } else if (course >= 67.5 && course < 112.5) {
                directionSymbol = "E"; // east
            } else if (course >= 112.5 && course < 157.5) {
                directionSymbol = "SE"; // south-east
            } else if (course >= 157.5 && course < 202.5) {
                directionSymbol = "S"; // south
            } else if (course >= 202.5 && course < 247.5) {
                directionSymbol = "SW"; // south-west
            } else if (course >= 247.5 && course < 292.5) {
                directionSymbol = "W"; // west
            } else if (course >= 292.5 && course < 337.5) {
                directionSymbol = "NW"; // north-west
            } else {
                directionSymbol = "N"; // north
            }
            return (">> " + (int)course + "\u00B0 " + directionSymbol);
        } else { // NaN
            return ""; // invalid data
        }
    }

    public static String convertSpeedToString(float speed, int speedUnits) {

///!!! use String.substring() to trim long values!!!

        if (!Float.isNaN(speed)) {
            switch (speedUnits) {
                case GPSLoggerSettings.SPEED_UNITS_KPH: // kilometers per hour
                    // convert m/s to km/h
                    return "" + (int)(speed * 3.6f) + " km/h";
                case GPSLoggerSettings.SPEED_UNITS_MPH: // miles per hour
                    // convert m/s to mph
                    return "" + (int)(speed * 2.236936f) + " mph";
                case GPSLoggerSettings.SPEED_UNITS_KNOTS: // knots, sea miles per hour
                    // convert m/s to knots
                    return "" + (int)(speed * 1.943844f) + " kn";
                case GPSLoggerSettings.SPEED_UNITS_MPS: // meters per second
                    // leave m/s as is
                    return "" + speed + " m/sec";
                default: // invalid mode
                    return "";
            }
        } else { // NaN
            return ""; // invalid data
        }
    }
}
