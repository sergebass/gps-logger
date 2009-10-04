/*
 * RasterMapRenderer.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * RasterMapRenderer.
 * 
 * @author Serge Perinsky
 */
public class RasterMapRenderer
        extends MapRenderer {

    RasterMapConfiguration configuration = null;

/// tmp:
Image bgImage = null;
///

    public RasterMapRenderer(RasterMapConfiguration configuration) {

        this.configuration = configuration;

///tmp:
        try {
            bgImage = Image.createImage("/images/_map.png");
        } catch (IOException e) {

        }
///^
    }

/// perform image loading in a separate thread!

    public boolean render(Graphics g, int x, int y, int width, int height) {
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
            return true;
        } else { // no background image?
            return false;
        }
    }
}
