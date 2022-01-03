package com.logic.custom;

import com.logic.components.LComponent;

import java.util.ArrayList;
import java.util.List;

public class NodeBox implements Node{

    private Node[] inner;

    private int numStart;

    private int[] in;

    private int[] out;

    private int[] signal;

    public NodeBox(Node[] inner, int[] in, int[] out) {
        this.inner = inner;
        this.in = in;
        this.out = out;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int[] inputs = new int[in.length / 2];
        for(int i = 0; i < in.length; i += 2) inputs[i] = nb.get(in[i], in[i + 1]);
        update(inputs);
    }

    public void update(int[] inputs){
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
    }

    @Override
    public int getSignal(int n){
        return 0;
    }

    public int get(int node, int nodeOut){
        if(node == -1) return 0;
        return inner[node].getSignal(nodeOut);
    }

    public int get(int input){
        return 0;
    }
}
