package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NodeBox2 {

    private final Node[] nodes;

    /**
     * Maps nodes to signal outputs in the format {node 1 ID, node 1 output index, node 2 ID, node 2 output index...}
     */
    private final int[] outNodes;

    /**
     * Lists spontaneous nodes that must always be updated (clocks, CustomNodes containing clocks)
     */
    private final int[] spontaneous;

    public NodeBox2(Node[] nodes, int[] outNodes) {
        this.nodes = nodes;
        this.outNodes = outNodes;
        spontaneous = findSpontaneous();
    }

    private int[] findSpontaneous(){
        ArrayList<Integer> spontList = new ArrayList<>();
        for(int i = 0; i < nodes.length; i++) {
            if(nodes[i] instanceof ClockNode) spontList.add(i);
            else if(nodes[i] instanceof CustomNode &&
                    ((CustomNode) nodes[i]).getType().nodeBox.isSpontaneous()) spontList.add(i);
        }
        return spontList.stream().mapToInt(i->i).toArray();
    }

    public int[] update(SignalProvider spIn, int[] inputs) {
        ArrayList<Integer> activeIn = new ArrayList<>();
        for(int i = 0; i < inputs.length; i++){
            int currentSignal = spIn.getSignal(i, 0);
            int newSignal = inputs[i];

            if(currentSignal != newSignal) {
                spIn.setSignal(i, 0, newSignal);
                nodes[i].update(spIn, activeIn, i);
            }
        }
        activeIn.addAll(Arrays.stream(spontaneous).boxed().collect(Collectors.toList()));

        while(activeIn.size() > 0) {
            List<Integer> oldActive = activeIn;
            activeIn = new ArrayList<>();
            for (int n : oldActive) {
                nodes[n].update(spIn, activeIn, n);
            }
        }

        int[] outputs = new int[outNodes.length / 2];
        for (int i = 0; i < outNodes.length / 2; i++) {
            outputs[i] = spIn.getSignal(outNodes[i * 2], outNodes[i * 2 + 1]);
        }
        return outputs;
    }

    public boolean isSpontaneous(){
        return spontaneous.length > 0;
    }
}
