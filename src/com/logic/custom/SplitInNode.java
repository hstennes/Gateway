package com.logic.custom;

import java.util.ArrayList;

public class SplitInNode extends Node{

    private final int[] split;

    private final int[] in;

    private final int[] inOutIndex;

    public SplitInNode(int[] in, int[] inOutIndex, int[] split) {
        super(new int[1]);
        this.split = split;
        this.in = in;
        this.inOutIndex = inOutIndex;
    }

    @Override
    public void update(Node[] nodes, ArrayList<Integer> active) {

    }
}
