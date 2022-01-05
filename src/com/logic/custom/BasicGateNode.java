package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BasicGateNode implements Node{

    private final byte function;

    private final int[] in;

    private final int[] out;

    private int signal;

    public BasicGateNode(int[] in, int[] out, CompType type) {
        this.in = in;
        this.out = out;
        function = (byte) LogicFunctions.getFunctionIndex(type);
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int[] inputs = new int[in.length / 2];
        for(int i = 0; i < inputs.length; i++) inputs[i] = nb.get(in[i * 2], in[i * 2 + 1]);
        int newSignal = LogicFunctions.basicLogic(inputs, function);
        if(newSignal == signal) return;
        signal = newSignal;
        active.addAll(Arrays.stream(out).boxed().collect(Collectors.toList()));
    }

    @Override
    public int getSignal(int n) {
        return signal;
    }
}
