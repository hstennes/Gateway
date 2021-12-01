package com.logic.ui;

import com.logic.components.*;
import com.logic.input.Camera;
import com.logic.input.Selection;
import org.apache.batik.gvt.GraphicsNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Renderer {

    public final static int CONNECT_RAD = 9;

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
        CachedImage cached = cache.get(lcomp);
        Point p = circuitToScreen(lcomp.getX(), lcomp.getY());

        if(cached == null){
            CachedImage image = renderComponentImage(lcomp);
            cache.add(lcomp, image);
            //g2d.rotate(Math.PI / 2, p.x, p.y + image.y2 - image.y1);
            g2d.drawImage(image, p.x - image.x1, p.y - image.y1, null);
            //g2d.rotate(-Math.PI / 2, p.x, p.y + image.y2 - image.y1);
        }
        else {
            //g2d.rotate(Math.PI / 2, p.x, p.y + cached.y2 - cached.y1);
            g2d.drawImage(cached, p.x - cached.x1, p.y - cached.y1, null);
            //g2d.rotate(-Math.PI / 2, p.x, p.y + cached.y2 - cached.y1);
        }
    }

    private CachedImage renderComponentImage(LComponent lcomp){
        Rectangle lb = lcomp.getBoundsRight();
        Rectangle cb = lcomp.getIO().getConnectionBounds();

        GraphicsNode svg = lcomp.getDrawer().getActiveSVG();
        CachedImage image = new CachedImage((int) (cb.width * zoom), (int) (cb.height * zoom),
                (int) (-cb.x * zoom),
                (int) (-cb.y * zoom),
                (int) ((-cb.x + lb.width) * zoom),
                (int) ((-cb.y + lb.height) * zoom));
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.scale(zoom, zoom);

        drawConnections(g2d, lcomp, -cb.x, -cb.y);

        int size = Math.max(lb.width, lb.height);
        float difference = Math.abs(lb.width - lb.height);
        if(lb.width <= lb.height) svg.setTransform(new AffineTransform(size, 0, 0, size, -cb.x - difference * 0.5, -cb.y));
        else svg.setTransform(new AffineTransform(size, 0, 0, size, -cb.x,  -cb.y - difference * 0.5));
        svg.paint(g2d);

        CompType type = lcomp.getType();
        if(type == CompType.XOR || type == CompType.XNOR) drawExclusive(g2d, -cb.x, -cb.y);
        if(type == CompType.NAND || type == CompType.NOR || type == CompType.XNOR) drawInverted(g2d, -cb.x, -cb.y);

        g2d.setColor(Color.BLUE);
        g2d.scale(invZoom, invZoom);
        g2d.drawRect(0, 0, image.getWidth(), image.getHeight());

        return image;
    }

    /**
     * Draws the connections (with lines) for the component as specified by its IOManager
     * @param g2d The graphics object to use
     * @param lcomp The LComponent
     */
    public void drawConnections(Graphics2D g2d, LComponent lcomp, int dx, int dy){
        IOManager io = lcomp.getIO();
        Point barStart = null, barStop = null;
        for(int i = 0; i < io.getNumInputs(); i++) {
            Point result = drawConnection(g2d, io.connectionAt(i, Connection.INPUT), dx, dy);
            if(i == 0) barStart = result;
            if(i == io.getNumInputs() - 1) barStop = result;
        }
        for(int i = 0; i < io.getNumOutputs(); i++) {
            drawConnection(g2d, io.connectionAt(i, Connection.OUTPUT), dx, dy);
        }

        if(lcomp instanceof BasicGate && io.getNumInputs() > 2) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(barStart.x, barStart.y, barStop.x, barStop.y);
        }
    }

    /**
     * Draws a line with a dot to represent a connection, taking the direction of the connection into account.  Returns a point showing
     * the other end of the line, which is used for drawing another connecting line on a BasicGate when there are many inputs.
     * @param c The connection to render
     * @param g2d The Graphics2D object to use
     * @return The endpoint of the line opposite the connection.
     */
    private Point drawConnection(Graphics2D g2d, Connection c, int dx, int dy){
        Point p = new Point(c.getX(), c.getY());
        p.translate(dx, dy);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        int direction = c.getDirection();
        Point connectEnd = null;
        if(direction == CompRotator.RIGHT) {
            g2d.drawLine(p.x, p.y, p.x - 37, p.y);
            connectEnd = new Point(p.x - 37, p.y);
        }
        if(direction == CompRotator.UP) {
            g2d.drawLine(p.x, p.y, p.x, p.y + 37);
            connectEnd = new Point(p.x, p.y + 37);
        }
        if(direction == CompRotator.LEFT) {
            g2d.drawLine(p.x, p.y, p.x + 37, p.y);
            connectEnd = new Point(p.x + 37, p.y);
        }
        if(direction == CompRotator.DOWN) {
            g2d.drawLine(p.x, p.y, p.x, p.y - 37);
            connectEnd = new Point(p.x, p.y - 37);
        }
        g2d.setColor(Selection.SELECT_COLOR);
        g2d.fillOval(p.x - 9, p.y - 9, 18, 18);
        return connectEnd;
    }

    /**
     * Draws a curved line to show that a gate is an XOR or an XNOR
     * @param g2d The Graphics2D object to use
     */
    public void drawExclusive(Graphics2D g2d, int dx, int dy){
        GeneralPath shape = new GeneralPath();
        shape.moveTo(dx - 8, dy + 3);
        shape.curveTo(dx + 5, dy + 30, dx + 5, dy + 50, dx - 8, dy + 77);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(shape);
    }

    /**
     * Draws a dot to indicate that the gate is a "not" variant
     * @param g2d The Graphics2D object to use
     */
    public void drawInverted(Graphics2D g2d, int dx, int dy){
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.WHITE);
        g2d.fillOval(dx + 75, dy + 33, 14, 14);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(dx + 75, dy + 33, 15, 15);
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
