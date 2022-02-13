package com.logic.custom;

import com.logic.components.CompType;

import java.util.ArrayList;

public class PlaceholderNode extends Node{

    private final CompType type;

    public PlaceholderNode(CompType type, int[] in, int[][] out){
        super(in, out);
        this.type = type;
    }

    @Override
    public void update(SignalProvider sp, ArrayList<Integer> active, int id) { }
}
