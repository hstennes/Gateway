package com.logic.ui;

import com.logic.components.LComponent;
import com.logic.input.Camera;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer {

    /**
     * The collection of cached images
     */
    private ImageCache cache;

    /**
     * The zoom level last time render was called, used to determine if cache should be cleared
     */
    private double zoom;

    public void render(Graphics2D g2d, double x, double y, double newZoom){
        if(newZoom != zoom) cache.clear();
        zoom = newZoom;

        renderWires(g2d);
        renderComponents(g2d);
    }

    private void renderWires(Graphics2D g2d){

    }

    private void renderComponents(Graphics2D g2d){

    }

    private void renderComponent(LComponent lcomp){
        BufferedImage cached = cache.get(lcomp);
        if(cached == null){
            Rectangle b = lcomp.getBoundsRight();
            BufferedImage image = new BufferedImage((int) (b.width * zoom), (int) (b.height * zoom), Transparency.BITMASK);

            //render the component, refactor from CompDrawer

            cache.add(lcomp, image);
        }
    }
}
