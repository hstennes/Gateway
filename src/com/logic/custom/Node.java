package com.logic.custom;

import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Creates a FileNode of this node for saving to JSON
     * @return The FileNode object
     */
    public abstract FileNode serialize();
}
