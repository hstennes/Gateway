package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.util.Constants;

import java.awt.*;
import java.util.stream.IntStream;

public class SplitOut extends Splitter {

    public SplitOut(int x, int y, int[] split) {
        super(x, y, CompType.SPLIT_OUT, split);

        Point[] connectPos = calcConnectionPositions(width + connectOffset, split.length);
        for(int i = 0; i < split.length; i++){
            io.addConnection(connectPos[i].x, connectPos[i].y, Connection.OUTPUT, Constants.RIGHT);
            io.outputConnection(i).changeBitWidth(split[i]);
        }

        io.addConnection(-connectOffset, 0, Connection.INPUT, Constants.LEFT);
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
}
