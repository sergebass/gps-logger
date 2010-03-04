/*
 * RasterMapTile.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

/**
 *
 * @author Serge Perinsky
 */
public class RasterMapTile
        extends MapTile {

    String mapDescriptorFilePath = null;

    String fileName;
    
    double minLatitude;
    double maxLatitude;
    double minLongitude;
    double maxLongitude;
    
    Image image = null;

    public RasterMapTile(String mapDescriptorFilePath,
                         String fileName,
                         double minLatitude,
                         double maxLatitude,
                         double minLongitude,
                         double maxLongitude) {
        this.mapDescriptorFilePath = mapDescriptorFilePath;
        this.fileName = fileName;
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    public Image getImage() {
        // implement lazy image loading (avoid unnecessary permission dialogs)
        if (image == null) {
            // if the file name is not absolute (no file:// protocol prefix),
            // consider it relative to the map descriptor file:
            String filePath = fileName;
            if ((fileName.indexOf(':') == -1)  // no protocol prefix
                && (mapDescriptorFilePath != null)) {
                    filePath = mapDescriptorFilePath.substring
                            (0, mapDescriptorFilePath.lastIndexOf('/'))
                            + "/" + fileName;
            }

            // load the image in a separate thread
            final String imageFilePath = filePath;
            new Thread() {
                public void run() {
///won't we have weird synchronization problems here?
                    try {
                        image = loadImage(imageFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        image = null;
                    }
                }
            }.start();
        }
        return image;
    }

    private Image loadImage(String filePath) throws IOException {
        System.out.println("Loading map tile from " + filePath + "...");
        FileConnection fc = (FileConnection) Connector.open(filePath, Connector.READ);
        return Image.createImage(fc.openInputStream());
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }

    public double getXPixelsPerDegree(double latitude) {

// (ignore the latitude parameter currently ^^^)
        
        Image mapImage = getImage();

        if (mapImage != null) {
            int imageWidth = mapImage.getWidth();
            // this is our tile image resolutions (in pixels/degree):
            return imageWidth / Math.abs(maxLongitude - minLongitude);
        }

        return 0.0;
    }

    public double getYPixelsPerDegree(double longitude) {

// (ignore the longitude parameter currently ^^^)

        Image mapImage = getImage();

        if (mapImage != null) {
            int imageHeight = mapImage.getHeight();
            // this is our tile image resolutions (in pixels/degree):
            return imageHeight / Math.abs(maxLatitude - minLatitude);
        }

        return 0.0;
    }
}
