/*
 * (C) Serge Perinsky, 2009
 */

import java.io.IOException;
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
///tmp:
Image bgImage = null;
///
    double directionAngle = 0.0; // degrees

    String timeString = "";
    String dateString = "";

    String latitudeString = "";
    String longitudeString = "";
    String altitudeString = "";

    String directionString = "";
    String speedString = "";
    String distanceString = "";
    String tripTimeString = "";
    String totalTimeString = "";

    public GPSScreen(GPSLogger midlet) {
        super(false); // do not suppress key events
        this.midlet = midlet;
        this.setFullScreenMode(false);
        init();
    }

    public void setTime(String string) {
        timeString = string;
    }

    public void displayTime() {
        displayString(timeString,
                    0, smallFont.getHeight() * 0,
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
                    0, smallFont.getHeight() * 1,
                    smallFont.stringWidth(dateString), smallFont.getHeight(),
                    0xFF00FFFF, 0xA0000000, // cyan on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setLatitude(String string) {
        latitudeString = string;
    }

    public void displayLatitude() {
        displayString(latitudeString,
                    0, smallFont.getHeight() * 2,
                    smallFont.stringWidth(latitudeString), smallFont.getHeight(),
                    0xFF00FF00, 0xA0000000, // green on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setLongitude(String string) {
        longitudeString = string;
    }

    public void displayLongitude() {
        displayString(longitudeString,
                    0, smallFont.getHeight() * 3,
                    smallFont.stringWidth(longitudeString), smallFont.getHeight(),
                    0xFF00FF00, 0xA0000000, // green on 60% black
                    smallFont,
                    false,
                    false);
    }
    
    public void setAltitude(String string) {
        altitudeString = string;
    }

    public void displayAltitude() {
        displayString(altitudeString,
                    0, smallFont.getHeight() * 4,
                    smallFont.stringWidth(altitudeString), smallFont.getHeight(),
                    0xFF00FF00, 0xA0000000, // green on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setDirection(double directionAngle) {
        this.directionAngle = directionAngle;
    }

    public void setDirection(String string) {
        directionString = string;
    }

    public void displayDirection() {
        displayString(directionString,
                    0, smallFont.getHeight() * 5,
                    smallFont.stringWidth(directionString), smallFont.getHeight(),
                    0xFFFFFF00, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setSpeed(String string) {
        speedString = string;
    }

    public void displaySpeed() {
        displayString(speedString,
                    0, smallFont.getHeight() * 6,
                    smallFont.stringWidth(speedString), smallFont.getHeight(),
                    0xFFFFFF00, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    public void setDistance(String string) {
        distanceString = string;
    }

    public void displayDistance() {
        displayString(distanceString,
                    0, smallFont.getHeight() * 7,
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
                    0, smallFont.getHeight() * 8,
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
                    0, smallFont.getHeight() * 9,
                    smallFont.stringWidth(totalTimeString), smallFont.getHeight(),
                    0xFFFFFF00, 0xA0000000, // yellow on 60% black
                    smallFont,
                    false,
                    false);
    }

    void init() {
        addCommand(new Command("Exit", Command.EXIT, 1));
///     addCommand(new Command("Back", Command.BACK, 2));
        setCommandListener(midlet);

///tmp:
try {
    bgImage = Image.createImage("/images/_map.png");
} catch (IOException e) {

}
///
    }

    void drawBackground(int x, int y, int width, int height,
                        boolean mustFlushGraphics) {

        Graphics g = getGraphics();

        int imageWidth = bgImage.getWidth();
        int imageHeight = bgImage.getHeight();

        if (bgImage != null) {
            /// how do I scale it over the whole screen?
            g.drawRegion(bgImage,
                    x, y,
                    Math.min(width, imageWidth), // source width
                    Math.min(height, imageHeight), // source image region boundaries
                    Sprite.TRANS_NONE, // transform
                    x, y, // destination area
                    0); // anchor
        } else { // no background image?
            g.setColor(0x000000); // black
            g.fillRect(x, y, width, height); // just fill/clear it...
        }

///tmp: FIX ASAP
drawCompass(directionAngle, getWidth() - 22, getHeight() - 22, 20);
///

        if (mustFlushGraphics) {
            flushGraphics(x, y, width, height);
        }
    }

    public void paint(Graphics g) {

        drawBackground(0, 0, getWidth(), getHeight(), false);

        displayTime();
        displayDate();
        displayLatitude();
        displayLongitude();
        displayAltitude();
        displayDirection();
        displaySpeed();
        displayDistance();
        displayTripTime();
        displayTotalTime();

        flushGraphics();
    }

    public void displayString(String string,
                         int x, int y, int width, int height,
                         int fgColor, int bgColor,
                         Font font,
                         boolean mustDrawBackground,
                         boolean mustFlushGraphics) {

        if (!isShown() || string.equals("")) {
            return; // do not waste battery energy in vain
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

    void drawCompass(double angle, int centerX, int centerY, int radius) {

        Graphics g = getGraphics();
        g.setColor(0xFFFFFFFF); ///tmp
        g.drawArc(centerX - radius,
                  centerY - radius,
                  radius + radius,
                  radius + radius,
                  0, 360);

        double angleInRadians = directionAngle / 180.0 * Math.PI;

        int y = centerY + (int)(((double)radius) * Math.sin(angleInRadians));
        int x = centerX + (int)(((double)radius) * Math.cos(angleInRadians));

        g.setColor(0xFFFFFF00);
        g.drawLine(centerX, centerY, x, y);
    }
}
