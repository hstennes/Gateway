package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SplitOutNode extends Node{

    private final int[] split;

    public SplitOutNode(int[] in, int[][] mark, int address, int[] split) {
        super(in, mark, address);
        this.split = split;
    }

    @Override
    public void update(int[] signals, int offset, ArrayList<Integer> active) {
        int input = signals[in[0] + offset];
        for(int i = 0; i < split.length; i++){
            int newSignal = input & (1 << split[i]) - 1;
            input >>= split[i];
            int index = address + offset + i;
            if(newSignal == signals[index]) continue;
            signals[index] = newSignal;
            active.addAll(mark.get(i));
        }
    }
}
