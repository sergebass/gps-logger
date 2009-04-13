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

        setFullScreenMode(true); /// customizable?
        
        try {
            // Initialize and set the VideoControl instance associated to this
            // view finder
            videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
            videoControl.setDisplayFullScreen(true); /// customizable?
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

    public void keyPressed(final int keyCode) {
        // process keyboard events in a separate thread
        // to avoid lockups
        new Thread() {
            public void run() {
                if (keyCode == getKeyCode(FIRE)) {
                    saveCurrentPhoto();
                } else if (keyCode == getKeyCode(UP)) {
///implement zoom-in
                } else if (keyCode == getKeyCode(DOWN)) {
///implement zoom-out
                } else if (keyCode == getKeyCode(LEFT)) {
///implement exposure--
                } else if (keyCode == getKeyCode(RIGHT)) {
///implement exposure++
                }
            }
        }.start();
    }

    public void saveCurrentPhoto() {
        ;
    }
}
