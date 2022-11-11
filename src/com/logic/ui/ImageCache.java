package com.logic.ui;

import com.logic.components.*;
import com.logic.custom.OpCustom2;

import java.util.HashMap;

/**
 * Stores images to speed up rendering
 */
public class ImageCache {

    /**
     * Maps LComponent strings from getHashString to cached images
     */
    private HashMap<String, CachedImage> staticImages;

    public ImageCache(){
        staticImages = new HashMap<>();
    }

    /**
     * Adds the given image to the cache.
     * @param image The image
     */
    public void addStaticImage(CachedImage image){
        staticImages.put(image.getHashString(), image);
    }

    /**
     * Returns a cached image that can be used for the given component, or null if there is no image
     * @param lcomp The LComponent
     * @return The image
     */
    public CachedImage getStaticImage(LComponent lcomp, int sensitiveCompData){
        return staticImages.get(getHashString(lcomp, sensitiveCompData));
    }

    /**
     * Clears the cache. Do this whenever the user zooms.
     */
    public void clear(){
        staticImages.clear();
    }

    /**
     * Converts LComponent to string that corresponds to required image
     * @param lcomp The LComponent
     * @return The string to be used as the HashMap key
     */
    public static String getHashString(LComponent lcomp, int sensitiveCompData){
        String ext = "";
        if(lcomp instanceof BasicGate) ext = Integer.toString(lcomp.getIO().getNumInputs());
        else if(lcomp instanceof Light) ext = sensitiveCompData + "b" + ((Light) lcomp).getBitWidth();
        else if(lcomp instanceof Switch) ext = sensitiveCompData + "b" + ((Switch) lcomp).getBitWidth();
        else if(lcomp instanceof Button) ext = Integer.toString(sensitiveCompData);
        else if(lcomp instanceof Clock) ext = Integer.toString(sensitiveCompData);
        else if(lcomp instanceof Custom) ext = ((Custom) lcomp).getLabel();
        else if(lcomp instanceof OpCustom2) ext = ((OpCustom2) lcomp).getCustomType().label;
        else if(lcomp instanceof Splitter) ext = arrayString(((Splitter) lcomp).getSplit());
        else if(lcomp instanceof Display) ext = Integer.toString(sensitiveCompData) + lcomp.getRotation();
        else if(lcomp instanceof UserLabel) ext = lcomp.getName();
        return lcomp.getType().toString() + ext;
    }

    private static String arrayString(int[] arr){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < arr.length - 1; i++) str.append(arr[i]).append(",");
        str.append(arr[arr.length - 1]);
        return str.toString();
    }
}
