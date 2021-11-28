package com.logic.ui;

import com.logic.components.LComponent;
import com.logic.components.Wire;
import com.logic.input.Camera;
import org.apache.batik.gvt.GraphicsNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Renderer {

    private CircuitPanel cp;

    /**
     * The collection of cached images
     */
    private ImageCache cache;

    /**
     * The zoom level last time render was called, used to determine if cache should be cleared
     */
    private double zoom;

    /**
     * The camera translation the last time render was called
     */
    private double x, y;

    public Renderer(CircuitPanel cp){
        this.cp = cp;
        cache = new ImageCache();
    }

    public void render(Graphics2D g2d, ArrayList<LComponent> lcomps, ArrayList<Wire> wires, double x, double y, double newZoom){
        if(newZoom != zoom) cache.clear();
        zoom = newZoom;
        this.x = x;
        this.y = y;

        Rectangle view = new Rectangle(screenToCircuit(new Point(0, 0), x, y, zoom));
        view.add(screenToCircuit(new Point(cp.getWidth(), cp.getHeight()), x, y, zoom));

        renderWires(g2d, wires, view);
        renderComponents(g2d, lcomps, view);
    }

    private void renderWires(Graphics2D g2d, ArrayList<Wire> wires, Rectangle view){

    }

    private void renderComponents(Graphics2D g2d, ArrayList<LComponent> lcomps, Rectangle view){
        for(LComponent lcomp : lcomps) {
            if(view.intersects(lcomp.getBounds())) renderComponent(g2d, lcomp);
        }
    }

    private void renderComponent(Graphics2D g2d, LComponent lcomp){
        BufferedImage cached = cache.get(lcomp);
        Point p = circuitToScreen(new Point(lcomp.getX(), lcomp.getY()), x, y, zoom);
        if(cached == null){
            BufferedImage image = renderComponentImage(lcomp);
            cache.add(lcomp, image);
            g2d.drawImage(image, p.x, p.y, null);
        }
        else g2d.drawImage(cached, p.x, p.y, null);
    }

    private BufferedImage renderComponentImage(LComponent lcomp){
        Rectangle b = lcomp.getBoundsRight();
        GraphicsNode svg = lcomp.getDrawer().getActiveSVG();
        int width = (int) (b.width * zoom);
        int height = (int) (b.height * zoom);
        BufferedImage image = new BufferedImage((int) (b.width * zoom), (int) (b.height * zoom), Transparency.BITMASK);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.max(width, height);
        float difference = Math.abs(width - height);
        if(width <= height)
            svg.setTransform(new AffineTransform(size, 0, 0, size, -difference / 2, 0));
        else
            svg.setTransform(new AffineTransform(size, 0, 0, size, 0,  -difference / 2));
        svg.paint(g2d);
        return image;
    }

    private Point circuitToScreen(Point c, double camX, double camY, double zoom){
        return new Point((int) ((c.x + camX) * zoom), (int) ((c.y + camY) * zoom));
    }

    private Point screenToCircuit(Point s, double camX, double camY, double zoom){
        return new Point((int) (s.x / zoom - camX), (int) (s.y / zoom - camY));
    }
}
