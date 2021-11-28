package com.logic.ui;

import com.logic.components.*;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Stores images to speed up rendering
 */
public class ImageCache {

    /**
     * Maps LComponent strings from getHashString to cached images
     */
    private HashMap<String, BufferedImage> images;

    /**
     * Adds the given image to the cache.
     * @param lcomp The LComponent associated with the image
     * @param image The image
     */
    public void add(LComponent lcomp, BufferedImage image){
        images.put(getHashString(lcomp), image);
    }

    /**
     * Returns a cached image that can be used for the given component, or null if there is no image
     * @param lcomp The LComponent
     * @return The image
     */
    public BufferedImage get(LComponent lcomp){
        return images.get(getHashString(lcomp));
    }

    /**
     * Clears the cache. Do this whenever the user zooms.
     */
    public void clear(){
        images.clear();
    }

    /**
     * Converts LComponent to string that corresponds to required image
     * @param lcomp The LComponent
     * @return The string to be used as the HashMap key
     */
    public String getHashString(LComponent lcomp){
        String ext = "";
        if(lcomp instanceof BasicGate) ext = Integer.toString(lcomp.getIO().getNumInputs());
        else if(lcomp instanceof Light) ext = lcomp.getIO().getInput(0) ? "1" : "0";
        else if(lcomp instanceof IComponent) ext = ((IComponent) lcomp).getState() ? "1" : "0";
        else if(lcomp instanceof Clock) ext = ((Clock) lcomp).isOn() ? "1" : "0";
        return lcomp.getType().toString() + ext;
    }
}
