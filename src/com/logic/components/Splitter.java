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

    public Splitter(int x, int y, CompType type, int[] split) {
        super(x, y, type);
        this.split = split;
        setImages(new int[] {0});

        Point[] connectionPositions = CompUtils.calcEvenConnectionPositions(25, split.length);
        for(Point p : connectionPositions){
            io.addConnection(p.x, p.y, Connection.OUTPUT, Constants.RIGHT);
        }

        io.addConnection(-25, 40, Connection.INPUT, Constants.LEFT);
        io.inputConnection(0).changeBitWidth(IntStream.of(split).sum());
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
}
