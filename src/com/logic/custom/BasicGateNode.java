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
    public void update(int[] signals, int offset, ActiveStack active) {
        int newSignal = signals[in[0] + offset];
        for(int i = 1; i < in.length; i++){
            newSignal = LogicFunctions.twoInput.get(function).apply(newSignal, signals[in[i] + offset]);
        }
        if(newSignal == signals[address + offset]) return;
        signals[address + offset] = newSignal;
        active.mark(mark[0]);
    }
}
