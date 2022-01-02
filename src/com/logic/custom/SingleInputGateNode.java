package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;

import java.util.ArrayList;

public class SingleInputGateNode extends Node {

    private final int function;

    private final int in;

    private final int inOutIndex;

    public SingleInputGateNode(int in, int inOutIndex, CompType type) {
        super(new int[1]);
        this.in = in;
        this.inOutIndex = inOutIndex;
        if(type == CompType.BUFFER) function = 1;
        else function = 0;
    }

    @Override
    public void update(Node[] nodes, ArrayList<Integer> active) {
        out[0] = LogicFunctions.singleInput.get(function).apply(nodes[in].out[inOutIndex]);
    }
}
