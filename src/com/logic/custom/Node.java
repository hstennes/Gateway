package com.logic.custom;

import java.util.ArrayList;

public abstract class Node {

    public final int[] in;

    public final int[][] out;

    public Node(int[] in, int[][] out){
        this.in = in;
        this.out = out;
    }

    /**
     * Updates the output signal based on inputs acquired from the given NodeBox
     * @param sp The SignalProvider
     */
    public abstract void update(SignalProvider sp, ArrayList<Integer> active, int id);
}
