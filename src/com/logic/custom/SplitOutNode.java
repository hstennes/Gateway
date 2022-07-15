package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SplitOutNode extends Node{

    private final int[] split;

    public SplitOutNode(int[] in, int[][] mark, int address, int[] split) {
        super(in, mark, address);
        this.split = split;
    }

    @Override
    public void updateEvent(int[] signals, int offset, ActiveStack active){
        int input = signals[in[0] + offset];
        for(int i = 0; i < split.length; i++){
            int newSignal = input & (1 << split[i]) - 1;
            input >>= split[i];
            int index = address + offset + i;
            if(newSignal == signals[index]) continue;
            signals[index] = newSignal;
            active.mark(mark[i]);
        }
    }

    @Override
    public void updateLCC(int[] signals, int offset, ActiveStack active){
        int input = signals[in[0] + offset];
        for(int i = 0; i < split.length; i++){
            signals[address + offset + i] = input & (1 << split[i]) - 1;
            input >>= split[i];
        }
    }

    @Override
    public int getNumOutputs(){
        return split.length;
    }

    @Override
    public Node makeCopyWithOffset(int sigOffset, int nodeOffset){
        int[] newIn = new int[in.length];
        for(int i = 0; i < newIn.length; i++) newIn[i] = in[i] + sigOffset;
        int[][] newMark = copyMarkWithOffset(nodeOffset);
        return new SplitOutNode(newIn, newMark, address + sigOffset, split);
    }
}
