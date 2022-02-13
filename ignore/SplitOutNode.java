package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SplitOutNode extends Node{

    private final int[] split;

    public SplitOutNode(int[] in, int[][] out, int[] split) {
        super(in, out);
        this.split = split;
    }

    @Override
    public void update(SignalProvider sp, ArrayList<Integer> active, int id) {
        int input = sp.getSignal(in[0], in[1]);
        for(int i = 0; i < split.length; i++){
            int newSignal = input & (1 << split[i]) - 1;
            input >>= split[i];
            if(newSignal == sp.getSignal(id, i)) continue;
            sp.setSignal(id, i, newSignal);
            active.addAll(Arrays.stream(out[i]).boxed().collect(Collectors.toList()));
        }
    }
}
