package com.logic.custom;

import com.logic.components.CompType;

import java.util.ArrayList;

public class PlaceholderNode extends Node{

    private final CompType type;

    public PlaceholderNode(int[] in, int[][] mark, int address, CompType type){
        super(in, mark, address);
        this.type = type;
    }

    @Override
    public void update(int[] signals, int offset, ArrayList<Integer> active) { }
}
