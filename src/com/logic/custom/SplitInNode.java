package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SplitInNode extends Node{

    private final int[] split;

    public SplitInNode(int[] in, int[][] mark, int address, int[] split) {
        super(in, mark, address);
        this.split = split;
    }

    @Override
    public void update(int[] signals, int offset, ArrayList<Integer> active) {
        int newSignal = 0;
        int shift = 0;
        for(int i = 0; i < split.length; i++){
            newSignal |= (signals[in[i] + offset] & ((1 << split[i]) - 1)) << shift;
            shift += split[i];
        }
        if(newSignal == signals[address + offset]) return;
        signals[address + offset] = newSignal;
        active.addAll(mark.get(0));
    }
}
