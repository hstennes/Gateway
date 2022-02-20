package com.logic.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StartNode extends Node{

    public StartNode(int[] in, int[][] mark, int address) {
        super(in, mark, address);
    }

    @Override
    public void update(int[] signals, int offset, ArrayList<Integer> active) {
        active.addAll(mark.get(0));
    }
}
