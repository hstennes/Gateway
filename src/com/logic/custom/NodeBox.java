package com.logic.custom;

import com.logic.components.LComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NodeBox implements Node{

    private final Node[] inner;

    private final int[] in;

    private final int[][] out;

    private final int[] outNodes;

    public final int[] signal;

    public NodeBox(Node[] inner, int[] outNodes){
        this.inner = inner;
        in = null;
        out = null;
        this.outNodes = outNodes;
        signal = new int[outNodes.length / 2];
    }

    public NodeBox(Node[] inner, int[] in, int[][] out, int[] outNodes) {
        this.inner = inner;
        this.in = in;
        this.out = out;
        this.outNodes = outNodes;
        signal = new int[outNodes.length / 2];
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int[] inputs = new int[in.length / 2];
        for(int i = 0; i < in.length; i += 2) inputs[i] = nb.get(in[i], in[i + 1]);
        update(inputs, active);
    }

    public void update(int[] inputs){
        update(inputs, null);
    }

    private void update(int[] inputs, List<Integer> outerActive){
        List<Integer> active = new ArrayList<>();
        for(int i = 0; i < inputs.length; i++){
            if(inputs[i] != inner[i].getSignal(0)) {
                ((StartNode) inner[i]).setSignal(inputs[i]);
                active.add(i);
            }
        }

        while(active.size() > 0) {
            List<Integer> oldActive = active;
            active = new ArrayList<>();
            for (int n : oldActive) {
                inner[n].update(this, active);
            }
        }

        if(outerActive != null) {
            for (int i = 0; i < signal.length; i++) {
                int newSignal = get(outNodes[2 * i], outNodes[2 * i + 1]);
                if (newSignal != signal[i]) {
                    signal[i] = newSignal;
                    outerActive.addAll(Arrays.stream(out[i]).boxed().collect(Collectors.toList()));
                }
            }
        }
        else{
            for (int i = 0; i < signal.length; i++) {
                signal[i] = get(outNodes[2 * i], outNodes[2 * i + 1]);
            }
        }
    }

    @Override
    public int getSignal(int n){
        return signal[n];
    }

    public int get(int node, int nodeOut){
        if(node == -1) return 0;
        return inner[node].getSignal(nodeOut);
    }
}