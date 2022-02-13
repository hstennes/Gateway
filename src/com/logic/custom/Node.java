package com.logic.custom;

import java.util.ArrayList;

public abstract class Node {

    //Relative index in big array of the input signals, in order
    public final int[] in;

    //Index in Nodes array of the nodes to mark
    public final int[][] mark;

    public final int address;

    public Node(int[] in, int[][] mark, int address){
        this.in = in;
        this.mark = mark;
        this.address = address;
    }

    /**
     * Updates the output signal based on inputs acquired from the given NodeBox
     */
    public abstract void update(int[] signals, int offset, ArrayList<Integer> active);
}
