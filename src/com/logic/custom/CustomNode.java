package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CustomNode extends Node{

    private final CustomType type;

    //Start position for the signal data relative to the position of the enclosing NodeBox
    private int innerOffset;

    public CustomNode(int[] in, int[][] mark, int address, CustomType type, int innerOffset){
        super(in, mark, address);
        this.type = type;
        this.innerOffset = innerOffset;
    }

    @Override
    public void updateEvent(int[] signals, int offset, ActiveStack active){
        int[] inputs = new int[in.length];
        for(int i = 0; i < inputs.length; i++) inputs[i] = signals[offset + in[i]];

        active.startInner();
        int[] outputs = type.nodeBox.update(signals, inputs, offset + innerOffset, active);
        active.finishInner();
        for(int i = 0; i < outputs.length; i++){
            int index = address + offset + i;
            int newSignal = outputs[i];
            int oldSignal = signals[index];
            if(newSignal == oldSignal) continue;
            signals[index] = newSignal;
            active.mark(mark[i]);
        }
    }

    @Override
    public void updateLCC(int[] signals, int offset, ActiveStack active) {
        int[] inputs = new int[in.length];
        for(int i = 0; i < inputs.length; i++) inputs[i] = signals[offset + in[i]];

        int[] outputs = type.nodeBox.update(signals, inputs, offset + innerOffset, active);
        for(int i = 0; i < outputs.length; i++) signals[address + offset + i] = outputs[i];
    }

    public CustomType getType(){
        return type;
    }

    public int getInnerOffset(){
        return innerOffset;
    }

    public void setInnerOffset(int innerOffset){
        this.innerOffset = innerOffset;
    }

    @Override
    public int getNumOutputs(){
        return type.nodeBox.getNumOutputs();
    }
}
