/*
 * (C) Serge Perinsky, 2009
 */

import com.sergebass.geography.GeoLocation;
import com.sergebass.util.Instant;
import java.util.TimeZone;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 *
 * @author Serge Perinsky
 */
public class GPSScreen
        extends GameCanvas {

    GPSLogger midlet = null;

    Font smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    GeoLocation location = null;
    boolean isLocationValid = false;

    String latitudeString = "";
    String longitudeString = "";
    String altitudeString = "";

    String timeString = "";
    String dateString = "";

    String satelliteInfoString = "";
    
    float course = Float.NaN;

    String courseString = "";
    String speedString = "";
    String distanceString = "";
    String tripTimeString = "";
    String totalTimeString = "";

    String messageString = null;

/// tmp:
Image bgImage = null;
///

    public GPSScreen(GPSLogger midlet) {
        super(false); // do not suppress key events
        this.midlet = midlet;
        this.setFullScreenMode(false); /// actually, let user decide between full-screen and not

        addCommand(midlet.getMarkWaypointCommand());
        addCommand(midlet.getStopCommand());
///        addCommand(midlet.getResetCommand());
        setCommandListener(midlet);

/*///tmp:
try {
    bgImage = Image.createImage("/images/_map.png");
} catch (IOException e) {

}
*///
    }

    ///!!! display location/fix method! (location.getLocationMethod())

    public void setLocation(GeoLocation location) {
        this.location = location;

        if (location == null) { // bad data, clear everything

            setLatitude("");
            setLongitude("");
            setAltitude("");
            setCourse("");
            setSpeed("");
            setDate("");
            setTime("");
            setSatelliteInfo("Location data unavailable");

            return;
        }

        double latitude = location.getLatitude();
        setLatitude(GPSLoggerUtils.convertLatitudeToString(latitude));

        double longitude = location.getLongitude();
        setLongitude(GPSLoggerUtils.convertLongitudeToString(longitude));

        float altitude = location.getAltitude();
        setAltitude(GPSLoggerUtils.convertAltitudeToString(altitude));

        this.course = location.getCourse();
        setCourse(GPSLoggerUtils.convertCourseToString(course));

        float speed = location.getSpeed();
        setSpeed(GPSLoggerUtils.convertSpeedToString(speed));

        String date = location.getDateString();
        setDate(date);

        String time = location.getTimeString();
        setTime(time + " UTC/GPS");

        setLocationValid(location.isValid());
        
        int satelliteCount = location.getSatelliteCount();
        if (satelliteCount < 0) {
            setSatelliteInfo("Satellite info unavailable");
        } else {
            setSatelliteInfo(satelliteCount + " satellite(s)");
        }
    }

    public void setLocationValid(boolean isValid) {
        this.isLocationValid = isValid;
    }

    public void setLatitude(String string) {
        latitudeString = string;
    }

    public void displayLatitude() {
        displayString(latitudeString,
                    0, smallFont.getHeight() * 0,
                    smallFont.stringWidth(latitudeString), smallFont.getHeight(),
                    isLocationValid? 0xFF00FF00 : 0xFF008000, 0xA0000000, // green on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setLongitude(String string) {
        longitudeString = string;
    }

    public void displayLongitude() {
        displayString(longitudeString,
                    0, smallFont.getHeight() * 1,
                    smallFont.stringWidth(longitudeString), smallFont.getHeight(),
                    isLocationValid? 0xFF00FF00 : 0xFF008000, 0xA0000000, // green on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setAltitude(String string) {
        altitudeString = string;
    }

    public void displayAltitude() {
        displayString(altitudeString,
                    0, smallFont.getHeight() * 2,
                    smallFont.stringWidth(altitudeString), smallFont.getHeight(),
                    isLocationValid? 0xFF00FF00 : 0xFF008000, 0xA0000000, // green on 60% black
                    smallFont,
                    false,
                    false);
    }

/// use java.util.Timer/TimerTask as a separate clock thread
    
    public void displayLocalTime() {
        Instant now = new Instant(System.currentTimeMillis());
        TimeZone timeZone = TimeZone.getDefault();
        String localTimeString = now.getISO8601TimeId(timeZone)
                                    + " "
                                    + timeZone.getID();
        displayString(localTimeString,
                    0, getHeight() - smallFont.getHeight() * 3,
                    smallFont.stringWidth(localTimeString), smallFont.getHeight(),
                    0xFFFFFFFF, 0xA0000000, // white on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setTime(String string) {
        timeString = string;
    }

    public void displayTime() {
        displayString(timeString,
                    0, getHeight() - smallFont.getHeight() * 2,
                    smallFont.stringWidth(timeString), smallFont.getHeight(),
                    0xFF00FFFF, 0xA0000000, // cyan on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setDate(String string) {
        dateString = string;
    }

    public void displayDate() {
        displayString(dateString,
                    0, getHeight() - smallFont.getHeight() * 1,
                    smallFont.stringWidth(dateString), smallFont.getHeight(),
                    0xFF00FFFF, 0xA0000000, // cyan on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setSatelliteInfo(String string) {
        satelliteInfoString = string;
    }

    public void displaySatelliteInfo() {
        displayString(satelliteInfoString,
                    getWidth() - smallFont.stringWidth(satelliteInfoString),
                    smallFont.getHeight() * 0,
                    smallFont.stringWidth(satelliteInfoString), smallFont.getHeight(),
                    isLocationValid? 0xFFFFB0B0 : 0xFFFF0000, // pinkish (valid) / red (invalid)
                    0xA0000000, // 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setSpeed(String string) {
        speedString = string;
    }

    public void displaySpeed() {
        displayString(speedString,
                    getWidth() - smallFont.stringWidth(speedString),
                    smallFont.getHeight() * 1,
                    smallFont.stringWidth(speedString), smallFont.getHeight(),
                    isLocationValid? 0xFFFFFF00 : 0xFF808000, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setCourse(float course) {
        this.course = course;
    }

    public void setCourse(String string) {
        courseString = string;
    }

    public void displayCourse() {
        displayString(courseString,
                    getWidth() - smallFont.stringWidth(courseString),
                    smallFont.getHeight() * 2,
                    smallFont.stringWidth(courseString), smallFont.getHeight(),
                    isLocationValid? 0xFFFFFF00 : 0xFF808000, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setDistance(String string) {
        distanceString = string;
    }

    public void displayDistance() {
        displayString(distanceString,
                    getWidth() - smallFont.stringWidth(distanceString),
                    getHeight() - smallFont.getHeight() * 3,
                    smallFont.stringWidth(distanceString), smallFont.getHeight(),
                    0xFFFFFF00, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setTripTime(String string) {
        tripTimeString = string;
    }

    public void displayTripTime() {
        displayString(tripTimeString,
                    getWidth() - smallFont.stringWidth(tripTimeString),
                    getHeight() - smallFont.getHeight() * 2,
                    smallFont.stringWidth(tripTimeString), smallFont.getHeight(),
                    0xFFFFFF00, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setTotalTime(String string) {
        totalTimeString = string;
    }

    public void displayTotalTime() {
        displayString(totalTimeString,
                    getWidth() - smallFont.stringWidth(totalTimeString),
                    getHeight() - smallFont.getHeight() * 1,
                    smallFont.stringWidth(totalTimeString), smallFont.getHeight(),
                    0xFFFFFF00, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setMessage(String string) {
        messageString = string;
    }

    public void displayMessage() {

        if (messageString == null) {
            return;
        }
        
        // make the information message centered on the screen
        displayString(messageString,
                    (getWidth() - smallFont.stringWidth(messageString)) / 2,
                    (getHeight() - smallFont.getHeight()) / 2,
                    smallFont.stringWidth(messageString), smallFont.getHeight(),
                    0xFFFF0000, 0xFF000000, // red on 100% opaque black
                    smallFont,
                    false,
                    false);
    }

    void drawBackground(int x, int y, int width, int height,
                        boolean mustFlushGraphics) {

        Graphics g = getGraphics();

/// clear the background regardless of anything?
g.setColor(0x000000); // black
g.fillRect(x, y, width, height); // just fill/clear it...
///

        if (bgImage != null) {

            int imageWidth = bgImage.getWidth();
            int imageHeight = bgImage.getHeight();

            /// how do I scale it over the whole screen?
            g.drawRegion(bgImage,
                    x, y,
                    Math.min(width, imageWidth), // source width
                    Math.min(height, imageHeight), // source image region boundaries
                    Sprite.TRANS_NONE, // transform
                    x, y, // destination area
                    0); // anchor
        } else { // no background image?
/// if the BG was cleared before, no need to do this again (remove if necessary):
            g.setColor(0x000000); // black
            g.fillRect(x, y, width, height); // just fill/clear it...
        }

        // center the compass in the screen
        drawCourseArrow(course, getWidth() / 2, getHeight() / 2, 20);

        if (mustFlushGraphics) {
            flushGraphics(x, y, width, height);
        }
    }

    public void forceRepaint() {
        paint(getGraphics());
    }

    public void paint(Graphics g) {

        drawBackground(0, 0, getWidth(), getHeight(), false);

        displayLatitude();
        displayLongitude();
        displayAltitude();

        displayLocalTime();
        displayTime();
        displayDate();

        displaySatelliteInfo();
        displaySpeed();
        displayCourse();

        displayDistance();
        displayTripTime();
        displayTotalTime();

        displayMessage();

        flushGraphics();
    }

    public void displayString(String string,
                         int x, int y, int width, int height,
                         int fgColor, int bgColor,
                         Font font,
                         boolean mustDrawBackground,
                         boolean mustFlushGraphics) {

        if (string == null) {
            return;
        }

        if (!isShown() || string.equals("")) {
            return; // do not waste battery energy in vain
        }

        // check the value limits

        if (width <= 0 || height <= 0) {
            return;
        }

        if (x < 0) {
            x = 0;
        }
        
        if (y < 0) {
            y = 0;
        }

        if (width > getWidth()) {
            width = getWidth();
        }

        if (height > getHeight()) {
            height = getHeight();
        }

        boolean isFGTransparent = ((fgColor & 0xFF000000) != 0xFF000000);
        boolean isBGTransparent = ((bgColor & 0xFF000000) != 0xFF000000);

        int opaqueFGColor = 0xFF000000 | fgColor; // force setting alpha to 0xFF
        int opaqueBGColor = 0xFF000000 | bgColor; // force setting alpha to 0xFF

        Image image = Image.createImage(width, height);
        Graphics g2 = image.getGraphics();
        g2.setColor(opaqueBGColor); // alpha channel is ignored anyway
        g2.fillRect(0, 0, width, height); // clear/fill image background
        g2.setFont(font);
        g2.setColor(opaqueFGColor); // alpha channel is ignored anyway
        g2.drawString(string, 0, 0, 0);

        int[] dataPixels = new int[width * height];
        image.getRGB(dataPixels, 0, width, 0, 0, width, height);

        // restore alpha to our pixels, if necessary
        // (alpha was ignored in setColor());
        // do not run this loop if there are no transparent pixels
        if (isFGTransparent || isBGTransparent) {
            for (int i = 0; i < dataPixels.length; i++) {
                if (dataPixels[i] == opaqueFGColor) {
                    dataPixels[i] = fgColor;
                } else if (dataPixels[i] == opaqueBGColor) {
                    dataPixels[i] = bgColor;
                }
            }
        }

        // this will be our data, but possibly (semi)transparent
        image = Image.createRGBImage(dataPixels, width, height, true); // with alpha

        if (mustDrawBackground) {
            drawBackground(x, y, width, height, false); // restore background under our data
        }

        Graphics g = getGraphics();
        g.drawImage(image, x, y, 0); // overlay our image with data

        if (mustFlushGraphics) {
            flushGraphics(x, y, width, height);
        }
    }

    void drawCourseArrow(double angle, int centerX, int centerY, int radius) {

        Graphics g = getGraphics();
        g.setColor(0xFFFFFFFF); // white circle
        g.drawArc(centerX - radius,
                  centerY - radius,
                  radius + radius,
                  radius + radius,
                  0, 360); // this is a full circle (0-360 degrees)

        // shift the angle as well, our 0 degrees direction points upwards
        double angleInRadians = (270.0 - angle) * Math.PI / 180.0;
        int arrowLength = radius * 2; // twice as long as the circle radius
        
        // besides, our Y axis is upside down
        int y = centerY - (int)(((double)arrowLength) * Math.sin(angleInRadians));
        int x = centerX + (int)(((double)arrowLength) * Math.cos(angleInRadians));

        int arrowHeadLength = radius / 2; // twice as short as the circle radius
        double arrowHeadAngle1 = angleInRadians + Math.PI / 6.0; // +30 degrees
        double arrowHeadAngle2 = angleInRadians - Math.PI / 6.0; // -30 degrees

        int y1 = centerY - (int)(((double)arrowHeadLength) * Math.sin(arrowHeadAngle1));
        int x1 = centerX + (int)(((double)arrowHeadLength) * Math.cos(arrowHeadAngle1));

        int y2 = centerY - (int)(((double)arrowHeadLength) * Math.sin(arrowHeadAngle2));
        int x2 = centerX + (int)(((double)arrowHeadLength) * Math.cos(arrowHeadAngle2));

        g.setColor(0xFFFFFF00); // yellow arrow
        g.drawLine(centerX, centerY, x, y);
        g.drawLine(centerX, centerY, x1, y1);
        g.drawLine(centerX, centerY, x2, y2);
    }
}
