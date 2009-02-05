/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.geography;

import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Serge Perinsky
 */
public class KMLWriter
        extends OutputStreamWriter {

    public KMLWriter(OutputStream stream)
            throws UnsupportedEncodingException {
        super(stream, "UTF-8");
    }
}
