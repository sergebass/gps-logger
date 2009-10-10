/*
 * RasterMapConfigurationParser.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * RasterMapConfigurationParser.
 *
 * @author Serge Perinsky
 */
public class RasterMapConfigurationParser
        extends DefaultHandler{
    
    RasterMapConfiguration configuration;
    String mapDescriptorFilePath = null;

    public RasterMapConfigurationParser(RasterMapConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setMapDescriptorFilePath(String mapDescriptorFilePath) {
        this.mapDescriptorFilePath = mapDescriptorFilePath;
    }

    public void startElement(java.lang.String uri,
                             java.lang.String localName,
                             java.lang.String qName,
                             Attributes attributes)
                  throws SAXException {
        
        if (qName.equals("tile")) {
            configuration.setTile(new RasterMapTile
                (mapDescriptorFilePath,
                attributes.getValue("file"),
                Double.parseDouble(attributes.getValue("minLatitude")),
                Double.parseDouble(attributes.getValue("maxLatitude")),
                Double.parseDouble(attributes.getValue("minLongitude")),
                Double.parseDouble(attributes.getValue("maxLongitude"))));
        }
    }
}
