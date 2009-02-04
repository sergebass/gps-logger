/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

import java.io.IOException;

/**
 *
 * @author Serge Perinsky
 */
public interface GeoLocator {
    public GeoLocation getLocation();
    public void setLocationListener(GeoLocationListener listener);
    public void close() throws IOException;
}
