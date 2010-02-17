/*
 * (C) Serge Perinsky, 2009
 */

import com.sergebass.geo.GeoLocation;
import com.sergebass.geo.map.*;
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

    MapRenderer mapRenderer = null;

    boolean isFullScreenMode = false;
    boolean isDataDisplayMode = true;

    public GPSScreen(GPSLogger midlet) {
        super(false); // do not suppress key events
        this.midlet = midlet;
        this.setFullScreenMode(false); // initial setting, make configurable by user?

        addCommand(midlet.getMarkWaypointCommand());
        addCommand(midlet.getStopCommand());
        addCommand(midlet.getSettingsCommand());
        addCommand(midlet.getMinimizeCommand());
        setCommandListener(midlet);

        mapRenderer = MapRenderer.newInstance(midlet.getMapConfiguration());
    }

///!!! display location fix method! (location.getLocationMethod())

    public void setLocation(GeoLocation location) {
        this.location = location;

        if (location == null) { // invalid data, clear everything

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

        if (mapRenderer != null) {
            mapRenderer.setLocation(location);
        }

        double latitude = location.getLatitude();
        setLatitude(GPSLoggerUtils.convertLatitudeToString(latitude, midlet.getCoordinatesMode()));

        double longitude = location.getLongitude();
        setLongitude(GPSLoggerUtils.convertLongitudeToString(longitude, midlet.getCoordinatesMode()));

        float altitude = location.getAltitude();
        setAltitude(GPSLoggerUtils.convertAltitudeToString(altitude, midlet.getAltitudeUnits()));

        this.course = location.getCourse();
        setCourse(GPSLoggerUtils.convertCourseToString(course));

        float speed = location.getSpeed();
        setSpeed(GPSLoggerUtils.convertSpeedToString(speed, midlet.getSpeedUnits()));

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

    public void displayLatitude(Graphics g) {
        displayString(latitudeString,
                    g, 0, smallFont.getHeight() * 0,
                    smallFont.stringWidth(latitudeString), smallFont.getHeight(),
                    isLocationValid? 0xFFFFFF00 : 0xFFC0C000, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setLongitude(String string) {
        longitudeString = string;
    }

    public void displayLongitude(Graphics g) {
        displayString(longitudeString,
                    g, 0, smallFont.getHeight() * 1,
                    smallFont.stringWidth(longitudeString), smallFont.getHeight(),
                    isLocationValid? 0xFFFFFF00 : 0xFFC0C000, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setAltitude(String string) {
        altitudeString = string;
    }

    public void displayAltitude(Graphics g) {
        displayString(altitudeString,
                    g, 0, smallFont.getHeight() * 2,
                    smallFont.stringWidth(altitudeString), smallFont.getHeight(),
                    isLocationValid? 0xFFFFFF00 : 0xFFC0C000, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

/// use java.util.Timer/TimerTask as a separate clock thread
    
    public void displayLocalTime(Graphics g) {
        Instant now = new Instant(System.currentTimeMillis());
        TimeZone timeZone = TimeZone.getDefault();
        String localTimeString = now.getISO8601TimeId(timeZone)
                                    + " "
                                    + timeZone.getID();
        displayString(localTimeString,
                    g, 0, getHeight() - smallFont.getHeight() * 1,
                    smallFont.stringWidth(localTimeString), smallFont.getHeight(),
                    0xFFFFFFFF, 0xA0000000, // white on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setTime(String string) {
        timeString = string;
    }

    public void displayTime(Graphics g) {
        displayString(timeString,
                    g, 0, getHeight() - smallFont.getHeight() * 2,
                    smallFont.stringWidth(timeString), smallFont.getHeight(),
                    0xFF00FFFF, 0xA0000000, // cyan on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setDate(String string) {
        dateString = string;
    }

    public void displayDate(Graphics g) {
        displayString(dateString,
                    g, 0, getHeight() - smallFont.getHeight() * 3,
                    smallFont.stringWidth(dateString), smallFont.getHeight(),
                    0xFF00FFFF, 0xA0000000, // cyan on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setSatelliteInfo(String string) {
        satelliteInfoString = string;
    }

    public void displaySatelliteInfo(Graphics g) {
        displayString(satelliteInfoString,
                    g, getWidth() - smallFont.stringWidth(satelliteInfoString),
                    smallFont.getHeight() * 0,
                    smallFont.stringWidth(satelliteInfoString), smallFont.getHeight(),
                    isLocationValid? 0xFFFFB0B0 : 0xFFFF0000, // pinkish (valid) / red (invalid)
                    0xA0000000, // 60% black
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

    public void displayCourse(Graphics g) {
        displayString(courseString,
                    g, getWidth() - smallFont.stringWidth(courseString),
                    smallFont.getHeight() * 1,
                    smallFont.stringWidth(courseString), smallFont.getHeight(),
                    isLocationValid? 0xFF00FF00 : 0xFF00C000, 0xA0000000, // green on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setSpeed(String string) {
        speedString = string;
    }

    public void displaySpeed(Graphics g) {
        displayString(speedString,
                    g, getWidth() - smallFont.stringWidth(speedString),
                    smallFont.getHeight() * 2,
                    smallFont.stringWidth(speedString), smallFont.getHeight(),
                    isLocationValid? 0xFF00FF00 : 0xFF00C000, 0xA0000000, // green on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setDistance(String string) {
        distanceString = string;
    }

    public void displayDistance(Graphics g) {
        displayString(distanceString,
                    g, getWidth() - smallFont.stringWidth(distanceString),
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

    public void displayTripTime(Graphics g) {
        displayString(tripTimeString,
                    g, getWidth() - smallFont.stringWidth(tripTimeString),
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

    public void displayTotalTime(Graphics g) {
        displayString(totalTimeString,
                    g, getWidth() - smallFont.stringWidth(totalTimeString),
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

    public void displayMessage(Graphics g) {

        if (messageString == null) {
            return;
        }
        
        // make the information message centered on the screen
        displayString(messageString,
                    g, (getWidth() - smallFont.stringWidth(messageString)) / 2,
                    (getHeight() - smallFont.getHeight()) / 2,
                    smallFont.stringWidth(messageString), smallFont.getHeight(),
                    0xFFFF0000, 0xC0000000, // red on 90% opaque black
                    smallFont,
                    false,
                    false);
    }

    void drawBackground(Graphics g,
                          int clipX, int clipY, int clipWidth, int clipHeight,
                          boolean mustFlushGraphics) {

        boolean isMapRendered = false;

        if (mapRenderer != null) {
            isMapRendered = mapRenderer.render(g,
                    clipX, clipY, clipWidth, clipHeight,
                    getWidth() / 2, getHeight() / 2); // our marker is in the center
        }

        if (!isMapRendered) { // no map rendering performed
            g.setColor(0x000000); // just a black background
            g.fillRect(clipX, clipY, clipWidth, clipHeight); // just fill/clear it...
        }

        // center the compass in the screen
        if (course != Float.NaN) {
            drawTargetCursor(course, g, getWidth() / 2, getHeight() / 2, 20);
            drawCourseArrow(course, g, getWidth() / 2, getHeight() / 2, 20);
        }

        if (mustFlushGraphics) {
            flushGraphics(clipX, clipY, clipWidth, clipHeight);
        }
    }

    public void paint(Graphics g) {

        // check clipping region
        ///g.getClipX();
        ///g.getClipY();
        ///g.getClipWidth();
        ///g.getClipHeight();

///? if the BG was cleared before, no need to do this again (remove if necessary):
        drawBackground(g, 0, 0, getWidth(), getHeight(), false);

        if (isDataDisplayMode) {
            displayLatitude(g);
            displayLongitude(g);
            displayAltitude(g);

            displayDate(g);
            displayTime(g);
            displayLocalTime(g);

            displaySatelliteInfo(g);
            displayCourse(g);
            displaySpeed(g);

            displayDistance(g);
            displayTripTime(g);
        }

        // let's display total time anyway, to keep indication of progress for the user
        displayTotalTime(g);

        displayMessage(g);
    }

    protected void sizeChanged(int newWidth, int newHeight) {
        super.sizeChanged(newWidth, newHeight);
        ///do something else on canvas size change???
    }

    public void displayString(String string,
                               Graphics g,
                               int x, int y, int width, int height,
                               int fgColor, int bgColor,
                               Font font,
                               boolean mustDrawBackground,
                               boolean mustFlushGraphics) {

        if (!isShown() || string == null || string.equals("")) {
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

        if (mapRenderer != null) { // drawing over map
            Image image = Image.createImage(width, height);
            Graphics g2 = image.getGraphics(); // off-screen drawing context
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
                drawBackground(g, x, y, width, height, false); // restore background under our data
            }

            g.drawImage(image, x, y, 0); // overlay our image with data

            if (mustFlushGraphics) {
                flushGraphics(x, y, width, height);
            }
        } else { // non-overlay mode
            g.setColor(opaqueBGColor); // alpha channel is ignored anyway
            g.fillRect(x, y, width, height); // clear/fill image background
            g.setFont(font);
            g.setColor(opaqueFGColor); // alpha channel is ignored anyway
            g.drawString(string, x, y, 0);
        }
    }

    void drawTargetCursor(double angle, Graphics g, int centerX, int centerY, int radius) {

        g.setColor(0xFF00FF00); // green circle
        g.drawArc(centerX - radius,
                  centerY - radius,
                  radius + radius,
                  radius + radius,
                  0, 360); // this is a full circle (0-360 degrees)
    }

    void drawCourseArrow(double angle, Graphics g, int centerX, int centerY, int radius) {

        // shift the angle as well, our 0 degrees direction points upwards
        double angleInRadians = (270.0 - angle) * Math.PI / 180.0;
        int arrowLength = radius * 2 / 3;

        double arrowHeadAngle1 = angleInRadians + Math.PI / 8.0; // +22.5 degrees
        double arrowHeadAngle2 = angleInRadians - Math.PI / 8.0; // -22.5 degrees
        int arrowSideLength = radius * 10 / 11;
        
        if (mapRenderer != null) { // drawing over map
            // our Y axis is upside down
            int y = centerY - (int)(((double)arrowLength) * Math.sin(angleInRadians));
            int x = centerX + (int)(((double)arrowLength) * Math.cos(angleInRadians));

            int y1 = centerY - (int)(((double)arrowSideLength) * Math.sin(arrowHeadAngle1));
            int x1 = centerX + (int)(((double)arrowSideLength) * Math.cos(arrowHeadAngle1));

            int y2 = centerY - (int)(((double)arrowSideLength) * Math.sin(arrowHeadAngle2));
            int x2 = centerX + (int)(((double)arrowSideLength) * Math.cos(arrowHeadAngle2));

            g.setColor(0xFFFF0000); // red arrow part
            g.fillTriangle(centerX, centerY, x, y, x1, y1);
            g.setColor(0xC0C00000); // dark red arrow part
            g.fillTriangle(centerX, centerY, x, y, x2, y2);

        } else { // non-overlay mode
            // our Y axis is upside down
            int y = centerY + (int)(((double)arrowLength / 2) * Math.sin(angleInRadians));
            int x = centerX - (int)(((double)arrowLength / 2) * Math.cos(angleInRadians));

            int y1 = centerY + (int)(((double)arrowSideLength) * Math.sin(arrowHeadAngle1));
            int x1 = centerX - (int)(((double)arrowSideLength) * Math.cos(arrowHeadAngle1));

            int y2 = centerY + (int)(((double)arrowSideLength) * Math.sin(arrowHeadAngle2));
            int x2 = centerX - (int)(((double)arrowSideLength) * Math.cos(arrowHeadAngle2));

            g.setColor(0xFFFF0000); // red arrow part
            g.fillTriangle(centerX, centerY, x, y, x1, y1);
            g.setColor(0xC0C00000); // dark red arrow part
            g.fillTriangle(centerX, centerY, x, y, x2, y2);

/// localize course symbols
            Font font = smallFont;
            g.setFont(font);
            g.setColor(0xFF00FFFF); // cyan course symbols
            int fontHeight = font.getHeight();
            
            g.drawString("N", centerX - font.stringWidth("N") / 2, centerY - radius - fontHeight, 0);
            g.drawString("S", centerX - font.stringWidth("S") / 2, centerY + radius, 0);
            g.drawString("W", centerX - radius - font.stringWidth("W"), centerY - fontHeight / 2, 0);
            g.drawString("E", centerX + radius + font.stringWidth("E"), centerY - fontHeight / 2, 0);
        }
    }

    void toggleFullScreenMode() {
        setFullScreenMode(!isFullScreenMode);
        isFullScreenMode = !isFullScreenMode;
    }

    void toggleDataDisplayMode() {
        isDataDisplayMode = !isDataDisplayMode;
        repaint();
    }

    public void keyPressed(int keyCode) {

///add the list of supported keys to a help item!
        
        if (getKeyCode(FIRE) == keyCode) {
            midlet.markWaypoint();
        } else if (getKeyCode(UP) == keyCode) {
            ///
        } else if (getKeyCode(DOWN) == keyCode) {
            ///
        } else if (getKeyCode(LEFT) == keyCode) {
            ///
        } else if (getKeyCode(RIGHT) == keyCode) {
            ///
        } else if (KEY_STAR == keyCode) {
            /// zoom-in
        } else if (KEY_POUND == keyCode) {
            /// zoom-out
        } else if (KEY_NUM0 == keyCode) {
            /// where am I? (set cursor to GPS fix position)
        } else if (KEY_NUM1 == keyCode) {
            toggleFullScreenMode();
        } else if (KEY_NUM2 == keyCode) {
            toggleDataDisplayMode();
        } else if (KEY_NUM3 == keyCode) {
            /// toggle grid/scale indicator
        }
    }
}
