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
}
