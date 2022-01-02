package com.logic.custom;

import java.util.ArrayList;

public class NodeBox extends Node{

    /**
     * Input -
     * Output - list component indexes whose outputs serve as the final output
     */

    private Node[] inner;

    public NodeBox(int[] in, int[] inOutIndex, int[] out) {
        super(out);
    }

    @Override
    public void update(Node[] nodes, ArrayList<Integer> active) {
        //this is also going to suck
    }
}
