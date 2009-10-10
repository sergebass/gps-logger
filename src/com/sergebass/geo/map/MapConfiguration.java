/*
 * MapConfiguration.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 * The base for map configuration classes.
 *
 * @author Serge Perinsky
 */
public abstract class MapConfiguration {

    public static MapConfiguration newInstance(InputStream inputStream)
            throws Exception {
///figure out the map type from stream contents?
        return new RasterMapConfiguration(inputStream);
    }

    public static MapConfiguration newInstance(String fileName)
            throws Exception {
///figure out the map type from file name?
        FileConnection fc = (FileConnection) Connector.open(fileName, Connector.READ);
        return new RasterMapConfiguration(fc.openInputStream(), fileName);
    }

///TMP! must be much more sophisticated than this! (multiple zoom levels/tiles)
    public abstract MapTile getTile();
}
