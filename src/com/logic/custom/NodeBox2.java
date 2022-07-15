package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NodeBox2 {

    protected final Node[] nodes;

    /**
     * The indices of the output signals in the signals array
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

    public int getNumOutputs(){
        return outNodes.length;
    }
}
