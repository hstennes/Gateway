package com.logic.ui;

import com.logic.components.*;
import com.logic.custom.OpCustom;
import com.logic.main.LogicSimApp;
import com.logic.util.CompUtils;
import com.logic.util.Constants;
import org.apache.batik.gvt.GraphicsNode;

import javax.swing.plaf.basic.BasicIconFactory;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

public class Renderer {

    public final static int CONNECT_RAD = 9;

    /**
     * Don't remember how this worked
     */
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
     * The amount of space between consecutive inputs on a BasicGate
     */
    public static final int BASIC_INPUT_SPACING = 50;

    /**
     * The length of the line extending from each connection
     */
    public static final int CONNECT_LENGTH = 37;

    /**
     * The spacing of bits within a multi bit switch
     */
    public static final int SWITCH_BIT_SPACING = 35;

    /**
     * The height of "switches" and "lights" when in multi-bit form
     */
    public static final int MULTI_BIT_SL_HEIGHT = 40;

    /**
     * The font used to display the label of the component
     */
    public static final Font CUSTOM_LABEL_FONT = new Font("Arial", Font.PLAIN, 15);

    /**
     * The font used for multi bit switches
     */
    public static final Font SWITCH_FONT = new Font("Arial", Font.PLAIN, 34);

    /**
     * The font used for rendering Display components
     */
    private final Font DISPLAY_FONT;

    /**
     * The x and y length of the divider lines drawn around the center rectangle in the custom builder
     */
    private static final int CUSTOM_DIVIDER_SIZE = 1000;

    /**
     * The CircuitPanel instance
     */
    private final CircuitPanel cp;

    /**
     * The collection of cached images
     */
    private final ImageCache cache;

    /**
     * The camera position
     */
    private float cx, cy, zoom;

    private final LabelDrawer userMessageDrawer;

    /**
     * Inverse zoom, used to optimize dividing by zoom
     */
    private float invZoom;

    public Renderer(CircuitPanel cp){
        this.cp = cp;
        cache = new ImageCache();
        userMessageDrawer = new LabelDrawer(UserMessage.MESSAGE_FONT, UserMessage.MESSAGE_COLOR, UserMessage.X_MARGIN, UserMessage.Y_MARGIN);
        DISPLAY_FONT = LogicSimApp.fontLoader.sevenSegFont.deriveFont(70f);
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
        renderCustomCreator(g2d);
        reverseTransform(g2d);
        renderUserMessage(g2d);
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
            if (!wire.isComplete()) renderWire(g2d, wire);
            else if (view.contains(wire.getSourceConnection().getCoord()) ||
                    view.contains(wire.getDestConnection().getCoord())) {
                renderWire(g2d, wire);
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
        if(bounds.width == 0 && bounds.height == 0) return;
        g2d.setColor(SELECT_COLOR);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.fill(bounds);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        g2d.draw(bounds);
    }

    private void renderCustomCreator(Graphics2D g2d){
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

    private void renderUserMessage(Graphics2D g2d){
        UserMessage message = cp.getUserMessage();
        if(message == null) return;
        userMessageDrawer.render(g2d,
                cp.getWidth() / 2,
                UserMessage.Y_OFFSET,
                LabelDrawer.CENTER,
                LabelDrawer.START,
                message.getText());
    }

    private void renderWire(Graphics2D g2d, Wire wire){
        CubicCurve2D curve = wire.getCurveUpdate(cp);
        if(wire.isSelected()) {
            g2d.setColor(Renderer.SELECT_COLOR);
            g2d.setStroke(new BasicStroke(10));
        }
        else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(7));
        }

        g2d.draw(curve);
        if (wire.getSignalOld()) g2d.setColor(Color.ORANGE);
        else g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(curve);
    }

