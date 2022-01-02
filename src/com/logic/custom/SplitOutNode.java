package com.logic.custom;

import java.util.ArrayList;

public class SplitOutNode extends Node {

    private final int[] split;

    private final int in;

    private final int inOutIndex;

    public SplitOutNode(int in, int inOutIndex, int[] out, int[] split) {
        super(new int[split.length]);
        this.split = split;
        this.in = in;
        this.inOutIndex = inOutIndex;
    }

    @Override
    public void update(Node[] nodes, ArrayList<Integer> active) {

    }
}
