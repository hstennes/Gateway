package com.logic.ui;

import com.logic.components.*;

import java.util.HashMap;
import java.util.Set;

/**
 * Stores images to speed up rendering
 */
public class ImageCache {

    /**
     * Maps LComponent strings from getHashString to cached images
     */
    private HashMap<String, CachedImage> images;

    public ImageCache(){
        images = new HashMap<>();
    }

    /**
     * Adds the given image to the cache.
     * @param lcomp The LComponent associated with the image
     * @param image The image
     */
    public void add(LComponent lcomp, CachedImage image){
        images.put(getHashString(lcomp), image);
    }

    /**
     * Returns a cached image that can be used for the given component, or null if there is no image
     * @param lcomp The LComponent
     * @return The image
     */
    public CachedImage get(LComponent lcomp){
        return null;
        //TODO restore once OpCustom is fixed
        //return images.get(getHashString(lcomp));
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
        else if(lcomp instanceof Light) ext = lcomp.getIO().getInput(0) + "b" + ((Light) lcomp).getBitWidth();
        else if(lcomp instanceof Switch) ext = ((Switch) lcomp).getState() + "b" + ((Switch) lcomp).getBitWidth();
        else if(lcomp instanceof Button) ext = Integer.toString(((Button) lcomp).getState());
        else if(lcomp instanceof Clock) ext = ((Clock) lcomp).isOn() ? "1" : "0";
        else if(lcomp instanceof Custom) ext = ((Custom) lcomp).getLabel();
        else if(lcomp instanceof Splitter) ext = arrayString(((Splitter) lcomp).getSplit());
        return lcomp.getType().toString() + ext;
        //TODO add correct caching for Splitter, Display
    }

    private String arrayString(int[] arr){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < arr.length - 1; i++) str.append(arr[i]).append(",");
        str.append(arr[arr.length - 1]);
        return str.toString();
    }
}
