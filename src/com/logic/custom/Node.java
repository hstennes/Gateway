package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Node {

    //Relative index in big array of the input signals, in order
    public final int[] in;

    //Index in Nodes array of the nodes to mark
    public final Integer[][] mark;

    public final int address;

    public Node(int[] in, int[][] rawMark, int address){
        this.in = in;
        if(rawMark != null) {
            mark = new Integer[rawMark.length][];
            for (int i = 0; i < mark.length; i++) {
                this.mark[i] = new Integer[rawMark[i].length];
                for (int x = 0; x < mark[i].length; x++) mark[i][x] = rawMark[i][x];
            }
        } else mark = null;
        this.address = address;
    }

    /**
     * Updates the output signals of this node. This method is for an event-based simulation, which requires each node
     * to mark the nodes that should be updated next
     * @param signals The signals array
     * @param offset The signal address offset of the NodeBox
     * @param active The active stack
     */
    public abstract void updateEvent(int[] signals, int offset, ActiveStack active);

    /**
     * Updates the output signals of this node. This method is for an LCC simulation, which does not require nodes
     * to mark other nodes for updating
     * @param signals The signals array
     * @param offset The signal address offset of the NodeBox
     * @param active The active stack (only useful for CustomNode)
     */
    public abstract void updateLCC(int[] signals, int offset, ActiveStack active);
}
