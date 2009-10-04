/*
 * RasterMapConfiguration.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import java.io.InputStream;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 * RasterMapConfiguration - map configuration object.
 * 
 * @author Serge Perinsky
 */
public class RasterMapConfiguration
        extends MapConfiguration {

    public RasterMapConfiguration(InputStream is) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            InputSource inputSource = new InputSource(is);
///            saxParser.parse(is,new BasicHandler(this));
        } catch(Exception ex) {
        }
    }
}
