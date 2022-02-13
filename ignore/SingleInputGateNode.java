package com.logic.custom;

import com.logic.components.CompType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SingleInputGateNode extends Node {

    private final int mask;

    public SingleInputGateNode(int[] in, int[][] out, CompType type) {
        super(in, out);
        mask = type == CompType.NOT ? -1 : 0;
    }

    @Override
    public void update(SignalProvider sp, ArrayList<Integer> active, int id) {
        int newSignal = sp.getSignal(in[0], in[1]) ^ mask;
        if(newSignal == sp.getSignal(id, 0)) return;
        sp.setSignal(id, 0, newSignal);
        active.addAll(Arrays.stream(out[0]).boxed().collect(Collectors.toList()));
    }
}
