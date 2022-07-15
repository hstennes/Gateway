package com.logic.custom;

import com.logic.components.CompType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SingleInputGateNode extends Node {

    private final int mask;

    public SingleInputGateNode(int[] in, int[][] mark, int address, CompType type) {
        super(in, mark, address);
        mask = type == CompType.NOT ? -1 : 0;
    }

    @Override
    public void updateEvent(int[] signals, int offset, ActiveStack active){
        int newSignal = signals[in[0] + offset] ^ mask;
        if(newSignal == signals[address + offset]) return;
        signals[address + offset] = newSignal;
        active.mark(mark[0]);
    }

    @Override
    public void updateLCC(int[] signals, int offset, ActiveStack active){
        signals[address + offset] = signals[in[0] + offset] ^ mask;
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
        return new SingleInputGateNode(newIn, newMark, address + sigOffset, mask == -1 ? CompType.NOT : CompType.BUFFER);
    }
}
