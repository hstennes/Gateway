package com.logic.custom;

import com.logic.components.SplitIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SplitInNode extends Node{

    private final int[] split;

    public SplitInNode(int[] in, int[][] mark, int address, int[] split) {
        super(in, mark, address);
        this.split = split;
    }

    @Override
    public void updateEvent(int[] signals, int offset, ActiveStack active){
        int newSignal = doLogic(signals, offset);
        if(newSignal == signals[address + offset]) return;
        signals[address + offset] = newSignal;
        active.mark(mark[0]);
    }

    @Override
    public void updateLCC(int[] signals, int offset, ActiveStack active){
        signals[address + offset] = doLogic(signals, offset);
    }

    private int doLogic(int[] signals, int offset){
        int newSignal = 0;
        int shift = 0;
        for(int i = 0; i < split.length; i++){
            newSignal |= (signals[in[i] + offset] & ((1 << split[i]) - 1)) << shift;
            shift += split[i];
        }
        return newSignal;
    }

    @Override
    public int getNumOutputs(){
        return 1;
    }

    @Override
    public Node makeCopyWithOffset(int sigOffset, int nodeOffset){
        int[] newIn = new int[in.length];
        for(int i = 0; i < newIn.length; i++) newIn[i] = in[i] + sigOffset;
        int[][] newMark = copyMarkWithOffset(nodeOffset);
        return new SplitInNode(newIn, newMark, address + sigOffset, split);
    }
}
