/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.video;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

/**
 *
 * @author Serge Perinsky
 */
public class CameraCanvas
        extends Canvas {

    private VideoControl videoControl;

    public CameraCanvas(VideoControl theVideoControl) {
        this.videoControl = theVideoControl;

        setFullScreenMode(true);
        
        try {
            // Initialize and set the VideoControl instance associated to this
            // view finder
            videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
            videoControl.setDisplayFullScreen(true);
        } catch (MediaException ex) {
            ex.printStackTrace();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
        videoControl.setVisible(true);
    }

    protected void paint(Graphics g) {
        // just clear background
        g.setColor(0, 0, 0); // pure black
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
