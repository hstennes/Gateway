package com.logic.custom;

import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CustomNode extends Node{

    private CustomType type;

    private int spIndex;

    public CustomNode(int[] in, int[][] out, CustomType type, int spIndex){
        super(in, out);
        this.type = type;
        this.spIndex = spIndex;
    }

    @Override
    public void update(SignalProvider spOut, ArrayList<Integer> active, int id) {
        SignalProvider spIn = spOut.getNestedSP(spIndex);
        int[] inputs = new int[in.length / 2];
        for(int i = 0; i < inputs.length; i++) inputs[i] = spOut.getSignal(in[i * 2], in[i * 2 + 1]);

        int[] outputs = type.nodeBox.update(spIn, inputs);
        for(int i = 0; i < outputs.length; i++){
            int newSignal = outputs[i];
            int oldSignal = spOut.getSignal(id, i);
            if(newSignal == oldSignal) continue;
            spOut.setSignal(id, i, newSignal);
            active.addAll(Arrays.stream(out[i]).boxed().collect(Collectors.toList()));
        }
    }

    @Override
    public FileNode serialize() {
        return null;
    }

    public CustomType getType(){
        return type;
    }
}