    private void renderComponent(Graphics2D g2d, LComponent lcomp){
        CachedImage image = cache.get(lcomp);
        if(image == null){
            image = renderComponentImage(lcomp, zoom, LogicSimApp.DISP_SCALE);
            cache.add(lcomp, image);
        }

        Point p = circuitToScreen(lcomp.getX(), lcomp.getY());
        int rot = lcomp.getRotation();
        double radians = CompUtils.RAD_ROTATION[rot];

        g2d.rotate(radians, p.x, p.y);
        Rectangle imageBox =  new Rectangle(
                p.x - image.anchors[rotationAnchorTranslate[rot][0]],
                p.y - image.anchors[rotationAnchorTranslate[rot][1]],
                (int) (image.getWidth() * LogicSimApp.INV_DISP_SCALE),
                (int) (image.getHeight() * LogicSimApp.INV_DISP_SCALE));


        g2d.drawImage(image,
                imageBox.x, imageBox.y, imageBox.width, imageBox.height, null);
        if(lcomp.isSelected()) {
            g2d.setColor(SELECT_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(imageBox);
        }

        g2d.rotate(-radians, p.x, p.y);
    }

    public CachedImage renderComponentImage(LComponent lcomp, float zoom, float dpiScale){
        //set up BufferedImage and graphics object, same for all components
        Rectangle lb = lcomp.getBoundsRight();
        Rectangle cb = lcomp.getIO().getConnectionBounds();
        CompType type = lcomp.getType();
        float invDpiScale = 1 / dpiScale;
        zoom *= dpiScale;
        CachedImage image = new CachedImage(
                (int) (cb.width * zoom),
                (int) (cb.height * zoom),
                (int) (-cb.x * zoom * invDpiScale),
                (int) (-cb.y * zoom * invDpiScale),
                (int) ((-cb.x + lb.width) * zoom * invDpiScale),
                (int) ((-cb.y + lb.height) * zoom * invDpiScale));
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.scale(zoom, zoom);

        //Draw connections, same for all components
        drawConnections(g2d, lcomp, -cb.x, -cb.y);

        //Special rendering currently for Custom, multi bit switch, multi bit light
        if(type == CompType.CUSTOM) {
            drawCustomBody(g2d, (OpCustom) lcomp, -cb.x, -cb.y);
            return image;
        }
        if(type == CompType.SWITCH && ((Switch) lcomp).getBitWidth() > 1){
            drawSignalBox(g2d, lb,
                    ((Switch) lcomp).getState(),
                    ((Switch) lcomp).getBitWidth(),
                    -cb.x, -cb.y);
            return image;
        }
        if(type == CompType.LIGHT && ((Light) lcomp).getBitWidth() > 1){
            drawSignalBox(g2d, lb,
                    lcomp.getIO().getInput(0),
                    ((Light) lcomp).getBitWidth(),
                    -cb.x, -cb.y);
            return image;
        }

        //Otherwise render component image if there is one
        GraphicsNode svg = lcomp.getActiveImage();
        if(svg != null) {
            int size = Math.max(lb.width, lb.height);
            float difference = Math.abs(lb.width - lb.height);
            if (lb.width <= lb.height)
                svg.setTransform(new AffineTransform(size, 0, 0, size, -cb.x - difference * 0.5, -cb.y));
            else svg.setTransform(new AffineTransform(size, 0, 0, size, -cb.x, -cb.y - difference * 0.5));
            svg.paint(g2d);
        }

        //Final step of adding marks for basic gates and value for display
        if(type == CompType.XOR || type == CompType.XNOR) drawExclusive(g2d, -cb.x, -cb.y);
        if(type == CompType.NAND || type == CompType.NOR || type == CompType.XNOR || type == CompType.NOT) drawInverted(g2d, -cb.x, -cb.y);
        if(type == CompType.DISPLAY) drawDisplayValue(g2d, ((Display) lcomp), -cb.x, -cb.y);
        return image;
    }

    private void drawCustomBody(Graphics2D g2d, OpCustom custom, int dx, int dy){
        Rectangle bounds = custom.getBoundsRight();
        bounds.translate(dx, dy);
        g2d.setColor(Color.WHITE);
        g2d.fill(bounds);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.draw(bounds);

        g2d.setFont(CUSTOM_LABEL_FONT);
        FontMetrics metrics = g2d.getFontMetrics(CUSTOM_LABEL_FONT);
        g2d.drawString(custom.getLabel(),
                (bounds.width - metrics.stringWidth(custom.getLabel())) / 2 + dx,
                (bounds.height - metrics.getHeight()) / 2 + dy + metrics.getAscent());
    }

    private void drawSignalBox(Graphics2D g2d, Rectangle lb, int signal, int bitWidth, int dx, int dy){
        g2d.setColor(Color.WHITE);
        g2d.fillRect(dx, dy, lb.width, lb.height);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(dx + 2, dy + 2, lb.width - 4, lb.height - 4);

        g2d.setFont(SWITCH_FONT);
        FontMetrics metrics = g2d.getFontMetrics(SWITCH_FONT);
        for(int i = 0; i < bitWidth; i++){
            String bit = Integer.toString((signal >> i) & 1);
            int strWidth = metrics.stringWidth(bit);
            g2d.drawString(bit,
                    dx + lb.width - SWITCH_BIT_SPACING * (i + 1) + (SWITCH_BIT_SPACING - strWidth) / 2,
                    dy + (lb.height - metrics.getHeight()) / 2 + dy + metrics.getAscent());
        }
    }

    private void drawDisplayValue(Graphics2D g2d, Display display, int dx, int dy){
        double radians = CompUtils.RAD_ROTATION[display.getRotation()];
        Rectangle bounds = display.getBounds();
        g2d.rotate(-radians, dx + bounds.width / 2, dy + bounds.height / 2);
        g2d.setFont(DISPLAY_FONT);
        g2d.setColor(SELECT_COLOR);
        g2d.drawString(display.getValue(), dx + 22, dy + 85);
        g2d.rotate(radians, dx + bounds.width / 2, dy + bounds.height / 2);
    }

    /**
     * Draws the connections (with lines) for the component as specified by its IOManager
     * @param g2d The graphics object to use
     * @param lcomp The LComponent
     */
    private void drawConnections(Graphics2D g2d, LComponent lcomp, int dx, int dy){
        IOManager io = lcomp.getIO();
        Point barStart = null, barStop = null;
        for(int i = 0; i < io.getNumInputs(); i++) {
            Point result = drawConnection(g2d, io.inputConnection(i), dx, dy);
            if(lcomp instanceof BasicGate | lcomp instanceof SplitIn) {
                if (i == 0) barStart = result;
                if (i == io.getNumInputs() - 1) barStop = result;
            }
        }
        for(int i = 0; i < io.getNumOutputs(); i++) {
            Point result = drawConnection(g2d, io.outputConnection(i), dx, dy);
            if(lcomp instanceof SplitOut) {
                if (i == 0) barStart = result;
                if (i == io.getNumOutputs() - 1) barStop = result;
            }
        }

        if(lcomp instanceof BasicGate && io.getNumInputs() > 2 || lcomp instanceof Splitter) {
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
        if(direction == Constants.RIGHT) {
            g2d.drawLine(p.x, p.y, p.x - CONNECT_LENGTH, p.y);
            connectEnd = new Point(p.x - CONNECT_LENGTH, p.y);
        }
        if(direction == Constants.UP) {
            g2d.drawLine(p.x, p.y, p.x, p.y + CONNECT_LENGTH);
            connectEnd = new Point(p.x, p.y + CONNECT_LENGTH);
        }
        if(direction == Constants.LEFT) {
            g2d.drawLine(p.x, p.y, p.x + CONNECT_LENGTH, p.y);
            connectEnd = new Point(p.x + CONNECT_LENGTH, p.y);
        }
        if(direction == Constants.DOWN) {
            g2d.drawLine(p.x, p.y, p.x, p.y - CONNECT_LENGTH);
            connectEnd = new Point(p.x, p.y - CONNECT_LENGTH);
        }
        g2d.setColor(SELECT_COLOR);
        g2d.fillOval(p.x - 9, p.y - 9, 18, 18);
        return connectEnd;
    }

    /**
     * Draws a curved line to show that a gate is an XOR or an XNOR
     * @param g2d The Graphics2D object to use
     */
    private void drawExclusive(Graphics2D g2d, int dx, int dy){
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
    private void drawInverted(Graphics2D g2d, int dx, int dy){
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
