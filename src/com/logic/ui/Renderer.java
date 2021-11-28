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
    private final ImageCache cache;

    /**
     * The camera position
     */
    private float cx, cy, zoom;

    /**
     * Inverse zoom, used to optimize dividing by zoom
     */
    private float invZoom;

    public Renderer(CircuitPanel cp){
        this.cp = cp;
        cache = new ImageCache();
    }

    public void render(Graphics2D g2d, ArrayList<LComponent> lcomps, ArrayList<Wire> wires, float cx, float cy, float newZoom){
        if(newZoom != zoom) cache.clear();
        this.cx = cx;
        this.cy = cy;
        zoom = newZoom;
        invZoom = 1.0f / zoom;

        Rectangle view = new Rectangle(screenToCircuit(0, 0));
        view.add(screenToCircuit(cp.getWidth(), cp.getHeight()));

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
        Point p = circuitToScreen(lcomp.getX(), lcomp.getY());
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

    private void drawLine(Graphics2D g2d, int x1, int y1, int x2, int y2){
        Point p1 = circuitToScreen(x1, y1);
        Point p2 = circuitToScreen(x2, y2);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void drawCircle(Graphics2D g2d, int x, int y, int r){
        Point p = circuitToScreen(x, y);
        int sr = (int) (r * zoom);
        g2d.drawOval(p.x - sr, p.y - sr, sr * 2, sr * 2);
    }

    private Point circuitToScreen(int x, int y){
        return new Point((int) ((x + cx) * zoom), (int) ((y + cy) * zoom));
    }

    private Point screenToCircuit(int x, int y){
        return new Point((int) (x * invZoom - cx), (int) (y * invZoom - cy));
    }
}
