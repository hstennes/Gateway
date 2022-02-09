package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClockNode extends Node{

    public ClockNode(int[] in, int[][] out) {
        super(in, out);
    }

    @Override
    public void update(SignalProvider sp, ArrayList<Integer> active, int id) {
        active.addAll(Arrays.stream(out[0]).boxed().collect(Collectors.toList()));
    }
}
