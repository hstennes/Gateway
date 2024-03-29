package com.logic.ui;

import com.logic.components.*;
import com.logic.custom.CustomType;
import com.logic.custom.OpCustom2;
import com.logic.input.WireEditor;
import com.logic.main.LogicSimApp;
import com.logic.util.CompUtils;
import com.logic.util.Constants;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.bcel.classfile.ConstantNameAndType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.ArrayList;

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
    public static final Font CUSTOM_LABEL_FONT = new Font("Arial", Font.PLAIN, 20);

    /**
     * The font used for multi bit switches
     */
    public static final Font SWITCH_FONT = new Font("Arial", Font.PLAIN, 34);

    public static final Font USER_LABEL_FONT = new Font("Arial", Font.PLAIN, 25);

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

    private LabelDrawer userLabelDrawer;

    private final LabelDrawer userMessageDrawer;

    /**
     * Inverse zoom, used to optimize dividing by zoom
     */
    private float invZoom;

    public Renderer(CircuitPanel cp){
        this.cp = cp;
        cache = new ImageCache();
        userLabelDrawer = new LabelDrawer(USER_LABEL_FONT, UserMessage.MESSAGE_COLOR, UserLabel.MARGIN, UserLabel.MARGIN);
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
        applyTransform(g2d);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderGrid(g2d, view);
        if(!cp.isHighQuality()) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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
        Path2D curve = wire.getCurveUpdate(cp);
        if(wire.isSelected()) {
            g2d.setColor(Renderer.SELECT_COLOR);
            g2d.setStroke(new BasicStroke(12));
        }
        else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(7));
        }

        g2d.draw(curve);
        if ((wire.getSignal() & 1) == 1) g2d.setColor(Color.ORANGE);
        else g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(curve);

        WireEditor wireEditor = cp.getEditor().getWireEditor();
        ArrayList<Point> shapePoints = wire.getShapePoints();
        for(int i = 0; i < shapePoints.size(); i++) {
            drawWireShapePoint(g2d, shapePoints.get(i), wireEditor.isPointSelected(wire, i));
        }
    }

    private void renderComponent(Graphics2D g2d, LComponent lcomp){
        CachedImage image;
        if(lcomp.getType() == CompType.SCREEN){
            image = renderComponentImage(lcomp, zoom, LogicSimApp.DISP_SCALE, null);
        }
        else {
            image = cache.getStaticImage(lcomp, CompUtils.getSensitiveCompData(lcomp));
            if(image == null){
                image = renderComponentImage(lcomp, zoom, LogicSimApp.DISP_SCALE, null);
                cache.addStaticImage(image);
            }
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

    public CachedImage renderComponentImage(LComponent lcomp, float zoom, float dpiScale, CachedImage oldImage){
        //Record component state that could change ahead of time to avoid threading issues
        int compData = CompUtils.getSensitiveCompData(lcomp);
        //set up BufferedImage and graphics object, same for all components
        Rectangle lb = lcomp.getBoundsRight();
        Rectangle cb = lcomp.getIO().getConnectionBounds();
        CompType type = lcomp.getType();
        float invDpiScale = 1 / dpiScale;
        zoom *= dpiScale;
        CachedImage image;
        if(oldImage == null) {
            image = new CachedImage((int) (cb.width * zoom), (int) (cb.height * zoom),
                    (int) (-cb.x * zoom * invDpiScale), (int) (-cb.y * zoom * invDpiScale),
                    (int) ((-cb.x + lb.width) * zoom * invDpiScale), (int) ((-cb.y + lb.height) * zoom * invDpiScale),
                    ImageCache.getHashString(lcomp, compData));
        }
        else image = oldImage;
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.scale(zoom, zoom);

        //Draw connections, same for all components
        if(oldImage == null) drawConnections(g2d, lcomp, -cb.x, -cb.y);

        switch(type){
            case CUSTOM:
                drawBoxComponent(g2d, lcomp.getBoundsRight(), ((OpCustom2) lcomp).getCustomType().label, -cb.x, -cb.y);
                drawCustomConnectionLabels(g2d, ((OpCustom2) lcomp), -cb.x, -cb.y);
                return image;
            case SWITCH:
                if(((Switch) lcomp).getBitWidth() == 1) break;
                drawSignalBox(g2d, lb, compData, ((Switch) lcomp).getBitWidth(), -cb.x, -cb.y);
                return image;
            case LIGHT:
                if(((Light) lcomp).getBitWidth() == 1) break;
                drawSignalBox(g2d, lb, compData, ((Light) lcomp).getBitWidth(), -cb.x, -cb.y);
                return image;
            case ROM:
                drawBoxComponent(g2d, lcomp.getBoundsRight(), "ROM", -cb.x, -cb.y);
                return image;
            case RAM:
                drawBoxComponent(g2d, lcomp.getBoundsRight(), "RAM", -cb.x, -cb.y);
                drawRamConnectionLabels(g2d, ((RAM) lcomp), -cb.x, -cb.y);
                return image;
            case SCREEN:
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                if(oldImage == null) drawScreen(g2d, (Screen) lcomp, -cb.x, -cb.y);
                else updateScreen(g2d, (Screen) lcomp, -cb.x, -cb.y);
                break;
            case LABEL:
                userLabelDrawer.render(g2d, UserLabel.BOUNDS_PADDING, UserLabel.BOUNDS_PADDING,
                        LabelDrawer.START, LabelDrawer.START, lcomp.getName());
        }

        //Otherwise render component image if there is one
        GraphicsNode svg = lcomp.getActiveImage(compData);
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
        if(type == CompType.DISPLAY) drawDisplayValue(g2d, ((Display) lcomp), compData, -cb.x, -cb.y);
        return image;
    }

    private void drawBoxComponent(Graphics2D g2d, Rectangle bounds, String label, int dx, int dy){
        bounds.translate(dx, dy);
        g2d.setColor(Color.WHITE);
        g2d.fill(bounds);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);

        g2d.setFont(CUSTOM_LABEL_FONT);
        FontMetrics metrics = g2d.getFontMetrics(CUSTOM_LABEL_FONT);
        g2d.drawString(label,
                (bounds.width - metrics.stringWidth(label)) / 2 + dx,
                (bounds.height - metrics.getHeight()) / 2 + dy + metrics.getAscent());
    }

    private void drawCustomConnectionLabels(Graphics2D g2d, OpCustom2 custom, int dx, int dy){
        IOManager io = custom.getIO();
        CustomType type = custom.getCustomType();
        for(int i = 0; i < io.getNumInputs(); i++) {
            Connection connect = io.inputConnection(i);
            int[] sideNum = type.helper.getSideAndNum(i, Connection.INPUT);
            String label = type.connectionLabels[sideNum[0]][sideNum[1]];
            drawCustomConnectionLabel(g2d, connect, label, dx, dy);
        }
        for(int i = 0; i < io.getNumOutputs(); i++) {
            Connection connect = io.outputConnection(i);
            int[] sideNum = type.helper.getSideAndNum(i, Connection.OUTPUT);
            String label = type.connectionLabels[sideNum[0]][sideNum[1]];
            drawCustomConnectionLabel(g2d, connect, label, dx, dy);
        }
    }

    private void drawRamConnectionLabels(Graphics2D g2d, RAM ram, int dx, int dy){
        IOManager io = ram.getIO();
        for(int i = 0; i < io.getNumInputs(); i++) {
            Connection connect = io.inputConnection(i);
            drawCustomConnectionLabel(g2d, connect, RAM.INPUT_LABELS[i], dx, dy);
        }
        for(int i = 0; i < io.getNumOutputs(); i++) {
            Connection connect = io.outputConnection(i);
            drawCustomConnectionLabel(g2d, connect, RAM.OUTPUT_LABELS[i], dx, dy);
        }
    }

    private void drawCustomConnectionLabel(Graphics2D g2d, Connection connect, String label, int dx, int dy) {
        Point connectPos = translateDir(new Point(connect.getX(), connect.getY()),
                CONNECT_LENGTH,
                (connect.getDirection() + 2) % 4);
        connectPos.translate(dx, dy);

        int alignX;
        int direction = connect.getDirection();
        if(direction == Constants.UP || direction == Constants.DOWN) alignX = LabelDrawer.CENTER;
        else if(direction == Constants.RIGHT) alignX = LabelDrawer.END;
        else alignX = LabelDrawer.START;

        Point strLoc = LabelDrawer.calcTextPositioning(g2d,
                connectPos.x,
                connectPos.y,
                alignX,
                LabelDrawer.CENTER,
                label, CUSTOM_LABEL_FONT);
        g2d.drawString(label, strLoc.x, strLoc.y);
    }

    private void drawSignalBox(Graphics2D g2d, Rectangle lb, int signal, int bitWidth, int dx, int dy){
        drawBox(g2d, dx, dy, lb);

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

    private void drawScreen(Graphics2D g2d, Screen screen, int dx, int dy){
        Rectangle bounds = screen.getBoundsRight();
        drawBox(g2d, dx, dy, bounds);

        RAM ram = screen.getRamIfExists();
        if(ram != null){
            for(int row = 0; row < 256; row++){
                for(int reg = 0; reg < 32; reg++){
                    int regVal = ram.getData()[16384 + row * 32 + reg];
                    for(int x = 0; x < 16; x++){
                        if(((regVal >> x) & 1) == 1) g2d.setColor(Color.DARK_GRAY);
                        else g2d.setColor(Color.WHITE);
                        g2d.fillRect(dx + Screen.PADDING + (x + 16 * reg) * 2, dy + Screen.PADDING + row * 2, 2, 2);
                    }
                }
            }
        }
    }

    private void updateScreen(Graphics2D g2d, Screen screen, int dx, int dy){
        RAM ram = screen.getRamIfExists();
        if(ram != null){
            for(int address : screen.getRamUpdates()){
                int register = address - Screen.ADDR;
                int value = ram.getData()[address];
                int y = register / 32;
                int xStart = (register % 32) * 16;
                for(int x = 0; x < 16; x++){
                    if(((value >> x) & 1) == 1) g2d.setColor(Color.DARK_GRAY);
                    else g2d.setColor(Color.WHITE);
                    g2d.fillRect(dx + Screen.PADDING + (x + xStart) * 2, dy + Screen.PADDING + y * 2, 2, 2);
                }
            }
            screen.clearRamUpdates();
        }
    }

    private void drawBox(Graphics2D g2d, int dx, int dy, Rectangle bounds) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(dx, dy, bounds.width, bounds.height);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(dx + 2, dy + 2, bounds.width - 4, bounds.height - 4);
    }

    private void drawDisplayValue(Graphics2D g2d, Display display, int compData, int dx, int dy){
        double radians = CompUtils.RAD_ROTATION[display.getRotation()];
        Rectangle bounds = display.getBounds();
        g2d.rotate(-radians, dx + bounds.width / 2, dy + bounds.height / 2);
        g2d.setFont(DISPLAY_FONT);
        g2d.setColor(SELECT_COLOR);
        g2d.drawString(Display.VALUE_STRS[compData], dx + 22, dy + 85);
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
        Point connectEnd = translateDir(new Point(p.x, p.y), CONNECT_LENGTH, (direction + 2) % 4);
        g2d.drawLine(p.x, p.y, connectEnd.x, connectEnd.y);
        g2d.setColor(SELECT_COLOR);
        g2d.fillOval(p.x - 9, p.y - 9, 18, 18);
        return connectEnd;
    }

    private void drawWireShapePoint(Graphics2D g2d, Point p, boolean selected) {
        g2d.setColor(SELECT_COLOR);
        g2d.fillOval(p.x - 9, p.y - 9, 18, 18);
        if(selected) g2d.drawRect(p.x - 15, p.y - 15, 30, 30);
    }

    /**
     * Returns a new point that represents the given point translated in one of four directions
     * @param p The point
     * @param dist The distance to translate
     * @param direction The direction
     * @return The new point
     */
    private Point translateDir(Point p, int dist, int direction) {
        if(direction == Constants.RIGHT) return new Point(p.x + dist, p.y);
        else if(direction == Constants.UP) return new Point(p.x, p.y - dist);
        else if(direction == Constants.LEFT) return new Point(p.x - dist, p.y);
        return new Point(p.x, p.y + dist);
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
