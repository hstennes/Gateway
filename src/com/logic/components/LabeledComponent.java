package com.logic.components;

import com.logic.ui.CompRotator;
import com.logic.ui.LabelDrawer;

import java.awt.*;

/**
 * LComponent subclass for components that can optionally display their name as a label next to the component
 */
public abstract class LabeledComponent extends LComponent{

    /**
     * Optionally display the component name next to the component
     */
    protected boolean showLabel = false;

    /**
     * The font used to label the connection when renderLabel is called
     */
    private final Font labelFont = new Font("Arial", Font.PLAIN, 15);

    /**
     * Constructs a new LabeledComponent
     * @param x    The x position
     * @param y    The y position
     * @param type The type of component
     */
    public LabeledComponent(int x, int y, CompType type) {
        super(x, y, type);
    }

    /**
     * Renders the name of the component with LabelDrawer on the given absolute side of the component
     * @param g The Graphics object
     * @param absDirection Absolute direction
     */
    protected void renderLabel(Graphics g, int absDirection){
        if(!showLabel) return;
        Rectangle bounds = getBounds();
        Point center = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        Point end = new Point(bounds.x + bounds.width, bounds.y + bounds.height);
        LabelDrawer drawer = new LabelDrawer(labelFont, Color.WHITE, 3, 2);
        int offset = 10;
        if(absDirection == CompRotator.LEFT)
            drawer.render(((Graphics2D) g), bounds.x - offset, center.y, LabelDrawer.END, LabelDrawer.CENTER, getName());
        else if(absDirection == CompRotator.UP)
            drawer.render(((Graphics2D) g), center.x, bounds.y - offset, LabelDrawer.CENTER, LabelDrawer.END, getName());
        else if(absDirection == CompRotator.RIGHT)
            drawer.render(((Graphics2D) g), end.x + offset, center.y, LabelDrawer.START, LabelDrawer.CENTER, getName());
        else if(absDirection == CompRotator.DOWN)
            drawer.render(((Graphics2D) g), center.x, end.y + offset, LabelDrawer.CENTER, LabelDrawer.START, getName());
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }
}
