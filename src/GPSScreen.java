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

    Font font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
///tmp
Image bgImage = null;
///

    public GPSScreen(GPSLogger midlet) {
        super(false); // do not suppress key events
        this.midlet = midlet;
        this.setFullScreenMode(false);
        init();
    }

    void init() {
        addCommand(new Command("Back", Command.BACK, 1));
        addCommand(new Command("Exit", Command.EXIT, 2));
        setCommandListener(midlet);

        try {
            bgImage = Image.createImage("images/_map.png");
        } catch (IOException e) {
            
        }
        
        repaint();
    }

    public void paint(Graphics g) {
        
    }

    public void setTime(String text) {

        if (!isShown()) {
            return; // do not waste battery energy in vain
        }

        Image image = Image.createImage(80, 30);
        Graphics g2 = image.getGraphics();
        g2.setColor(0x000000); // black background
        g2.fillRect(0, 0, 80, 30);
        g2.setFont(font);
        g2.setColor(0xFFFFFF); // white characters
        g2.drawString(text, 0, 0, 0);

        int[] dataPixels = new int[80 * 30];
        image.getRGB(dataPixels, 0, 80, 0, 0, 80, 30);

        // add alpha to our pixels
        for (int i = 0; i < dataPixels.length; i++) {
            if (dataPixels[i] == 0xFF000000) { // black?
                //dataPixels[i] &= 0xFFFFFF; // remove opaqueness altogether
                //dataPixels[i] |= 0x80000000; // specify the new alpha level
                dataPixels[i] = 0x00000000;
            }
        }

        // this will be our data, but semi-transparent
        image = Image.createRGBImage(dataPixels, 80, 30, true);

///------------- Background: tmp -----

        Graphics g = getGraphics();

        int width = getWidth();
        int height = getHeight();

        g.drawImage(bgImage, 0, 0, 0);

        g.setColor(0xFF8080); // pink
        g.setFont(font);
        g.drawString("size = " + width + "x" + height + ", " + Display.getDisplay(midlet).numAlphaLevels() + " alpha levels",
                0, 120, 0 /* anchor */);

        g.drawImage(image, 0, 0, 0);
        g.drawImage(image, 40, 70, 0);
        g.drawImage(image, 80, 140, 0);

        flushGraphics();
    }
}
