package com.logic.custom;

import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NodeBox2 extends Node{

    private final Node[] nodes;

    private final int[] outNodes;

    private final int spIndex;

    public NodeBox2(int[] in, Node[] nodes, int[] outNodes) {
        super(in, null);
        this.nodes = nodes;
        this.outNodes = outNodes;
        spIndex = 0;
    }

    private NodeBox2(int[] in, int[][] out, Node[] nodes, int[] outNodes, int spIndex){
        super(in, out);
        this.nodes = nodes;
        this.outNodes = outNodes;
        this.spIndex = spIndex;
    }

    @Override
    public void update(SignalProvider spOut, ArrayList<Integer> activeOut, int id) {
        SignalProvider spIn = spOut.getNestedSP(spIndex);

        ArrayList<Integer> activeIn = new ArrayList<>();
        for(int i = 0; i < in.length / 2; i++){
            int currentSignal = spIn.getSignal(i, 0);
            int newSignal = spOut.getSignal(in[i * 2], in[i * 2 + 1]);

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

        boolean mark = activeOut != null;
        for (int i = 0; i < out.length; i++) {
            int currentSignal = spOut.getSignal(id, i);
            int newSignal = spIn.getSignal(outNodes[i * 2], outNodes[i * 2 + 1]);

            if (currentSignal != newSignal) {
                spOut.setSignal(id, i, newSignal);
                if(mark) activeOut.addAll(Arrays.stream(out[i]).boxed().collect(Collectors.toList()));
            }
        }
    }

    public NodeBox2 duplicate(int[] in, int[][] out, int spIndex){
        return new NodeBox2(in, out, nodes, outNodes, spIndex);
    }

    @Override
    public FileNode serialize() {
        return null;
    }

    public Node[] getInnerNodes(){
        return nodes;
    }

    public int[] getOutNodes(){
        return outNodes;
    }
}
