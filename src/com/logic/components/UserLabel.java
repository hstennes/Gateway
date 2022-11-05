package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.ui.Renderer;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * A label component for labeling things
 */
public class UserLabel extends LComponent {

    /**
     * The space between the text of the label and the box drawn around the label
     */
    public static final int MARGIN = 5;

    /**
     * The space between the label box and the component bounds
     */
    public static final int BOUNDS_PADDING = 5;

    /**
     * Default label text when label is first created
     */
    private static final String DEFAULT_TEXT = "Label";

    /**
     * The width of the component, recalculated when the text changes
     */
    private int width;

    /**
     * The height of the component, calculated once when the component is created
     */
    private int height;

    /**
     * Creates a new UserLabel with the default text. The label text is stored through the component's name.
     * @param x x position
     * @param y y position
     */
    public UserLabel(int x, int y) {
        super(x, y, CompType.LABEL);
        name = DEFAULT_TEXT;
        calcWidth();
        calcHeight();
    }

    @Override
    public void update(LogicEngine logicEngine) { }

    @Override
    public Rectangle getBoundsRight() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        calcWidth();
    }

    /**
     * Refreshes the width of the component to reflect the current text
     */
    private void calcWidth() {
        FontRenderContext frc = new FontRenderContext(
                Renderer.USER_LABEL_FONT.getTransform(), true, true);
        Rectangle2D textBounds = Renderer.USER_LABEL_FONT.getStringBounds(name, frc);
        width = (int) textBounds.getWidth() + 2 * (MARGIN + BOUNDS_PADDING);
    }

    /**
     * Refreshes the height of the component
     */
    private void calcHeight() {
        FontRenderContext frc = new FontRenderContext(
                Renderer.USER_LABEL_FONT.getTransform(), true, true);
        Rectangle2D textBounds = Renderer.USER_LABEL_FONT.getStringBounds(name, frc);
        height = (int) textBounds.getHeight() + 2 * (MARGIN + BOUNDS_PADDING);
    }

    @Override
    public LComponent makeCopy() {
        UserLabel newLabel = new UserLabel(x, y);
        newLabel.setName(name);
        return newLabel;
    }
}
