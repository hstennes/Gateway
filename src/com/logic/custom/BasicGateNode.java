package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;

import java.util.ArrayList;

public class BasicGateNode extends Node{

    private final int function;

    private final int[] in;

    private final int[] inOutIndex;

    public BasicGateNode(int[] in, int[] inOutIndex, CompType type) {
        super(new int[1]);
        this.in = in;
        this.inOutIndex = inOutIndex;
        function = LogicFunctions.getFunctionIndex(type);
    }

    @Override
    public void update(Node[] nodes, ArrayList<Integer> active) {
        int[] inputs = new int[in.length];
        for(int i = 0; i < in.length; i++) inputs[i] = nodes[in[i]].out[inOutIndex[i]];
        out[0] = LogicFunctions.basicLogic(inputs, function);

    }
}
