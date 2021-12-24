package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.util.Constants;

import java.awt.*;
import java.util.stream.IntStream;

public class SplitIn extends Splitter {

    public SplitIn(int x, int y, int[] split){
        super(x, y, CompType.SPLIT_IN, split);

        Point[] connectPos = calcConnectionPositions(-connectOffset, split.length);
        for(int i = 0; i < split.length; i++){
            io.addConnection(connectPos[i].x, connectPos[i].y, Connection.INPUT, Constants.LEFT);
            io.inputConnection(i).changeBitWidth(split[i]);
        }

        io.addConnection(width + connectOffset, 0, Connection.OUTPUT, Constants.RIGHT);
        io.outputConnection(0).changeBitWidth(IntStream.of(split).sum());
    }

    @Override
    public void update(LogicEngine engine) {
        int output = 0;
        int shift = 0;
        for(int i = 0; i < split.length; i++){
            output |= io.getInputStrict(i) << shift;
            shift += split[i];
        }
        io.setOutput(0, output, engine);
    }
}
