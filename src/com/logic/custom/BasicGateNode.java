package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;

import java.util.ArrayList;
import java.util.List;

public class BasicGateNode implements Node{

    private final int function;

    private final int[] in;

    private final int out;

    private int signal;

    public BasicGateNode(int[] in, int out, CompType type) {
        this.in = in;
        this.out = out;
        function = LogicFunctions.getFunctionIndex(type);
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int[] inputs = new int[in.length / 2];
        for(int i = 0; i < in.length; i += 2) inputs[i] = nb.get(in[i], in[i + 1]);
        int newSignal = LogicFunctions.basicLogic(inputs, function);
        if(newSignal == signal) return;
        signal = newSignal;
        active.add(out);
    }

    @Override
    public int getSignal(int n) {
        return signal;
    }
}
