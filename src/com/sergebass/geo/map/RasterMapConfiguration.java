/*
 * RasterMapConfiguration.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;

/**
 * RasterMapConfiguration - map configuration object.
 * 
 * @author Serge Perinsky
 */
public class RasterMapConfiguration
        extends MapConfiguration {

    public RasterMapConfiguration(InputStream inputStream)
            throws ParserConfigurationException, SAXException, IOException {
        this(inputStream, null);
    }

    public RasterMapConfiguration(InputStream inputStream,
                                     String mapDescriptorFilePath)
            throws ParserConfigurationException, SAXException, IOException {
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        RasterMapConfigurationParser configurationParser = new RasterMapConfigurationParser(this);
        configurationParser.setMapDescriptorFilePath(mapDescriptorFilePath);
        saxParser.parse(inputStream, configurationParser);
    }

///TMP! must be much more sophisticated than this! (multiple zoom levels/tiles)
    RasterMapTile tile = null;

    public void setTile(RasterMapTile tile) {
        this.tile = tile;
    }

    public MapTile getTile() {
        return tile;
    }
}
