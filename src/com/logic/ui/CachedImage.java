package com.logic.ui;

import java.awt.image.BufferedImage;

public class CachedImage extends BufferedImage{

    /**
     * The position of the top-left corner of the component within the image. The image should be drawn so that this
     * point is at the correct location of the component in circuit space
     */
    public int cx, cy;

    /**
     * Creates a new CachedImage, which is simply a BufferedImage with type BITMASK that also stores integers cx and cy
     * @param width The width of the image
     * @param height The height of the image
     * @param cx The cx value
     * @param cy The cy value
     */
    public CachedImage(int width, int height, int cx, int cy){
        super(width, height, BITMASK);
        this.cx = cx;
        this.cy = cy;
    }
}
