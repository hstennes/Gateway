package com.logic.custom;

import com.logic.components.SplitIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SplitInNode implements Node{

    private int[] split;

    private final int[] in;

    private final int[] out;

    private int signal;

    public SplitInNode(int[] split, int[] in, int[] out, int signal){
        this.split = split;
        this.in = in;
        this.out = out;
        this.signal = signal;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int newSignal = 0;
        int shift = 0;
        for(int i = 0; i < split.length; i++){
            newSignal |= nb.get(in[i * 2], in[i * 2 + 1]) << shift;
            shift += split[i];
        }
        if(newSignal == signal) return;
        signal = newSignal;
        active.addAll(Arrays.stream(out).boxed().collect(Collectors.toList()));
    }

    @Override
    public int getSignal(int n) {
        return signal;
    }

    @Override
    public Node duplicate() {
        return new SplitInNode(split, in, out, signal);
    }
}
