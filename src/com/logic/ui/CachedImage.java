package com.logic.ui;

import java.awt.image.BufferedImage;

public class CachedImage extends BufferedImage{

    /**
     * The positions of the top left and bottom right corners of the component body within the image.
     */
    public int x1, y1, x2, y2;

    /**
     * Creates a new CachedImage, which is simply a BufferedImage with type BITMASK that also stores component layout information
     * @param width The width of the image
     * @param height The height of the image
     */
    public CachedImage(int width, int height, int x1, int y1, int x2, int y2){
        super(width, height, BITMASK);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}
