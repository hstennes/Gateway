package com.logic.custom;

import java.util.ArrayList;
import java.util.List;

public class StartNode implements Node{

    private final int out;

    public int signal;

    public StartNode(int out){
        this.out = out;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        active.add(out);
    }

    @Override
    public int getSignal(int n) {
        return signal;
    }

    public void setSignal(int signal){
        this.signal = signal;
    }
}
