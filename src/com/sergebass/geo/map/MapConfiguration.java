/*
 * MapConfiguration.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 * The base for map configuration classes.
 *
 * @author Serge Perinsky
 */
public class MapConfiguration {

    public static MapConfiguration newInstance(InputStream inputStream) {
///figure out the map type from stream contents?
        return new RasterMapConfiguration(inputStream);
    }

    public static MapConfiguration newInstance(String fileName)
            throws IOException {
///figure out the map type from file name?
        FileConnection fc = (FileConnection) Connector.open(fileName);
        return new RasterMapConfiguration(fc.openInputStream());
    }
}
