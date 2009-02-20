/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

/**
 *
 * @author Serge Perinsky
 */
public class NMEA0183ParserWatchdog
        extends Thread {

    NMEA0183Parser parser = null;
    long timeout = 0L;

    boolean isRunning = false;

    public NMEA0183ParserWatchdog(NMEA0183Parser parser, long timeout) {
        this.parser = parser;
        this.timeout = timeout;
    }

    public void stop() {
        isRunning = false;
    }

    public void run() {
        isRunning = true;

        while (isRunning) {

            long lastSentenceTime = parser.getLastSentenceTime();
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastSentenceTime > timeout) { // is it time to set alarm off?
                parser.stop();
                parser.onParsingComplete(); // notify as if the connection was lost
            }

            if (isRunning) {
                try {
                    Thread.sleep(1000L); // 1 second polling interval
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
