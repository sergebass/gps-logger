/*
 * (C) Serge Perinsky, 2009
 */

/**
 *
 * @author Serge Perinsky
 */
public class GPSLoggerUtils {

    public static String convertLatitudeToString(double latitude) {

/// use settings data here:

///!!! use String.substring() to trim long values!!!

        if (!Double.isNaN(latitude)) {
            // leave 6 digits after decimal point
            latitude = ((long)(latitude * 1000000.0)) / 1000000.0;
            if (latitude >= 0) { // northern hemisphere
                return (GPSLoggerLocalization.getMessage("latitudeN")
                            + " " + latitude + "\u00B0");
            } else { // southern hemisphere
                return (GPSLoggerLocalization.getMessage("latitudeS")
                            + " " + (-latitude) + "\u00B0");
            }
        } else { // NaN
            return ""; // invalid data
        }
    }

    public static String convertLongitudeToString(double longitude) {

/// use settings data here:

///!!! use String.substring() to trim long values!!!

        if (!Double.isNaN(longitude)) {
            // leave 6 digits after decimal point
            longitude = ((long)(longitude * 1000000.0)) / 1000000.0;
            if (longitude >= 0) { // eastern hemisphere
                return (GPSLoggerLocalization.getMessage("longitudeE")
                            + " " + longitude + "\u00B0");
            } else { // western hemisphere
                return (GPSLoggerLocalization.getMessage("longitudeW")
                            + " " + (-longitude) + "\u00B0");
            }
        } else { // NaN
            return ""; // invalid data
        }
    }


    public static String convertAltitudeToString(float altitude) {

/// use settings data here:
        
///!!! use String.substring() to trim long values!!!
        
        if (!Float.isNaN(altitude)) {
            altitude = ((long)(altitude * 10.0f)) / 10.0f; // leave 1 digit after decimal point
            return ("A " + altitude + "m");
        } else { // NaN
            return ""; // invalid data
        }
    }

    public static String convertCourseToString(float course) {

/// use settings data here:

///!!! use String.substring() to trim long values!!!

        if (!Float.isNaN(course)) {
            course = ((long)(course * 10.0f)) / 10.0f; // leave 1 digit after decimal point
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
            return ("^ " + course + "\u00B0 " + directionSymbol);
        } else { // NaN
            return ""; // invalid data
        }
    }

    public static String convertSpeedToString(float speed) {

/// use settings data here:

///!!! use String.substring() to trim long values!!!

        if (!Float.isNaN(speed)) {
            // convert m/s to km/h at the same time
            speed = ((long)(speed * 36.0f)) / 10.0f; // leave 1 digit after decimal point
            return ("v " + speed + " km/h");
        } else { // NaN
            return ""; // invalid data
        }
    }
}
