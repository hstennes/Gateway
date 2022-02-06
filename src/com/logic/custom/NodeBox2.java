package com.logic.custom;

import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NodeBox2 {

    private final Node[] nodes;

    private final int[] outNodes;

    public NodeBox2(Node[] nodes, int[] outNodes) {
        this.nodes = nodes;
        this.outNodes = outNodes;
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

    public Node[] getInnerNodes(){
        return nodes;
    }

    public int[] getOutNodes(){
        return outNodes;
    }
}
