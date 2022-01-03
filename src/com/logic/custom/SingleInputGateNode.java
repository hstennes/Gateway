package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;

import java.util.ArrayList;
import java.util.List;

public class SingleInputGateNode implements Node {

    private final int mask;

    private final int in;

    private final int inOut;

    private int signal;

    private final int out;

    public SingleInputGateNode(int in, int inOut, int out, CompType type) {
        this.in = in;
        this.inOut = inOut;
        this.out = out;
        mask = type == CompType.NOT ? -1 : 0;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int newSignal = nb.get(in, inOut) ^ mask;
        if(newSignal == signal) return;
        signal = newSignal;
        active.add(out);
    }

    @Override
    public int getSignal(int n) {
        return signal;
    }
}
