/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.util;

import javax.microedition.lcdui.Display;

/**
 * Vibrator controller.
 * 
 * @author Serge Perinsky
 */
public class Vibrator {

    Display display;
    
    public Vibrator(Display display) {
        this.display = display;
    }

    public boolean cancel() {
        return display.vibrate(0); // switch the vibrator off
    }

    /**
     * This call is asynchronous (non-blocking).
     */
    public boolean vibrate(int durationMillis) {
        return display.vibrate(durationMillis);
    }

    public boolean vibrate(final int[] pattern, final int repeatCount) {
        // first, stop the vibration already in progress
        if (!cancel()) {
            return false;
        }

        // then, start the new one in a separate thread (to make it non-blocking)
        new Thread() {
            public void run() {
                for (int i = 0; i < repeatCount; i++) {
                    for (int n = 0; n < pattern.length; n++) {
                        // even index = vibration duration, odd index = pause
                        if (n % 2 == 0) {
                            vibrate(pattern[n]);
                        }
                        // let's wait in either case (vibrate() is asynchronous)
                        try {
                            Thread.sleep(pattern[n]);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
        
        return true;
    }
}
