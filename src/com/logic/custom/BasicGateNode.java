package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BasicGateNode extends Node{

    private final byte function;

    public BasicGateNode(int[] in, int[][] mark, int address, CompType type) {
        super(in, mark, address);
        function = (byte) LogicFunctions.getFunctionIndex(type);
    }

    @Override
    public void update(int[] signals, int offset, ArrayList<Integer> active) {
        int[] inputs = new int[in.length];
        for(int i = 0; i < inputs.length; i++) inputs[i] = signals[in[i] + offset];
        int newSignal = LogicFunctions.basicLogic(inputs, function);
        if(newSignal == signals[address + offset]) return;
        signals[address + offset] = newSignal;
        active.addAll(Arrays.stream(mark[0]).boxed().collect(Collectors.toList()));
    }
}
