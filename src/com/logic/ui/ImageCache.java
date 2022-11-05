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

    private HashMap<LComponent, CachedImage> updateImages;

    public ImageCache(){
        staticImages = new HashMap<>();
        updateImages = new HashMap<>();
    }

    /**
     * Adds the given image to the cache.
     * @param lcomp The LComponent associated with the image
     * @param image The image
     */
    public void addStaticImage(LComponent lcomp, CachedImage image){
        staticImages.put(getHashString(lcomp), image);
    }

    public void addUpdateImage(LComponent lcomp, CachedImage image){
        updateImages.put(lcomp, image);
    }

    /**
     * Returns a cached image that can be used for the given component, or null if there is no image
     * @param lcomp The LComponent
     * @return The image
     */
    public CachedImage getStaticImage(LComponent lcomp){
        return staticImages.get(getHashString(lcomp));
    }

    public CachedImage getUpdateImage(LComponent lcomp){
        /*four cases:
        There is no image for the component --> return null, renderer must completely draw the image
        There is an image for the component, and it must be modified --> return image, renderer must modify the image
        There is an image for the component, and it is good as is --> return image, renderer must draw the image
        There is an image for the component, but the image must be completely redrawn --> return null, renderer will redraw the image
         */
        return checkUpdateImageValid(lcomp) ? updateImages.get(lcomp) : null;
    }

    /**
     * Clears the cache. Do this whenever the user zooms.
     */
    public void clear(){
        staticImages.clear();
        updateImages.clear();
    }

    /**
     * Converts LComponent to string that corresponds to required image
     * @param lcomp The LComponent
     * @return The string to be used as the HashMap key
     */
    private String getHashString(LComponent lcomp){
        String ext = "";
        if(lcomp instanceof BasicGate) ext = Integer.toString(lcomp.getIO().getNumInputs());
        else if(lcomp instanceof Light) ext = lcomp.getIO().getInput(0) + "b" + ((Light) lcomp).getBitWidth();
        else if(lcomp instanceof Switch) ext = ((Switch) lcomp).getState() + "b" + ((Switch) lcomp).getBitWidth();
        else if(lcomp instanceof Button) ext = Integer.toString(((Button) lcomp).getState());
        else if(lcomp instanceof Clock) ext = ((Clock) lcomp).isOn() ? "1" : "0";
        else if(lcomp instanceof Custom) ext = ((Custom) lcomp).getLabel();
        else if(lcomp instanceof OpCustom2) ext = ((OpCustom2) lcomp).getCustomType().label;
        else if(lcomp instanceof Splitter) ext = arrayString(((Splitter) lcomp).getSplit());
        else if(lcomp instanceof Display) ext = ((Display) lcomp).getValue() + lcomp.getRotation();
        else if(lcomp instanceof UserLabel) ext = lcomp.getName();
        return lcomp.getType().toString() + ext;
    }

    private boolean checkUpdateImageValid(LComponent lcomp){
        if(lcomp.getType() == CompType.SCREEN){
            Screen screen = (Screen) lcomp;
            return !screen.mustFullRedraw();
        }
        return false;
    }

    private String arrayString(int[] arr){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < arr.length - 1; i++) str.append(arr[i]).append(",");
        str.append(arr[arr.length - 1]);
        return str.toString();
    }
}
