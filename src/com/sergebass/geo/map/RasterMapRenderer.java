/*
 * RasterMapRenderer.java (C) Serge Perinsky, 2009
 */

package com.sergebass.geo.map;

import com.sergebass.geo.GeoLocation;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * RasterMapRenderer.
 * 
 * @author Serge Perinsky
 */
public class RasterMapRenderer
        extends MapRenderer {

    RasterMapConfiguration configuration = null;

    public RasterMapRenderer(RasterMapConfiguration configuration) {

        this.configuration = configuration;
    }

    public boolean render(Graphics g,
            int clipX, int clipY, int clipWidth, int clipHeight,
            int markerX, int markerY) {

///this is actually going to be more complicated (to do yet)
        MapTile tile = configuration.getTile();

/// DO NOT CACHE MAP IMAGES!!!
/// 1) they may be loaded later, in a separate thread
/// 2) save memory by releasing unused Images
        
        Image mapImage = tile.getImage();
        GeoLocation location = getLocation();

        if (mapImage != null && location != null) {

            int imageWidth = mapImage.getWidth();
            int imageHeight = mapImage.getHeight();

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            double minLatitude = tile.getMinLatitude();
            double maxLatitude = tile.getMaxLatitude();
            double minLongitude = tile.getMinLongitude();
            double maxLongitude = tile.getMaxLongitude();

            // this is our tile image resolutions (in pixels/degree):
            double xPixelsPerDegree = imageWidth / Math.abs(maxLongitude - minLongitude);
            double yPixelsPerDegree = imageHeight / Math.abs(maxLatitude - minLatitude);

            // figure our pixel coordinates on the tile image:
            int xLocationOnTile = (int)(xPixelsPerDegree * (longitude - minLongitude));
            // our Y-axis is upside down (lower values are above)
            int yLocationOnTile = imageHeight - (int)(yPixelsPerDegree * (latitude - minLatitude));

///
System.out.println("xPixelsPerDegree=" + xPixelsPerDegree);
System.out.println("yPixelsPerDegree=" + yPixelsPerDegree);
System.out.println("Location on tile: x=" + xLocationOnTile + ", y=" + yLocationOnTile);
///
            int mapSectionX = xLocationOnTile - markerX;
            int mapSectionY = yLocationOnTile - markerY;

            int destinationX = clipX;
            int destinationY = clipY;

            if (mapSectionX < 0) {
                destinationX = clipX - mapSectionX;
                mapSectionX = 0;
            }

            if (mapSectionY < 0) {
                destinationY = clipY - mapSectionY;
                mapSectionY = 0;
            }

            int mapSectionWidth = Math.min(clipWidth, Math.abs(imageWidth - mapSectionX));
            int mapSectionHeight = Math.min(clipHeight, Math.abs(imageHeight - mapSectionY));

/*///
System.out.println("mapSectionX=" + mapSectionX);
System.out.println("mapSectionY=" + mapSectionY);
System.out.println("mapSectionW=" + mapSectionWidth);
System.out.println("mapSectionH=" + mapSectionHeight);
System.out.println("clipX=" + clipX);
System.out.println("clipY=" + clipY);
System.out.println("clipW=" + clipWidth);
System.out.println("clipH=" + clipHeight);
System.out.println("destinationX=" + destinationX);
System.out.println("destinationY=" + destinationY);
*///

///tmp!!! fill the map area with blue color:
            g.setColor(0x808080); // just a grey background
            g.fillRect(clipX, clipY, clipWidth, clipHeight); // just fill/clear it...
///^^
            // make sure our image is inside the screen:
            if (destinationX < clipX + clipWidth && destinationY < clipY + clipHeight &&
                mapSectionX < imageWidth && mapSectionY < imageHeight) {
                g.drawRegion(mapImage,
                        mapSectionX, mapSectionY, mapSectionWidth, mapSectionHeight,
                        Sprite.TRANS_NONE, // transform
                        destinationX, destinationY, // destination area
                        0); // anchor
            }
            return true; // consider the map as rendered in any case
        } else { // no map?
            return false; // there was no map to render
        }
    }
}
