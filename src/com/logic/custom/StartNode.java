package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StartNode extends Node{

    public StartNode(int[] in, int[][] mark, int address) {
        super(in, mark, address);
    }

    @Override
    public void updateEvent(int[] signals, int offset, ActiveStack active) {
        active.mark(mark[0]);
    }

    public void updateLCC(int[] signals, int offset, ActiveStack active) { }

    @Override
    public int getNumOutputs(){
        return 1;
    }

    @Override
    public Node makeCopyWithOffset(int sigOffset, int nodeOffset){
        int[] newIn = new int[in.length];
        for(int i = 0; i < newIn.length; i++) newIn[i] = in[i] + sigOffset;
        int[][] newMark = copyMarkWithOffset(nodeOffset);
        return new StartNode(newIn, newMark, address + sigOffset);
    }
}
