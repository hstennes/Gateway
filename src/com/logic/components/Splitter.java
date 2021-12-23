package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.ui.Renderer;
import com.logic.util.CompUtils;
import com.logic.util.Constants;

import java.awt.*;
import java.util.stream.IntStream;

public class Splitter extends LComponent{

    /**
     * Defines how the bits are split. Each index specifies one connection. Index 0 corresponds to the lest significant
     * bit(s). Each element specifies how many bits from the full signal are included in that connection.
     */
    private int[] split;

    private int height;

    public Splitter(int x, int y, CompType type, int[] split) {
        super(x, y, type);
        this.split = split;
        //setImages(new int[] {0});

        Point[] connectionPositions = calcOutputPositions(50, split.length);
        for(Point p : connectionPositions){
            io.addConnection(p.x, p.y, Connection.OUTPUT, Constants.RIGHT);
        }

        io.addConnection(-10, 0, Connection.INPUT, Constants.LEFT);
        io.inputConnection(0).changeBitWidth(IntStream.of(split).sum());

        height = (split.length - 1) * Renderer.BASIC_INPUT_SPACING;
    }

    @Override
    public void update(LogicEngine engine) {
        int input = io.getInput(0);
        for(int i = 0; i < split.length; i++){
            io.setOutput(i, input & (1 << split[i]) - 1, engine);
            input >>= split[i];
        }
    }

    @Override
    public LComponent makeCopy() {
        Splitter result = new Splitter(x, y, type, split);
        result.setRotation(rotation);
        result.setName(getName());
        return result;
    }

    public static Point[] calcOutputPositions(int xPos, int numConnections){
        Point[] positions = new Point[numConnections];
        for(int i = 0; i < numConnections; i++){
            positions[i] = new Point(xPos, i * Renderer.BASIC_INPUT_SPACING);
        }
        return positions;
    }

    @Override
    public Rectangle getBoundsRight(){
        return new Rectangle(x, y, 40, height);
    }
}
