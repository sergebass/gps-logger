/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geo;

import java.io.IOException;

/**
 *
 * @author Serge Perinsky
 */
public interface GeoLocator {
    
    static final int STATE_AVAILABLE = 0;
    static final int STATE_OUT_OF_SERVICE = 1;
    static final int STATE_TEMPORARILY_UNAVAILABLE = 2;

    public GeoLocation getLocation();
    public void setLocationListener(GeoLocationListener listener);
    public void close() throws IOException;
}
