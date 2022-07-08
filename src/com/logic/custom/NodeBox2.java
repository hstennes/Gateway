package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NodeBox2 {

    protected final Node[] nodes;

    /**
     * Maps nodes to signal outputs in the format {node 1 ID, node 1 output index, node 2 ID, node 2 output index...}
     *
     * TODO Needs updated documentation
     */
    protected final int[] outNodes;

    public NodeBox2(Node[] nodes, int[] outNodes) {
        this.nodes = nodes;
        this.outNodes = outNodes;
    }

    public abstract int[] update(int[] signals, int[] in, int offset, ActiveStack active);

    protected int[] getOutputs(int[] signals, int offset){
        int[] outputs = new int[outNodes.length];
        for (int i = 0; i < outNodes.length; i++) {
            outputs[i] = signals[offset + outNodes[i]];
        }
        return outputs;
    }

    public Node[] getNodes(){
        return nodes;
    }
}
