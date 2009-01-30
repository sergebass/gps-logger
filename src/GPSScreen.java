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

    public GPSScreen(GPSLogger midlet) {
        super(false); // do not suppress key events
        this.midlet = midlet;
        this.setFullScreenMode(true);
        init();
    }

    void init() {
        addCommand(new Command("Back", Command.BACK, 1));
        addCommand(new Command("Exit", Command.EXIT, 2));
        setCommandListener(midlet);

///tmp:
try {
    bgImage = Image.createImage("/images/_map.png");
} catch (IOException e) {

}
///
        
        repaint();
    }

    void drawBackground(int x, int y, int width, int height) {

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

        flushGraphics(x, y, width, height);
    }

    public void paint(Graphics g) {
        drawBackground(0, 0, getWidth(), getHeight());
    }

    public void displayText(String text,
                         int x, int y, int width, int height,
                         int fgColor, int bgColor,
                         Font font) {

        if (!isShown()) {
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
        g2.drawString(text, 0, 0, 0);

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

        drawBackground(x, y, width, height); // restore background under our data
        Graphics g = getGraphics();
        g.drawImage(image, x, y, 0); // overlay our image with data

        flushGraphics(x, y, width, height);
    }

    public void displayTime(String text) {
        displayText(text,
                    0, smallFont.getHeight() * 0,
                    smallFont.stringWidth(text), smallFont.getHeight(),
                    0xC0FFFFFF, 0x80000000, // transparent white on 50% black
                    smallFont);
    }

    public void displayDate(String text) {
        displayText(text,
                    0, smallFont.getHeight() * 1,
                    smallFont.stringWidth(text), smallFont.getHeight(),
                    0xFF00FFFF, 0x80000000, // cyan on 50% black
                    smallFont);
    }

    public void displayLatitude(String text) {
        displayText(text,
                    0, smallFont.getHeight() * 2,
                    smallFont.stringWidth(text), smallFont.getHeight(),
                    0xFF00FF00, 0x80000000, // green on 50% black
                    smallFont);
    }

    public void displayLongitude(String text) {
        displayText(text,
                    0, smallFont.getHeight() * 3,
                    smallFont.stringWidth(text), smallFont.getHeight(),
                    0xFFFFFF00, 0x80000000, // yellow on 50% black
                    smallFont);
    }
}
