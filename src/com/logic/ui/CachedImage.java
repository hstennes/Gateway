package com.logic.ui;

import com.logic.components.LComponent;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A BufferedImage with type BITMASK that stores component layout information, to be used for caching rendered components
 */
public class CachedImage extends BufferedImage{

    /**
     * Holds anchor coordinates in the order {x1, y1, x2, y2}. See constructor.
     */
    public int[] anchors;

    private String hashString;

    /**
     * Creates a new CachedImage, which is simply a BufferedImage with type BITMASK that also stores component layout information
     * @param width The width of the image
     * @param height The height of the image
     * @param x1 The x-coordinate of the upper left corner of the component body within the image
     * @param y1 The y-coordinate of the upper left corner
     * @param x2 The x-coordinate of the lower right corner
     * @param y2 The y-coordinate of the lower right corner
     */
    public CachedImage(int width, int height, int x1, int y1, int x2, int y2, String hashString){
        super(width, height, BITMASK);
        anchors = new int[] {x1, y1, x2, y2};
        this.hashString = hashString;
    }

    public String getHashString() {
        return hashString;
    }
}
