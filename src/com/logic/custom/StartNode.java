package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StartNode implements Node{

    private final int[] out;

    public int signal;

    public StartNode(int[] out){
        this.out = out;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        active.addAll(Arrays.stream(out).boxed().collect(Collectors.toList()));
    }

    @Override
    public int getSignal(int n) {
        return signal;
    }

    public void setSignal(int signal){
        this.signal = signal;
    }
}
