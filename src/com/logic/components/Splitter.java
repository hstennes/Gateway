package com.logic.components;

import com.logic.ui.Renderer;

import java.awt.*;

public abstract class Splitter extends LComponent{

    /**
     * Defines how the bits are split. Each index specifies one connection. Index 0 corresponds to the lest significant
     * bit(s). Each element specifies how many bits from the full signal are included in that connection.
     */
    protected int[] split;

    private final int height;

    protected final int width = 54;

    protected final int connectOffset = 10;

    public Splitter(int x, int y, CompType type, int[] split) {
        super(x, y, type);
        this.split = split;
        height = (split.length - 1) * Renderer.BASIC_INPUT_SPACING;
    }

    @Override
    public LComponent makeCopy() {
        Splitter result = type == CompType.SPLIT_OUT ? new SplitOut(x, y, split) :
                new SplitIn(x, y, split);
        result.setRotation(rotation);
        result.setName(getName());
        return result;
    }

    protected Point[] calcConnectionPositions(int xPos, int numConnections){
        Point[] positions = new Point[numConnections];
        for(int i = 0; i < numConnections; i++){
            positions[i] = new Point(xPos, i * Renderer.BASIC_INPUT_SPACING);
        }
        return positions;
    }

    @Override
    public Rectangle getBoundsRight(){
        return new Rectangle(x, y, width, height);
    }
}
