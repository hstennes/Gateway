package com.logic.ui;

import com.logic.components.*;
import com.logic.input.Selection;
import com.logic.util.Debug;
import org.apache.batik.gvt.GraphicsNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

public class Renderer {

    public final static int CONNECT_RAD = 9;

    public final static int[][] rotationAnchorTranslate = new int[][]{
            new int[] {0, 1},
            new int[]{0, 3},
            new int[] {2, 3},
            new int[] {2, 1}};

    /**
     * The side length of each grid square
     */
    public static final int GRID_SPACING = 25;

    /**
     * The Color of the box around a component when it is selected
     */
    public static final Color SELECT_COLOR = new Color(66, 82, 255);

    /**
     * The x and y length of the divider lines drawn around the center rectangle
     */
    private static final int CUSTOM_DIVIDER_SIZE = 1000;

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

    public void render(Graphics2D g2d, ArrayList<LComponent> lcomps, ArrayList<Wire> wires, float cx, float cy, float zoom){
        if(this.zoom != zoom) cache.clear();
        this.cx = cx;
        this.cy = cy;
        this.zoom = zoom;
        invZoom = 1.0f / zoom;

        Rectangle screen = new Rectangle(0, 0, cp.getWidth(), cp.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.fillRect(screen.x, screen.y, screen.width, screen.height);
        Rectangle view = new Rectangle(screenToCircuit(0, 0));
        view.add(screenToCircuit(screen.width, screen.height));

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if(cp.isHighQuality()) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        applyTransform(g2d);
        renderGrid(g2d, view);
        renderWires(g2d, wires, view);
        reverseTransform(g2d);
        renderComponents(g2d, lcomps, view);
        applyTransform(g2d);
        renderHighLight(g2d);
        renderCustom(g2d);
        reverseTransform(g2d);
    }

    private void renderGrid(Graphics2D g2d, Rectangle view){
        if(!cp.isShowGrid()) return;
        g2d.setColor(Color.GRAY);
        int startX = (view.x / GRID_SPACING) * GRID_SPACING;
        int startY = (view.y / GRID_SPACING) * GRID_SPACING;
        for(int i = startX; i < view.x + view.width; i += GRID_SPACING) g2d.drawLine(i, view.y, i, view.y + view.height);
        for(int i = startY; i < view.y + view.height; i += GRID_SPACING) g2d.drawLine(view.x, i, view.x + view.width, i);
    }

    private void renderWires(Graphics2D g2d, ArrayList<Wire> wires, Rectangle view){
        for (Wire wire : wires) {
            if (!wire.isComplete()) wire.render(g2d, cp);
            else if (view.contains(wire.getSourceConnection().getCoord()) ||
                    view.contains(wire.getDestConnection().getCoord())) {
                wire.render(g2d, cp);
            }
        }
    }

    private void renderComponents(Graphics2D g2d, ArrayList<LComponent> lcomps, Rectangle view){
        for(LComponent lcomp : lcomps) {
            if(view.intersects(lcomp.getBounds())) renderComponent(g2d, lcomp);
        }
    }

    private void renderHighLight(Graphics2D g2d){
        Rectangle bounds = cp.getEditor().getHighlight().getBounds();
        g2d.setColor(SELECT_COLOR);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.fill(bounds);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        g2d.draw(bounds);
    }

    private void renderCustom(Graphics2D g2d){
        if(!cp.getEditor().getCustomCreator().isActive()) return;
        Rectangle centerRect = cp.getEditor().getCustomCreator().getCenterRect();
        
        g2d.setColor(SELECT_COLOR);
        g2d.setStroke(new BasicStroke(5));
        g2d.draw(centerRect);
        int x = centerRect.x;
        int y = centerRect.y;
        int x2 = centerRect.x + centerRect.width;
        int y2 = centerRect.y + centerRect.height;
        g2d.drawLine(x, y, x - CUSTOM_DIVIDER_SIZE, y - CUSTOM_DIVIDER_SIZE);
        g2d.drawLine(x2, y, x2 + CUSTOM_DIVIDER_SIZE, y - CUSTOM_DIVIDER_SIZE);
        g2d.drawLine(x2, y2, x2 + CUSTOM_DIVIDER_SIZE, y2 + CUSTOM_DIVIDER_SIZE);
        g2d.drawLine(x, y2, x - CUSTOM_DIVIDER_SIZE, y2 + CUSTOM_DIVIDER_SIZE);
        g2d.setStroke(new BasicStroke(1));
    }

    private void renderComponent(Graphics2D g2d, LComponent lcomp){
        CachedImage image = cache.get(lcomp);
        if(image == null){
            image = renderComponentImage(lcomp);
            cache.add(lcomp, image);
        }

        Point p = circuitToScreen(lcomp.getX(), lcomp.getY());
        int rot = lcomp.getRotator().getRotation();
        double radians = CompRotator.RAD_ROTATION[rot];

        g2d.rotate(radians, p.x, p.y);
        g2d.drawImage(image,
                p.x - image.anchors[rotationAnchorTranslate[rot][0]],
                p.y - image.anchors[rotationAnchorTranslate[rot][1]],
                null);
        g2d.rotate(-radians, p.x, p.y);
    }

    private CachedImage renderComponentImage(LComponent lcomp){
        Rectangle lb = lcomp.getBoundsRight();
        Rectangle cb = lcomp.getIO().getConnectionBounds();
        CompType type = lcomp.getType();

        CachedImage image = new CachedImage(
                (int) (cb.width * zoom),
                (int) (cb.height * zoom),
                (int) (-cb.x * zoom),
                (int) (-cb.y * zoom),
                (int) ((-cb.x + lb.width) * zoom),
                (int) ((-cb.y + lb.height) * zoom));
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.scale(zoom, zoom);

        drawConnections(g2d, lcomp, -cb.x, -cb.y);

        if(type == CompType.CUSTOM) {
            Rectangle bounds = lcomp.getBoundsRight();
            bounds.translate(-cb.x, -cb.y);
            g2d.setColor(Color.WHITE);
            g2d.fill(bounds);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(4));
            g2d.draw(bounds);
            return image;
        }

        GraphicsNode svg = lcomp.getDrawer().getActiveSVG();
        int size = Math.max(lb.width, lb.height);
        float difference = Math.abs(lb.width - lb.height);
        if(lb.width <= lb.height) svg.setTransform(new AffineTransform(size, 0, 0, size, -cb.x - difference * 0.5, -cb.y));
        else svg.setTransform(new AffineTransform(size, 0, 0, size, -cb.x,  -cb.y - difference * 0.5));
        svg.paint(g2d);


        if(type == CompType.XOR || type == CompType.XNOR) drawExclusive(g2d, -cb.x, -cb.y);
        if(type == CompType.NAND || type == CompType.NOR || type == CompType.XNOR) drawInverted(g2d, -cb.x, -cb.y);
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
        g2d.setColor(SELECT_COLOR);
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

    private void applyTransform(Graphics2D g2d){
        g2d.scale(zoom, zoom);
        g2d.translate(cx, cy);
    }

    private void reverseTransform(Graphics2D g2d){
        g2d.translate(-cx, -cy);
        g2d.scale(invZoom, invZoom);
    }

    private Point circuitToScreen(int x, int y){
        return new Point((int) ((x + cx) * zoom), (int) ((y + cy) * zoom));
    }

    private Point screenToCircuit(int x, int y){
        return new Point((int) (x * invZoom - cx), (int) (y * invZoom - cy));
    }
}
