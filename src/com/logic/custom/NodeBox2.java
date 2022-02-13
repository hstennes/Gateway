package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NodeBox2 {

    private final Node[] nodes;

    /**
     * Maps nodes to signal outputs in the format {node 1 ID, node 1 output index, node 2 ID, node 2 output index...}
     *
     * NOT ANYMORE!
     */
    private final int[] outNodes;

    /**
     * Lists spontaneous nodes that must always be updated (clocks, CustomNodes containing clocks)
     */
    //private final int[] spontaneous;

    public NodeBox2(Node[] nodes, int[] outNodes) {
        this.nodes = nodes;
        this.outNodes = outNodes;
        //spontaneous = findSpontaneous();
    }

    /*private int[] findSpontaneous(){
        ArrayList<Integer> spontList = new ArrayList<>();
        for(int i = 0; i < nodes.length; i++) {
            if(nodes[i] instanceof ClockNode) spontList.add(i);
            else if(nodes[i] instanceof CustomNode &&
                    ((CustomNode) nodes[i]).getType().nodeBox.isSpontaneous()) spontList.add(i);
        }
        return spontList.stream().mapToInt(i->i).toArray();
    }*/

    public int[] update(int[] signals, int[] in, int offset) {
        ArrayList<Integer> activeIn = new ArrayList<>();
        for(int i = 0; i < in.length; i++){
            int address = nodes[i].address + offset;
            int currentSignal = signals[address];
            int newSignal = in[i];

            if(currentSignal != newSignal) {
                signals[address] = newSignal;
                nodes[i].update(signals, offset, activeIn);
            }
        }
        //activeIn.addAll(Arrays.stream(spontaneous).boxed().collect(Collectors.toList()));

        while(activeIn.size() > 0) {
            List<Integer> oldActive = activeIn;
            activeIn = new ArrayList<>();
            for (int n : oldActive) {
                nodes[n].update(signals, offset, activeIn);
            }
        }

        int[] outputs = new int[outNodes.length];
        for (int i = 0; i < outNodes.length; i++) {
            outputs[i] = signals[offset + outNodes[i]];
        }
        return outputs;
    }

    /*public boolean isSpontaneous(){
        return spontaneous.length > 0;
    }*/

    public Node[] getNodes(){
        return nodes;
    }
}
