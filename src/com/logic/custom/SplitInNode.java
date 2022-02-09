package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SplitInNode extends Node{

    private final int[] split;

    public SplitInNode(int[] in, int[][] out, int[] split) {
        super(in, out);
        this.split = split;
    }

    @Override
    public void update(SignalProvider sp, ArrayList<Integer> active, int id) {
        int newSignal = 0;
        int shift = 0;
        for(int i = 0; i < split.length; i++){
            newSignal |= (sp.getSignal(in[i * 2], in[i * 2 + 1]) & (1 << split[i] - 1)) << shift;
            shift += split[i];
        }
        if(newSignal == sp.getSignal(id, 0)) return;
        sp.setSignal(id, 0, newSignal);
        active.addAll(Arrays.stream(out[0]).boxed().collect(Collectors.toList()));
    }
}
