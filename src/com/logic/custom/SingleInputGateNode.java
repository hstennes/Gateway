package com.logic.custom;

import com.logic.components.CompType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SingleInputGateNode extends Node {

    private final int mask;

    public SingleInputGateNode(int[] in, int[][] mark, int address, CompType type) {
        super(in, mark, address);
        mask = type == CompType.NOT ? -1 : 0;
    }

    @Override
    public void update(int[] signals, int offset, ArrayList<Integer> active) {
        int newSignal = signals[in[0] + offset] ^ mask;
        if(newSignal == signals[address + offset]) return;
        signals[address + offset] = newSignal;
        active.addAll(Arrays.stream(mark[0]).boxed().collect(Collectors.toList()));
    }
}
